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
 * Wrapper around {@link PositionKeeper} which additionally keeps track of the
 * high-water-marks per asset.
 */
public class HighWaterMarkPositionKeeper implements PositionKeeper {
	
	private final Map<Asset, HighLowMark> highLowMarksPerAsset = new HashMap<>();
	private final PositionKeeper delegate;
	
	private static class HighLowMark {
		private volatile double high = 0;
		private volatile double low = 0;
		public void update(double position) {
			if (position > 0) {
				high = Math.max(high, position);
			} else if (position < 0) {
				low = Math.min(low, position);
			}
		}
	}
	
	public HighWaterMarkPositionKeeper(RiskLimits riskLimits) {
		this(new PositionKeeperImpl(riskLimits));
	}
	public HighWaterMarkPositionKeeper(PositionKeeper delegate) {
		this.delegate = Objects.requireNonNull(delegate, "delegate is null");
	}

	@Override
	public RiskLimits getRiskLimits() {
		return delegate.getRiskLimits();
	}

	@Override
	public long getMaxPossibleFillWithoutBreachingRiskLimits(AssetPair<?, ?> assetPair, Side orderSide, double rate) {
		return delegate.getMaxPossibleFillWithoutBreachingRiskLimits(assetPair, orderSide, rate);
	}

	@Override
	public Set<Asset> getAssets() {
		return delegate.getAssets();
	}

	@Override
	public double getPosition(Asset asset) {
		return delegate.getPosition(asset);
	}

	@Override
	public Valuator getValuator(Currency valuationCurrency) {
		return delegate.getValuator(valuationCurrency);
	}

	@Override
	public void updatePosition(Deal deal, Side side) {
		delegate.updatePosition(deal, side);
		updateHighLowMarkFor(deal.getAssetPair().getBase()); 
		updateHighLowMarkFor(deal.getAssetPair().getTerms()); 
	}

	private void updateHighLowMarkFor(Asset asset) {
		final double position = getPosition(asset);
		if (position != 0) {
			HighLowMark waterMark = highLowMarksPerAsset.get(asset);
			if (waterMark == null) {
				waterMark = new HighLowMark();
				highLowMarksPerAsset.put(asset, waterMark);
			}
			waterMark.update(position);
		}
	}
	
	public double getHighWaterMark(Asset asset) {
		final HighLowMark waterMark = highLowMarksPerAsset.get(asset);
		return waterMark == null ? 0 : waterMark.high;
	}

	public double getLowWaterMark(Asset asset) {
		final HighLowMark waterMark = highLowMarksPerAsset.get(asset);
		return waterMark == null ? 0 : waterMark.low;
	}

	@Override
	public void resetPosition(Asset asset) {
		delegate.resetPosition(asset);
		highLowMarksPerAsset.remove(asset);
	}

	@Override
	public void resetPositions() {
		delegate.resetPositions();
		highLowMarksPerAsset.clear();
	}

	@Override
	public String toString() {
		return PositionKeeperImpl.toString(getClass().getSimpleName(), delegate);
	}
}
