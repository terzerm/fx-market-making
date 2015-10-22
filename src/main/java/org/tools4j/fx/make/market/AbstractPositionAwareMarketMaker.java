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

import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.OrderImpl;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.position.AssetPositions;

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

	protected final AssetPositions assetPositions;
	protected final AssetPair<?, ?> assetPair;
	
	private final AtomicReference<Side> nextSide = new AtomicReference<Side>(Side.BUY);
	private final AtomicInteger nullOrderCount = new AtomicInteger(0);
	
	public AbstractPositionAwareMarketMaker(AssetPositions assetPositions, AssetPair<?, ?> assetPair) {
		this.assetPositions = Objects.requireNonNull(assetPositions, "assetPositions is null");
		this.assetPair = Objects.requireNonNull(assetPair, "assetPair is null");
	}
	
	@Override
	public Set<? extends AssetPair<?, ?>> getAssetPairs() {
		return Collections.singleton(assetPair);
	}
	
	@Override
	public Spliterator<Order> trySplit() {
		return null;
	}
	
	@Override
	public long estimateSize() {
		return Long.MAX_VALUE;
	}

	@Override
	public boolean tryAdvance(Consumer<? super Order> action) {
		if (nullOrderCount.get() != 0) {
			return false;
		}
		final Instant time = nextTime();
		//try up to 3 times, since this could be ASK and there is no ASK
		//then try next round BID and there is no BID, and same round try ASK
		Side side;
		int nullOrders;
		do {
			side = nextSide();
			final Order order = nextOrder(time, side);
			if (order != null) {
				action.accept(order);
				nullOrderCount.set(0);
				return true;
			}
			nullOrders = nullOrderCount.incrementAndGet();
		} while (nullOrders < 2 || (nullOrders == 2 && side == Side.BUY));
		return false;
	}
	
	private Side nextSide() {
		Side side = nextSide.get();
		while (!nextSide.compareAndSet(side, side.opposite())) {
			side = nextSide.get();
		}
		return side;
	}
	
	protected Order nextOrder(Instant time, Side side) {
		final String party = nextParty(time, side);
		final long desiredQuantity = nextQuantity(time, side, party);
		final double price = nextPrice(time, side, party, desiredQuantity);
		final long constrainedQuantity = nextConnstrainedQuantity(time, side, party, desiredQuantity, price);
		return constrainedQuantity > 0 ? new OrderImpl(time, assetPair, party, side, price, constrainedQuantity) : null;
	}
	
	abstract protected Instant nextTime();

	abstract protected String nextParty(Instant time, Side side);

	abstract protected long nextQuantity(Instant time, Side side, String party);

	abstract protected double nextPrice(Instant time, Side side, String party, long desiredQuantity);

	protected long nextConnstrainedQuantity(Instant time, Side side, String party, long desiredQuantity, double price) {
		// side is the maker side, but assetPositions expects taker side
		final Side takerSide = side.opposite();
		final long maxQuantity = assetPositions.getMaxPossibleFillWithoutBreachingRiskLimits(assetPair, takerSide, price);
		return maxQuantity == -1 ? desiredQuantity : Math.min(desiredQuantity, maxQuantity);
	}

}
