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
package org.tools4j.fx.make.flow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.tools4j.fx.make.asset.AssetPair;
import org.tools4j.fx.make.execution.Order;
import org.tools4j.fx.make.execution.OrderImpl;
import org.tools4j.fx.make.execution.Side;

/**
 * Order flow based on a CSV file with tick data in the following format:
 * <pre>
Time,Ask,Bid,AskVolume,BidVolume
2015-07-01 11:47:19.707,1.11022,1.11018,2.25,4.12
2015-07-01 11:47:20.214,1.11022,1.11018,2.25,3.82
2015-07-01 11:47:20.750,1.11021,1.11018,1.5,2.32
2015-07-01 11:47:22.209,1.11021,1.11019,1.5,1
 * </pre>
 */
public class CsvOrderFlow implements OrderFlow {
	
	public static final int CHARACTERISTICS = REQUIRED_CHARACTERISTICS | IMMUTABLE;
	
	private final AssetPair<?, ?> assetPair;
	private final String party;
	private final BufferedReader reader;
	private final AtomicLong lineNo = new AtomicLong(0);
	private final AtomicReference<Order> ask = new AtomicReference<Order>(null);
	
	public CsvOrderFlow(AssetPair<?, ?> assetPair, File file) throws FileNotFoundException {
		this(assetPair, file.getName(), new FileReader(file));
	}
	public CsvOrderFlow(AssetPair<?, ?> assetPair, String party, File file) throws FileNotFoundException {
		this(assetPair, party, new FileReader(file));
	}
	private CsvOrderFlow(AssetPair<?, ?> assetPair, String party, Reader reader) {
		this.assetPair = Objects.requireNonNull(assetPair, "assetPair is null");
		this.party = Objects.requireNonNull(party, "party is null");
		this.reader = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
	}
	
	public static Instant getInitialTime(File file) throws FileNotFoundException, IOException {
		return getInitialTime(new FileReader(file));
	}
	
	public static Instant getInitialTime(Reader reader) throws IOException {
		final BufferedReader bufferedReader = reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
		try {
			bufferedReader.readLine();//header line
			final String line = bufferedReader.readLine();
			return parseTime(line);
		} finally {
			bufferedReader.close();
		}
	}
	@Override
	public int characteristics() {
		return CHARACTERISTICS;
	}
	
	@Override
	public long estimateSize() {
		return Long.MAX_VALUE;
	}
	
	@Override
	public Spliterator<Order> trySplit() {
		return null;
	}
	
	@Override
	public boolean tryAdvance(Consumer<? super Order> action) {
		try {
			//ask from previous line
			final Order ask = this.ask.getAndSet(null);
			if (ask != null) {
				action.accept(ask);
				return true;
			}
			//read new line (skip header line if first line)
			String line = readLine();
			if (line != null & lineNo.get() == 1) {
				line = readLine();
			}
			//usually bid, but could be ask if bid qty is 0
			final Order order = parseLine(line);
			if (order != null) {
				action.accept(order);
				return true;
			}
			return false;
		} catch (Exception e) {
			throw new RuntimeException("[line=" + lineNo + "] error reading " + assetPair + " csv file for party '" + party + "', e=" + e , e);
		}
	}
	private String readLine() throws IOException {
		final String line = reader.readLine();
		if (line != null) {
			lineNo.incrementAndGet();
		}
		return line;
	}
	private Order parseLine(String line) throws IOException {
		while (line != null) {
			final Instant time = parseTime(line);
			final double bid = parseRate(line, 2);
			final double ask = parseRate(line, 1);
			final long bidVol = parseVol(line, 4);
			final long askVol = parseVol(line, 3);
			if (bidVol > 0 & askVol > 0) {
				this.ask.set(createOrder(Side.SELL, time, ask, askVol));
				return createOrder(Side.BUY, time, bid, bidVol);
			}
			if (bidVol > 0) {
				return createOrder(Side.BUY, time, bid, bidVol);
			}
			if (askVol > 0) {
				return createOrder(Side.SELL, time, ask, askVol);
			}
			line = readLine();
		}
		return null;
	}
	private static Instant parseTime(String line) {
		//2015-07-01 11:47:19.707
		final int yyyy = Integer.parseInt(line.substring(0, 4));
		final int mM = Integer.parseInt(line.substring(5, 7));
		final int dd = Integer.parseInt(line.substring(8, 10));
		final int hh = Integer.parseInt(line.substring(11, 13));
		final int mm = Integer.parseInt(line.substring(14, 16));
		final int ss = Integer.parseInt(line.substring(17, 19));
		final int ms = Integer.parseInt(line.substring(20, 23));
		final int ns = (int)TimeUnit.MILLISECONDS.toNanos(ms);
		return LocalDateTime.of(yyyy, mM, dd, hh, mm, ss, ns).toInstant(ZoneOffset.UTC);
	}
	private static double parseRate(String line, int commasBefore) {
		return parseDouble(line, commasBefore);
	}
	private long parseVol(String line, int commasBefore) {
		final double vol = parseDouble(line, commasBefore);
		return Math.round(vol * 1000000.0);
	}
	private static double parseDouble(String line, int commasBefore) {
		int start = 0;
		int index = line.indexOf(',');
		for (int i = 0; i < commasBefore; i++) {
			start = index + 1;
			index = line.indexOf(',', start);
		}
		return Double.parseDouble(index >= 0 ? line.substring(start, index) : line.substring(start));
	}
	private Order createOrder(Side side, Instant time, double rate, long quantity) {
		return new OrderImpl(time, assetPair, party, side, rate, quantity);
	}

}
