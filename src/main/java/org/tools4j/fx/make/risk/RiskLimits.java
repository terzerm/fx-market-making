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

import org.tools4j.fx.make.asset.Asset;

/**
 * Risk limits for positions such as maximum allowed position size.
 */
public interface RiskLimits {
	/**
	 * The maximum positions size for the specified asset, never negative
	 * 
	 * @param asset
	 *            the asset of interest
	 * @return the absolute max position size for the given asset, not negative
	 */
	long getMaxAllowedPositionSize(Asset asset);
	
	RiskLimits UNLIMITED = (a -> Long.MAX_VALUE);

	/**
	 * Builder for {@link RiskLimits} which are immutable;
	 */
	interface Builder {
		/**
		 * The maximum positions size for the specified asset, never negative
		 * 
		 * @param asset
		 *            the asset of interest
		 * @param maxPositionSize
		 *            the absolute max position size for the given asset, not
		 *            negative
		 * @throws IllegalArgumentException
		 *             if size is negative
		 * @throws NullPointerException
		 *             if asset is null
		 * @return this builder for chained method invocation
		 */
		Builder withMaxAllowedPositionSize(Asset asset, long maxPositionSize);

		/**
		 * Returns a new immutable settings instance.
		 * 
		 * @return a new immutable settings instance.
		 */
		RiskLimits build();
	}
}
