package com.tyaer.util.zookeeper;

public interface DistributeXLock {
	
	public boolean lock();
	
	public void releaseLock();

}
