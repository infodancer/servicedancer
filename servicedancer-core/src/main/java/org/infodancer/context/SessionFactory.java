package org.infodancer.context;

import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

public class SessionFactory implements ObjectFactory 
{
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			Hashtable<?, ?> environment) throws Exception 
	{
		if (obj instanceof Reference)
		{
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			Properties properties = new Properties();	
			return Session.getDefaultInstance(properties);
		}
		return null;
	}

}
