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
package org.tools4j.fx.make.market;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.tools4j.fx.make.execution.Order;

/**
 * A {@link OrderFlow} composite of multiple underlying flows. Provides an easy
 * way to construct a single multiply asset-pair flow from single-asset-pair
 * flows.
 */
public class CompositeOrderFlow implements OrderFlow {

	protected final OrderFlow[] orderFlows;

	public CompositeOrderFlow(OrderFlow... orderFlows) {
		this.orderFlows = Arrays.copyOf(orderFlows, orderFlows.length);
	}

	public CompositeOrderFlow(Collection<? extends OrderFlow> orderFlows) {
		this(orderFlows.toArray(new OrderFlow[orderFlows.size()]));
	}

	@Override
	public List<Order> nextOrders() {
		final ArrayList<Order> orders = new ArrayList<>();
		for (final OrderFlow orderFlow : orderFlows) {
			orders.addAll(orderFlow.nextOrders());
		}
		return orders;
	}
}
