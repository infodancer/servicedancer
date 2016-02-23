package org.infodancer.msgdancer.smtp;

public class SMTPException extends Exception
{
	public int result;
	
	public SMTPException(int result, String msg)
	{
		super(msg);
		this.result = result;
	}
	
	public String getMessage()
	{
		return result + " " + super.getMessage();
	}
}
