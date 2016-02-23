package org.infodancer.msgdancer.pop;

import java.io.IOException;

public class Unimplemented extends AbstractPOPCommand implements POPCommand
{
	public boolean match(POPHandler handler, String line) 
	{
		return false;
	}

	public void process(POPHandler handler, String line) 
	throws IOException, POPException 
	{
		handler.writeLine("-ERR Command not implemented.");
	}
}
