package org.infodancer.msgdancer.smtp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.infodancer.message.EmailAddress;
import org.infodancer.msgdancer.Envelope;
import org.infodancer.msgdancer.MessageEngine;
import org.infodancer.service.api.domain.Domain;
import org.infodancer.service.api.domain.DomainManager;
import org.infodancer.service.api.sync.SyncConnection;
import org.infodancer.user.User;
import org.infodancer.user.UserManager;

public class SMTPProtocol implements Runnable
{
	private static final Logger log = Logger.getLogger(SMTPProtocol.class.getName());
	boolean bounce;
	Envelope envelope;
	SyncConnection connection;
	MessageEngine msgengine;
	DomainManager dm;
	UserManager um;
	BufferedReader reader;
	BufferedWriter writer;
	SMTPService service;
	String remoteHelo;
	EmailAddress sender;
	EmailAddress authenticated;
	Set<EmailAddress> recipients;
	SMTPDeliveryStream delivery;
	Domain authdomain;
	User authuser;
	byte[] msgbytes;
	
	public SMTPProtocol(SMTPService service, SyncConnection connection)
	{
		this.service = service;
		this.connection = connection;
		this.msgengine = service.getMessageEngine();
		this.dm = service.getDomainManager();
		this.recipients = new HashSet<EmailAddress>();
		clear();
	}

	public void start()
	{	
		try
		{
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			writer.write("220 " + service.getGreeting() + " ESMTP\r\n");
			writer.flush();
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			stop();
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
			stop();
		}
	}

	public InetAddress getRemoteAddress()
	{
		return connection.getRemoteAddress();
	}
	
	public Envelope getEnvelope()
	{
		return envelope;
	}

	public void setEnvelope(Envelope envelope)
	{
		this.envelope = envelope;
	}

	public SyncConnection getConnection()
	{
		return connection;
	}

	public void setConnection(SyncConnection connection)
	{
		this.connection = connection;
	}

	public MessageEngine getMsgengine()
	{
		return msgengine;
	}

	public void setMsgengine(MessageEngine msgengine)
	{
		this.msgengine = msgengine;
	}

	public DomainManager getManager()
	{
		return dm;
	}

	public void setManager(DomainManager manager)
	{
		this.dm = manager;
	}

	public SMTPService getService()
	{
		return service;
	}

	public void setService(SMTPService service)
	{
		this.service = service;
	}

	public String getRemoteHelo()
	{
		return remoteHelo;
	}

	public void setRemoteHelo(String remoteHelo)
	{
		this.remoteHelo = remoteHelo;
	}

	public EmailAddress getAuthenticated()
	{
		return authenticated;
	}

	public void setAuthenticated(EmailAddress authenticated)
	{
		this.authenticated = authenticated;
	}

	public EmailAddress getSender()
	{
		return sender;
	}

	public void setRecipients(Set<EmailAddress> recipients)
	{
		this.recipients = recipients;
	}

