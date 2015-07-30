package org.tools4j.fx.make.api;

import java.util.List;

/**
 * An order book with bid and ask orders.
 */
public interface Maker {
	/**
	 * Returns a list of orders that this maker is willing to place in the
	 * market. The orders need not necessarily be sorted by price.
	 * 
	 * @param side
	 *            the side of the orders to return
	 * @return a list of orders all orders on the given side
	 */
	List<Order> make(Side side);
}
