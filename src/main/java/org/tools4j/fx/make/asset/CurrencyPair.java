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

import static java.util.Arrays.asList;
import static org.tools4j.fx.make.asset.Currency.AUD;
import static org.tools4j.fx.make.asset.Currency.BWP;
import static org.tools4j.fx.make.asset.Currency.EUR;
import static org.tools4j.fx.make.asset.Currency.FJD;
import static org.tools4j.fx.make.asset.Currency.GBP;
import static org.tools4j.fx.make.asset.Currency.NZD;
import static org.tools4j.fx.make.asset.Currency.PGK;
import static org.tools4j.fx.make.asset.Currency.SBD;
import static org.tools4j.fx.make.asset.Currency.TOP;
import static org.tools4j.fx.make.asset.Currency.USD;
import static org.tools4j.fx.make.asset.Currency.WST;

import java.util.List;

public class CurrencyPair extends AbstractAssetPair<Currency, Currency> {

	private static final List<Currency> PRECEDENCE = asList(EUR, GBP, AUD, NZD, FJD, TOP, WST, PGK, BWP, SBD, USD);

	public CurrencyPair(Currency base, Currency terms) {
		super(base, terms);
	}
	public static CurrencyPair toMarketConvention(Currency currency) {
		return toMarketConvention(currency, Currency.USD);
	}
	public static CurrencyPair toMarketConvention(Currency currency1, Currency currency2) {
		final int index1 = precedence(currency1);
		final int index2 = precedence(currency2);
		if (index1 >= 0 | index2 >= 0) {
			if (index1 < index2) {
				return new CurrencyPair(currency1, currency2);
			}
			if (index1 > index2) {
				return new CurrencyPair(currency2, currency1);
			}
		}
		throw new IllegalArgumentException("market convention undefined for: " + currency1 + "/" + currency2);
	}

	private static int precedence(Currency currency) {
		final int index = PRECEDENCE.indexOf(currency);
		return index >= 0 ? index : Integer.MAX_VALUE;
	}

}
