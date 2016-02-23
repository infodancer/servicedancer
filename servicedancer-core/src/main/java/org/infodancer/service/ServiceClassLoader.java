package org.infodancer.service;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.infodancer.archive.JarFileFilter;

public class ServiceClassLoader extends URLClassLoader
{
	public ServiceClassLoader(URL[] urls, ClassLoader parent)
	{
		super(urls, parent);
	}
	
	public static ServiceClassLoader createServiceClassLoader(File serviceDirectory, ClassLoader parent)
	{
		ArrayList<URL> urls = new ArrayList<URL>(); 
		File libDirectory = new File(serviceDirectory + File.separator + "lib");
		File[] jars = libDirectory.listFiles(new JarFileFilter());
		if (jars != null)
		{
			for (File jar : jars)
			{
				try
				{
					urls.add(jar.toURI().toURL());
					System.out.println("Adding " + jar + " to classpath for service " + serviceDirectory);
				}
				
				catch (Exception e)
				{
					System.out.println("Could not load " + jar.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
		return new ServiceClassLoader(urls.toArray(new URL[urls.size()]), parent);
	}

}
