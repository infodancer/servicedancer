package org.infodancer.domain;

import java.util.concurrent.ThreadFactory;

import org.infodancer.service.api.domain.Domain;

public class DomainThreadFactory implements ThreadFactory 
{
	int threadcount;
	Domain domain;
	ThreadGroup group;
	
	public DomainThreadFactory(Domain domain)
	{
		this.threadcount = 0;
		this.domain = domain;
		this.group = new ThreadGroup(domain.getDomainName() + "_workers");
	}
	
	public Thread newThread(Runnable task) 
	{
		Thread result = new Thread(group, task);
		result.setContextClassLoader(domain.getClassLoader());
		result.setName(domain.getDomainName() + "_worker_" + threadcount++);
		return result;
	}
}
