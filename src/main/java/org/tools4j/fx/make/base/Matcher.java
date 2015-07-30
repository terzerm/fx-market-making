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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.tools4j.fx.make.api.ExecutionResult;
import org.tools4j.fx.make.api.Maker;
import org.tools4j.fx.make.api.MakerDealer;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.OrderPriceComparator;
import org.tools4j.fx.make.api.Settings;
import org.tools4j.fx.make.api.Side;
import org.tools4j.fx.make.impl.OrderImpl;

/**
 * A <tt>Matcher</tt> queries orders from a {@link Maker} and a
 * {@link MakerDealer} and offers them to the maker/dealer at mid price if a
 * match is possible. The matcher also keeps track of the maker/dealer's position
 * and throws an exception if the position exceeds the max allowed limit.
 */
public class Matcher {

	private final Maker maker;
	private final MakerDealer makerDealer;
	private final Settings settings;
	private final PositionKeeper dealerPosition = new PositionKeeper();

	public Matcher(Maker maker, MakerDealer makerDealer, Settings settings) {
		this.maker = Objects.requireNonNull(maker, "maker is null");
		this.makerDealer = Objects.requireNonNull(makerDealer, "makerDealer is null");
		this.settings = Objects.requireNonNull(settings, "settings is null");
	}

	public int matchNext() {
		int count = 0;
		final List<Order> bids1 = maker.make(Side.BUY);
		final List<Order> asks2 = makerDealer.make(Side.SELL);
		count += match(bids1, asks2, Side.BUY);
		final List<Order> asks1 = maker.make(Side.SELL);
		final List<Order> bids2 = makerDealer.make(Side.BUY);
		count += match(bids2, asks1, Side.SELL);
		return count;
	}

	private final int match(List<Order> bids, List<Order> asks, Side takerSide) {
		if (bids.isEmpty() | asks.isEmpty()) {
			return 0;
		}
		if (bids.size() == 1 & asks.size() == 1) {
			return match(bids.get(0), asks.get(0), takerSide) ? 1 : 0;
		}
		int count = 0;
		final LinkedList<Order> sortedBids = new LinkedList<Order>(bids);
		final LinkedList<Order> sortedAsks = new LinkedList<Order>(asks);
		Collections.sort(sortedBids, OrderPriceComparator.BUY);
		Collections.sort(sortedAsks, OrderPriceComparator.SELL);
		while (!sortedBids.isEmpty() & !sortedAsks.isEmpty()) {
			final Order bid = sortedBids.removeFirst();
			for (final Order ask : sortedAsks) {
				if (!matchesPrice(bid, ask)) {
					return count;
				}
				if (matchesQuantity(bid, ask)) {
					if (match(bid, ask, takerSide)) {
						count++;
						sortedAsks.removeFirst();
						break;
					}
				}
			}
		}
		return count;
	}

	private final boolean match(Order bid, Order ask, Side takerSide) {
		final Order midOrder = matchMidOrNull(bid, ask, takerSide);
		final ExecutionResult result = makerDealer.acceptOrReject(midOrder);
		if (result == ExecutionResult.FILLED) {
			dealerPosition.update(midOrder);
			if (dealerPosition.getPosition() > settings.getMaxAllowedPositionSize()) {
				throw new IllegalStateException("dealer position exceeds max allowed position size: "
						+ dealerPosition.getPosition() + " > " + settings.getMaxAllowedPositionSize());
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns true if both orders have the same quantity, one order is a BUY
	 * and the other a SELL order and if the order prices cross the spread.
	 * 
	 * @param order1
	 *            the first order
	 * @param order2
	 *            the second order
	 * @return true if a match is possible
	 */
	public static boolean matches(Order order1, Order order2) {
		return matchesQuantity(order1, order2) && matchesPrice(order1, order2);
	}

	private static boolean matchesQuantity(Order order1, Order order2) {
		return order1.getQuantity() == order2.getQuantity();
	}

	private static boolean matchesPrice(Order order1, Order order2) {
		if (isBuy(order1) & isSell(order2)) {
			return order1.getPrice() >= order2.getPrice();
		} else if (isSell(order1) & isBuy(order2)) {
			return order1.getPrice() <= order2.getPrice();
		}
		return false;
	}

	/**
	 * Returns a mid price order if the two orders {@link #matches(Order, Order)
	 * match} and null otherwise.
	 * 
	 * @param order1
	 *            the first order
	 * @param order2
	 *            the second order
	 * @param side
	 *            the side of the returned order
	 * @return a mid price order on the given side if a match is possible, and
	 *         null otherwise
	 */
	public static Order matchMidOrNull(Order order1, Order order2, Side side) {
		if (matches(order1, order2)) {
			return new OrderImpl(side, getMid(order1, order2), order1.getQuantity());
		}
		return null;
	}

	private static boolean isBuy(Order order) {
		return order.getSide() == Side.BUY;
	}

	private static boolean isSell(Order order) {
		return order.getSide() == Side.SELL;
	}

	private static double getMid(Order order1, Order order2) {
		return (order1.getPrice() + order2.getPrice()) / 2;
	}
}
