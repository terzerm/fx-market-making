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
package org.tools4j.fx.make.match;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.tools4j.fx.make.asset.Currency;
import org.tools4j.fx.make.asset.CurrencyPair;
import org.tools4j.fx.make.flow.CsvOrderFlow;
import org.tools4j.fx.make.flow.OrderFlow;
import org.tools4j.fx.make.market.MarketMaker;
import org.tools4j.fx.make.market.MarketPrinter;
import org.tools4j.fx.make.market.MarketPrinter.Mode;
import org.tools4j.fx.make.market.PosTrendingMarketMaker;
import org.tools4j.fx.make.match.MatchingEngine.PartyState;
import org.tools4j.fx.make.position.AssetPositions;
import org.tools4j.fx.make.position.MarketSnapshot;
import org.tools4j.fx.make.position.PositionKeeperImpl;
import org.tools4j.fx.make.risk.RiskLimits;
import org.tools4j.fx.make.util.StringUtil;

/**
 * Unit test for {@link MatchingEngine} and {@link MatchingEngineImpl}.
 */
@Ignore//run manually
@RunWith(Parameterized.class)
public class MatchingEngineCsvTest {
	
	private static final File FOLDER = new File("/Users/terz/Documents/invest/fx");
	private static final String FILE_NAME = "%s_UTC_Ticks_Bid_%s.csv";
	
	private static final Map<CurrencyPair, List<Double>> PNL_USD = new LinkedHashMap<>();
	
	private static final CurrencyPair[] PAIRS = {//
			new CurrencyPair(Currency.EUR, Currency.USD),//
			new CurrencyPair(Currency.AUD, Currency.USD),//
			new CurrencyPair(Currency.GBP, Currency.USD),//
			new CurrencyPair(Currency.USD, Currency.JPY),//
			new CurrencyPair(Currency.USD, Currency.CAD),//
			new CurrencyPair(Currency.USD, Currency.CHF)//
	};
	private static final String[] DATES = {//
//			"2015.01.01_2015.01.31",//
//			"2015.02.01_2015.02.28",//
//			"2015.03.01_2015.03.31",//
//			"2015.04.01_2015.04.30",//
//			"2015.05.01_2015.05.31",//
//			"2015.06.01_2015.06.30",//
//			"2015.07.01_2015.07.31",//
			"2014.01.01_2014.12.31",//
//			"2013.01.01_2013.12.31",//
//			"2012.01.01_2012.12.31",//
//			"2011.01.01_2011.12.31",//
//			"2010.01.01_2010.12.31",//
	};
	
	private final MarketPrinter printer = new MarketPrinter();

	@Rule
	public final TestName testName = new TestName();
	
	private final CurrencyPair currencyPair;
	private final String date;
	private final int month;
	
	private String partyName;
	
	public MatchingEngineCsvTest(CurrencyPair currencyPair, String date, int month) {
		this.currencyPair = Objects.requireNonNull(currencyPair, "currencyPair is null");
		this.date = Objects.requireNonNull(date, "date is null");
		this.month = month;
	}
	
	@Parameters(name = "{0} {1}/{2}")
	public static List<Object[]> getCurrencyPairs() {
		final List<Object[]> list = new ArrayList<>();
		for (final CurrencyPair ccyPair : PAIRS) {
			for (final String date : DATES) {
				//2013.01.01_2013.12.31
				if (date.contains(".01.01_") && date.contains(".12.31")) {
					for (int month = 1; month < 12; month++) {
						list.add(new Object[] {ccyPair, date, month});
					}
				} else {
					list.add(new Object[] {ccyPair, date, 0});
				}
			}
		}
		return list;
	}


