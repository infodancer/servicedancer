package org.infodancer.domain;

import java.util.Hashtable;

import org.infodancer.context.SimpleContext;
import org.infodancer.service.api.domain.Domain;

public class DomainContext extends SimpleContext 
{
	Domain domain;
	java.io.File directory;
	java.util.Hashtable<?,?> environment;
	
	DomainContext(Domain domain, java.io.File directory, Hashtable<?,?> environment)
	{
		super(environment);
		this.environment = environment;
		this.directory = directory;
		this.domain = domain;
	}
}
