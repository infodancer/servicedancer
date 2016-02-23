package org.infodancer.msgdancer.maildir;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.infodancer.message.EmailAddress;
import org.infodancer.msgdancer.MessageEngineException;

public class MaildirMessageEnvelope 
{
	String msgid;
	EmailAddress sender;
	List<EmailAddress> recipients;
	
	public MaildirMessageEnvelope(String msgid, EmailAddress recipient)
	{
		this.msgid = msgid;
		this.recipients = new ArrayList<EmailAddress>();
		this.recipients.add(recipient);
	}

	public MaildirMessageEnvelope(String msgid, List<EmailAddress> recipients)
	{
		this.msgid = msgid;
		this.recipients = new ArrayList<EmailAddress>();
		this.recipients.addAll(recipients);
	}
	
	public String getMsgid()
	{
		return msgid;
	}

	public void setMsgid(String msgid) 
	{
		this.msgid = msgid;
	}

	public List<EmailAddress
	> getRecipients() 
	{
		return recipients;
	}

	public void setRecipients(List<EmailAddress> recipients) 
	{
		this.recipients = recipients;
	}

	public static void exportFile(MaildirMessageEnvelope env, File file) throws MessageEngineException
	{
		BufferedWriter output = null;
		
		try
		{
			output = new BufferedWriter(new FileWriter(file));
			output.write(env.msgid);
			output.write("\n");
			for (EmailAddress recipient : env.recipients)
			{
				output.write(recipient.toString());
				output.write("\n");
			}
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			throw new MessageEngineException(e);
		}
		
		finally
		{
			try { if (output != null) output.close(); } catch (Exception e) { e.printStackTrace();}
		}

	}

	public static MaildirMessageEnvelope importFile(File file) throws MessageEngineException
	{
		BufferedReader input = null;
		
		try
		{
			input = new BufferedReader(new FileReader(file));
			List<EmailAddress> recipients = new ArrayList<EmailAddress>();
			String msgid = input.readLine();
			String line = null;
			while ((line = input.readLine()) != null)
			{
				recipients.add(new EmailAddress(line));
			}
			return new MaildirMessageEnvelope(msgid, recipients);
		}
		
		catch (Exception e)
		{
			e.printStackTrace();
			throw new MessageEngineException(e);
		}
		
		finally
		{
			try { if (input != null) input.close(); } catch (Exception e) { e.printStackTrace();}
		}
	}
}
