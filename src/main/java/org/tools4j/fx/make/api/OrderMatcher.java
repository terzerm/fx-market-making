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
package org.tools4j.fx.make.api;

/**
 * Matches two orders if a match is possible.
 */
public enum OrderMatcher {
	/**
	 * Matcher allowing only full fills, that is, the order quantities must be
	 * identical to allow for a match.
	 */
	FULL, //
	/**
	 * Matcher allowing for partial and full fills, that is, the order
	 * quantities are allowed to be different.
	 */
	PARTIAL;

	/**
	 * Returns the match quantity if a match is possible and zero if not.
	 * 
	 * @param order1
	 *            the first order to match
	 * @param order2
	 *            the second order to match
	 * @return the match quantity (no more than min of the order quantities) or
	 *         zero for no match
	 */
	public long matchQuantity(Order order1, Order order2) {
		if (isMatchPossible(order1, order2)) {
			return Math.min(order1.getQuantity(), order2.getQuantity());
		}
		return 0;
	}

	/**
	 * Returns true if a match is possible and false if not.
	 * 
	 * @param order1
	 *            the first order to match
	 * @param order2
	 *            the second order to match
	 * @return true if and only if a match is possible between the two orders
	 */
	public boolean isMatchPossible(Order order1, Order order2) {
		if (!order1.getAssetPair().equals(order2.getAssetPair())) {
			return false;
		}
		if (this == FULL & order1.getQuantity() != order2.getQuantity()) {
			return false;
		}
		final Order buyOrder = order1.getSide() == Side.BUY ? order1 : order2.getSide() == Side.BUY ? order2 : null;
		final Order sellOrder = order1.getSide() == Side.SELL ? order1 : order2.getSide() == Side.SELL ? order2 : null;
		if (buyOrder == null | sellOrder == null) {
			return false;
		}
		return buyOrder.getPrice() >= sellOrder.getPrice();
	}
}
