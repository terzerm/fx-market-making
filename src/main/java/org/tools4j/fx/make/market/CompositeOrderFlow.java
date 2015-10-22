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

import java.util.Arrays;
import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.flow.OrderFlow;

/**
 * A {@link OrderFlow} composite of multiple underlying flows. Provides an easy
 * way to construct a single multiply asset-pair flow from single-asset-pair
 * flows.
 */
public class CompositeOrderFlow implements OrderFlow {

	private final AtomicLong nextFlowIndex = new AtomicLong(0);  
	protected final OrderFlow[] orderFlows;

	public CompositeOrderFlow(OrderFlow... orderFlows) {
		this.orderFlows = Arrays.copyOf(orderFlows, orderFlows.length);
		if (Arrays.stream(orderFlows).anyMatch(x -> x == null)) {
			throw new IllegalArgumentException("at least one element in array is null: " + Arrays.toString(orderFlows));
		}
	}

	public CompositeOrderFlow(Collection<? extends OrderFlow> orderFlows) {
		this(orderFlows.toArray(new OrderFlow[orderFlows.size()]));
	}
	
	@Override
	public Spliterator<Order> trySplit() {
		return null;
	}
	
	@Override
	public long estimateSize() {
		long size = 0;
		for (final OrderFlow orderFlow : orderFlows) {
			final long flowSize = orderFlow.estimateSize();
			if (flowSize == Long.MAX_VALUE) {
				return Long.MAX_VALUE;
			}
			size += flowSize;
		}
		return size;
	}

	@Override
	public boolean tryAdvance(Consumer<? super Order> action) {
		final int len = orderFlows.length;
		for (int i = 0; i < len; i++) {
			final OrderFlow flow = nextOrderFlow();
			if (flow.tryAdvance(action)) {
				return true;
			}
		}
		return false;
	}

	private OrderFlow nextOrderFlow() {
		final long nextIndex = nextFlowIndex.getAndIncrement();
		final int index = (int)(nextIndex % orderFlows.length);
		return orderFlows[index];
	}
}
