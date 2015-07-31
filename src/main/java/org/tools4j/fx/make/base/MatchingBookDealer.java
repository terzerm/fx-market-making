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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.tools4j.fx.make.api.Dealer;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.OrderMatcher;
import org.tools4j.fx.make.impl.OrderImpl;

/**
 * A {@link Dealer} who accepts an {@link Order} if it matches any of the orders
 * in a matching book. Depending on the {@link OrderMatcher} passed to the
 * constructor, partial matches may be supported; a match removes the whole or
 * the matched part of the order from the matching book. The matching book can
 * be set or replaced via {@link #setMatchingBook(List)}.
 */
public class MatchingBookDealer implements Dealer {

	private final OrderMatcher orderMatcher;
	private final AtomicReference<List<Order>> matchingBook = new AtomicReference<List<Order>>(Collections.emptyList());

	public MatchingBookDealer(OrderMatcher orderMatcher) {
		this.orderMatcher = Objects.requireNonNull(orderMatcher, "orderMatcher is null");
	}

	public MatchingBookDealer(OrderMatcher orderMatcher, List<Order> initialMatchingBook) {
		this(orderMatcher);
		setMatchingBook(initialMatchingBook);
	}

	public void setMatchingBook(List<Order> matchingBook) {
		this.matchingBook.set(new ArrayList<>(matchingBook));
	}

	@Override
	public long acceptOrReject(Order order) {
		final List<Order> empty = Collections.emptyList();
		final List<Order> book = matchingBook.getAndSet(empty);
		try {
			for (int i = 0; i < book.size(); i++) {
				final Order ownOrder = book.get(i);
				final long matchQty = orderMatcher.matchQuantity(ownOrder, order);
				if (matchQty > 0) {
					if (matchQty == ownOrder.getQuantity()) {
						// remove order, cannot be matched again
						book.remove(i);
					} else {
						// replace order with remaining-quantity order
						book.set(i, new OrderImpl(ownOrder, ownOrder.getQuantity() - matchQty));
					}
					return matchQty;
				}
			}
			return 0;
		} finally {
			matchingBook.compareAndSet(empty, book);
		}
	}

}
