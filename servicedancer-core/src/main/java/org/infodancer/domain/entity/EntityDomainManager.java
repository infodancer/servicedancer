package org.infodancer.domain.entity;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;

import org.infodancer.service.api.LifecycleException;
import org.infodancer.service.api.domain.Domain;
import org.infodancer.service.api.domain.DomainManager;

/**
 * This DomainManager will treat a database as containing the definitive list of domains.
 * It will store certain information locally in a directory tree, but directories will be created 
 * as necessary to reflect the database.
 * @author matthew
 *
 */
public class EntityDomainManager implements DomainManager
{
	EntityManager em;
	
	public void start() throws LifecycleException
	{
		try
		{
			InitialContext context = new InitialContext();
			em = (EntityManager) context.lookup("EntityManager");
		}
		
		catch (javax.naming.NamingException e)
		{
			throw new LifecycleException(e);
		}
	}

	public void stop() throws LifecycleException
	{
		// TODO Auto-generated method stub
		
	}

	public Domain getDomain(String name)
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void createDomain(String name)
	{
		// TODO Auto-generated method stub
		
	}

	public void removeDomain(String name)
	{
		// TODO Auto-generated method stub
		
	}

}
