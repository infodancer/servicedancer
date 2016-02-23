package org.infodancer.domain.directory;

import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;

import org.infodancer.context.AbstractContext;
import org.infodancer.service.api.domain.Domain;

public class DirectoryDomainContext extends AbstractContext
{
	Domain domain;
	java.io.File directory;
	protected static java.util.concurrent.ConcurrentHashMap<String, Object> bindings;
	static
	{
		bindings = new java.util.concurrent.ConcurrentHashMap<String, Object>();
	}

	DirectoryDomainContext(Domain domain, java.io.File directory, Hashtable<?,?> environment)
	{
		super(environment);
		this.directory = directory;
		this.domain = domain;
	}

	@Override
	protected Map<String, Object> getBindings()
	{
		return bindings;
	}

	@Override
	protected Context getNewInstance(Hashtable env)
	{
		return new DirectoryDomainContext(domain, directory, environment);
	}	
}
