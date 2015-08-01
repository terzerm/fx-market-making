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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.tools4j.fx.make.api.Asset;
import org.tools4j.fx.make.api.AssetPair;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.Settings;
import org.tools4j.fx.make.api.Side;

/**
 * Keeps track positions for several symbols.
 * <p>
 * The class is NOT thread safe.
 */
public class PositionKeeper {
	
	private final Settings settings;
	private final Map<Asset, AtomicLong> positionByAsset = new HashMap<>();
	
	public PositionKeeper(Settings settings) {
		this.settings = Objects.requireNonNull(settings, "settings is null");
	}

	public long fillWithoutExceedingMax(Order order, boolean allowPartial) {
		final AssetPair<?, ?> assetPair = order.getAssetPair();
		final long orderQty = order.getQuantity();
		final long basePos = getPosition(assetPair.getBase());
		final long termsPos = getPosition(assetPair.getTerms());
		//opposite side for base because we fill the order, i.e. we take the opposite position
		final long baseQty = getSignedQuantity(orderQty, order.getSide().opposite());
		final long termsQty = getSignedQuantity(orderQty, order.getSide());
		final long baseCutOff = Math.max(0, Math.abs(basePos + baseQty) - settings.getMaxAllowedPositionSize(assetPair.getBase()));
		final long termsCutOff = Math.max(0, Math.abs(termsPos + termsQty) - settings.getMaxAllowedPositionSize(assetPair.getTerms()));
		final long cutOff = Math.max(baseCutOff, termsCutOff);
		final long baseChange = baseQty - Long.signum(baseQty) * cutOff;
		final long termsChange = termsQty - Long.signum(termsQty) * cutOff;
		if (baseChange != 0 & (allowPartial | orderQty == Math.abs(baseChange))) {
			getOrCreatePosition(assetPair.getBase()).addAndGet(baseChange);
			getOrCreatePosition(assetPair.getTerms()).addAndGet(termsChange);
			return Math.abs(baseChange);
		}
		// no or not enough quantity left
		return 0;
	}
	
	private static long getSignedQuantity(long quantity, Side side) {
		return side == Side.BUY ? quantity : -quantity;
	}
	
	private AtomicLong getOrCreatePosition(Asset asset) {
		AtomicLong position = positionByAsset.get(asset);
		if (position == null) {
			position = new AtomicLong(0);
			positionByAsset.put(asset, position);
		}
		return position;
	}

	public long getPosition(Asset asset) {
		final AtomicLong position = positionByAsset.get(asset);
		return position == null ? 0 : position.longValue();
	}

	public void resetPosition(Asset asset) {
		positionByAsset.remove(asset);
	}
	
	public void resetPositions() {
		positionByAsset.clear();
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + positionByAsset.toString();
	}
}
