package com.github.hydos.multithreading;

import java.util.*;

import com.github.halotroop.litecraft.Litecraft;
import com.github.hydos.ginger.engine.common.io.Window;

public class GingerThreading extends Thread
{
	public List<GingerThread> worldChunkThreadWaitlist;

	public GingerThreading()
	{ worldChunkThreadWaitlist = new ArrayList<>(); }

	public void registerChunkThreadToWaitlist(GingerThread thread)
	{ worldChunkThreadWaitlist.add(thread); }
	
	@Override
	public void run() {
		while(!Window.closed()) {
			if(worldChunkThreadWaitlist.size() != 0) {
				Litecraft.getInstance().threadWaitlist = worldChunkThreadWaitlist.size();
				GingerThread yes = worldChunkThreadWaitlist.get(0);
				if(yes.finished) {
					worldChunkThreadWaitlist.remove(0);
					if(worldChunkThreadWaitlist.size() != 0) {
						worldChunkThreadWaitlist.get(0).start();
					}
				}else {
					if(!yes.isAlive() && !yes.started) {
						yes.start();
					}
				}
			}
		}
	}
}
