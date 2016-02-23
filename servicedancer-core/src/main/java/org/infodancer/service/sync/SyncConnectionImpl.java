package org.infodancer.service.sync;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import javax.net.ssl.SSLSocket;

import org.infodancer.service.ServiceConnectionImpl;
import org.infodancer.service.api.sync.SyncConnection;

public class SyncConnectionImpl extends ServiceConnectionImpl implements SyncConnection
{
	boolean ssl;
	SyncListener listener;
	
	public SyncConnectionImpl(Socket socket, SyncListener listener)
	{
		super();
		this.socket = socket;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		return socket.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		return socket.getOutputStream();
	}

	@Override
	public boolean startTLS() throws IOException
	{
		if (ssl) return true;
		else
		{
			sslSocket = (SSLSocket) sslSocketFactory.createSocket(socket, socket.getInetAddress().getHostAddress(), socket.getPort(), true);			
			InputStream inputStream = sslSocket.getInputStream();
			OutputStream outputStream = sslSocket.getOutputStream();
			ssl = true;
		}
		return false;
	}

	@Override
	public boolean isSSLEnabled()
	{
		return ssl;
	}
}
