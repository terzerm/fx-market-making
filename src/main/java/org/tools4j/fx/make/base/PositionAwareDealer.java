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

import java.util.Objects;

import org.tools4j.fx.make.api.Asset;
import org.tools4j.fx.make.api.Dealer;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.Settings;

/**
 * A {@link Dealer} accepting all orders as long as the position is within the
 * allowed max size.
 * <p>
 * The class is NOT thread safe.
 */
public class PositionAwareDealer implements Dealer {
	
	private final PositionKeeper positionKeeper;
	private final boolean allowPartial;

	public PositionAwareDealer(Settings settings, boolean allowPartial) {
		this(new PositionKeeper(settings), allowPartial);
	}
	public PositionAwareDealer(PositionKeeper positionKeeper, boolean allowPartial) {
		this.positionKeeper= Objects.requireNonNull(positionKeeper, "positionKeeper is null");
		this.allowPartial = allowPartial;
	}

	@Override
	public long acceptOrReject(Order order) {
		return positionKeeper.fillWithoutExceedingMax(order, allowPartial);
	}
	
	public long getPosition(Asset asset) {
		return positionKeeper.getPosition(asset);
	}

	public void resetPosition(Asset asset) {
		positionKeeper.resetPosition(asset);
	}

	public void resetPositions() {
		positionKeeper.resetPositions();
	}
}
