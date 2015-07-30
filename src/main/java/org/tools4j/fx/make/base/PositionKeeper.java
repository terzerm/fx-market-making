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
