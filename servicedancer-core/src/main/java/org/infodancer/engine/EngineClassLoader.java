package org.infodancer.engine;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import javax.naming.Context;

import org.infodancer.archive.JarFileFilter;
import org.infodancer.context.ContextClassLoader;

public class EngineClassLoader extends ContextClassLoader
{
	URLClassLoader urlparent;
	
	public EngineClassLoader(URL[] urls, URLClassLoader parent, Context context)
	{
		super(urls, parent, context);
	}
	
	public static EngineClassLoader createEngineClassLoader(File directory, URLClassLoader parent, Context context)
	{
		EngineClassLoader result = null; 
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
					System.out.println("Adding " + jar + " to classpath for engine" + directory);
				}
				
				catch (Exception e)
				{
					System.out.println("Could not load " + jar.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}

		result = new EngineClassLoader(urls.toArray(new URL[urls.size()]), parent, context);
		return result;
	}
}
