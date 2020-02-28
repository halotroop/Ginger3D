package com.github.hydos.multiThreading;

public abstract class GingerThread extends Thread
{
	public boolean finished = true;
	public String threadName;
	public boolean started = false;
}
