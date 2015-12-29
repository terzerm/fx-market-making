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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.position.MarketSnapshot;
import org.tools4j.fx.make.position.MarketSnapshotImpl;

/**
 * Observes the market and registers deals and best orders as last market rates.
 * The market rate is either a deal or the mid price of two best orders, whichever
 * occurred last.
 */
public class MidMarketRates implements MarketObserver {
	
	private final Map<AssetPair<?, ?>, double[]> lastBidAskRates = new ConcurrentHashMap<>();
	
	@Override
	public void onDeal(Deal deal) {
		lastBidAskRates.put(deal.getAssetPair(), new double[]{deal.getPrice(), deal.getPrice()});
	}
	
	@Override
	public void onOrder(Order order) {
		//don't include non-best orders
	}
	
	@Override
	public void onBest(Order order) {
		final double[] rates = lastBidAskRates.computeIfAbsent(order.getAssetPair(), k -> new double[] {0, Double.POSITIVE_INFINITY});
		rates[order.getSide() == Side.BUY ? 0 : 1] = order.getPrice();
	}
	
	public MarketSnapshot getMarketSnapshot() {
		final Map<AssetPair<?, ?>, Double> lastMidRates = lastBidAskRates.keySet().stream().collect(Collectors.toMap(k -> k, k -> getMid(lastBidAskRates.get(k))));
		return new MarketSnapshotImpl(lastMidRates);
	}
	
	private static final double getMid(final double[] bidAsk) {
		return (bidAsk[0] + bidAsk[1]) / 2;
	}
	
}
