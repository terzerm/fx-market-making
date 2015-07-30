package org.tools4j.fx.make.api;

/**
 * An order.
 */
public interface Order {
	/**
	 * Returns the side, never null.
	 * @return the order side
	 */
	Side getSide();
	double getPrice();
	/**
	 * The quantity of the order, non-negative
	 * @return the quantity or amount, not negative
	 */
	long getQuantity();
}
