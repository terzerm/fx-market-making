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
import java.util.concurrent.atomic.AtomicLong;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.config.Settings;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.Side;

/**
 * Keeps track positions for several symbols.
 * <p>
 * The class is NOT thread safe.
 */
public class PositionKeeperImpl implements PositionKeeper {

	private final Settings settings;
	private final Map<Asset, AtomicLong> positionByAsset = new HashMap<>();

	public PositionKeeperImpl(Settings settings) {
		this.settings = Objects.requireNonNull(settings, "settings is null");
	}

	@Override
	public long getMaxPossibleFillWithoutExceedingMax(AssetPair<?, ?> assetPair, Side orderSide, double rate) {
		final long baseMax = settings.getMaxAllowedPositionSize(assetPair.getBase());
		final long termsMax = settings.getMaxAllowedPositionSize(assetPair.getTerms());
		final long basePos = getPosition(assetPair.getBase());
		final long termsPos = getPosition(assetPair.getTerms());
		// opposite side for base because we fill the order, i.e. we take the
		// opposite position
		double baseQty = baseMax - getSignedQuantity(basePos, orderSide.opposite());
		double termsQty = termsMax - getSignedQuantity(termsPos, orderSide);
		return (long) (baseQty * rate <= termsQty ? baseQty : termsQty / rate);
	}

	@Override
	public long fillWithoutExceedingMax(Order order, boolean allowPartial) {
		final AssetPair<?, ?> assetPair = order.getAssetPair();
		final long orderQty = order.getQuantity();
		final long maxQty = getMaxPossibleFillWithoutExceedingMax(assetPair, order.getSide(), order.getPrice());
		final long partialQty = Math.min(orderQty, maxQty);
		if (allowPartial | orderQty == partialQty) {
			// opposite side for base because we fill the order, i.e. we take
			// the opposite position
			final long baseQty = (long) getSignedQuantity(partialQty, order.getSide().opposite());
			final long termsQty = (long) getSignedQuantity(partialQty * order.getPrice(), order.getSide());
			if (baseQty != 0 & termsQty != 0) {
				getOrCreatePosition(assetPair.getBase()).addAndGet(baseQty);
				getOrCreatePosition(assetPair.getTerms()).addAndGet(termsQty);
				return Math.abs(baseQty);
			}
		}
		// no or not enough quantity left
		return 0;
	}

	private static double getSignedQuantity(double quantity, Side side) {
		return side == Side.BUY ? quantity : -quantity;
	}

	private AtomicLong getOrCreatePosition(Asset asset) {
		AtomicLong position = positionByAsset.get(asset);
		if (position == null) {
			position = new AtomicLong(0);
			positionByAsset.put(asset, position);
		}
		return position;
	}

	@Override
	public Set<Asset> getPositionAssets() {
		return Collections.unmodifiableSet(positionByAsset.keySet());
	}

	@Override
	public long getPosition(Asset asset) {
		final AtomicLong position = positionByAsset.get(asset);
		return position == null ? 0 : position.longValue();
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
}
