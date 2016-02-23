package org.infodancer.domain;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.naming.Context;

import org.infodancer.archive.JarFileFilter;
import org.infodancer.context.ContextClassLoader;
import org.infodancer.service.api.domain.Domain;

public class DomainClassLoader extends ContextClassLoader
{
	Domain domain;
	
	public DomainClassLoader(URL[] urls, ClassLoader parent, Domain domain, Context context)
	{
		super(urls, parent, context);
		this.domain = domain;
	}

	public DomainClassLoader(URL[] urls, Domain domain, Context context)
	{
		super(urls, context);
		this.domain = domain;
	}

	public Domain getDomain()
	{
		return domain;
	}

	public static DomainClassLoader createDomainClassLoader(File directory, URLClassLoader parent, Context context, Domain domain)
	{
		DomainClassLoader result = null; 
		ArrayList<URL> urls = new ArrayList<URL>();
		File libDirectory = new File(directory + File.separator + "lib");
		File[] libjars = libDirectory.listFiles(new JarFileFilter());
		if (libjars != null)
		{
			for (File jar : libjars)
			{
				try
				{
					urls.add(jar.toURI().toURL());
					System.out.println("Adding " + jar + " to classpath for domain " + directory);
				}
				
				catch (Exception e)
				{
					System.out.println("Could not load " + jar.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}

		result = new DomainClassLoader(urls.toArray(new URL[urls.size()]), parent, domain, context);
		return result;
	}
}
