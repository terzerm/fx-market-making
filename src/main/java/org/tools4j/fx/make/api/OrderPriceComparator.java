package org.tools4j.fx.make.api;

import java.util.Comparator;

/**
 * Compares order prices in descending (BUY) and ascending (SELL) order.
 * Can be used to construct an order book sorted by price.
 */
public enum OrderPriceComparator implements Comparator<Order> {
	BUY, SELL;
	@Override
	public int compare(Order o1, Order o2) {
		final int cmp = Double.compare(o1.getPrice(), o2.getPrice());
		return this == BUY ? -cmp : cmp;
	}
}
