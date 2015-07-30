package org.tools4j.fx.make.impl;

import java.util.Objects;

import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.Side;

/**
 * An immutable {@link Order}.
 */
public class OrderImpl implements Order {
	private final Side side;
	private final double price;
	private final long quantity;

	public OrderImpl(Side side, double price, long quantity) {
		this.side = Objects.requireNonNull(side, "side is null");
		this.price = price;
		this.quantity = quantity;
	}

	@Override
	public Side getSide() {
		return side;
	}

	@Override
	public double getPrice() {
		return price;
	}

	@Override
	public long getQuantity() {
		return quantity;
	}
}
