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
import java.util.Set;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.execution.Side;

/**
 * Implementation of {@link PositionLookup} around  a {@link PositionKeeper}
 * to hide and herewith protect the modifying methods from unauthorized use.
 */
public class PositionLookupImpl implements PositionLookup {

	private final PositionKeeper positionKeeper;
	
	public PositionLookupImpl(PositionKeeper positionKeeper) {
		this.positionKeeper = Objects.requireNonNull(positionKeeper, "positionKeeper is null");
	}
	@Override
	public long getMaxPossibleFillWithoutExceedingMax(AssetPair<?, ?> assetPair, Side orderSide, double rate) {
		return positionKeeper.getMaxPossibleFillWithoutExceedingMax(assetPair, orderSide, rate);
	}

	@Override
	public Set<Asset> getPositionAssets() {
		return positionKeeper.getPositionAssets();
	}

	@Override
	public long getPosition(Asset asset) {
		return positionKeeper.getPosition(asset);
	}

	@Override
	public Valuator getValuator(Currency valuationCurrency) {
		return positionKeeper.getValuator(valuationCurrency);
	}

}
