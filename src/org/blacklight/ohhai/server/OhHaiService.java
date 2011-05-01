package org.blacklight.ohhai.server;

import org.blacklight.ohhai.socket.*;

public class OhHaiService extends Thread {
	private OhHaiServerSocket serverSock;
	private OhHaiServerSocket.ServerSocketType sockType;
	
	public OhHaiService (OhHaiServerSocket.ServerSocketType type, int listenPort)
	{
		sockType = type;
		
        try
        {
        	serverSock = new OhHaiServerSocket(sockType, listenPort);
        }
        
        catch (Exception e)
        {
        	OhHaiProgram.addMessage("Error while opening the server socket: " + e.toString(), null, e);
        }
	}
        	
	@Override
	public void run()
	{
		try
		{
        	while (true)
        	{
	        	new OhHaiServer(serverSock.accept()).start();
        	}
        }
        
        catch (Exception e)  {
        	OhHaiProgram.addMessage("Error while sending the message: " + e.toString(), null, e);
        }
	}
}
