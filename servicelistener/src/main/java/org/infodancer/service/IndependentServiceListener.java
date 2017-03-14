package org.infodancer.service;

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
 * However, those separate context files will not represent the context instance, and thus interprocess communication
 * will need to use a different mechanism.  
 * @author matthew@infodancer.org
 *
 */
public class IndependentServiceListener
{
	public static final void main(String[] args)
	{
		if (args.length < 2) 
		{
			System.out.println("USAGE: java -jar servicelistener.jar <ipaddress> <port>");
			System.out.println("       --context=<context file>");
			System.exit(1);
		}
	}
}
