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
 * <p>
 * The class is NOT thread safe.
 */
public class ValuatorImpl implements Valuator {

	private final Currency valuationCurrency;
	private final PositionKeeper positionKeeper;

	public ValuatorImpl(Currency valuationCurrency, PositionKeeper positionKeeper) {
		this.valuationCurrency = Objects.requireNonNull(valuationCurrency, "valuationCurrency is null");
		this.positionKeeper = Objects.requireNonNull(positionKeeper, "positionKeeper is null");
	}
	
	@Override
	public PositionKeeper getPositionKeeper() {
		return positionKeeper;
	}
	
	@Override
	public Currency getValuationCurrency() {
		return valuationCurrency;
	}

	@Override
	public double getValuation(MarketRates marketRates) {
		double value = 0;
		for (final Asset asset : positionKeeper.getPositionAssets()) {
			value += getValuation(asset, marketRates);
		}
		return value;
	}

	@Override
	public double getValuation(Asset asset, MarketRates marketRates) {
		Objects.requireNonNull(asset, "asset is null");
		Objects.requireNonNull(marketRates, "marketRates is null");
		final long position = positionKeeper.getPosition(asset);
		if (position == 0) {
			return 0;
		}
		final double rate = marketRates.getRate(asset, valuationCurrency);
		return rate * position;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{valuationCurrency=" + valuationCurrency + ", positionKeeper=" + positionKeeper + "}";
	}
}
