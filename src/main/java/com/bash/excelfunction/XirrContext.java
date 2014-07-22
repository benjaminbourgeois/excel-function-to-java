package com.bash.excelfunction;

final class XirrContext {

	private final double guess;
	private final int maxIteration;
	private final double epsilonMax;

	public XirrContext(double guess, int maxIteration, double epsilonMax) {
		super();
		this.guess = guess;
		this.maxIteration = maxIteration;
		this.epsilonMax = epsilonMax;
	}

	public double getEpsilonMax() {
		return epsilonMax;
	}

	public double getGuess() {
		return guess;
	}

	public int getMaxIteration() {
		return maxIteration;
	}

}
