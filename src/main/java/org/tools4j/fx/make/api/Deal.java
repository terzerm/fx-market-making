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

import java.util.concurrent.atomic.AtomicLong;

/**
 * A done deal, that is, a match or partial of two orders agreed by the involved
 * parties.
 */
public interface Deal {
	/**
	 * The deal ID unique among all deals.
	 * 
	 * @return unique deal ID
	 */
	long getId();

	/**
	 * The symbol such as "AUD/USD"
	 * 
	 * @return the symbol of the traded security
	 */
	String getSymbol();

	/**
	 * The deal price, for instance mid price between the two involved order
	 * prices.
	 * 
	 * @return the price of this deal
	 */
	double getPrice();

	/**
	 * The quantity of the deal, non-negative and no more than any of the
	 * involved orders.
	 * 
	 * @return the quantity or amount, not negative
	 */
	long getQuantity();

	/**
	 * Returns the order ID of the BUY side order.
	 * 
	 * @return the buy-side order ID
	 */
	long getBuyOrderId();

	/**
	 * Returns the party behind the BUY side order.
	 * 
	 * @return the buy-side party
	 */
	String getBuyParty();

	/**
	 * Returns the order ID of the SELL side order.
	 * 
	 * @return the sell-side order ID
	 */
	long getSellOrderId();

	/**
	 * Returns the party behind the SELL side order.
	 * 
	 * @return the sell-side party
	 */
	String getSellParty();

	/**
	 * Returns a string of the form: 1.2M@1.24637
	 * @return a short string with quantity and price
	 */
	String toShortString();

	/**
	 * Generator for unique deal ID's.
	 */
	AtomicLong ID_GENERATOR = new AtomicLong(System.currentTimeMillis());
}
