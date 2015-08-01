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
package org.tools4j.fx.make.base;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tools4j.fx.make.api.Currency;
import org.tools4j.fx.make.api.Settings;
import org.tools4j.fx.make.api.Side;
import org.tools4j.fx.make.impl.CurrencyPair;
import org.tools4j.fx.make.impl.OrderImpl;
import org.tools4j.fx.make.impl.SettingsImpl;

/**
 * Unit test for {@link PositionKeeper}
 */
public class PositionKeeperTest {
	
	private final String party = "PositionKeeperTest";
	private final CurrencyPair audUsd = new CurrencyPair(Currency.AUD, Currency.USD);
	private final CurrencyPair eurUsd = new CurrencyPair(Currency.EUR, Currency.USD);
	private final CurrencyPair eurAud = new CurrencyPair(Currency.EUR, Currency.AUD);
	
	private PositionKeeper positionKeeper;
	
	@Before
	public void beforeEach() {
		final Settings settings = SettingsImpl.builder()
				.withMaxAllowedPositionSize(Currency.AUD, 2000000)
				.withMaxAllowedPositionSize(Currency.USD, 1500000)
				.withMaxAllowedPositionSize(Currency.EUR, 1500000)
				.build();
		positionKeeper = new PositionKeeper(settings);
	}

