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
package org.tools4j.fx.make.flow;

import java.util.Collection;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.tools4j.fx.make.execution.Order;

/**
 * An {@link OrderFlow} returning orders based on a stream (or collection)
 * of orders.
 */
public class StreamOrderFlow implements OrderFlow {
	
	private final Spliterator<Order> orders;
	
	public StreamOrderFlow(Collection<Order> orders) {
		this(orders.stream());
	}
	public StreamOrderFlow(Stream<Order> orders) {
		this.orders = orders.sorted(COMPARATOR).spliterator();
	}
	
	@Override
	public int characteristics() {
		return orders.characteristics() | REQUIRED_CHARACTERISTICS;
	}
	
	@Override
	public long estimateSize() {
		return orders.estimateSize();
	}
	
	@Override
	public Spliterator<Order> trySplit() {
		return orders.trySplit();
	}
	
	@Override
	public boolean tryAdvance(Consumer<? super Order> action) {
		return orders.tryAdvance(action);
	}

}
