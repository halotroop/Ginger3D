package com.github.hydos.multiThreading;

public abstract class GingerThread extends Thread
{
	public boolean finished = false;
	public String threadName;
	public boolean started = false;
}
