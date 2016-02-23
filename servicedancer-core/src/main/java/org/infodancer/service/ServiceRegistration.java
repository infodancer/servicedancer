/**
 * Copyright by matthew on Feb 2, 2006 as part of the infodancer services project.
 */

package org.infodancer.service;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import org.infodancer.handler.ServiceHandlerImpl;
import org.infodancer.service.async.AsyncConnectionImpl;

public class ServiceRegistration
{
	public int ops;
	public ServiceHandlerImpl handler;
	public AsyncConnectionImpl connection;
	
	public ServiceRegistration(AsyncConnectionImpl connection, ServiceHandlerImpl handler)
	{
		this.ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
		this.connection = connection;
		this.handler = handler;
	}
	
	public SelectionKey register(Selector selector) throws java.io.IOException
	{
		SelectableChannel channel = connection.getSocketChannel();
		channel.configureBlocking(false);
		SelectionKey key = channel.register(selector, ops, handler);
		return key;
	}
}
