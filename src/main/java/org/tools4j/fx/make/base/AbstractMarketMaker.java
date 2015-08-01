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

import java.util.List;

import org.tools4j.fx.make.api.Dealer;
import org.tools4j.fx.make.api.MarketMaker;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.OrderMatcher;

/**
 * An abstract base implementation of {@link MarketMaker} accepting an order if
 * it matches any of the orders that have been previously returned by
 * {@link #nextOrders()}. Deals are furthermore only accepted if they do not
 * breach the maximum allowed position. 
 * <p>
 * Depending on the {@link OrderMatcher} passed to the
 * constructor, partial matches may be supported; a match removes the whole or
 * the matched part of the order from the book. New calls to
 * {@link #nextOrders()} will replace the order book. 
 * <p>
 * The {@link #nextOrders()} method delegate to {@link #nextOrdersInternal()}
 * which is to be implemented by subclasses.
 * <p>
 * The implementation uses a {@link MatchingBookDealer} and a {@link PositionAwareDealer}
 * which need to agree to accept an order.
 * <p>
 * The class is NOT thread safe.
 */
abstract public class AbstractMarketMaker implements MarketMaker {

	private final MatchingBookDealer matchingBookDealer;
	private final Dealer positionAwareMatchingBookDealer;

	public AbstractMarketMaker(PositionKeeper positionKeeper, OrderMatcher orderMatcher) {
		this.matchingBookDealer = new MatchingBookDealer(orderMatcher);
		this.positionAwareMatchingBookDealer = new PositionAwareDealer(positionKeeper, orderMatcher == OrderMatcher.PARTIAL)
				.agreeWith(matchingBookDealer);
	}

	@Override
	public final List<Order> nextOrders() {
		final List<Order> orders = nextOrdersInternal();
		matchingBookDealer.setMatchingBook(orders);
		return orders;
	}

	abstract protected List<Order> nextOrdersInternal();

	@Override
	public long acceptOrReject(Order order) {
		return positionAwareMatchingBookDealer.acceptOrReject(order);
	}

}
