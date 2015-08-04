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
package org.tools4j.fx.make.market;

import java.util.Objects;

import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.position.AssetPositions;

/**
 * A simple {@link MarketMaker} for a single symbol, party and a constant
 * quantity. The mid market maker starts making with zero BID and infinite OFFER
 * and then always offers mid rate plus some constant spread.
 * <p>
 * If the position allows the makes are one level bid/ask. If the position
 * constrains the order making bid and offered quantities are adjusted and one
 * or both sides are omitted in the making activity if necessary.
 * <p>
 * The class is NOT thread safe.
 */
public class MidMarketMaker extends AbstractPositionAwareMarketMaker {

	private final String party;
	private final double spread;
	private final long maxQuantity;
	private volatile double lastBid = Double.NaN;
	private volatile double lastAsk = Double.NaN;

	public MidMarketMaker(AssetPositions assetPositions, AssetPair<?, ?> assetPair, String party, double spread, long maxQuantity) {
		super(assetPositions, assetPair);
		if (spread < 0) {
			throw new IllegalArgumentException("spread is negative: " + spread);
		}
		if (maxQuantity < 0) {
			throw new IllegalArgumentException("maxQuantity is negative: " + maxQuantity);
		}
		this.party = Objects.requireNonNull(party, "party is null");
		this.spread = spread;
		this.maxQuantity = maxQuantity;
	}

	@Override
	protected String nextParty(Side side) {
		return party;
	}
	
	@Override
	protected long nextQuantity(Side side, String party) {
		return maxQuantity;
	}
	
	@Override
	protected double nextPrice(Side side, String party, long desiredQuantity) {
		final double mid = getMid();
		if (side == Side.BUY) {
			return Double.isNaN(mid) ? 0 : mid - spread / 2;
		} else {
			return Double.isNaN(mid) ? Double.POSITIVE_INFINITY : mid + spread / 2;
		}
	}

	public double getMid() {
		return (lastBid + lastAsk) / 2;
	}

	@Override
	public void onOrder(Order order) {
		if (order.getSide() == Side.BUY) {
			lastBid = order.getPrice();
		} else {
			lastAsk = order.getPrice();
		}
	}

	@Override
	public void onDeal(Deal deal) {
		lastBid = deal.getPrice();
		lastAsk = deal.getPrice();
	}

}
