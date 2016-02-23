/**
 * 
 */
package org.infodancer.context;

import java.io.File;
import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * @author matthew
 *
 */
public class InitialContextFactoryBuilder implements javax.naming.spi.InitialContextFactoryBuilder
{
	File contextFile;
	
	public InitialContextFactoryBuilder(File contextFile)
	{
		this.contextFile = contextFile;
	}
	
	public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> arg0) throws NamingException
	{
		org.infodancer.context.InitialContextFactory factory = new org.infodancer.context.InitialContextFactory();
		factory.setContextFile(contextFile);
		return factory;
	}	
}
