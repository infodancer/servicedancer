package org.infodancer.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;

import org.infodancer.context.ContextParser;
import org.infodancer.context.SimpleContext;
import org.infodancer.service.sync.SyncListener;

/**
 * Runs from the command line to listen on a port for incoming network connections.
 * This class is designed to operation along lines similar to djb's tcpserver program. 
 * It will take the address and port number to listen to on the command line.  In addition
 * there are options to provide a context file to load.  The program will output log information
 * to standard out and errors to standard error, and will not daemonize itself.  
 * 
 * This approach offers several benefits for java-based network services over either using tcpserver
 * directly or using a single wrapper program like Tomcat or the full ServiceDancer stack.  In particular,
 * it allows services to be started and stopped independently of each other, allows the admin to make use 
 * of daemon management programs to automatically restart when needed, separates services from each other
 * in case of problems, can be used to run individual services under their own user ids, allows the JVM to 
 * take advantage of long-running processes for code optimization, and can be set up to use a shared configuration
 * by pointing multiple services to the same context file.  
 * 
 * However, those separate context files will not represent the same context instance, and thus interprocess communication
 * will need to use a different mechanism.  
 * 
 * The third argument here is uncertain.  Should we assume the current working directory is a service directory, 
 * with a context file, a lib directory for jars, and so on?  Should we provide a service directory on the command line
 * as the third argument?  Should we provide the context file?
 * @author matthew@infodancer.org
 *
 */
public class IndependentServiceListener extends SyncListener
{
	private static final String CONTEXT_XML = "context.xml";

	public IndependentServiceListener(ClassLoader cl, File directory)
	{
		super(cl, directory);
	}
	
	
	public static final void main(String[] args)
	{
		try
		{
			if (args.length < 2) 
			{
				System.out.println("USAGE: java -jar servicelistener.jar <ipaddress> <port> <service directory>");
				System.exit(1);
			}
			
			InetAddress ipaddress = validateIpAddressArgument(args[1]);
			int port = ServiceUtility.validatePortArgument(args[2]);
			File serviceDirectory = validateServiceDirectoryArgument(args[3]);

			ClassLoader parentClassLoader = ClassLoader.getSystemClassLoader();
			ServiceClassLoader cl = ServiceClassLoader.createServiceClassLoader(serviceDirectory, parentClassLoader);
			// We want to start out with an empty context and then parse the context file
			SimpleContext context = new SimpleContext(new Hashtable());
			
			File contextFile = new File(serviceDirectory + File.separator + CONTEXT_XML);
			if (contextFile.exists())
			{
				ContextParser.parseContextFile(context, contextFile);
			}
			else System.out.println("Warning: No context file found at " + contextFile.getAbsolutePath());
			IndependentServiceListener listener = new IndependentServiceListener(cl, serviceDirectory);
			listener.setAddress(ipaddress);
			listener.setPort(port);
			listener.start();
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	private static File validateServiceDirectoryArgument(String s) 
	{
		File result = new File(s);
		if (result.exists())
		{
	
		}
		else
		{
			
		}

		return result;
	}

	private static InetAddress validateIpAddressArgument(String s) throws UnknownHostException 
	{
		return InetAddress.getByName(s);
	}
}
