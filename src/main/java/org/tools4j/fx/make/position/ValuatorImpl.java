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

import java.util.Objects;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.asset.Currency;

/**
 * Implements {@link Valuator}.
 */
public class ValuatorImpl implements Valuator {

	private final Currency valuationCurrency;
	private final AssetPositions assetPositions;

	public ValuatorImpl(Currency valuationCurrency, AssetPositions assetPositions) {
		this.valuationCurrency = Objects.requireNonNull(valuationCurrency, "valuationCurrency is null");
		this.assetPositions = Objects.requireNonNull(assetPositions, "positionKeeper is null");
	}
	
	@Override
	public AssetPositions getAssetPositions() {
		return assetPositions;
	}
	
	@Override
	public Currency getValuationCurrency() {
		return valuationCurrency;
	}

	@Override
	public double getValuation(MarketSnapshot marketSnapshot) {
		double value = 0;
		for (final Asset asset : assetPositions.getAssets()) {
			value += getValuation(asset, marketSnapshot);
		}
		return value;
	}

	@Override
	public double getValuation(Asset asset, MarketSnapshot marketSnapshot) {
		Objects.requireNonNull(asset, "asset is null");
		Objects.requireNonNull(marketSnapshot, "marketSnapshot is null");
		final double position = assetPositions.getPosition(asset);
		if (position == 0) {
			return 0;
		}
		final double rate = marketSnapshot.getRate(asset, valuationCurrency);
		return rate * position;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{valuationCurrency=" + valuationCurrency + ", positionKeeper=" + assetPositions + "}";
	}
}
