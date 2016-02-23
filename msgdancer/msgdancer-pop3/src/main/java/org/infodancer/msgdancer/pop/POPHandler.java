package org.infodancer.msgdancer.pop;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.infodancer.message.EmailAddress;
import org.infodancer.message.Message;
import org.infodancer.message.MessageFolder;
import org.infodancer.message.MessageStore;
import org.infodancer.service.api.sync.SyncConnection;
import org.infodancer.user.UserManager;

public class POPHandler
{
	private static final Logger log = Logger.getLogger(POPHandler.class.getName());
	POPService popservice;
	EmailAddress user;
	boolean authenticated;
	MessageStore msgstore;
	MessageFolder folder;
	List<Message> messages;
	UserManager manager;
	java.util.HashMap<String,Object> attributes;
	BufferedReader reader;
	BufferedWriter writer;
	/** Holds the last command executed, in case of partial execution **/
	POPCommand command;
	SyncConnection connection;
	
	public POPHandler(POPService service, SyncConnection connection)
	{
		this.popservice = service;
		this.connection = connection;
		this.command = null;
		this.user = null;
		this.authenticated = false;
		this.attributes = new HashMap<String, Object>();
	}

	/**
	 * Writes a line to the network.
	 * We can't use the usual BufferedWriter.writeLine() because we are a network protocol,
	 * the linefeed character we use does not depend on our platform.
	 * @param line
	 * @throws IOException
	 */
	public void writeLine(String line) throws IOException
	{
		writer.write(line);
		writer.write('\n');
		writer.flush();
	}

	public synchronized void process(String line) throws IOException 
	{
		try
		{
			boolean result = false;
			log.fine("RECV: " + line);
			
			for (POPCommand command : popservice.commands)
			{
				if (command.match(this, line))
				{
					command.process(this, line);
					result = true;
					break;
				}
			}
			
			// If nothing matched...
			if (!result) 
			{
				writeLine("-ERR Unrecognized command or syntax error");
				connection.log("-ERR Unrecognized command or syntax error");
			}
		}
		
		catch (POPException e)
		{
			e.printStackTrace();
			try { writeLine("-ERR Unrecognized command or syntax error"); } catch (Exception ee) { } 
			stop();
		}

		catch (Throwable e)
		{
			e.printStackTrace();
			String error = "-ERR Unrecognized command or syntax error";
			connection.log(error, e);
			writeLine(error);
			stop();
		}
	}
	
	public void start()
	{
		try
		{
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			String greeting = popservice.getGreeting();
			if (greeting == null) greeting = "POP3 server ready";
			writer.write("+OK " + greeting + "\r\n");
			writer.flush();
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			stop();
		}		
	}
	
	public void stop()
	{
		try
		{
			if (!connection.isClosed())
			{
				writeLine("+OK Closing transmission channel");
				connection.close();
			}
		}

		catch (Exception e)
		{
			e.printStackTrace();
		}

		finally
		{
			try { if (reader != null) reader.close(); } catch (Exception e) { }
			try { if (writer != null) writer.close(); } catch (Exception e) { } 
		}		
	}

	public void run()
	{
		try
		{
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				// Just ignore blank lines completely
				if (line.trim().length() > 0)
				{
					process(line);
				}
				if (connection.isClosed()) break;
			}			
		}

		catch (Exception e)
		{
			e.printStackTrace();
			try { writeLine("-ERR Exception occurred, closing connection"); } catch (IOException ee) { } 
			stop();
		}
	}
}
