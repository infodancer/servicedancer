package org.infodancer.service.api.udp;

import java.io.IOException;
import java.net.DatagramPacket;

import org.infodancer.service.api.Service;
import org.infodancer.service.api.ServiceException;

public interface UDPService extends Service
{
	public void service(DatagramPacket packet) throws IOException, ServiceException;
}
