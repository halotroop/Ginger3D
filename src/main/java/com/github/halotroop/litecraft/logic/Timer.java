package com.github.halotroop.litecraft.logic;

import java.util.*;

import com.github.hydos.multithreading.GingerThread;

/**
 * @author Jack Wilsdon (Stack Exchange)
 * https://codereview.stackexchange.com/questions/111855/ticker-for-game-timing
 */
public class Timer extends GingerThread
{
	@Override
	public void run() {
		for (TickListener listener : tickListeners)
		{ listener.onTick(deltaTime); }
	}
	
	public interface TickListener
	{
		void onTick(float deltaTime);
	}
	
	float deltaTime;
	private double lastTick;
	private double nextTick;
	private int tickRate;
	private Set<TickListener> tickListeners = new HashSet<>();

	public Timer(int tickRate)
	{ this.tickRate = tickRate; }

	public void addTickListener(TickListener listener)
	{ tickListeners.add(listener); }

	public int getTickRate()
	{ return tickRate; }

	public void removeTickListener(TickListener listener)
	{ tickListeners.remove(listener); }

	public void reset()
	{
		lastTick = 0;
		nextTick = 0;
	}

	public void setTickRate(int tickRate)
	{ this.tickRate = tickRate; }

	public boolean tick()
	{
		long currentTime = System.nanoTime();
		if (currentTime >= nextTick)
		{
			long targetTimeDelta = 1000000000L / tickRate;
			if (lastTick == 0 || nextTick == 0)
			{
				lastTick = currentTime - targetTimeDelta;
				nextTick = currentTime;
			}
			deltaTime = (float) (currentTime - lastTick) / targetTimeDelta;
			this.run();
			lastTick = currentTime;
			nextTick = currentTime + targetTimeDelta;
			return true;
		}
		return false;
	}
}
