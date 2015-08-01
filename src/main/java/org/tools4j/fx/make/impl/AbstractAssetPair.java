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
package org.tools4j.fx.make.impl;

import java.util.Objects;

import org.tools4j.fx.make.api.Asset;
import org.tools4j.fx.make.api.AssetPair;

abstract public class AbstractAssetPair<B extends Asset, T extends Asset> implements AssetPair<B, T> {
	private final B base;
	private final T terms;

	public AbstractAssetPair(B base, T terms) {
		this.base = Objects.requireNonNull(base, "base is null");
		this.terms = Objects.requireNonNull(terms, "terms is null");
		if (base.equals(terms)) {
			throw new IllegalArgumentException("base equals terms: " + base + "/" + terms);
		}
	}

	public B getBase() {
		return base;
	}

	public T getTerms() {
		return terms;
	}

	@Override
	public String toString() {
		return getBase() + "/" + getTerms();
	}

	@Override
	public int hashCode() {
		return 31*base.hashCode() + terms.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AbstractAssetPair<?, ?> other = (AbstractAssetPair<?, ?>) obj;
		return base.equals(other.base) && terms.equals(other.terms);
	}

}
