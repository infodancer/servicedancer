package org.infodancer.context;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

public class InitialContextFactory implements javax.naming.spi.InitialContextFactory
{
	private static int initcount = 0;
	private static boolean initialized = false;
	private static java.io.File contextFile;
	private static SimpleContext toplevel;  
	private static SimpleContext context;
	
	public static java.io.File getContextFile() 
	{
		return contextFile;
	}

	public static void setContextFile(java.io.File contextFile) 
	{
		InitialContextFactory.contextFile = contextFile;
	}

	/**
	 * Provides the caller with the Context appropriate to the caller's environment.
	 * The mechanism for this is to check and see if the Thread's ClassLoader is a ContextClassLoader.
	 * If so, return the Context indicated by that ClassLoader; otherwise return the default context.
	 */
	
	public Context getInitialContext(Hashtable<?, ?> environment)
			throws NamingException
	{
		try 
		{
			Thread current = Thread.currentThread();
			ClassLoader loader = current.getContextClassLoader();

			if (loader instanceof ContextClassLoader) 
			{
				return ((ContextClassLoader) loader).getContext();
			}
			else 
			{
				if (!initialized)
				{
					toplevel = new SimpleContext(environment);
					context = new SimpleContext(environment);
					if (contextFile != null)
					{
						if (contextFile.exists())
						{
							initialized = true;
							ContextParser.parseContextFile(context, contextFile);
						}
					}
					toplevel.bind("java:/comp/env", context);
					initialized = true;
				}
				return toplevel;
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			throw new NamingException("Could not parse context configuration!");
		}
	}
}
