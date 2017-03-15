package org.infodancer.domain.directory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.NamingException;

import org.infodancer.context.ContextParser;
import org.infodancer.domain.AbstractDomain;
import org.infodancer.domain.DomainClassLoader;
import org.infodancer.domain.DomainThreadFactory;
import org.infodancer.message.MessageStore;
import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.domain.Domain;
import org.infodancer.user.UserManager;

public class DirectoryDomain extends AbstractDomain implements Domain
{
	private java.io.File directory;
	private java.io.File jars;

	public void setDirectory(java.io.File directory)
	{
		this.directory = directory;
		this.jars = new File(directory + File.separator + "lib");
	}
	
	public java.io.File getDirectory()
	{
		return directory;
	}

	protected void initializeDomainContext(Context context) throws ServiceException
	{
		File contextFile = new File(directory.getAbsoluteFile() + File.separator + "context.xml");
		if (contextFile.exists()) ContextParser.parseContextFile(context, contextFile);		
	}
	
	protected Context createDomainContext() throws ServiceException
	{
		DirectoryDomainContext context = new DirectoryDomainContext(this, directory, environment);
		return context;
	}
	
	protected DomainClassLoader createDomainClassLoader(ClassLoader parent) throws ServiceException
	{
		ArrayList<URL> list = new ArrayList<URL>();
		java.io.File[] files = jars.listFiles(new JarFilenameFilter());
		if (files != null)
		{
			for (File file : files) 
			{
				try
				{
					list.add(file.toURL());
				}
				
				catch (java.net.MalformedURLException e)
				{
					throw new ServiceException("Error loading jar file " + file.getAbsolutePath() + " into classpath!", e);
				}				
			}
		}
		
		URL[] urls = new URL[list.size()]; 
		urls = list.toArray(urls);
		return new DomainClassLoader(urls, parent, this, context);
	}

	protected DomainThreadFactory createDomainThreadFactory() throws ServiceException
	{
		return new DomainThreadFactory(this);
	}

	private class JarFilenameFilter implements FilenameFilter 
	{
		public boolean accept(File arg0, String arg1) 
		{
			if (arg1.endsWith(".jar")) return true;
			else return false;
		}
	}

	/**
	 * Attempts to acquire the UserManager for this domain from the domain context.
	 * @return UserManager is found, null if not.
	 */
	public UserManager getUserManager()
	{
		try
		{
			Context context = getContext();
			return (UserManager) context.lookup(UserManager.CONTEXT_USER_MANAGER);
		}
		
		catch (NamingException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Attempts to acquire the UserManager for this domain from the domain context.
	 * @return UserManager is found, null if not.
	 */
	public MessageStore getMessageStore()
	{
		try
		{
			Context context = getContext();
			return (MessageStore) context.lookup(MessageStore.CONTEXT_MESSAGE_STORE);
		}
		
		catch (NamingException e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
