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

import java.time.Instant;
import java.util.Comparator;
import java.util.Spliterator;

import org.tools4j.fx.make.execution.Order;

/**
 * Order flow is a stream of orders. This can be a market maker or an input flow
 * of client orders; it may also refer to the stream of bid/ask prices from a
 * price source such as a file with historical data.
 */
public interface OrderFlow extends Spliterator<Order> {
	/**
	 * The characteristics required for all order flow implementations.
	 */
	int REQUIRED_CHARACTERISTICS = ORDERED | DISTINCT | SORTED | NONNULL;

	/**
	 * The order-flow comparator which sorts orders by time, older orders first, then by ID.
	 */
	Comparator<Order> COMPARATOR = Comparator.<Order, Instant>comparing(o -> o.getTime()).thenComparing(o -> o.getId()); 

	/**
	 * Default implementation returning {@link #REQUIRED_CHARACTERISTICS}.
	 * 
	 * @return {@link #REQUIRED_CHARACTERISTICS}.
	 */
	@Override
	default int characteristics() {
		return REQUIRED_CHARACTERISTICS;
	}

	/**
	 * Default implementation returning {@link #COMPARATOR}.
	 * 
	 * @return comparator sorting orders by time in ascending order
	 */
	default Comparator<? super Order> getComparator() {
		return COMPARATOR;
	}

}
