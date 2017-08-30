package com.tyaer.util.zookeeper.jp;

import java.util.concurrent.TimeUnit;


import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webant.lock.DistributeXLock;

public class CuratorLock implements DistributeXLock {
	private Logger logger = LoggerFactory.getLogger(CuratorLock.class);
	private  InterProcessMutex lock;

	
	
	
	public CuratorLock(InterProcessMutex lock){
		this.lock = lock;
	}

	@Override
	public boolean lock() {
		try {
			if(lock.acquire(60*1000, TimeUnit.MILLISECONDS)){
				return true;
			}
		} catch (Exception e) {
			logger.error("failed to obtain the lock,e={}",e);
		}
		return false;
	}

	@Override
	public void releaseLock() {
		// TODO Auto-generated method stub
		try {
			lock.release();
		} catch (Exception e) {
			logger.error("failed to release the lock,e={}",e);
		}
	}

}
