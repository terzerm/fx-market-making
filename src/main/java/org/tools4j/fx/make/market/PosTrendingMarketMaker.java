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

import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.position.PositionKeeper;

/**
 * Pulls off price after being hit trying to anticipate a trend in that direction.
 */
public class PosTrendingMarketMaker extends AbstractPositionAwareMarketMaker {

	private final double spread;
	private final long maxQuantity;
	private volatile double lastBid = Double.NaN;
	private volatile double lastAsk = Double.NaN;
	private volatile Side lastSide = null;
	private volatile int updatesSinceOwn = 0;
	private volatile int bidsUpdatesSinceLast = 1; 
	private volatile int asksUpdatesSinceLast = 1; 

	public PosTrendingMarketMaker(PositionKeeper positionKeeper, AssetPair<?, ?> assetPair, double spread, long maxQuantity) {
		this(positionKeeper, assetPair, PosTrendingMarketMaker.class.getSimpleName(), spread, maxQuantity);
	}
	public PosTrendingMarketMaker(PositionKeeper positionKeeper, AssetPair<?, ?> assetPair, String party, double spread, long maxQuantity) {
		super(positionKeeper, assetPair, party);
		if (assetPair.getBase() != Currency.USD && assetPair.getTerms() != Currency.USD) {
			throw new IllegalArgumentException("base or terms must be USD: " + assetPair);
		}
		if (spread < 0) {
			throw new IllegalArgumentException("spread is negative: " + spread);
		}
		if (maxQuantity < 0) {
			throw new IllegalArgumentException("maxQuantity is negative: " + maxQuantity);
		}
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
	
	private boolean isBaseUSD() {
		return Currency.USD == assetPair.getBase();
	}
	
	private double getPosition() {
		if (isBaseUSD()) {
			return -positionKeeper.getPosition(assetPair.getTerms());
		} else {
			return positionKeeper.getPosition(assetPair.getBase());
		}
	}

	@Override
	protected double nextPrice(Side side, String party, long desiredQuantity) {
		final double mid = getMid();
		if (Double.isNaN(mid)) {
			return side == Side.BUY ? 0 : Double.POSITIVE_INFINITY;
		}
		final double pos = getPosition();
		final double log = Math.max(0, Math.log(Math.abs(pos) / (isJPY() ? 100*maxQuantity : maxQuantity)));
		final double posInc = Math.pow(1.15, log);
		final double posDec = Math.pow(1.15, -log);
		final int maxUpdates = 10;
		if (updatesSinceOwn < maxUpdates) {
			final double trendInc = Math.pow(1.05, maxUpdates-updatesSinceOwn);
			final double trendDec = Math.pow(1.05, updatesSinceOwn-maxUpdates);
			if (lastSide == Side.BUY) {
				//market sells, we should sell
				if (pos > 0) {
					return side == Side.BUY ? addHalfSpreadToMid(-posInc*trendDec) : addHalfSpreadToMid(+posDec*trendInc);
				} else {
					return side == Side.BUY ? addHalfSpreadToMid(-posDec*trendDec) : addHalfSpreadToMid(+posInc*trendInc);
				}
			} else if (lastSide == Side.SELL) {
				//market buys, we should buy
				if (pos > 0) {
					return side == Side.BUY ? addHalfSpreadToMid(-posInc*trendInc) : addHalfSpreadToMid(+posDec*trendDec);
				} else {
					return side == Side.BUY ? addHalfSpreadToMid(-posDec*trendInc) : addHalfSpreadToMid(+posInc*trendDec);
				}
			}
		}
		if (pos > 0) {
			return side == Side.BUY ? addHalfSpreadToMid(-posInc) : addHalfSpreadToMid(+posDec);
		} else {
			return side == Side.BUY ? addHalfSpreadToMid(-posDec) : addHalfSpreadToMid(+posInc);
		}
	}
	
	private final double addHalfSpreadToMid(final double f) {
		return Math.max(0, getMid() + (spread/2) * f);
	}
	
	private final boolean isJPY() {
		return assetPair.getBase() == Currency.JPY || assetPair.getTerms() == Currency.JPY;
	}

	@Override
	protected Order nextOrder(Side side) {
		if (side == Side.BUY) {
			if (bidsUpdatesSinceLast == 0) {
				return null;
			}
			bidsUpdatesSinceLast = 0;
		} else {
			if (asksUpdatesSinceLast == 0) {
				return null;
			}
			asksUpdatesSinceLast = 0;
		}
		return super.nextOrder(side);
	}

	public double getMid() {
		return (lastBid + lastAsk) / 2;
	}
	
	@Override
	public void onOrder(Order order) {
		// we only want best orders
	}

	@Override
	public void onBest(Order order) {
		if (!party.equals(order.getParty())) {
			if (order.getSide() == Side.BUY) {
				lastBid = order.getPrice();
				bidsUpdatesSinceLast++; 
			} else {
				lastAsk = order.getPrice();
				asksUpdatesSinceLast++; 
			}
		}
	}

	@Override
	public void onDeal(Deal deal) {
		super.onDeal(deal);
		lastBid = deal.getPrice();
		lastAsk = deal.getPrice();
		bidsUpdatesSinceLast++; 
		asksUpdatesSinceLast++; 
		if (party.equals(deal.getBuyParty())) {
			updatesSinceOwn = 0;
			lastSide = Side.BUY;
		} else if (party.equals(deal.getSellParty())) {
			updatesSinceOwn = 0;
			lastSide = Side.SELL;
		} else {
			updatesSinceOwn++;
		}
	}

}
