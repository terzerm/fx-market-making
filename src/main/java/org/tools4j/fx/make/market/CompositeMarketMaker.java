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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Order;

/**
 * A {@link MarketMaker} composite of multiple underlying makers. Provides an
 * easy way to support multiply asset-pairs by simply composing
 * single-asset-pair makers.
 */
public class CompositeMarketMaker extends CompositeOrderFlow implements MarketMaker {

	public CompositeMarketMaker(MarketMaker... marketMakers) {
		super(marketMakers);
	}

	public CompositeMarketMaker(Collection<? extends MarketMaker> marketMakers) {
		this(marketMakers.toArray(new MarketMaker[marketMakers.size()]));
	}

	@Override
	public Set<? extends AssetPair<?, ?>> getAssetPairs() {
		final Set<AssetPair<?, ?>> assetPairs = new LinkedHashSet<>();
		for (final MarketMaker marketMaker : (MarketMaker[]) orderFlows) {
			assetPairs.addAll(marketMaker.getAssetPairs());
		}
		return assetPairs;
	}

	@Override
	public void onOrder(Order order) {
		for (final MarketMaker marketMaker : (MarketMaker[]) orderFlows) {
			marketMaker.onOrder(order);
		}
	}

	@Override
	public void onDeal(Deal deal) {
		for (final MarketMaker marketMaker : (MarketMaker[]) orderFlows) {
			marketMaker.onDeal(deal);
		}
	}
	
	@Override
	public void onBest(Order order) {
		for (final MarketMaker marketMaker : (MarketMaker[]) orderFlows) {
			marketMaker.onBest(order);
		}
	}

}
