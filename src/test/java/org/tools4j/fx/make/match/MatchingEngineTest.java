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
package org.tools4j.fx.make.match;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.asset.CurrencyPair;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.OrderImpl;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.flow.ListOrderFlow;
import org.tools4j.fx.make.flow.OrderFlow;
import org.tools4j.fx.make.market.MarketPrinter;

/**
 * Unit test for {@link MatchingEngine} and {@link MatchingEngineImpl}.
 */
public class MatchingEngineTest {
	
	@Rule
	public final TestName testName = new TestName();

	private final MarketPrinter printer = new MarketPrinter();
	private final CurrencyPair audUsd = CurrencyPair.toMarketConvention(Currency.AUD);

	@Test
	public void shouldMatchEmptyFlow() {
		// given
		final List<Order> orders = new ArrayList<>();
		final OrderFlow orderFlow = new ListOrderFlow(orders);
		final MatchingEngine engine = MatchingEngineImpl.builder()//
				.addOrderFlow(orderFlow)//
				.addMarketObserver(printer)//
				.build();

		// when
		final MatchingEngine.MatchingState state = engine.matchAll();

		// then
		Assert.assertEquals("unexpected match index", 0, state.getMatchIndex());
		Assert.assertEquals("unexpected party size", 0, state.getParties().size());
	}

	@Test
	public void shouldMatchThreeOrders() {
		// given
		final List<Order> orders = new ArrayList<>();
		orders.add(new OrderImpl(audUsd, "ANZ", Side.BUY, 0.7134, 1000000));
		orders.add(new OrderImpl(audUsd, "UBS", Side.SELL, 0.7132, 2000000));
		orders.add(new OrderImpl(audUsd, "CS", Side.BUY, 0.7136, 1000000));
		final OrderFlow orderFlow = new ListOrderFlow(orders);
		final MatchingEngine engine = MatchingEngineImpl.builder()//
				.addOrderFlow(orderFlow)//
				.addMarketObserver(printer)//
				.build();

		// when
		final MatchingEngine.MatchingState state = engine.matchAll();

		// then
		Assert.assertEquals("unexpected match index", 1, state.getMatchIndex());
		Assert.assertEquals("unexpected party size", 3, state.getParties().size());
		Assert.assertEquals("unexpected position size", 1000000, getPosition(state, "ANZ", Currency.AUD), 0);
		Assert.assertEquals("unexpected position size", -1000000 * .7133, getPosition(state, "ANZ", Currency.USD), 0);
		Assert.assertEquals("unexpected position size", -2000000, getPosition(state, "UBS", Currency.AUD), 0);
		Assert.assertEquals("unexpected position size", 2000000 * .71335, getPosition(state, "UBS", Currency.USD), 0);
		Assert.assertEquals("unexpected position size", 1000000, getPosition(state, "CS", Currency.AUD), 0);
		Assert.assertEquals("unexpected position size", -1000000 * .7134, getPosition(state, "CS", Currency.USD), 0);
	}

	private static final double getPosition(MatchingEngine.MatchingState state, String party, Currency ccy) {
		return state.getPartyState(party).getAssetPositions().getPosition(ccy);
	}
}
