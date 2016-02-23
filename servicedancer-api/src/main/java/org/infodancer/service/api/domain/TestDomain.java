package org.infodancer.service.api.domain;

import javax.naming.Context;

import org.infodancer.message.MessageStore;
import org.infodancer.service.api.LifecycleException;
import org.infodancer.user.UserManager;

/**
 * A Domain implementation intended to facilitate junit tests.
 * @author matthew
 *
 */
public class TestDomain implements Domain
{
	String name;
	Context context;
	UserManager um;
	ClassLoader cl;
	MessageStore msgstore;
	
	@Override
	public void start() throws LifecycleException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stop() throws LifecycleException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public ClassLoader getClassLoader()
	{
		return cl;
	}

	public void setClassLoader(ClassLoader cl)
	{
		this.cl = cl;
	}
	
	@Override
	public Context getContext()
	{
		return context;
	}

	public void setContext(Context context)
	{
		this.context = context;
	}
	
	@Override
	public String getDomainName()
	{
		return name;
	}

	@Override
	public UserManager getUserManager()
	{
		return um;
	}
	
	public void setUserManager(UserManager um)
	{
		this.um = um;
	}

	@Override
	public MessageStore getMessageStore()
	{
		return msgstore;
	}
	
	public void setMessageStore(MessageStore msgstore)
	{
		this.msgstore = msgstore;
	}
}
