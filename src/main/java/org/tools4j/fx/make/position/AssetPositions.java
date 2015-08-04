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

import java.util.Set;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.risk.RiskLimits;

/**
 * Provides position information for multiple {@link Asset}s.
 */
public interface AssetPositions {

	/**
	 * Returns the risk limits associated with this {@code AssetPositions}
	 * object.
	 * 
	 * @return the risk limits
	 */
	RiskLimits getRiskLimits();

	/**
	 * Returns the maximum possible quantity an order could have to be fully
	 * filled without breaching the risk limits. The order side refers to the
	 * side of the order to be filled; the positions are checked for capacity to
	 * be acting as a counter party to the order. For instance a call with
	 * "AUD/USD --  SELL" checks by how much the AUD position can be reduced and
	 * by how much the USD position can be increased without breaching risk
	 * limits.
	 * 
	 * @param assetPair
	 *            the asset pair to check
	 * @param orderSide
	 *            the order side to be filled (acting as counter party)
	 * @param rate
	 *            the rate for transformation of base to terms asset
	 * @return the maximum possible position; non-negative, zero if no fill is
	 *         possible
	 */
	long getMaxPossibleFillWithoutBreachingRiskLimits(AssetPair<?, ?> assetPair, Side orderSide, double rate);

	/**
	 * Returns all assets with non-zero positions.
	 * 
	 * @return non-zero position assets
	 */
	Set<Asset> getAssets();

	/**
	 * Returns the position size for the given asset, positive for long and
	 * negative for short.
	 * 
	 * @param asset
	 *            the asset
	 * @return the position for the given asset
	 */
	long getPosition(Asset asset);

	/**
	 * Returns a valuator object for this {@code AssetPositions} object and the
	 * specified {@code valuationCurrency}.
	 * 
	 * @param valuationCurrency
	 *            the currency in which the valuation should be expressed
	 * @return the valuator to value positions of this {@code AssetPositions}
	 *         object
	 */
	Valuator getValuator(Currency valuationCurrency);

}
