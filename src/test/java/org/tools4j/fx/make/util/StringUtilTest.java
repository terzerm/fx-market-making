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
package org.tools4j.fx.make.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit test for {@link StringUtil}.
 */
public class StringUtilTest {
	@Test
	public void shouldFormatQuantity() {
		assertEquals("0M", StringUtil.formatQuantity(0));
		assertEquals("1M", StringUtil.formatQuantity(1000000));
		assertEquals("2M", StringUtil.formatQuantity(2000000));
		assertEquals("9M", StringUtil.formatQuantity(9000000));
		assertEquals("1.2M", StringUtil.formatQuantity(1200000));
		assertEquals("1.9M", StringUtil.formatQuantity(1900000));
		assertEquals("2.4M", StringUtil.formatQuantity(2400000));
		assertEquals("2.8M", StringUtil.formatQuantity(2800000));
		assertEquals("2.41M", StringUtil.formatQuantity(2410000));
		assertEquals("2.82M", StringUtil.formatQuantity(2820000));
		assertEquals("3.412M", StringUtil.formatQuantity(3412000));
		assertEquals("5.825M", StringUtil.formatQuantity(5825000));
	}
}
