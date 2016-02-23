package org.infodancer.msgdancer.smtp;

/** 
 * Defines an interface marking an SMTP extension, accessible via the EHLO 
 * command.
 **/

public interface SMTPExtension extends SMTPCommandHandler
{
	public String getExtensionName();
}
