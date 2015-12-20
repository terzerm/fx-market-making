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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.OrderImpl;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.position.PositionKeeper;

/**
 * A {@link MarketMaker} who takes the position size into account when a single
 * level bid/offer market is made. If the position puts no constraint on the
 * making it is always two-sided. If the position constrains the order making
 * bid and offered quantities are adjusted and one or both sides are omitted in
 * the making activity if necessary.
 * <p>
 * The class is NOT thread safe.
 */
abstract public class AbstractPositionAwareMarketMaker implements MarketMaker {

	protected final PositionKeeper positionKeeper;
	protected final AssetPair<?, ?> assetPair;
	protected final String party;

	public AbstractPositionAwareMarketMaker(PositionKeeper positionKeeper, AssetPair<?, ?> assetPair, String party) {
		this.positionKeeper = Objects.requireNonNull(positionKeeper, "positionKeeper is null");
		this.assetPair = Objects.requireNonNull(assetPair, "assetPair is null");
		this.party = Objects.requireNonNull(party, "party is null");
	}
	
	@Override
	public Set<? extends AssetPair<?, ?>> getAssetPairs() {
		return Collections.singleton(assetPair);
	}

	@Override
	public List<Order> nextOrders() {
		final Order bid = nextOrder(Side.BUY);
		final Order ask = nextOrder(Side.SELL);
		if (bid != null & ask != null) {
			return Arrays.asList(bid, ask);
		} else if (bid != null) {
			return Collections.singletonList(bid);
		} else if (ask != null) {
			return Collections.singletonList(ask);
		}
		return Collections.emptyList();
	}

	protected Order nextOrder(Side side) {
		final String party = nextParty(side);
		final long desiredQuantity = nextQuantity(side, party);
		final double price = nextPrice(side, party, desiredQuantity);
		final long constrainedQuantity = nextConnstrainedQuantity(side, party, desiredQuantity, price);
		return constrainedQuantity > 0 ? new OrderImpl(assetPair, party, side, price, constrainedQuantity) : null;
	}

	abstract protected String nextParty(Side side);

	abstract protected long nextQuantity(Side side, String party);

	abstract protected double nextPrice(Side side, String party, long desiredQuantity);

	protected long nextConnstrainedQuantity(Side side, String party, long desiredQuantity, double price) {
		// side is the maker side, but positionKeeper expects taker side
		final Side takerSide = side.opposite();
		final long maxQuantity = positionKeeper.getMaxPossibleFillWithoutBreachingRiskLimits(assetPair, takerSide, price);
		return maxQuantity == -1 ? desiredQuantity : Math.min(desiredQuantity, maxQuantity);
	}
	
	@Override
	public void onDeal(Deal deal) {
		if (party.equals(deal.getBuyParty())) {
			((PositionKeeper)positionKeeper).updatePosition(deal, Side.BUY);
		} else if (party.equals(deal.getSellParty())) {
			((PositionKeeper)positionKeeper).updatePosition(deal, Side.SELL);
		}
	}
	
}
