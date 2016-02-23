package org.infodancer.msgdancer.smtp;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.infodancer.message.EmailAddress;

public class DATA extends AbstractSMTPCommand implements SMTPCommandHandler
{
	private static Logger log = Logger.getLogger(DATA.class.getName());
	static Pattern dataPattern;
	static Pattern msgidPattern;
	
	static 
	{
		dataPattern = Pattern.compile("^(?i)DATA.*");
		msgidPattern = Pattern.compile("^(?i)Message-ID: <(.*)>");
	}
	
	/*
	private String createLocalAddress(SMTPState handler)
	{
		StringBuffer result = new StringBuffer();
		java.net.InetAddress address = socket.getLocalAddress();
		result.append(address.getCanonicalHostName());
		result.append(address.toString());
		return result.toString();
	}
	*/
	/*
	private String createRemoteAddress(SMTPState handler)
	{
		StringBuffer result = new StringBuffer();
		java.net.InetAddress address = handler.connection.socket.getInetAddress();
		result.append(address.getCanonicalHostName());
		result.append(address.toString());
		return result.toString();
	}
	*/
	
	private static String createReceived(SMTPProtocol handler)
	throws java.nio.charset.CharacterCodingException
	{
		StringBuffer result = new StringBuffer();
		result.append("Received: ");
		// result.append(" FROM " + createRemoteAddress(handler));
		// result.append(" BY " + createLocalAddress(handler));
		result.append(" WITH SMTP; ");
		result.append(new java.util.Date().toString());
		return result.toString();
	}
	
	public boolean match(String line)
	{
		Matcher command = dataPattern.matcher(line);
		if (command.matches()) return true;
		else return false;
	}
	
	public void receiveMessageData(SMTPProtocol handler, SMTPDeliveryStream writer)
	throws IOException
	{
		try
		{
			String line = null;
			while ((line = handler.readLine()) != null)
			{
				if (!line.equals("."))
				{
					if (line.startsWith("..")) line = line.substring(1, line.length());
					writer.write(line.getBytes());
					writer.write('\n');
				}
				else 
				{
					handler.commit();
					handler.writeLine("250 OK");	
				}				
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			handler.abort();
			handler.writeLine("451 Temporary error; try again later.");
			log.severe("Unexpected exception writing to queue!");
		}
	}
	
	public void process(SMTPProtocol handler, String line)
	throws java.io.IOException
	{
		try
		{
			Matcher command = dataPattern.matcher(line);
			if (command.matches())
			{
				int recipients = handler.getRecipients().size();
				if (recipients >= 1)
				{
					SMTPDeliveryStream delivery = handler.deliver();
					handler.writeLine("354 Start mail input; end with <CRLF>.<CRLF>");
					createReceivedHeaders(handler, delivery);
					receiveMessageData(handler, delivery);
				}
				else handler.writeLine("554 No recipients!");
			}
		}

		catch (SMTPException e)
		{
			handler.abort();
			handler.writeLine(e.getMessage());
		}
		
		catch (Exception e)
		{
			log.severe("Exception writing to queue!");
			handler.writeLine("500 " + e.getMessage());
		}
	}

	private static void createReceivedHeaders(SMTPProtocol handler, SMTPDeliveryStream delivery)
	throws java.io.IOException
	{
		StringBuilder s = new StringBuilder();
		s.append("Received: from ");
		InetAddress remoteAddress = handler.getRemoteAddress();
		if (remoteAddress != null)
		{
			s.append(remoteAddress.toString());
		}
		else
		{
			s.append(" unknown ");
		}
		s.append(" (HELO " + handler.getRemoteHelo() + ")");
		s.append(" by ");
		s.append(" ");
		EmailAddress auth = handler.getAuthenticated();
		if (auth != null)
		{
			s.append(" by ");
			
		}
		s.append("\r\n");
		delivery.write(s.toString().getBytes());
	}
}
