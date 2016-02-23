package org.infodancer.service.netstring;

import java.io.IOException;

public class NetStringProtocolException extends IOException 
{
	NetStringProtocolException(String message)
	{
		super(message);
	}
}
