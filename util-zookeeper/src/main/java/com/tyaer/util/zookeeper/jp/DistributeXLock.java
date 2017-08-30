package com.tyaer.util.zookeeper.jp;

public interface DistributeXLock {
	
	public boolean lock();
	
	public void releaseLock();

}
