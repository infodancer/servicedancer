package org.infodancer.service.async;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;

import javax.net.ssl.SSLServerSocket;

import org.infodancer.handler.ServiceHandlerImpl;
import org.infodancer.service.ServiceConnectionImpl;
import org.infodancer.service.ServiceHandlerWrapper;
import org.infodancer.service.ServiceListener;
import org.infodancer.service.ServiceWorker;
import org.infodancer.service.api.ServiceException;
import org.infodancer.service.api.async.AsyncService;
import org.infodancer.service.api.async.ServiceHandler;

public class AsyncListener extends ServiceListener
{
	Selector selector; 
	AsyncService service;
	protected ServerSocketChannel channel;
	protected LinkedList<ServiceHandlerImpl> handlers = new LinkedList<ServiceHandlerImpl>();

	public AsyncListener(ClassLoader loader, File directory)
	{
		super(loader, directory);
	}

	public void start() throws ServiceException
	{
		try
		{
			System.out.println("Starting service " + name + " on " + getAddress() + ":" + getPort());
			selector = java.nio.channels.Selector.open();
			channel = java.nio.channels.ServerSocketChannel.open();
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_ACCEPT, this);
			socket = (SSLServerSocket) channel.socket();
			socket.bind(new InetSocketAddress(ipaddress, port));
			new Thread(this).start();
			service.start();
		}
		
		catch (Exception e)
		{
			throw new ServiceException("Exception while starting service listener!", e);
		}
	}
	
	/**
	 * The ServiceListener thread now handles events for a single server socket.
	 */
	public void run()
	{
		running = true;
		while (running)
		{
			try
			{
				int readable = 0;
				int writable = 0;
				int acceptable = 0;
				synchronized (selector)
				{
					int ready = selector.select(timeout);
					if (ready > 0)
					{
						Iterator<SelectionKey> i = selector.selectedKeys().iterator();
						while (i.hasNext())
						{
							SelectionKey key = i.next();
							if (key.isValid())
							{
								if (key.isAcceptable())
								{
									acceptable++;
									handleAccept(key);
								}								
			
								if ((key.isReadable()) || (key.isWritable()))							
								{
									ServiceHandlerWrapper wrapper = (ServiceHandlerWrapper) key.attachment();
									if (key.isReadable()) readable++;
									if (key.isWritable()) writable++;
									key.interestOps(0);
									ServiceWorker worker = new ServiceWorker(key, wrapper);
									workers.submit(worker);
								}
							}
							else 
							{
								ServiceHandlerWrapper wrapper = (ServiceHandlerWrapper) key.attachment();
								wrapper.getHandler().close();
							}
							i.remove();
						}
					}
					
					Thread.yield();
					
					// System.out.println("[readable: " + readable + " writable: " + writable + " acceptable: " + acceptable + " total keys: " + selector.keys().size() + " total handlers: " + handlers.size());
				}
			}
			
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
		
		try
		{
			running = false;
			synchronized (handlers)
			{
				int count = 0;
				for (ServiceHandlerImpl handler : handlers)
				{
					try
					{
						handler.close();
						count++;
					}
					
					catch (Throwable e)
					{
						System.err.println("Exception while shutting down service handler " + handler.getClass().getName());
						e.printStackTrace();
					}				
				}
				System.out.println("Shut down " + count + " active handlers.");
			}
			try { channel.close(); } catch (Throwable e) { } 
			try {  socket.close(); } catch (Throwable e) { } 
			try {  service.stop(); } catch (Throwable e) { } 	
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void submit(Callable<ServiceConnectionImpl> worker)
	{
		workers.submit(worker);
	}
	
	public void stop() throws ServiceException
	{
		running = false;
	}

	private void handleAccept(SelectionKey key) throws ServiceException, IOException
	{
		ServerSocketChannel channel = (ServerSocketChannel) key.channel();
		SocketChannel socketchannel = channel.accept();
		if (socketchannel != null)
		{
			Socket socket = socketchannel.socket();
			System.out.println("Connection request from " + socket.getInetAddress() + " on port " + socket.getLocalPort());
			
			AsyncListener listener = (AsyncListener) key.attachment();
			if ((listener.filter(socket)) && (listener.getMaxConcurrency() > listener.getConnectionCount()))
			{
				socketchannel.configureBlocking(false);
				SelectionKey socketkey = socketchannel.register(selector, 0);
				AsyncConnectionImpl connection = new AsyncConnectionImpl(socketchannel, listener);
				ServiceHandler handler = service.service(connection);
				if (handler != null) 
				{
					System.out.println("Accepted connection from " + socket.getInetAddress() + " on port " + socket.getLocalPort());
					addConnection(connection);
					socketkey.attach(handler);
				}
				else
				{
					System.out.println("Dropped connection from " + socket.getInetAddress() + " on port " + socket.getLocalPort());
					try { socketchannel.close(); } catch (Exception e) { }
					try { connection.close(); } catch (Exception e) { }
					try { socket.close(); } catch (Exception e) { } 									
				}
			}
			else
			{
				System.out.println("Refused connection from " + socket.getInetAddress() + " on port " + socket.getLocalPort());
				try { socketchannel.close(); } catch (Exception e) { }
				try { socket.close(); } catch (Exception e) { } 									
			}
		}		
	}

	public void setAsyncService(AsyncService service) 
	{
		this.service = service;
	}

}
