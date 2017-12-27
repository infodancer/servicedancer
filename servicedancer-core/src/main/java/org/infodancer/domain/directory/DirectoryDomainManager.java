package org.infodancer.domain.directory;

import java.io.File;
import java.util.HashMap;

import org.infodancer.service.api.LifecycleException;
import org.infodancer.service.api.domain.Domain;
import org.infodancer.service.api.domain.DomainManager;

public class DirectoryDomainManager implements DomainManager 
{
	HashMap<String,Domain> domains = new HashMap<String,Domain>();
	File directory;

	public DirectoryDomainManager()
	{
		
	}
	
	public java.io.File getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = new java.io.File(directory);
	}

	public void start() throws LifecycleException 
	{
		try
		{
			if (directory.exists())
			{
				File[] domainDirectories = directory.listFiles();
				for (int i = 0; i < domainDirectories.length; i++)
				{
					if (domainDirectories[i].isDirectory())
					{
						String name = domainDirectories[i].getName();
						DirectoryDomain domain = new DirectoryDomain();
						domain.setDomainName(name);
						domain.setDirectory(domainDirectories[i]);
						domains.put(name, domain);
					}
				}
				
				for (Domain domain : domains.values())
				{
					System.out.println("Starting domain " + domain.getDomainName());
					domain.start();
				}
			}
			else throw new LifecycleException("Domain repository at " + directory + " does not exist!");
		}

		catch (Exception e)
		{
			e.printStackTrace();
			throw new LifecycleException(e);
		}
	}

	public void stop() throws LifecycleException 
	{
		for (Domain domain : domains.values())
		{
			try
			{
				System.out.println("Stopping domain " + domain.getDomainName());
				domain.stop();
			}
			
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public void createDomain(String name)
	{
		File domainDirectory = new File(directory + File.separator + name);
		domainDirectory.mkdir();
		File domainLibDir = new File(domainDirectory + File.separator + "lib");
		domainLibDir.mkdir();
		File domainUserDir = new File(domainDirectory + File.separator + "users");
		domainUserDir.mkdir();
		File domainWebappDir = new File(domainDirectory + File.separator + "webapps");
		domainUserDir.mkdir();
	}

	public Domain getDomain(String name)
	{
		return domains.get(name);
	}

	public void removeDomain(String name)
	{
		domains.remove(name);
	}
}