	@Test
	public void shouldUpdatePositionBuyThenSell() {
		//given
		long fillQty;
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.BUY, 0.80, 1000000), false);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 1000000, fillQty);
		Assert.assertEquals("unexpected AUD position", -1000000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD));
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.SELL, 0.75, 1000000), false);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 1000000, fillQty);
		Assert.assertEquals("unexpected AUD position", 0, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", 50000, positionKeeper.getPosition(Currency.USD));
	}
	
	@Test
	public void shouldUpdatePositionBuyThenBlockAnotherBuy() {
		//given
		long fillQty;
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.BUY, 0.80, 1000000), false);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 1000000, fillQty);
		Assert.assertEquals("unexpected AUD position", -1000000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD));
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.BUY, 0.75, 1000000), false);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 0, fillQty);
		Assert.assertEquals("unexpected AUD position", -1000000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD));
	}
	
	@Test
	public void shouldUpdatePositionPartialBuy() {
		//given
		long fillQty;
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.BUY, 0.40, 3000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 2000000, fillQty);
		Assert.assertEquals("unexpected AUD position", -2000000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD));
	}

	@Test
	public void shouldUpdatePositionPartialSell() {
		//given
		long fillQty;
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.SELL, 0.25, 10000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 2000000, fillQty);
		Assert.assertEquals("unexpected AUD position", 2000000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", -500000, positionKeeper.getPosition(Currency.USD));
	}

	@Test
	public void shouldUpdatePositionBuyThenPartialBuy() {
		//given
		long fillQty;
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.BUY, 0.80, 1000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 1000000, fillQty);
		Assert.assertEquals("unexpected AUD position", -1000000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", 800000, positionKeeper.getPosition(Currency.USD));
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.BUY, 0.80, 1000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 875000, fillQty);
		Assert.assertEquals("unexpected AUD position", -1875000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", 1500000, positionKeeper.getPosition(Currency.USD));
	}
	
	@Test
	public void shouldUpdatePositionSellThenPartialSell() {
		//given
		long fillQty;
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.SELL, 0.75, 1000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 1000000, fillQty);
		Assert.assertEquals("unexpected AUD position", 1000000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", -750000, positionKeeper.getPosition(Currency.USD));
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.SELL, 0.80, 1000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 937500, fillQty);
		Assert.assertEquals("unexpected AUD position", 1937500, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", -1500000, positionKeeper.getPosition(Currency.USD));
	}

	@Test
	public void shouldUpdatePositionSellAndSell3Pairs() {
		//given
		long fillQty;
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.SELL, 0.75, 1000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 1000000, fillQty);
		Assert.assertEquals("unexpected AUD position", 1000000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", -750000, positionKeeper.getPosition(Currency.USD));
		Assert.assertEquals("unexpected EUR position", 0, positionKeeper.getPosition(Currency.EUR));
		
		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(eurAud, party, Side.SELL, 1.25, 1000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 1000000, fillQty);
		Assert.assertEquals("unexpected AUD position", -250000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", -750000, positionKeeper.getPosition(Currency.USD));
		Assert.assertEquals("unexpected EUR position", 1000000, positionKeeper.getPosition(Currency.EUR));

		//when
		fillQty = positionKeeper.fillWithoutExceedingMax(new OrderImpl(eurUsd, party, Side.SELL, 1.20, 1000000), true);
		
		//then
		Assert.assertEquals("unexpected fill quantity", 500000, fillQty);
		Assert.assertEquals("unexpected AUD position", -250000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", -1350000, positionKeeper.getPosition(Currency.USD));
		Assert.assertEquals("unexpected EUR position", 1500000, positionKeeper.getPosition(Currency.EUR));
	}
	
	@Test
	public void shouldValuate() {
		//given: position
		positionKeeper.fillWithoutExceedingMax(new OrderImpl(audUsd, party, Side.SELL, 0.75, 1000000), true);
		positionKeeper.fillWithoutExceedingMax(new OrderImpl(eurAud, party, Side.SELL, 1.25, 1000000), true);
		positionKeeper.fillWithoutExceedingMax(new OrderImpl(eurUsd, party, Side.SELL, 1.20, 1000000), true);
		Assert.assertEquals("unexpected AUD position", -250000, positionKeeper.getPosition(Currency.AUD));
		Assert.assertEquals("unexpected USD position", -1350000, positionKeeper.getPosition(Currency.USD));
		Assert.assertEquals("unexpected EUR position", 1500000, positionKeeper.getPosition(Currency.EUR));

		//given: market rates
		final MarketRates marketRates = MarketRates.builder()
				.withRate(audUsd, 0.76)
				.withRate(eurUsd, 1.22)
				.withRate(eurAud, 1.26)
				.build();
		
		//when: DIRECT
		final double valAud = positionKeeper.getValuation(Currency.AUD, Currency.AUD, marketRates);
		final double valUsd = positionKeeper.getValuation(Currency.USD, Currency.USD, marketRates);
		final double valEur = positionKeeper.getValuation(Currency.EUR, Currency.EUR, marketRates);
		//then: DIRECT
		Assert.assertEquals("unexpected AUD position valuation", -250000, valAud, 0);
		Assert.assertEquals("unexpected USD position valuation", -1350000, valUsd, 0);
		Assert.assertEquals("unexpected EUR position valuation", 1500000, valEur, 0);
		//when: USD
		final double valAudInUsd = positionKeeper.getValuation(Currency.AUD, Currency.USD, marketRates);
		final double valUsdInUsd = positionKeeper.getValuation(Currency.USD, Currency.USD, marketRates);
		final double valEurInUsd = positionKeeper.getValuation(Currency.EUR, Currency.USD, marketRates);
		final double totalUsd = positionKeeper.getValuation(Currency.USD, marketRates);
		//then: USD
		Assert.assertEquals("unexpected AUD position valuation", -250000*.76, valAudInUsd, 0);
		Assert.assertEquals("unexpected USD position valuation", -1350000, valUsdInUsd, 0);
		Assert.assertEquals("unexpected EUR position valuation", 1500000*1.22, valEurInUsd, 0);
		Assert.assertEquals("unexpected total position valuation", 290000, totalUsd, 0);
		//when: EUR
		final double valAudInEur = positionKeeper.getValuation(Currency.AUD, Currency.EUR, marketRates);
		final double valUsdInEur = positionKeeper.getValuation(Currency.USD, Currency.EUR, marketRates);
		final double valEurInEur = positionKeeper.getValuation(Currency.EUR, Currency.EUR, marketRates);
		final double totalEur = positionKeeper.getValuation(Currency.EUR, marketRates);
		//then: EUR
		Assert.assertEquals("unexpected AUD position valuation", -250000/1.26, valAudInEur, 0);
		Assert.assertEquals("unexpected USD position valuation", -1350000/1.22, valUsdInEur, 0);
		Assert.assertEquals("unexpected EUR position valuation", 1500000, valEurInEur, 0);
		Assert.assertEquals("unexpected total position valuation", 195029.92, totalEur, 1e-2);
	}
}
