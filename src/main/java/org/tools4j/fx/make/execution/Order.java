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
package org.tools4j.fx.make.execution;

import java.util.concurrent.atomic.AtomicLong;

import org.tools4j.fx.make.asset.AssetPair;

/**
 * An order. All members return non-null values.
 */
public interface Order {
	/**
	 * The order ID unique among all orders even across different parties.
	 * 
	 * @return unique order ID
	 */
	long getId();

	/**
	 * The asset pair.
	 * 
	 * @return the traded asset pair
	 */
	AssetPair<?, ?> getAssetPair();

	/**
	 * The party behind this order
	 * 
	 * @return the issuing party
	 */
	String getParty();

	/**
	 * Returns the side, never null.
	 * 
	 * @return the order side
	 */
	Side getSide();

	/**
	 * The order price
	 * 
	 * @return the price of this order
	 */
	double getPrice();

	/**
	 * The quantity of the order, non-negative
	 * 
	 * @return the quantity or amount, not negative
	 */
	long getQuantity();
	
	/**
	 * Returns a string of the form: BUY:AUD/USD[1.2M@1.246370]
	 * @return a short string with side, symbol, quantity and price
	 */
	String toShortString();
	
	/**
	 * Generator for unique order ID's.
	 */
	AtomicLong ID_GENERATOR = new AtomicLong(System.currentTimeMillis());
}
