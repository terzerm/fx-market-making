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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.tools4j.fx.make.api.AssetPair;
import org.tools4j.fx.make.api.MarketMaker;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.OrderMatcher;
import org.tools4j.fx.make.api.Side;
import org.tools4j.fx.make.impl.OrderImpl;

/**
 * A simple {@link MarketMaker} for a single symbol, party and a constant
 * quantity. The mid market maker starts making with zero BID and infinite OFFER
 * and then always offers mid rate plus some constant spread. The makes are
 * always two-sided with depth 1.
 * <p>
 * Accepts orders as defined by the {@link AbstractMarketMaker superclass}.
 * <p>
 * The class is NOT thread safe.
 */
public class MidMarketMaker extends AbstractMarketMaker {

	private final AssetPair<?, ?> assetPair;
	private final String party;
	private final double spread;
	private final long quantity;
	private volatile double lastBid = Double.NaN;
	private volatile double lastAsk = Double.NaN;

	public MidMarketMaker(PositionKeeper positionKeeper, OrderMatcher orderMatcher, AssetPair<?, ?> assetPair, String party, double spread, long quantity) {
		super(positionKeeper, orderMatcher);
		if (spread < 0) {
			throw new IllegalArgumentException("spread is negative: " + spread);
		}
		if (quantity < 0) {
			throw new IllegalArgumentException("quantity is negative: " + quantity);
		}
		this.assetPair = Objects.requireNonNull(assetPair, "assetPair is null");
		this.party = Objects.requireNonNull(party, "party is null");
		this.spread = spread;
		this.quantity = quantity;
	}

	@Override
	protected List<Order> nextOrdersInternal() {
		final double mid = getMid();
		return Arrays.asList(createBuyOrder(mid), createSellOrder(mid));
	}

	private Order createBuyOrder(double mid) {
		final double price = Double.isNaN(mid) ? 0 : mid - spread / 2;
		return new OrderImpl(assetPair, party, Side.BUY, price, quantity);
	}

	private Order createSellOrder(double mid) {
		final double price = Double.isNaN(mid) ? Double.POSITIVE_INFINITY : mid + spread / 2;
		return new OrderImpl(assetPair, party, Side.SELL, price, quantity);
	}

	public double getMid() {
		return (lastBid + lastAsk) / 2;
	}

	@Override
	public long acceptOrReject(Order order) {
		if (order.getSide() == Side.BUY) {
			lastBid = order.getPrice();
		} else {
			lastAsk = order.getPrice();
		}
		return super.acceptOrReject(order);
	}

}
