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
package org.tools4j.fx.make.util;

/**
 * Every projects needs to have a string util class.
 */
public class StringUtil {

	/**
	 * Returns a string in millions if the quantity is a multiply of one
	 * thousand.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>1000000 --> 1M</li>
	 * <li>1200000 --> 1.2M</li>
	 * <li>1250000 --> 1.25M</li>
	 * <li>1125000 --> 1.125M</li>
	 * <li>2000000 --> 2M</li>
	 * <li>10000000 --> 10M</li>
	 * <li>12500000 --> 12.5M</li>
	 * <li>1125300 --> 1125300</li>
	 * </ul>
	 * 
	 * @param quantity
	 *            the quantity to format
	 * @return the formatted quantity string
	 */
	public static final String formatQuantity(long quantity) {
		if ((quantity % 1000) == 0) {
			final long thousands = quantity / 1000;
			if ((thousands % 1000) == 0) {
				return String.valueOf(thousands / 1000) + "M";
			}
			if ((thousands % 100) == 0) {
				return String.format("%0.1fM", (quantity / 1000d));
			}
			if ((thousands % 10) == 0) {
				return String.format("%0.2fM", (quantity / 1000d));
			}
			return String.format("%0.3fM", (quantity / 1000d));
		}
		return String.valueOf(quantity);
	}

	/**
	 * Returns the given price formatted to 6 decimal places
	 * 
	 * @param price
	 *            the price to format
	 * @return a formatted price with 6 decimal places
	 */
	public static final String formatPrice(double price) {
		return String.format("%0.6f", price);
	}

	/**
	 * Returns a string of the form: 1.2M@1.246372
	 * 
	 * @param quantity
	 *            the quantity to format
	 * @param price
	 *            the price to format
	 * @return a formatted string (price with 6 decimal places)
	 * @see #formatQuantity(long)
	 * @see #formatPrice(double)
	 */
	public static final String formatQuantityAndPrice(long quantity, double price) {
		return formatQuantity(quantity) + "@" + formatPrice(price);
	}

	// no instances
	private StringUtil() {
		super();
	}
}
