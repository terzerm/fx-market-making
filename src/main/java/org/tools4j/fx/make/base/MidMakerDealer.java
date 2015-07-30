package org.tools4j.fx.make.base;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.tools4j.fx.make.api.ExecutionResult;
import org.tools4j.fx.make.api.MakerDealer;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.Side;
import org.tools4j.fx.make.api.Dealer;
import org.tools4j.fx.make.impl.OrderImpl;

/**
 * A simple {@link MakerDealer} who starts making with zero BID and infinite OFFER
 * and then always offers mid rate plus some constant spread. The makes are
 * always two-sided with depth 1 and constant quantity defined at construction
 * time.
 * <p>
 * All {@link Dealer} activity is delegated to a dealer passed to the constructor.
 */
public class MidMakerDealer implements MakerDealer {

	private final double spread;
	private final long quantity;
	private final Dealer dealer;
	private volatile double lastBid = Double.NaN;
	private volatile double lastAsk = Double.NaN;

	public MidMakerDealer(double spread, long quantity, Dealer dealer) {
		if (spread < 0) {
			throw new IllegalArgumentException("spread is negative: " + spread);
		}
		if (quantity < 0) {
			throw new IllegalArgumentException("quantity is negative: " + quantity);
		}
		this.spread = spread;
		this.quantity = quantity;
		this.dealer = Objects.requireNonNull(dealer, "dealer is null");
	}

	@Override
	public List<Order> make(Side side) {
		final double price;
		final double mid = getMid();
		if (Double.isNaN(mid)) {
			price = side == Side.BUY ? 0 : Double.POSITIVE_INFINITY;
		} else {
			price = mid + (side == Side.BUY ? -spread : spread) / 2;
		}
		return Collections.singletonList(new OrderImpl(side, price, quantity));
	}
	
	public double getMid() {
		return (lastBid + lastAsk) / 2;
	}

	@Override
	public ExecutionResult acceptOrReject(Order order) {
		if (order.getSide() == Side.BUY) {
			lastBid = order.getPrice();
		} else {
			lastAsk = order.getPrice();
		}
		return dealer.acceptOrReject(order);
	}

}
