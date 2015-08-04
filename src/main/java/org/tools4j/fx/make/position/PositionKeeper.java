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

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Side;

/**
 * Keeps track positions for several {@link Asset}s. Adds modifying methods to
 * {@link AssetPositions}.
 */
public interface PositionKeeper extends AssetPositions {

	/**
	 * Update the position for the given {@code deal}. The specified
	 * {@code side} refers to the deal side viewed from the position to be
	 * updated (e.g. the position should be increased by the deal amount if side
	 * is BUY).
	 * 
	 * @param deal
	 *            the deal to incorprate into the position
	 * @param side
	 *            the side from the position keeper's view
	 * @throws IllegalArgumentException
	 *             if the deal is too large to be added to the position (it
	 *             would breach the risk limits)
	 */
	void updatePosition(Deal deal, Side side);

	/**
	 * Sets the position for the specified asset to zero
	 * 
	 * @param asset
	 *            the asset whose position to reset
	 */
	void resetPosition(Asset asset);

	/**
	 * Sets all positions to zero
	 */
	void resetPositions();

}
