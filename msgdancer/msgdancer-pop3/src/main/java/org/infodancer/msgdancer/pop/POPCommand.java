package org.infodancer.msgdancer.pop;

public interface POPCommand 
{
	/**
	 * Determines whether this SMTPCommand wants to process the provided line.
	 * @return true if this SMTPCommand should receive the line, false otherwise.
	 */
	public boolean match(POPHandler handler, String line);
	
	/** 
	 * Processes a command received from the client.  
	 * If this method throws an exception, the connection will be closed.  Please
	 * handle all exceptions that are not intended to have this effect within your 
	 * own code.  If the exception thrown is an instance of SMTPException, the 
	 * provided result code and message are returned before closing the connection.
	 **/
	 
	public void process(POPHandler handler, String line)
	throws java.io.IOException, POPException;
	
	/**
	 * Invoked during initialization to provide the command with a link to its POPService.
	 * @param service
	 */
	public void setService(POPService service);
	public POPService getService();
	
	
}
