package org.infodancer.service.api.domain;

import java.util.HashMap;
import java.util.Map;

import org.infodancer.message.MemoryMessageStore;
import org.infodancer.service.api.LifecycleException;
import org.infodancer.user.memory.MemoryUserManager;

public class TestDomainManager implements DomainManager
{
	Map<String,Domain> domains = new HashMap<String,Domain>();
	
	@Override
	public void start() throws LifecycleException
	{
		
	}

	@Override
	public void stop() throws LifecycleException
	{
		
	}

	@Override
	public Domain getDomain(String name)
	{
		return domains.get(name);
	}

	@Override
	public void createDomain(String name)
	{
		TestDomain domain = new TestDomain();
		domain.setUserManager(new MemoryUserManager());
		domain.setMessageStore(new MemoryMessageStore());
		domains.put(name, domain);
	}

	@Override
	public void removeDomain(String name)
	{
		domains.remove(name);
	}	
}
