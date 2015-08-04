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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.asset.CurrencyPair;

/**
 * Unit test for {@link MarketSnapshot} and {@link MarketSnapshotImpl}.
 */
public class MarketSnapshotTest {

	private PairAndRate[] pairsAndRates;
	private MarketSnapshot snapshot;

	@Before
	public void beforeEach() {
		// test data
		pairsAndRates = new PairAndRate[] { //
				new PairAndRate(Currency.AUD, Currency.USD, 0.7307), //
				new PairAndRate(Currency.AUD, Currency.NZD, 1.1081), //
				new PairAndRate(Currency.EUR, Currency.USD, 1.1010), //
				new PairAndRate(Currency.USD, Currency.JPY, 123.92), //
				new PairAndRate(Currency.USD, Currency.CAD, 1.3089),//
		};
		// construct MarketSnapshot
		final Map<CurrencyPair, Double> rates = new LinkedHashMap<>();
		for (final PairAndRate pairAndRate : pairsAndRates) {
			rates.put(pairAndRate.currencyPair, pairAndRate.rate);
		}
		this.snapshot = new MarketSnapshotImpl(rates);
	}

	@Test
	public void shouldFindSelfRate() {
		// given
		final MarketSnapshotImpl emptyRates = new MarketSnapshotImpl(Collections.emptyMap());

		// when + then
		for (final Currency currency : Currency.values()) {
			// when
			final double rate = emptyRates.getRate(currency, currency);
			// then
			assertEquals("self-rate should be 1", 1.0, rate, 0.0);
			// when
			final double again = snapshot.getRate(currency, currency);
			// then
			assertEquals("self-rate should be 1", 1.0, again, 0.0);
		}
	}

	@Test
	public void shouldFindSingleDirectAndIndirectRate() {
		// given
		final CurrencyPair audUsd = new CurrencyPair(Currency.AUD, Currency.USD);
		final double rate = 0.7307;
		final MarketSnapshotImpl rates = new MarketSnapshotImpl(Collections.singletonMap(audUsd, rate));

		// when
		final double direct = rates.getRate(Currency.AUD, Currency.USD);
		// then
		assertEquals("unexpected direct rate", rate, direct, 0.0);
		// when
		final double indirect = rates.getRate(Currency.USD, Currency.AUD);
		// then
		assertEquals("unexpected indirect rate", 1 / rate, indirect, 0.0);
	}

	@Test
	public void shouldFindDirectRates() {
		// when + then
		for (final PairAndRate pairAndRate : pairsAndRates) {
			// when
			final double direct = snapshot.getRate(pairAndRate.currencyPair.getBase(),
					pairAndRate.currencyPair.getTerms());
			// then
			assertEquals("unexpected direct rate for " + pairAndRate.currencyPair, pairAndRate.rate, direct, 0.0);
		}
	}

	@Test
	public void shouldFindIndirectRates() {
		// when + then
		for (final PairAndRate pairAndRate : pairsAndRates) {
			// when
			final double indirect = snapshot.getRate(pairAndRate.currencyPair.getTerms(),
					pairAndRate.currencyPair.getBase());
			// then
			assertEquals("unexpected indirect rate for " + pairAndRate.currencyPair, 1 / pairAndRate.rate, indirect,
					0.0);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExceptionIfRateNotFound() {
		// when
		snapshot.getRate(Currency.USD, Currency.CHF);
		// then: exception
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionForNullAsset1() {
		// when
		snapshot.getRate(null, Currency.USD);
		// then: exception
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionForNullAsset2() {
		// when
		snapshot.getRate(Currency.USD, null);
		// then: exception
	}

	@Test(expected = NullPointerException.class)
	public void shouldThrowExceptionForNullRateInRatesMap() {
		// when
		new MarketSnapshotImpl(Collections.singletonMap(new CurrencyPair(Currency.AUD, Currency.USD), (Double) null));
		// then: exception
	}

	private static final class PairAndRate {
		public final CurrencyPair currencyPair;
		public final double rate;

		public PairAndRate(Currency base, Currency terms, double rate) {
			this.currencyPair = new CurrencyPair(base, terms);
			this.rate = rate;
		}
	}
}
