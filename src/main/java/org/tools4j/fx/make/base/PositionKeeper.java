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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.Side;

/**
 * Keeps track positions for several symbols.
 * <p>
 * The class is thread safe.
 */
public class PositionKeeper {
	
	private final ConcurrentMap<String, Long> positionBySymbol = new ConcurrentHashMap<>();

	public long fillWithoutExceedingMax(Order order, boolean allowPartial, long maxPositionSize) {
		if (maxPositionSize < 0) {
			throw new IllegalArgumentException("max position size is negative: " + maxPositionSize);
		}
		final String symbol = order.getSymbol();
		final long orderQty = getSignedOrderQuantity(order);
		long pos = getPosition(symbol);
		long qty = allowPartial ? Math.min(orderQty, maxPositionSize - Math.abs(pos)) : orderQty;
		while (qty != 0 && Math.abs(pos + qty) <= maxPositionSize) {
			if (compareAndSet(symbol, pos, pos + qty)) {
				return qty;
			}
			// concurrent update, try again
			pos = getPosition(symbol);
			if (allowPartial) {
				qty = Math.min(orderQty, maxPositionSize - Math.abs(pos));
			}
		}
		// no or not enough quantity left
		return 0;
	}
	
	private boolean compareAndSet(String symbol, long expectedPosition, long newPosition) {
		if (newPosition == 0) {
			return positionBySymbol.remove(symbol, expectedPosition);
		}
		if (expectedPosition == 0) {
			final Long old = positionBySymbol.putIfAbsent(symbol, newPosition);
			if (old == null || old.longValue() == 0) {
				return true;
			}
		}
		return positionBySymbol.replace(symbol, expectedPosition, newPosition);
	}
	
	private static long getSignedOrderQuantity(Order order) {
		// if order is buying, we are selling, hence qty is negative
		return order.getSide() == Side.BUY ? -order.getQuantity() : order.getQuantity();
	}
	
	public long getPosition(String symbol) {
		final Long position = positionBySymbol.get(symbol);
		return position == null ? 0 : position.longValue();
	}

	public void resetPosition(String symbol) {
		positionBySymbol.remove(symbol);
	}
	
	public void resetPositions() {
		positionBySymbol.clear();
	}
}
