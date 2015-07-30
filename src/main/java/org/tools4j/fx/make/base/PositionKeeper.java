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

import java.util.concurrent.atomic.AtomicLong;

import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.Side;

/**
 * Keeps track of a position.
 * <p>
 * The class is thread safe and lock free.
 */
public class PositionKeeper {
	
	private final AtomicLong position = new AtomicLong();

	public boolean updateIfNotExceedsMax(Order order, long maxPositionSize) {
		if (maxPositionSize < 0) {
			throw new IllegalArgumentException("max position size is negative: " + maxPositionSize);
		}
		final long qty = getSignedOrderQuantity(order);
		long pos = position.get();
		while (Math.abs(pos + qty) <= maxPositionSize) {
			if (position.compareAndSet(pos, pos + qty)) {
				return true;
			}
			// concurrent update, try again
			pos = position.get();
		}
		// position would exceed max allowed limit, reject
		return false;
	}
	
	public long update(Order order) {
		return position.addAndGet(getSignedOrderQuantity(order));
	}
	
	private static long getSignedOrderQuantity(Order order) {
		// if order is buying, we are selling, hence qty is negative
		return order.getSide() == Side.BUY ? -order.getQuantity() : order.getQuantity();
	}
	
	public long getPosition() {
		return position.get();
	}

	public void resetPosition() {
		position.set(0);
	}
}
