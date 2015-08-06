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

import java.util.Set;

import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.flow.OrderFlow;

/**
 * A market maker is an {@link OrderFlow} as he provides a stream of making
 * orders (aka BID and OFFER prices); he is also a {@link MarketObserver} as he
 * usually bases the market making activity on observed orders and deals.
 */
public interface MarketMaker extends OrderFlow, MarketObserver {
	/**
	 * Returns a set with asset-pairs this maker is willing to make a market
	 * for.
	 * 
	 * @return the set of active asset-pairs for this maker
	 */
	Set<? extends AssetPair<?, ?>> getAssetPairs();
}
