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

import org.tools4j.fx.make.api.Deal;
import org.tools4j.fx.make.api.Order;
import org.tools4j.fx.make.api.Side;
import org.tools4j.fx.make.util.StringUtil;

public class DealImpl implements Deal {
	
	private final long id = ID_GENERATOR.incrementAndGet();
	private final String symbol;
	private final double price;
	private final long quantity;
	private final long buyOrderId;
	private final String buyParty;
	private final long sellOrderId;
	private final String sellParty;

	public DealImpl(String symbol, double price, long quantity, long buyOrderId, String buyParty, long sellOrderId, String sellParty) {
		this.symbol = Objects.requireNonNull(symbol, "symbol is null");
		this.price = price;
		this.quantity = quantity;
		this.buyOrderId = buyOrderId;
		this.buyParty = Objects.requireNonNull(buyParty, "buyParty is null");
		this.sellOrderId = sellOrderId;
		this.sellParty = Objects.requireNonNull(sellParty, "sellParty is null");
	}
	
	public DealImpl(double price, long quantity, Order order1, Order order2) {
		final Order buyOrder = order1.getSide() == Side.BUY ? order1 : order2.getSide() == Side.BUY ? order2 : null;
		final Order sellOrder = order1.getSide() == Side.SELL ? order1 : order2.getSide() == Side.SELL ? order2 : null;
		if (buyOrder == null | sellOrder == null) {
			throw new IllegalArgumentException("must be one BUY and one SELL order, but was: " + order1 + ", " + order2);
		}
		if (!buyOrder.getSymbol().equals(sellOrder.getSymbol())) {
			throw new IllegalArgumentException("orders must have same symbol: " + order1 + ", " + order2);
		}
		if (quantity <= 0) {
			throw new IllegalArgumentException("quantity must be positive: " + quantity);
		}
		if (quantity > order1.getQuantity() | quantity > order2.getQuantity()) {
			throw new IllegalArgumentException("quantity must be no more than order quantity: " + quantity + ", order1=" + order1 + ", order2=" + order2);
		}
		this.symbol = Objects.requireNonNull(order1.getSymbol(), "order1.symbol is null");
		this.price = price;
		this.quantity = quantity;
		this.buyOrderId = buyOrder.getId();
		this.buyParty = Objects.requireNonNull(buyOrder.getParty(), "buyOrder.party is null");
		this.sellOrderId = sellOrder.getId();
		this.sellParty = Objects.requireNonNull(sellOrder.getParty(), "sellOrder.party is null");
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getSymbol() {
		return symbol;
	}

	@Override
	public double getPrice() {
		return price;
	}

	@Override
	public long getQuantity() {
		return quantity;
	}

	@Override
	public long getBuyOrderId() {
		return buyOrderId;
	}

	@Override
	public String getBuyParty() {
		return buyParty;
	}

	@Override
	public long getSellOrderId() {
		return sellOrderId;
	}

	@Override
	public String getSellParty() {
		return sellParty;
	}

	public String toShortString() {
		return StringUtil.formatQuantity(getQuantity()) + "@" + StringUtil.formatPrice(getPrice());
	}

	@Override
	public String toString() {
		return "DealImpl [id=" + id + ", symbol=" + symbol + ", price=" + price + ", quantity=" + quantity
				+ ", buyOrderId=" + buyOrderId + ", buyParty=" + buyParty + ", sellOrderId=" + sellOrderId
				+ ", sellParty=" + sellParty + "]";
	}

}