	/**
	 * This method is used for processing commands that take more than one line.
	 * @return
	 * @throws IOException
	 */
	public String readLine() throws IOException
	{
		return reader.readLine();
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
	
	public void stop()
	{
		try
		{
			if (!connection.isClosed())
			{
				String greeting = service.getGreeting();
				if (greeting == null) greeting = "server";
				writeLine("221 " + greeting + " closing transmission channel");
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
	
	public synchronized void process(String line) throws java.io.IOException
	{
		try
		{
			boolean result = false;
			log.fine("RECV: " + line);
			for (SMTPCommandHandler command : service.commands)
			{
				if (command.match(line))
				{
					command.process(this, line);
					result = true;
					break;
				}
			}
			
			// If nothing matched...
			if (!result) 
			{
				writeLine("500 Unrecognized command or syntax error");
			}
		}
		
		catch (SMTPException e)
		{
			stop();
		}

		catch (OutOfMemoryError e)
		{
			e.printStackTrace();
			String error = "451 Out of memory, please try again later."; 
			connection.log(error, e);
			writeLine(error);
		}
		
		catch (Throwable e)
		{
			e.printStackTrace();
			String error = "500 Unrecognized command or syntax error";
			connection.log(error, e);
			writeLine(error);
		}
	}
	
	private boolean isDeliveryActive()
	{
		if (delivery != null) return true;
		else return false;
	}

	/**
	 * Aborts the current delivery.
	 */
	public void abort()
	{
		clear();
	}

	
	public void addRecipient(EmailAddress recipient)
	throws SMTPException
	{
		if (isBounceMessage())
		{
			if (recipients.size() > 0)
			{
				throw new SMTPException(550, "Bounce messages can have only one recipient!");
			}
			else
			{
				if (msgengine.verifyEmailAddress(recipient))
				{
					recipients.add(recipient);
				}
				else 
				{
					// This is something new; the msgengine will remember who it has 
					// sent mail to, and block bounce messages from addresses
					// it doesn't remember sending to.
					throw new SMTPException(550, "Sorry; we don't remember sending anything to that address.");
				}
			}
		}
		else if (isRelayAllowed())
		{
			recipients.add(recipient);
		}
		else if (service.isValidateUsers())
		{
			if (msgengine.verifyEmailAddress(recipient))
			{
				recipients.add(recipient);
			}
			else throw new SMTPException(451, "We do not accept mail for this recipient.");
		}
		else
		{
			recipients.add(recipient);
		}
	}

	public Set<EmailAddress> getRecipients()
	{
		return recipients;
	}

	public boolean isRelayAllowed()
	{
		
		return false;
	}

	public boolean isBounceMessage()
	{
		return bounce;
	}

	public void setBounceMessage(boolean bounce)
	{
		this.bounce = bounce;
	}
	
	public void setAuthenticatedSender(EmailAddress authenticated)
	{
		this.authenticated = authenticated;
	}

	public boolean isAuthenticated()
	{
		return (authenticated != null);
	}

	public void setSender(EmailAddress sender)
	{
		this.sender = sender;
	}

	/**
	 * Commits the currently pending message for delivery.
	 * @throws IOException 
	 */
	public void commit() throws IOException
	{
		try
		{
			delivery.close();
			msgbytes = delivery.toByteArray();
			if (msgengine != null)
			{
				for (EmailAddress recipient : recipients)
				{
					if (msgengine.isLocalAddress(recipient))
					{
						SMTPDeliveryAction action = new SMTPDeliveryAction(service, msgengine, sender, recipient, msgbytes);
						String msgid = action.call();
						System.out.println("[" + sender + ":" + msgid + ":" + recipient + "] queued");						
					}
					else if (isRelayAllowed())
					{
						SMTPDeliveryAction action = new SMTPDeliveryAction(service, msgengine, sender, recipient, msgbytes);
						String msgid = action.call();
						System.out.println("[" + sender + ":" + msgid + ":" + recipient + "] queued");						
					}
				}
			}
			else throw new Exception("MessageEngine not available!");
			delivery = null;
		}
		
		catch (Exception e)
		{
			try { writeLine("450 ERR Temporary delivery failure, try again later."); }
			catch (Exception ee) { stop(); } 
		}
	}

	public String getServerName()
	{
		String result = service.getGreeting();
		if ((result == null) || (result.trim().length() == 0))
		{
			result = connection.getLocalName();
		}
		else if ((result == null) || (result.trim().length() == 0))
		{
			result = connection.getLocalAddress().toString();
		}
		return result;
	}

	public SMTPDeliveryStream deliver() throws SMTPException
	{
		delivery = new SMTPDeliveryStream(this);
		return delivery;
	}

	/**
	 * Clears all SMTP session data.
	 */
	public void clear()
	{
		bounce = false;
		sender = null;
		authenticated = null;
		recipients.clear();
		if (delivery != null) delivery.reset();
	}
	
	public void setMessageBytes(byte[] msgbytes)
	{
		this.msgbytes = msgbytes;
	}

	public byte[] getMessageBytes()
	{
		return msgbytes;
	}
}
