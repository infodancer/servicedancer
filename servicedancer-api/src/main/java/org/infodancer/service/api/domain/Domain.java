package org.infodancer.service.api.domain;

import javax.naming.Context;

import org.infodancer.message.MessageStore;
import org.infodancer.service.api.Lifecycle;
import org.infodancer.user.UserManager;

public interface Domain extends Lifecycle
{
	public ClassLoader getClassLoader();
	public Context getContext();
	public String getDomainName();
	public UserManager getUserManager();
	public MessageStore getMessageStore();
}
