package org.infodancer.context;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.ObjectFactoryBuilder;
import java.util.logging.Logger;

public class ContextObjectFactoryBuilder implements ObjectFactoryBuilder 
{
	private ConnectionPoolFactory poolFactory;
	private SessionFactory sessionFactory;
	private BeanFactory beanFactory;
	
	private static final Logger log = Logger.getLogger(ContextObjectFactoryBuilder.class.getName());
	public ObjectFactory createObjectFactory(Object obj, Hashtable<?, ?> environment) throws NamingException 
	{
		if (obj instanceof Reference)
		{
			Reference ref = (Reference) obj;
			String className = ref.getClassName();
			if (className != null)
			{
				if ("javax.mail.Session".equalsIgnoreCase(className))
				{
					if (sessionFactory == null) sessionFactory = new SessionFactory();
					return sessionFactory;
				}
				else if ("javax.sql.DataSource".equalsIgnoreCase(className))
				{
					if (poolFactory == null) poolFactory = new ConnectionPoolFactory();
					return poolFactory;
				}
				else
				{
					// This is the default factory
					if (beanFactory == null) beanFactory = new BeanFactory();
					return beanFactory;
				}
			}
		}
		else log.warning("Could not identify a factory for " + obj);
		return null;
	}
}
