package org.infodancer.service;

import java.util.concurrent.ThreadFactory;

public class ServiceListenerThreadFactory implements ThreadFactory 
{
	public Thread newThread(Runnable r) 
	{
		Thread result = new Thread(r);
		result.setName("ServiceListenerThread");
		return result;
	}
}
