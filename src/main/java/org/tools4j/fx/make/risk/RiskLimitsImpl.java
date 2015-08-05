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
package org.tools4j.fx.make.risk;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.tools4j.fx.make.asset.Asset;

/**
 * Immutable implementation of {@link RiskLimits}.
 */
public class RiskLimitsImpl implements RiskLimits {

	private final Map<Asset, Long> maxPositionSizeByAsset;

	public RiskLimitsImpl(Map<? extends Asset, Long> maxPositionSizeByAsset) {
		this.maxPositionSizeByAsset = new HashMap<>(maxPositionSizeByAsset);
	}
	public long getMaxAllowedPositionSize(Asset asset) {
		final Long masPositionSize = maxPositionSizeByAsset.get(asset);
		return masPositionSize == null ? -1 : masPositionSize.longValue();
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "[maxPositionSizeByAsset=" + maxPositionSizeByAsset + "}"; 
	}
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder implements RiskLimits.Builder {
		
		private final Map<Asset, Long> maxPositionSizeByAsset = new HashMap<>();

		@Override
		public Builder withMaxAllowedPositionSize(Asset asset, long maxPositionSize) {
			Objects.requireNonNull(asset, "asset is null");
			if (maxPositionSize < -1) {
				throw new IllegalArgumentException("max position size is < -1: " + maxPositionSize);
			}
			if (maxPositionSize == -1) {
				maxPositionSizeByAsset.remove(asset);
			} else {
				maxPositionSizeByAsset.put(asset, maxPositionSize);
			}
			return this;
		}

		@Override
		public RiskLimits build() {
			return new RiskLimitsImpl(maxPositionSizeByAsset);
		}
		
		@Override
		public String toString() {
			return "Builder@" + build();
		}
	}
}
