package org.infodancer.context;

import javax.naming.Context;
import java.net.URL;
import java.net.URLClassLoader;

/** 
 * Provides a ClassLoader implementation that is aware of the appropriate Context for its clients.
 * @author matthew
 */

public class ContextClassLoader extends URLClassLoader 
{
	Context context;
	
	public ContextClassLoader(URL[] urls, ClassLoader parent, Context context)
	{
		super(urls, parent);
		this.context = context;
	}

	public ContextClassLoader(URL[] urls, Context context)
	{
		super(urls);
		this.context = context;
	}
	
	public Context getContext()
	{
		return context;
	}
	
	public void setContext(Context context)
	{
		this.context = context;
	}
}
