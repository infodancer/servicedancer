package org.infodancer.context;

import java.util.*;

import javax.naming.*;

import org.infodancer.domain.directory.DirectoryDomainContext;

/**
 * Implements a simple Context.  Data is stored in a static Map, shared across all instances.
 * This should function properly for the top-level context but not for the lower-level ones.  
 * @author matthew
 *
 */

public class SimpleContext extends AbstractContext implements javax.naming.Context 
{
	protected static java.util.concurrent.ConcurrentHashMap<String, Object> bindings;
	
	static
	{
		bindings = new java.util.concurrent.ConcurrentHashMap<String, Object>();
	}
	
	public SimpleContext(Hashtable<?,?> environment)
	{
		super(environment);
	}
	
	@Override
	protected Map<String, Object> getBindings()
	{
		return bindings;
	}

	@Override
	protected Context getNewInstance(Hashtable env)
	{
		return new SimpleContext(environment);
	}	
}
