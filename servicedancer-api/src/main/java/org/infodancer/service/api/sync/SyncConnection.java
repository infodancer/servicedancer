package org.infodancer.service.api.sync;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.infodancer.service.api.ServiceConnection;

public interface SyncConnection extends ServiceConnection
{
	/**
	 * The startTLS command initiates an SSL handshake.
	 * @return true if the handshake succeeds, false otherwise.
	 * @throws IOException
	 */
	public boolean startTLS() throws IOException;
	
	/**
	 * Indicates whether the current connection is encrypted.
	 * @return
	 */
	public boolean isSSLEnabled();
	public InputStream getInputStream() throws IOException;
	public OutputStream getOutputStream() throws IOException;
}
