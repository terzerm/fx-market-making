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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.asset.CurrencyPair;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.DealImpl;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.risk.RiskLimits;
import org.tools4j.fx.make.risk.RiskLimitsImpl;

/**
 * Unit test for {@link PositionKeeper} and {@link PositionKeeperImpl}.
 */
public class PositionKeeperTest {

	private static final double TOLERANCE = 0;
	private static final String BUY_PARTY = "PositionKeeperTest.BUY";
	private static final String SELL_PARTY = "PositionKeeperTest.SELL";
	private final CurrencyPair audUsd = new CurrencyPair(Currency.AUD, Currency.USD);
	private final CurrencyPair eurUsd = new CurrencyPair(Currency.EUR, Currency.USD);
	private final CurrencyPair eurAud = new CurrencyPair(Currency.EUR, Currency.AUD);

	private PositionKeeper positionKeeper;

	@Before
	public void beforeEach() {
		final RiskLimits riskLimits = RiskLimitsImpl.builder()//
				.withMaxAllowedPositionSize(Currency.AUD, 2000000)//
				.withMaxAllowedPositionSize(Currency.USD, 1500000)//
				.withMaxAllowedPositionSize(Currency.EUR, 1500000)//
				.build();
		positionKeeper = new PositionKeeperImpl(riskLimits);
	}

	@Test
	public void shouldUpdatePositionSellThenBuy() {
		// when
		positionKeeper.updatePosition(createDeal(audUsd, 0.80, 1000000), Side.SELL);

		// then
		assertEquals("unexpected AUD position", -1000000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD), TOLERANCE);

		// when
		positionKeeper.updatePosition(createDeal(audUsd, 0.75, 1000000), Side.BUY);

		// then
		assertEquals("unexpected AUD position", 0, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", 50000, positionKeeper.getPosition(Currency.USD), TOLERANCE);
	}

	@Test
	public void shouldUpdatePositionSellThenBlockAnotherSell() {
		// when
		positionKeeper.updatePosition(createDeal(audUsd, 0.80, 1000000), Side.SELL);

		// then
		assertEquals("unexpected AUD position", -1000000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD), TOLERANCE);

		// when
		try {
			positionKeeper.updatePosition(createDeal(audUsd, 0.75, 1000000), Side.SELL);
			Assert.fail("expected: " + IllegalArgumentException.class.getSimpleName());
		} catch (IllegalArgumentException e) {
			// then: deal breaches risk limits
		}

		// then: position unchanged
		assertEquals("unexpected AUD position", -1000000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD), TOLERANCE);
	}

	@Test
	public void shouldUpdatePositionWithUnlimitedRiskLimits() {
		// given
		final long mio = 1000000L;
		final long bio = 1000000000L;
		final long trio = 1000000000000L;
		positionKeeper = new PositionKeeperImpl(RiskLimits.UNLIMITED);

		// when
		positionKeeper.updatePosition(createDeal(audUsd, 1.0, mio), Side.BUY);
		positionKeeper.updatePosition(createDeal(audUsd, 1.0, bio), Side.BUY);
		positionKeeper.updatePosition(createDeal(audUsd, 1.0, trio), Side.BUY);

		// then
		assertEquals("unexpected AUD position", mio + bio + trio, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", -mio - bio - trio, positionKeeper.getPosition(Currency.USD), TOLERANCE);

		// when
		positionKeeper.updatePosition(createDeal(audUsd, 1.0, trio), Side.SELL);
		positionKeeper.updatePosition(createDeal(audUsd, 1.0, trio), Side.SELL);

		// then
		assertEquals("unexpected AUD position", mio + bio - trio, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", -mio - bio + trio, positionKeeper.getPosition(Currency.USD), TOLERANCE);
	}

	@Test
	public void shouldReturnMaxPossibleFillQuantityBuy() {
		// when
		final long fillQty = positionKeeper.getMaxPossibleFillWithoutBreachingRiskLimits(audUsd, Side.BUY, 0.40);

		// then
		assertEquals("unexpected fill quantity", 2000000, fillQty);
	}

	@Test
	public void shouldReturnMaxPossibleFillQuantitySell() {
		// when
		final long fillQty = positionKeeper.getMaxPossibleFillWithoutBreachingRiskLimits(audUsd, Side.SELL, 0.25);

		// then
		assertEquals("unexpected fill quantity", 2000000, fillQty);
	}

	@Test
	public void shouldReturnMaxPossibleFillQuantityBuyAfterPreviousSell() {
		// when
		positionKeeper.updatePosition(createDeal(audUsd, 0.80, 1000000), Side.SELL);

		// then
		assertEquals("unexpected AUD position", -1000000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD), TOLERANCE);

		// when
		final long fillQty = positionKeeper.getMaxPossibleFillWithoutBreachingRiskLimits(audUsd, Side.BUY, 0.80);

		// then
		assertEquals("unexpected fill quantity", 875000, fillQty);
	}

	@Test
	public void shouldReturnMaxPossibleFillQuantitySellAfterPreviousBuy() {
		// when
		positionKeeper.updatePosition(createDeal(audUsd, 0.75, 1000000), Side.BUY);

		// then
		assertEquals("unexpected AUD position", 1000000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", -750000, positionKeeper.getPosition(Currency.USD), TOLERANCE);

		// when
		final long fillQty = positionKeeper.getMaxPossibleFillWithoutBreachingRiskLimits(audUsd, Side.SELL, 0.80);

		// then
		assertEquals("unexpected fill quantity", 937500, fillQty);
	}

	@Test
	public void shouldUpdatePositionBuy3Pairs() {
		// when
		positionKeeper.updatePosition(createDeal(audUsd, 0.75, 1000000), Side.BUY);

		// then
		assertEquals("unexpected AUD position", 1000000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", -750000, positionKeeper.getPosition(Currency.USD), TOLERANCE);
		assertEquals("unexpected EUR position", 0, positionKeeper.getPosition(Currency.EUR), TOLERANCE);

		// when
		positionKeeper.updatePosition(createDeal(eurAud, 1.25, 1000000), Side.BUY);

		// then
		assertEquals("unexpected AUD position", -250000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", -750000, positionKeeper.getPosition(Currency.USD), TOLERANCE);
		assertEquals("unexpected EUR position", 1000000, positionKeeper.getPosition(Currency.EUR), TOLERANCE);

		// when
		positionKeeper.updatePosition(createDeal(eurUsd, 1.20, 500000), Side.BUY);

		// then
		assertEquals("unexpected AUD position", -250000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		assertEquals("unexpected USD position", -1350000, positionKeeper.getPosition(Currency.USD), TOLERANCE);
		assertEquals("unexpected EUR position", 1500000, positionKeeper.getPosition(Currency.EUR), TOLERANCE);
	}

	private static Deal createDeal(AssetPair<?, ?> assetPair, double price, long qty) {
		return new DealImpl(assetPair, price, qty, Order.ID_GENERATOR.incrementAndGet(), BUY_PARTY,
				Order.ID_GENERATOR.incrementAndGet(), SELL_PARTY);
	}

}
