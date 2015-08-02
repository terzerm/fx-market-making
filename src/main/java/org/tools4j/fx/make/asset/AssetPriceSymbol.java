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
package org.tools4j.fx.make.asset;

/**
 * Returns the symbol for an asset price, such as "GOOG/USD" reading that Google
 * shares are quoted in USD.
 *
 * @param <A>
 *            the asset type
 */
public class AssetPriceSymbol<A extends Asset> extends AbstractAssetPair<A, Currency> {

	/**
	 * Constructor with asset and quoting currency.
	 * 
	 * @param asset
	 *            the priced asset
	 * @param currency
	 *            the pricing currency
	 */
	public AssetPriceSymbol(A asset, Currency currency) {
		super(asset, currency);
	}

	/**
	 * Returns the asset delegating to {@link #getBase()}.
	 * 
	 * @return the base asset
	 */
	public A getAsset() {
		return getBase();
	}

	/**
	 * Returns the currency in which prices for the asset are quoted. Delegates
	 * to {@link #getTerms()}.
	 * 
	 * @return the terms currency
	 */
	public Currency getCurrency() {
		return super.getTerms();
	}

}
