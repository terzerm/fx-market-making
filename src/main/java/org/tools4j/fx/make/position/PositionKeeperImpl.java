/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 fx-market-making (tools4j), Marco Terzer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.tools4j.fx.make.position;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.risk.RiskLimits;

/**
 * Keeps track positions for several symbols.
 * <p>
 * The class is NOT thread safe.
 */
public class PositionKeeperImpl implements PositionKeeper {

	private final RiskLimits riskLimits;
	private final Map<Asset, Double> positionByAsset = new HashMap<>();

	public PositionKeeperImpl(RiskLimits riskLimits) {
		this.riskLimits = Objects.requireNonNull(riskLimits, "riskLimits is null");
	}

	@Override
	public RiskLimits getRiskLimits() {
		return riskLimits;
	}

	@Override
	public long getMaxPossibleFillWithoutBreachingRiskLimits(AssetPair<?, ?> assetPair, Side orderSide, double rate) {
		Objects.requireNonNull(assetPair, "assetPair is null");
		Objects.requireNonNull(orderSide, "orderSide is null");
		final long baseMax = riskLimits.getMaxAllowedPositionSize(assetPair.getBase());
		final long termsMax = riskLimits.getMaxAllowedPositionSize(assetPair.getTerms());
		final double basePos = getPosition(assetPair.getBase());
		final double termsPos = getPosition(assetPair.getTerms());
		// opposite side for base because we fill the order, i.e. we act as
		// counter party
		final double baseQty = baseMax >= 0 ? baseMax - getSignedQuantity(basePos, orderSide.opposite()) : -1;
		final double termsQty = termsMax >= 0 ? termsMax - getSignedQuantity(termsPos, orderSide) : -1;
		if (baseQty < 0 | termsQty < 0) {
			return (long)(baseQty < 0 ? termsQty : baseQty);
		}
		return (long) (baseQty * rate <= termsQty ? baseQty : termsQty / rate);
	}

	@Override
	public void updatePosition(Deal deal, Side side) {
		Objects.requireNonNull(deal, "deal is null");
		Objects.requireNonNull(side, "side is null");
		final AssetPair<?, ?> assetPair = deal.getAssetPair();
		final long dealQty = deal.getQuantity();
		final long maxQty = getMaxPossibleFillWithoutBreachingRiskLimits(assetPair, side.opposite(), deal.getPrice());
		if (maxQty >= 0 & dealQty > maxQty) {
			throw new IllegalArgumentException(
					"deal would breach risk limits: " + dealQty + " > " + maxQty + " for " + deal);
		}
		final double baseQty = getSignedQuantity(dealQty, side);
		final double termsQty = getSignedQuantity(dealQty * deal.getPrice(), side.opposite());
		incremetPosition(assetPair.getBase(), baseQty);
		incremetPosition(assetPair.getTerms(), termsQty);
	}

	private static double getSignedQuantity(double quantity, Side side) {
		return side == Side.BUY ? quantity : -quantity;
	}

	private void incremetPosition(Asset asset, double increment) {
		final Double position = positionByAsset.get(asset);
		if (position == null) {
			positionByAsset.put(asset, increment);
		} else {
			positionByAsset.put(asset, position + increment);
		}
	}

	@Override
	public Set<Asset> getAssets() {
		return Collections.unmodifiableSet(positionByAsset.keySet());
	}

	@Override
	public double getPosition(Asset asset) {
		final Double position = positionByAsset.get(asset);
		return position == null ? 0 : position.doubleValue();
	}

	@Override
	public void resetPosition(Asset asset) {
		positionByAsset.remove(asset);
	}

	@Override
	public void resetPositions() {
		positionByAsset.clear();
	}

	@Override
	public Valuator getValuator(Currency valuationCurrency) {
		return new ValuatorImpl(valuationCurrency, this);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + positionByAsset.toString();
	}

	static String toString(String prefix, PositionKeeper positionKeeper) {
		final String s = positionKeeper.toString();
		final int index = s.indexOf('{');
		return prefix + (index >= 0 ? s.substring(index) : s);
	}
}
