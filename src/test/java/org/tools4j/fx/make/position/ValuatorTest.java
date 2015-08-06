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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.asset.CurrencyPair;
import org.tools4j.fx.make.execution.DealImpl;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.risk.RiskLimits;
import org.tools4j.fx.make.risk.RiskLimitsImpl;

/**
 * Unit test for {@link Valuator} and {@link ValuatorImpl}.
 */
public class ValuatorTest {

	private static final double TOLERANCE = 0;
	private final String buyParty = "ValuatorTest.BUY";
	private final String sellParty = "ValuatorTest.SELL";
	private final CurrencyPair audUsd = new CurrencyPair(Currency.AUD, Currency.USD);
	private final CurrencyPair eurUsd = new CurrencyPair(Currency.EUR, Currency.USD);
	private final CurrencyPair eurAud = new CurrencyPair(Currency.EUR, Currency.AUD);

	private PositionKeeper positionKeeper;

	@Before
	public void beforeEach() {
		// risk limits
		final RiskLimits riskLimits = RiskLimitsImpl.builder()//
				.withMaxAllowedPositionSize(Currency.AUD, 2000000)//
				.withMaxAllowedPositionSize(Currency.USD, 1500000)//
				.withMaxAllowedPositionSize(Currency.EUR, 1500000)//
				.build();
		positionKeeper = new PositionKeeperImpl(riskLimits);

		// create some positions
		positionKeeper.updatePosition(new DealImpl(audUsd, 0.75, 1000000, 1, buyParty, 2, sellParty), Side.BUY);
		positionKeeper.updatePosition(new DealImpl(eurAud, 1.25, 1000000, 3, buyParty, 4, sellParty), Side.BUY);
		positionKeeper.updatePosition(new DealImpl(eurUsd, 1.20, 500000, 5, buyParty, 6, sellParty), Side.BUY);
		Assert.assertEquals("unexpected AUD position", -250000, positionKeeper.getPosition(Currency.AUD), TOLERANCE);
		Assert.assertEquals("unexpected USD position", -1350000, positionKeeper.getPosition(Currency.USD), TOLERANCE);
		Assert.assertEquals("unexpected EUR position", 1500000, positionKeeper.getPosition(Currency.EUR), TOLERANCE);
	}

	@Test
	public void shouldValuateInPositionCurrency() {
		// given: market rates
		final MarketSnapshotImpl marketRates = MarketSnapshotImpl.builder().build();

		// when:
		final double valAud = positionKeeper.getValuator(Currency.AUD).getValuation(Currency.AUD, marketRates);
		final double valUsd = positionKeeper.getValuator(Currency.USD).getValuation(Currency.USD, marketRates);
		final double valEur = positionKeeper.getValuator(Currency.EUR).getValuation(Currency.EUR, marketRates);

		// then: value same as position
		Assert.assertEquals("unexpected AUD position valuation", -250000, valAud, 0);
		Assert.assertEquals("unexpected USD position valuation", -1350000, valUsd, 0);
		Assert.assertEquals("unexpected EUR position valuation", 1500000, valEur, 0);
	}

	@Test
	public void shouldValuateInUSD() {
		// given: market rates
		final MarketSnapshotImpl marketRates = MarketSnapshotImpl.builder()//
				.withRate(audUsd, 0.76)//
				.withRate(eurUsd, 1.22)//
				.withRate(eurAud, 1.26)//
				.build();

		// when:
		final Valuator usdValuator = positionKeeper.getValuator(Currency.USD);
		final double valAudInUsd = usdValuator.getValuation(Currency.AUD, marketRates);
		final double valUsdInUsd = usdValuator.getValuation(Currency.USD, marketRates);
		final double valEurInUsd = usdValuator.getValuation(Currency.EUR, marketRates);
		final double totalUsd = usdValuator.getValuation(marketRates);

		// then:
		Assert.assertEquals("unexpected AUD position valuation", -250000 * .76, valAudInUsd, 0);
		Assert.assertEquals("unexpected USD position valuation", -1350000, valUsdInUsd, 0);
		Assert.assertEquals("unexpected EUR position valuation", 1500000 * 1.22, valEurInUsd, 0);
		Assert.assertEquals("unexpected total position valuation", 290000, totalUsd, 0);
	}

	@Test
	public void shouldValuateInEUR() {
		// given: market rates
		final MarketSnapshotImpl marketRates = MarketSnapshotImpl.builder()//
				.withRate(audUsd, 0.76)//
				.withRate(eurUsd, 1.22)//
				.withRate(eurAud, 1.26)//
				.build();

		// when:
		final Valuator eurValuator = positionKeeper.getValuator(Currency.EUR);
		final double valAudInEur = eurValuator.getValuation(Currency.AUD, marketRates);
		final double valUsdInEur = eurValuator.getValuation(Currency.USD, marketRates);
		final double valEurInEur = eurValuator.getValuation(Currency.EUR, marketRates);
		final double totalEur = eurValuator.getValuation(marketRates);

		// then:
		Assert.assertEquals("unexpected AUD position valuation", -250000 / 1.26, valAudInEur, 0);
		Assert.assertEquals("unexpected USD position valuation", -1350000 / 1.22, valUsdInEur, 0);
		Assert.assertEquals("unexpected EUR position valuation", 1500000, valEurInEur, 0);
		Assert.assertEquals("unexpected total position valuation", 195029.92, totalEur, 1e-2);
	}
}
