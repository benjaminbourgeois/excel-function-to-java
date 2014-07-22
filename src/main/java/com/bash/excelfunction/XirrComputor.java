package com.bash.excelfunction;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class XirrComputor {

	private static final XirrContext DEFAULT_CONTEXT = new XirrContext(0.1,
			100, 0.00000001);

	enum XirrErrorType {
		NONE, VALUE, NUMBER;
	}

	final static class XirrResult {
		private double value;
		private XirrErrorType errorType;
		private long computingTimeMilli;

		public XirrResult(double value, XirrErrorType errorType,
				long computingTimeMilli) {
			super();
			this.value = value;
			this.errorType = errorType;
			this.computingTimeMilli = computingTimeMilli;
		}

		public boolean isInError() {
			return errorType != XirrErrorType.NONE;
		}

		public double getValue() {
			return value;
		}

		public long getComputingTimeInMilliSec() {
			return computingTimeMilli;
		}

	}

	public static XirrResult xirr(double[] cashFlows, Date[] dates) {
		return xirr(cashFlows, dates, DEFAULT_CONTEXT);
	}

	/**
	 * 
	 * @throws NullPointerException
	 *             if context is null
	 */
	public static XirrResult xirr(double[] cashFlows, Date[] dates,
			XirrContext context) {

		long start = System.currentTimeMillis();
		double[] timeFractions;
		double resultRate = context.getGuess();

		// usual check
		if (cashFlows.length == 0 || cashFlows.length != dates.length) {
			return new XirrResult(0.0, XirrErrorType.NUMBER, start
					- System.currentTimeMillis());
		}
		timeFractions = new double[cashFlows.length];
		boolean hasPositiveValue = false;
		boolean hasNegativeValue = false;

		for (int i = 0; i < cashFlows.length; i++) {
			timeFractions[i] = Days.daysBetween(new DateTime(dates[0]),
					new DateTime(dates[i])).getDays() / 365.0;
			if (cashFlows[i] > 0) {
				hasPositiveValue = true;
			}
			if (cashFlows[i] < 0) {
				hasNegativeValue = true;
			}
		}

		if (!hasNegativeValue || !hasPositiveValue) {
			return new XirrResult(0.0, XirrErrorType.NUMBER, start
					- System.currentTimeMillis());
		}

		// Set maximum epsilon for end of iteration
		double epsMax = context.getEpsilonMax();

		// Set maximum number of iterations
		int iterMax = context.getMaxIteration();

		// Implement Newton's method
		double newRate, epsRate, resultValue;
		int iteration = 0;
		boolean contLoop = true;
		do {
			resultValue = couputeRate(cashFlows, dates, resultRate,
					timeFractions);
			newRate = resultRate
					- resultValue
					/ firstDerivation(cashFlows, dates, resultRate,
							timeFractions);
			epsRate = Math.abs(newRate - resultRate);
			resultRate = newRate;
			contLoop = (epsRate > epsMax) && (Math.abs(resultValue) > epsMax);
		} while (contLoop && (++iteration < iterMax));

		if (contLoop)
			return new XirrResult(0.0, XirrErrorType.VALUE, start
					- System.currentTimeMillis());

		// Return internal rate of return
		return new XirrResult(resultRate, XirrErrorType.NONE, start
				- System.currentTimeMillis());
	}

	private static double firstDerivation(double[] cashFlows, Date[] dates,
			double rate, double[] timeFractions) {
		double r = rate + 1;
		double result = 0.0;
		for (int i = 1; i < cashFlows.length; i++) {
			result -= timeFractions[i] * cashFlows[i]
					/ Math.pow(r, timeFractions[i] + 1);
		}
		return result;
	}

	private static double couputeRate(double[] cashFlows, Date[] dates,
			double rate, double[] timeFractions) {
		double r = rate + 1;
		double result = cashFlows[0];
		for (int i = 1; i < cashFlows.length; i++) {
			result += cashFlows[i] / Math.pow(r, (timeFractions[i]));
		}
		return result;
	}
}
