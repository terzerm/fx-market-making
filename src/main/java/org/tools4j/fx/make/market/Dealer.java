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

import org.tools4j.fx.make.execution.Order;

/**
 * A market participant that is willing to accept and sometimes reject orders.
 */
public interface Dealer {

	/**
	 * The specified order is offered to the dealer who responds with a fill
	 * quantity or zero if he rejects the order. Returning the order's quantity
	 * means that the order is fully filled.
	 * 
	 * @param order
	 *            the order offered to the dealer
	 * @return a quantity between zero and the quantity of the given order
	 */
	long acceptOrReject(Order order);

	/**
	 * A dealer fully accepting all orders.
	 */
	Dealer ACCEPT_ALL = (order -> order.getQuantity());

	/**
	 * A dealer rejecting all orders.
	 */
	Dealer REJECT_ALL = (order -> 0);

	/**
	 * Returns a combined dealer accepting orders only if {@code this} dealer
	 * and {@code other} agree. If they don't agree on the match quantity then
	 * the minimum match quantity is returned.
	 * 
	 * @param other
	 *            the other dealer to agree with
	 * @return a new dealer acting as "agree between this and other dealer"
	 */
	default Dealer agreeWith(Dealer other) {
		return (order -> Math.min(acceptOrReject(order), other.acceptOrReject(order)));
	}

	/**
	 * Returns a combined dealer accepting orders only if {@code this} dealer
	 * and all {@code others} dealers agree. If they don't agree on the match
	 * quantity then the minimum match quantity is returned.
	 * 
	 * @param others
	 *            the other dealers to agree with
	 * @return a new dealer acting as "agree between this and other dealer"
	 */
	default Dealer agreeWith(Dealer... others) {
		return (order -> {
			long qty = this.acceptOrReject(order);
			for (int i = 0; qty > 0 & i < others.length; i++) {
				qty = Math.min(qty, others[i].acceptOrReject(order));
			}
			return qty;
		});
	}
}
