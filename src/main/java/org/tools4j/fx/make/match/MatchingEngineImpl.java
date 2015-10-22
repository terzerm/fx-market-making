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
package org.tools4j.fx.make.match;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.DealImpl;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.OrderImpl;
import org.tools4j.fx.make.execution.OrderPriceComparator;
import org.tools4j.fx.make.execution.Side;
import org.tools4j.fx.make.flow.OrderFlow;
import org.tools4j.fx.make.market.LastMarketRates;
import org.tools4j.fx.make.market.MarketMaker;
import org.tools4j.fx.make.market.MarketObserver;
import org.tools4j.fx.make.position.AssetPositions;
import org.tools4j.fx.make.position.AssetPositionsImpl;
import org.tools4j.fx.make.position.HighWaterMarkPositionKeeper;
import org.tools4j.fx.make.position.MarketSnapshot;
import org.tools4j.fx.make.risk.RiskLimits;

public class MatchingEngineImpl implements MatchingEngine {

	private final Instant initialTime;
	private final List<OrderFlow> orderFlows;
	private final Map<String, PartyStateImpl> partyStateByParty;
	private final List<MarketObserver> marketObservers;

	public MatchingEngineImpl(Instant initialTime, List<? extends OrderFlow> orderFlows, Map<? extends String, ? extends RiskLimits> riskLimitsByParty, Collection<? extends MarketObserver> marketObservers) {
		Objects.requireNonNull(orderFlows, "orderFlows is null");
		Objects.requireNonNull(riskLimitsByParty, "riskLimitsByParty is null");
		Objects.requireNonNull(marketObservers, "marketObservers is null");
		this.initialTime = Objects.requireNonNull(initialTime, "initialTime is null");
		this.orderFlows = new ArrayList<>(orderFlows);
		this.partyStateByParty = riskLimitsByParty.entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> new PartyStateImpl(e.getKey(), e.getValue())));
		this.marketObservers = new ArrayList<>(marketObservers);
	}
	
	public static Builder builder() {
		return new BuilderImpl();
	}

	@Override
	public MatchingState matchFirst() {
		final MatchingStateImpl matchingState = new MatchingStateImpl(initialTime);
		if (!matchingState.nextOrderToOrderFlow.isEmpty()) {
			return matchingState.matchNext();
		}
		matchingState.index.incrementAndGet();
		return matchingState;
	}

	private void match(MatchingStateImpl matchingState, AssetPair<?, ?> assetPair, List<Order> assetOrders) {
		final Stream<Order> bidStream = assetOrders.stream().filter(o -> o.getSide() == Side.BUY).sorted(OrderPriceComparator.BUY);
		final Stream<Order> askStream = assetOrders.stream().filter(o -> o.getSide() == Side.SELL).sorted(OrderPriceComparator.SELL);
		final Iterator<Order> bids = bidStream.iterator();
		final Iterator<Order> asks = askStream.iterator();
		Order bid = matchingState.notifyAndReturnNextOrderOrNull(bids);
		Order ask = matchingState.notifyAndReturnNextOrderOrNull(asks);
		//match as long as possible
		while (bid != null & ask != null) {
			long matchQty = OrderMatcher.matchQuantity(bid, ask);
			if (matchQty != 0) {
				final double midRate = (bid.getPrice() + ask.getPrice()) / 2;
				final PartyStateImpl bidState = partyStateByParty.get(bid.getParty()); 
				final PartyStateImpl askState = partyStateByParty.get(ask.getParty());
				long bidQty = matchQty; 
				long askQty = matchQty; 
				if (bidState != null) {
					final long maxBid = bidState.getAssetPositions().getMaxPossibleFillWithoutBreachingRiskLimits(bid.getAssetPair(), Side.BUY, midRate);
					bidQty = maxBid >= 0 ? Math.min(matchQty, maxBid) : matchQty;
				}
				if (askState != null) {
					final long maxAsk = askState.getAssetPositions().getMaxPossibleFillWithoutBreachingRiskLimits(ask.getAssetPair(), Side.SELL, midRate);
					askQty = maxAsk >= 0 ? Math.min(matchQty, maxAsk) : matchQty;
				}
				if (bidQty != 0 & askQty != 0) {
					//match
					final long fillQty = Math.min(bidQty, askQty);
					final Instant time = bid.getTime().isAfter(ask.getTime()) ? ask.getTime() : bid.getTime();
					final Deal deal = new DealImpl(time, assetPair, midRate, fillQty, bid.getId(), bid.getParty(), ask.getId(), ask.getParty());
					getOrCreatePartyState(bid.getParty()).registerDeal(deal, Side.BUY);
					getOrCreatePartyState(ask.getParty()).registerDeal(deal, Side.SELL);
					matchingState.notifyAllMarketObservers(deal);
					//carve out fillQty or go to next if fully filled
					bid = matchingState.getRemainingOrderOrNext(deal, bid, bids);
					ask = matchingState.getRemainingOrderOrNext(deal, ask, asks);
				} else {
					//no match due to risk limit breaches, try next
					if (bidQty == 0) {
						bid = matchingState.notifyAndReturnNextOrderOrNull(bids);
					}
					if (askQty == 0) {
						ask = matchingState.notifyAndReturnNextOrderOrNull(asks);
					}
				}
			} else {
				//no match possible with increasing spread
				break;
			}
		}
		//notify market observers of the remaining unmatched orders
		while (bid != null) {
			bid = matchingState.notifyAndReturnNextOrderOrNull(bids);
		}
		while (ask != null) {
			ask = matchingState.notifyAndReturnNextOrderOrNull(asks);
		}
	}
	
	private PartyStateImpl getOrCreatePartyState(String party) {
		PartyStateImpl partyState = partyStateByParty.get(party);
		if (partyState == null) {
			partyState = new PartyStateImpl(party, RiskLimits.UNLIMITED);
			partyStateByParty.put(party, partyState);
		}
		return partyState;
	}

	private class PartyStateImpl implements PartyState {
		private final String party;
		private final HighWaterMarkPositionKeeper positionKeeper;
		private final AtomicLong dealCount = new AtomicLong();

		public PartyStateImpl(String party, RiskLimits riskLimits) {
			this.party = Objects.requireNonNull(party, "party is null");
			this.positionKeeper = new HighWaterMarkPositionKeeper(riskLimits);
		}
		
		@Override
		public String getParty() {
			return party;
		}

		@Override
		public RiskLimits getRiskLimits() {
			return positionKeeper.getRiskLimits();
		}

		@Override
		public AssetPositions getAssetPositions() {
			return new AssetPositionsImpl(positionKeeper);
		}
		
		@Override
		public double getHighWaterMark(Asset asset) {
			return positionKeeper.getHighWaterMark(asset);
		}
		
		@Override
		public double getLowWaterMark(Asset asset) {
			return positionKeeper.getLowWaterMark(asset);
		}

		@Override
		public long getDealCount() {
			return dealCount.get();
		}

		public void registerDeal(Deal deal, Side side) {
			positionKeeper.updatePosition(deal, side);
			dealCount.incrementAndGet();
		}
	}

	private class MatchingStateImpl implements MatchingState {
		private final AtomicLong index = new AtomicLong(-1);
		private final AtomicReference<Instant> startTime = new AtomicReference<Instant>(null);
		private final AtomicReference<Instant> endTime = new AtomicReference<Instant>(null);
		private final NavigableMap<Order, OrderFlow> nextOrderToOrderFlow = initOrderToOrderFlowMap();
		private final LastMarketRates lastMarketRates = new LastMarketRates();
		
		public MatchingStateImpl(Instant initialTime) {
			notifyAllMarketObservers(initialTime);
		}

		@Override
		public Instant getStartTime() {
			return startTime.get();
		}
		
		private NavigableMap<Order, OrderFlow> initOrderToOrderFlowMap() {
			final NavigableMap<Order, OrderFlow> nextOrderToOrderFlow = new TreeMap<>(OrderFlow.COMPARATOR);
			for (final OrderFlow orderFlow : orderFlows) {
				if (orderFlow.tryAdvance(o -> {
					nextOrderToOrderFlow.put(o, orderFlow);
				}));
			}
			return nextOrderToOrderFlow;
		}

		@Override
		public Instant getEndTime() {
			return endTime.get();
		}

		@Override
		public MarketSnapshot getMarketSnapshot() {
			return lastMarketRates.getMarketSnapshot();
		}

		@Override
		public Set<String> getParties() {
			return Collections.unmodifiableSet(partyStateByParty.keySet());
		}
		@Override
		public PartyState getPartyState(String party) {
			return partyStateByParty.get(party);
		}

		@Override
		public long getMatchIndex() {
			return index.get();
		}

		@Override
		public boolean hasNext() {
			return !nextOrderToOrderFlow.isEmpty();
		}

		@Override
		public MatchingState matchNext() {
			if (nextOrderToOrderFlow.isEmpty()) {
				throw new NoSuchElementException("no next match");
			}
			final List<Order> orders = matchNextOrders();
			this.startTime.set(orders.get(0).getTime());
			this.endTime.set(orders.get(orders.size() - 1).getTime());
			
			//group by asset pair and match each group
			final Set<AssetPair<?, ?>> assetPairs = orders.stream().map(o -> o.getAssetPair()).collect(Collectors.toSet());
			for (final AssetPair<?, ?> assetPair : assetPairs) {
				final List<Order> assetOrders = orders.stream().filter(o -> assetPair.equals(o.getAssetPair())).collect(Collectors.toList());
				match(this, assetPair, assetOrders);
			}
			this.index.incrementAndGet();
			return this;
		}

		private List<Order> matchNextOrders() {
			final List<Order> orders = new ArrayList<>();
			final Set<OrderFlow> flows = new HashSet<>();
			while (!nextOrderToOrderFlow.isEmpty()) { 
				final Order nextOrder = nextOrderToOrderFlow.firstKey();
				final OrderFlow orderFlow = nextOrderToOrderFlow.get(nextOrder);
				if (flows.contains(orderFlow)) {
					return orders;
				}
				flows.add(orderFlow);
				nextOrderToOrderFlow.remove(nextOrder);
				Order pre = null;
				final AtomicReference<Order> curRef = new AtomicReference<Order>(nextOrder);
				do {
					final Order cur = curRef.get();
					if (pre == null || pre.getTime().equals(cur.getTime())) {
						orders.add(cur);
						pre = cur;
					} else {
						nextOrderToOrderFlow.put(cur, orderFlow);
						break;
					}
				} while (orderFlow.tryAdvance(o -> {curRef.set(o);}));
			}
			return orders;
		}

		public void notifyAllMarketObservers(Instant initialTime) {
			lastMarketRates.onTime(initialTime);
			for (final MarketObserver observer : marketObservers) {
				observer.onTime(initialTime);
			}
		}

		public void notifyAllMarketObservers(Order order) {
			lastMarketRates.onOrder(order);
			for (final MarketObserver observer : marketObservers) {
				observer.onOrder(order);
			}
		}

		public void notifyAllMarketObservers(Deal deal) {
			lastMarketRates.onDeal(deal);
			for (final MarketObserver observer : marketObservers) {
				observer.onDeal(deal);
			}
		}

		private Order notifyAndReturnNextOrderOrNull(Iterator<Order> orders) {
			if (orders.hasNext()) {
				final Order order = orders.next();
				notifyAllMarketObservers(order);
				return order;
			}
			return null;
		}
		private Order getRemainingOrderOrNext(Deal deal, Order order, Iterator<Order> orders) {
			if (deal.getQuantity() < order.getQuantity()) {
				return new OrderImpl(order, deal.getTime(), order.getQuantity() - deal.getQuantity());
			}
			return notifyAndReturnNextOrderOrNull(orders);
		}

	}
	
	private static class BuilderImpl implements Builder {
		private Instant initialTime = null;
		private final List<OrderFlow> orderFlows = new ArrayList<>();
		private final Map<String, RiskLimits> riskLimitsByParty = new LinkedHashMap<>();
		private final List<MarketObserver> marketObservers = new ArrayList<>();
		
		@Override
		public Builder setInitialTime(Instant time) {
			this.initialTime = time;
			return this;
		}
		@Override
		public Builder addOrderFlow(OrderFlow orderFlow) {
			Objects.requireNonNull(orderFlow, "orderFlow is null");
			orderFlows.add(orderFlow);
			return this;
		}
		@Override
		public Builder addMarketMaker(MarketMaker marketMaker) {
			Objects.requireNonNull(marketMaker, "marketMaker is null");
			orderFlows.add(marketMaker);
			marketObservers.add(marketMaker);
			return this;
		}
		@Override
		public Builder setRiskLimits(String party, RiskLimits riskLimits) {
			Objects.requireNonNull(party, "party is null");
			Objects.requireNonNull(riskLimits, "riskLimits is null");
			riskLimitsByParty.put(party, riskLimits);
			return this;
		}
		@Override
		public Builder addMarketObserver(MarketObserver marketObserver) {
			Objects.requireNonNull(marketObserver, "marketObserver is null");
			marketObservers.add(marketObserver);
			return this;
		}
		
		@Override
		public MatchingEngine build() {
			return new MatchingEngineImpl(initialTime == null ? Instant.MIN : initialTime, orderFlows, riskLimitsByParty, marketObservers);
		}
		
	}

}
