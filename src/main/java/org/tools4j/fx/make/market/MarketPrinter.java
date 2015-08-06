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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.tools4j.fx.make.execution.Deal;
import org.tools4j.fx.make.execution.Order;

/**
 * Prints all actions occuring in the market to I/O.
 */
public class MarketPrinter implements MarketObserver {
	
	private final PrintStream printStream;
	private final AtomicReference<Mode> mode = new AtomicReference<>(Mode.ORDERS_AND_DEALS);
	
	public enum Mode {
		ORDERS,//
		DEALS,//
		ORDERS_AND_DEALS;
	}
	
	public MarketPrinter() {
		this(System.out);
	}
	public MarketPrinter(File file) throws FileNotFoundException {
		this(new FileOutputStream(file));
	}
	public MarketPrinter(OutputStream out) {
		this(new PrintStream(out));
	}
	public MarketPrinter(PrintStream printStream) {
		this.printStream = Objects.requireNonNull(printStream, "printStream is null");
	}
	
	public void setMode(Mode mode) {
		this.mode.set(Objects.requireNonNull(mode, "mode is null"));
	}
	
	@Override
	public void onOrder(Order order) {
		if (mode.get() != Mode.DEALS) {
			printStream.println("ORDER:\t" + order.toShortString() + "\t(" + order.getParty() + ")");
		}
	}

	@Override
	public void onDeal(Deal deal) {
		if (mode.get() != Mode.ORDERS) {
			printStream.println("DEAL:\t" + deal.toShortString() + "\t(" + deal.getBuyParty() + " <--> " + deal.getSellParty() + ")");
		}
	}

}
