package org.infodancer.service;

import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ServiceWorkerPool extends ScheduledThreadPoolExecutor
{
	public ServiceWorkerPool(int size)
	{
		super(size, new ServiceWorkerThreadFactory());
	}
}
