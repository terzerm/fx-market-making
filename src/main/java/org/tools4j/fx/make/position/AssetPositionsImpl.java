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
import java.util.Objects;
import java.util.Set;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.risk.RiskLimits;

/**
 * Implementation of {@link AssetPositions} wrapping a {@link PositionKeeper}
 * to hide and herewith protect the modifying methods from unauthorized use.
 */
public class AssetPositionsImpl implements AssetPositions {

	private final PositionKeeper positionKeeper;
	
	public AssetPositionsImpl(PositionKeeper positionKeeper) {
		this.positionKeeper = Objects.requireNonNull(positionKeeper, "positionKeeper is null");
	}

	@Override
	public RiskLimits getRiskLimits() {
		return positionKeeper.getRiskLimits();
	}
	@Override
	public long getMaxPossibleFillWithoutBreachingRiskLimits(AssetPair<?, ?> assetPair, Side orderSide, double rate) {
		return positionKeeper.getMaxPossibleFillWithoutBreachingRiskLimits(assetPair, orderSide, rate);
	}

	@Override
	public Set<Asset> getAssets() {
		return positionKeeper.getAssets();
	}

	@Override
	public double getPosition(Asset asset) {
		return positionKeeper.getPosition(asset);
	}

	@Override
	public Valuator getValuator(Currency valuationCurrency) {
		return positionKeeper.getValuator(valuationCurrency);
	}
	
	public static AssetPositions empty(RiskLimits riskLimits) {
		return new AssetPositions() {
			@Override
			public RiskLimits getRiskLimits() {
				return riskLimits;
			}
			@Override
			public Valuator getValuator(Currency valuationCurrency) {
				return new ValuatorImpl(valuationCurrency, this);
			}
			
			@Override
			public double getPosition(Asset asset) {
				return 0;
			}
			
			@Override
			public long getMaxPossibleFillWithoutBreachingRiskLimits(AssetPair<?, ?> assetPair, Side orderSide, double rate) {
				final long baseLimit = riskLimits.getMaxAllowedPositionSize(assetPair.getBase());
				final long termsLimit = riskLimits.getMaxAllowedPositionSize(assetPair.getTerms());
				return Math.min(baseLimit, termsLimit);
			}
			
			@Override
			public Set<Asset> getAssets() {
				return Collections.emptySet();
			}
		};
	}

	@Override
	public String toString() {
		return PositionKeeperImpl.toString(getClass().getSimpleName(), positionKeeper);
	}
}
