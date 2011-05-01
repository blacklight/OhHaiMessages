package org.blacklight.ohhai.socket;

import java.io.*;
import java.net.*;
import java.util.*;
import android.bluetooth.*;

public class OhHaiServerSocket {
	public enum ServerSocketType  { InetSocket, BluetoothSocket }
	private ServerSocketType sockType;
	private ServerSocket inetSock;
	private BluetoothServerSocket btSock;
	private final static UUID uuid = UUID.fromString("828b721e-8e88-276b-6c29-0987f79bdc21");
	private final static String serviceName = "OhHaiMessages";
	
	public OhHaiServerSocket (ServerSocketType type, int listenPort)
		throws IOException
	{
		sockType = type;
		
		switch (sockType)
		{
		case InetSocket:
        	inetSock = new ServerSocket(listenPort);
			break;
			
		case BluetoothSocket:
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			
			if (adapter == null)
			{
				throw new IOException ("The device does not support bluetooth connections");
			}
			
			if (!adapter.isEnabled())
			{
				throw new IOException ("Bluetooth interface not active, enable bluetooth on your device before starting the application");
			}
			
			btSock = adapter.listenUsingRfcommWithServiceRecord(serviceName, uuid);
			break;
		}
	}
	
	public OhHaiSocket accept()
		throws IOException
	{
		return (sockType == ServerSocketType.InetSocket) ? new OhHaiSocket(inetSock.accept()) : new OhHaiSocket(btSock.accept());
	}
	
	public void close()
		throws IOException
	{
		if (sockType == ServerSocketType.InetSocket)
		{
			inetSock.close();
		} else {
			btSock.close();
		}
	}
}
