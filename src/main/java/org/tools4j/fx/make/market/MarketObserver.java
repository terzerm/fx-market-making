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
package org.tools4j.fx.make.market;

import java.time.Instant;

import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Order;

/**
 * Someone who is looking what's on in the market.
 */
public interface MarketObserver {
	/**
	 * A time event occurred. At least the starting of the market causes a time
	 * event, with time being at or before all orders in the market. 
	 * 
	 * @param time
	 *            the time
	 */
	void onTime(Instant time);

	/**
	 * An order has been placed in the market. Only unmatched orders are seen
	 * that do not directly lead to deals. Orders crossing the spread usually
	 * lead to a match and hence to a deal.
	 * 
	 * @param order
	 *            the order that was placed in the market, may include own
	 *            orders if the observer is a {@link MarketMaker}
	 */
	void onOrder(Order order);

	/**
	 * A deal has happened in the market, that is, a successful order match took
	 * place. The deal can be an own deal (i.e. where buy or sell party is
	 * oneself) or the deal of two foreign parties.
	 * 
	 * @param deal
	 *            the deal that took place in the market
	 */
	void onDeal(Deal deal);
}
