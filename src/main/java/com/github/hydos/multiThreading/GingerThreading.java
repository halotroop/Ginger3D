package com.github.hydos.multiThreading;

import java.util.*;

public class GingerThreading
{
	public List<GingerThread> worldChunkThreadWaitlist;

	public GingerThreading()
	{ worldChunkThreadWaitlist = new ArrayList<GingerThread>(); }

	public void registerChunkThreadToWaitlist(GingerThread thread)
	{ worldChunkThreadWaitlist.add(thread); }
	
	public void update() {
		if(worldChunkThreadWaitlist.size() != 0) {
			System.out.println(worldChunkThreadWaitlist.size());
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
