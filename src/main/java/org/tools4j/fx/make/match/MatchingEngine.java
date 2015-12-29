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

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.tools4j.fx.make.asset.Asset;
import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.flow.OrderFlow;
import org.tools4j.fx.make.market.MidMarketRates;
import org.tools4j.fx.make.market.MarketMaker;
import org.tools4j.fx.make.market.MarketObserver;
import org.tools4j.fx.make.position.AssetPositions;
import org.tools4j.fx.make.position.MarketSnapshot;
import org.tools4j.fx.make.risk.RiskLimits;

/**
 * A matching engine fetches orders from {@link OrderFlow} instances and matches
 * those orders if possible. The matching engine maintains a set of
 * {@link AssetPositions} for every party involved in a matching {@link Deal}.
 * To control the asset position size of a party, {@link RiskLimits} can be
 * registered via {@link Builder#setRiskLimits(String, RiskLimits)}
 * <p>
 * After creating an engine via {@link Builder} (see
 * {@link MatchingEngineImpl#builder()}), matching can be performed
 * <ul>
 * <li><b>step by step:</b> via {@link #matchFirst()} and then
 * {@link MatchingState#matchNext()} invoked on the returned matching state
 * object</li>
 * <li><b>step by step with callback:</b> via {@link #forEach(Consumer)} where
 * each step {@link MatchingState} can be consumed by the consumer argument</li>
 * <li><b>all at once:</b> via {@link #matchAll()} which returns a final
 * {@link MatchingState} object for inspection of positions etc.</li>
 * </ul>
 */
public interface MatchingEngine {

	/**
	 * Returns the first initial {@link MatchingState} after performing all
	 * matches occurring at the first time step.
	 * 
	 * @return a matching state object to {@link MatchingState#matchNext()
	 *         invoke} the next matching step and to inspect the situation after
	 *         the first matches
	 */
	MatchingState matchFirst();

	/**
	 * Performs all matching steps until no order flow returns any more orders.
	 * The final matching state object is returned.
	 * 
	 * @return a matching state object to inspect the situation after performing
	 *         matching for all time steps
	 */
	default MatchingState matchAll() {
		MatchingState state = matchFirst();
		while (state.hasNext()) {
			state = state.matchNext();
		}
		return state;
	}

	/**
	 * Performs all matching steps until no order flow returns any more orders.
	 * The {@link MatchingState} after each time step is passed to the specified
	 * {@code consumer} argument for inspection.
	 * 
	 * @param consumer
	 *            consumer called with every matching state that occurs after a
	 *            time step
	 */
	default void forEach(Consumer<? super MatchingState> consumer) {
		Objects.requireNonNull(consumer);
		MatchingState state = matchFirst();
		while (state.hasNext()) {
			consumer.accept(state);
			state = state.matchNext();
		}
		consumer.accept(state);
	}

	/**
	 * A builder to construct a {@link MatchingEngine}, returned by
	 * {@link MatchingEngineImpl#builder()}.
	 */
	interface Builder {
		Builder addOrderFlow(OrderFlow orderFlow);

		Builder addMarketMaker(MarketMaker marketMaker);

		Builder setRiskLimits(String party, RiskLimits riskLimits);

		Builder addMarketObserver(MarketObserver marketObserver);

		MatchingEngine build();
	}

	/**
	 * Represents the matching state with information about market rates and
	 * positions after performing one or several rounds of order matching. A
	 * matching round is defined by all orders returned by all order flows in a
	 * single call to {@link OrderFlow#nextOrders()}.
	 */
	interface MatchingState {
		/**
		 * Returns a market snapshot of prices as they are captured from deals
		 * that occurred in the market.
		 * 
		 * @return the current market snapshot
		 * @see MidMarketRates
		 */
		MarketSnapshot getMarketSnapshot();

		/**
		 * Returns all parties which have been involved in a {@link Deal} and
		 * hence usually have a position, plus those parties for which
		 * {@link RiskLimits} have been defined.
		 * 
		 * @return all parties with deals or with risk limits defined
		 */
		Set<String> getParties();

		/**
		 * Returns the {@link PartyState} with information about this party,
		 * such as position and risk limits.
		 * 
		 * @param party
		 *            the party
		 * @return the state for this party, or null if this party was not
		 *         involved in deals and has no risk limits defined
		 */
		PartyState getPartyState(String party);

		/**
		 * Returns the time-step index of the current match, zero after the
		 * first round of matching.
		 * 
		 * @return the zero-based time step matching index
		 */
		long getMatchIndex();

		/**
		 * Returns true if {@link #matchNext()} is a legal operation, and false
		 * otherwise. Returns true if any of the order flow sources returned an
		 * order during the previous matching step, and false if no orders were
		 * found.
		 * 
		 * @return true if a {@link #matchNext() next} match is supported
		 */
		boolean hasNext();

		/**
		 * Performs the next matches and returns the state after performing
		 * those matches.
		 * 
		 * @return the matching state after performing the matches with orders
		 *         occurring at the next time step
		 */
		MatchingState matchNext();
	}

	/**
	 * The state of a party involved in matches or with a defined risk limit.
	 */
	interface PartyState {
		/**
		 * The party to which this state belongs.
		 * 
		 * @return the party
		 */
		String getParty();

		/**
		 * Returns the risk limits that have been defined for this party,
		 * {@link RiskLimits#UNLIMITED UNLIMITED} if not explicitly constrained
		 * when constructing the matching engine.
		 * 
		 * @return the risk limits for this party, never null
		 */
		RiskLimits getRiskLimits();

		/**
		 * Returns the asset positions for this party, never null.
		 * 
		 * @return this party's asset positions, never null
		 */
		AssetPositions getAssetPositions();

		/**
		 * Returns the highest position that was ever reached for the specified
		 * asset.
		 * 
		 * @param asset
		 *            the asset whose high watermark to return
		 * @return the highest position seen for this asset, never negative
		 */
		double getHighWaterMark(Asset asset);

		/**
		 * Returns the lowest position that was ever reached for the specified
		 * asset.
		 * 
		 * @param asset
		 *            the asset whose low watermark to return
		 * @return the lowest position seen for this asset, never positive
		 */
		double getLowWaterMark(Asset asset);

		/**
		 * Returns the number of deals that this party was involved in
		 * 
		 * @return the number of deals where this party was taking part
		 */
		long getDealCount();
	}
}
