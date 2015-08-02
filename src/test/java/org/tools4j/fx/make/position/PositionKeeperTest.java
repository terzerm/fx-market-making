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
import org.tools4j.fx.make.config.Settings;
import org.tools4j.fx.make.config.SettingsImpl;
import org.tools4j.fx.make.execution.OrderImpl;
import org.tools4j.fx.make.execution.Side;

/**
 * Unit test for {@link PositionKeeper} and {@link PositionKeeperImpl}.
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
		positionKeeper = new PositionKeeperImpl(settings);
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
}
