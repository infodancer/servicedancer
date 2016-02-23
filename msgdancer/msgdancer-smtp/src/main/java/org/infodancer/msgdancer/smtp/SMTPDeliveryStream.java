package org.infodancer.msgdancer.smtp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SMTPDeliveryStream extends ByteArrayOutputStream
{
	SMTPProtocol protocol;
	
	public SMTPDeliveryStream(SMTPProtocol protocol)
	{
		super();
		this.protocol = protocol;
	}
}
