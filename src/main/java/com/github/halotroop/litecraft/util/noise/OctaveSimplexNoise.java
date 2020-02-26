package com.github.halotroop.litecraft.util.noise;

import java.util.Random;

public final class OctaveSimplexNoise
{
	protected SimplexNoise[] samplers;
	private double clamp;
	private double spread, amplitudeLow, amplitudeHigh;

	public OctaveSimplexNoise(Random rand, int octaves, double spread, double amplitudeHigh, double amplitudeLow)
	{
		this.samplers = new SimplexNoise[octaves];
		this.clamp = 1D / (1D - (1D / Math.pow(2, octaves)));

		for (int i = 0; i < octaves; ++i)
		{
			samplers[i] = new SimplexNoise(rand.nextLong());
		}

		this.spread = spread;
		this.amplitudeLow = amplitudeLow;
		this.amplitudeHigh = amplitudeHigh;
	}

	public double sample(double x, double y)
	{
		double amplFreq = 0.5D;
		double result = 0;

		for (SimplexNoise sampler : this.samplers)
		{
			result += (amplFreq * sampler.sample(x / (amplFreq * this.spread), y / (amplFreq * this.spread)));
			amplFreq *= 0.5D;
		}

		result = result * this.clamp;
		return result > 0 ? result * this.amplitudeHigh : result * this.amplitudeLow;
	}
}
