package com.github.hydos.multiThreading;

import java.util.*;

public class GingerThreading {
	
	public List<GingerThread> threads;
	
	public GingerThreading() {
		threads = new ArrayList<GingerThread>();
	}
	
	public void registerThread(GingerThread thread) {
		threads.add(thread);
	}
	
}