	@Before
	public void beforeEach() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> " + testName.getMethodName());
		printer.setModes(Mode.ORDERS, Mode.DEALS);
	}
	
	@AfterClass
	public static void printSummary() {
		//header
		System.out.println("===============================================================");
		System.out.print("       ");
		for (int i = 0; i < DATES.length; i++) {
			System.out.print("\t     " + (i + 2));
		}
		System.out.println("\ttotal");
		//data
		for (final Map.Entry<CurrencyPair, List<Double>> e : PNL_USD.entrySet()) {
			System.out.print(e.getKey());
			double sum = 0;
			for (final Double val : e.getValue()) {
				System.out.print("\t" + StringUtil.formatQuantity(1000 * (long)(val / 1000d)));
				sum +=  val;
			}
			System.out.print("\t" + StringUtil.formatQuantity(1000 * (long)(sum / 1000d)));
			System.out.println();
		}
	}

	@Test
	public void shouldMatch() throws FileNotFoundException {
		// given
		final File file = new File(FOLDER, String.format(FILE_NAME, currencyPair.toSixCharString(), date));
		final Currency base = currencyPair.getBase();
		final Currency terms = currencyPair.getTerms();
		printer.setModes(Mode.DEALS);
		final OrderFlow orderFlow = (month == 0 ? 
				CsvOrderFlow.builder(currencyPair, file) : 
				CsvOrderFlow.builder(currencyPair, file).forMonth(month).withParty(file.getName() + "[month=" + month + "]")
			).build();
//		final double spread = 10 * (terms == Currency.JPY ? 0.01 : 0.0001);
		final double spread = 12 * (terms == Currency.JPY ? 0.01 : 0.0001);
//		final MarketMaker marketMaker = new MidMarketMaker(new PositionKeeperImpl(RiskLimits.UNLIMITED), currencyPair, spread, 1000000);
//		final MarketMaker marketMaker = new SkewedMarketMaker(new PositionKeeperImpl(RiskLimits.UNLIMITED), currencyPair, spread, 1000000);
//		final MarketMaker marketMaker = new TrendingMarketMaker(new PositionKeeperImpl(RiskLimits.UNLIMITED), currencyPair, spread, 1000000);
		final MarketMaker marketMaker = new PosTrendingMarketMaker(new PositionKeeperImpl(RiskLimits.UNLIMITED), currencyPair, spread, 1000000);
		partyName = marketMaker.getClass().getSimpleName();
		final MatchingEngine engine = MatchingEngineImpl.builder()//
				.addOrderFlow(orderFlow)//
				.addMarketMaker(marketMaker)
//				.addMarketObserver(printer)//
				.build();

		// when
		final MatchingEngine.MatchingState state = engine.matchAll();

		// then
		Assert.assertEquals("unexpected party size", 2, state.getParties().size());
		
		final MarketSnapshot marketSnapshot = state.getMarketSnapshot(); 
		System.out.println(marketSnapshot);
		System.out.println("match rounds=" + state.getMatchIndex());
		for (final String party : sort(state.getParties())) {
			System.out.println("==== " + party + " ====");
			final PartyState partyState = state.getPartyState(party);
			final AssetPositions pos = partyState.getAssetPositions();
			System.out.println("deals: " + partyState.getDealCount());
			System.out.println(pos);
			System.out.println("high/low[" + base + "]:" + partyState.getHighWaterMark(base) + ".." + partyState.getLowWaterMark(base));
			System.out.println("high/low[" + terms + "]:" + partyState.getHighWaterMark(terms) + ".." + partyState.getLowWaterMark(terms));
			final double valBase = pos.getValuator(base).getValuation(marketSnapshot);
			final double valTerms = pos.getValuator(terms).getValuation(marketSnapshot);
			System.out.println("value[" + base + "]=" + valBase + "\t= " + (valBase / partyState.getDealCount()) + " per deal");
			System.out.println("value[" + terms + "]=" + valTerms + "\t= " + (valTerms / partyState.getDealCount()) + " per deal");
		}
		//record pnl
		List<Double> pnls = PNL_USD.get(currencyPair);
		if (pnls == null) {
			pnls = new ArrayList<>();
			PNL_USD.put(currencyPair, pnls);
		}
		pnls.add(state.getPartyState(partyName).getAssetPositions().getValuator(Currency.USD).getValuation(marketSnapshot));
	}
	
	private static final Collection<String> sort(final Collection<? extends String> parties) {
		final String[] arr = parties.toArray(new String[parties.size()]);
		Arrays.sort(arr, PARTY_COMPARATOR);
		return Arrays.asList(arr);
	}
	private static final Comparator<String> PARTY_COMPARATOR = new Comparator<String>() {
		public int compare(String o1, String o2) {
			final boolean mm1 = o1.endsWith("MarketMaker");
			final boolean mm2 = o2.endsWith("MarketMaker");
			if (mm1 != mm2) return mm1 ? 1 : -1;
			return o1.compareTo(o2);
		}
	};
}
