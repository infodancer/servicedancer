package org.infodancer.service;

import java.io.File;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Startup
{
	
	/**
	 * Checks whether a specified file is a jar file by checking the filename extension.
	 */
	public static boolean isJarFile(File file)
	{
		String name = file.getName();
		if (name.endsWith(".jar")) return true;
		else if (name.endsWith(".JAR")) return true;
		else return false;
	}

	/**
	 * Creates a ClassLoader from the specified library path, and sets it to be the 
	 * current thread's class loader.
	 */
	public static ClassLoader createClassLoader(List<File> jars) throws Exception
	{
		java.util.ArrayList<java.net.URL> urls = new java.util.ArrayList<java.net.URL>();
		for (java.io.File jarFile : jars)
		{
			System.out.println("Adding " + jarFile + " to classloader.");
			urls.add(jarFile.toURI().toURL());
		}

		URLClassLoader loader = new URLClassLoader(urls.toArray(new java.net.URL[urls.size()]));
		// Thread.currentThread().setContextClassLoader(loader);
		return loader;
	}

	public static final void main(String[] args)
	{
		try
		{
			if (args.length == 0)
			{
				System.out.println("[USAGE] java -jar /opt/servicedancer/lib/servicedancer.jar <context path>");
				System.out.println("<context path> default: /opt/servicedancer/");
				System.out.println("The context path is the top level of the servicedancer directory structure.");
				System.exit(1);
			}
			else
			{
				List<File> jars = new ArrayList<File>();
				File rootDir = new File(args[0]).getAbsoluteFile();
				// TODO I don't like hardcoding this part, move some of the XML parsing out later
				File infoDir = new File("/opt/infodancer/lib/");
				for (File info : infoDir.listFiles())
				{
					if (isJarFile(info))
					{
						jars.add(info);
					}
				}
				File libDir = new File("/opt/servicedancer/lib/");
				for (File lib : libDir.listFiles())
				{
					if (isJarFile(lib))
					{
						jars.add(lib);
					}
				}
				File enginesDir = new File(rootDir + File.separator + "engines");
				for (File engine : enginesDir.listFiles())
				{
					if (engine.isDirectory())
					{
						File libdir = new File(engine + File.separator + "lib");
						if (libdir.isDirectory())
						{
							for (File jarfile : libdir.listFiles())
							{
								if (isJarFile(jarfile))
								{
									jars.add(jarfile);
								}
							}
						}
					}
				}
				File serviceDir = new File(rootDir + File.separator + "services");
				for (File service : serviceDir.listFiles())
				{
					if (service.isDirectory())
					{
						File libdir = new File(service + File.separator + "lib");
						if (libdir.isDirectory())
						{
							for (File jarfile : libdir.listFiles())
							{
								if (isJarFile(jarfile))
								{
									jars.add(jarfile);
								}
							}
						}						
					}
				}
				
				ClassLoader loader = createClassLoader(jars);
				Class servicedancer = loader.loadClass("org.infodancer.service.ServiceDancer");
				Runnable startup = (Runnable) servicedancer.newInstance();
				Thread startupThread = new Thread(startup);
				startupThread.setContextClassLoader(loader);
				startupThread.start();
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
