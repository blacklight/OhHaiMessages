package org.blacklight.ohhai.socket;

import java.io.*;
import java.net.*;
import android.bluetooth.*;

public class OhHaiSocket {
	private enum SocketType  { InetSocket, BluetoothSocket }
	private SocketType sockType;
	private Socket inetSock;
	private BluetoothSocket btSock;
	private BufferedReader in;
	private PrintWriter out;
	
	public OhHaiSocket (Socket sock)
		throws IOException
	{
		sockType = SocketType.InetSocket;
		inetSock = sock;
		in = new BufferedReader (new InputStreamReader (inetSock.getInputStream()));
		out = new PrintWriter (inetSock.getOutputStream(), true);
	}
	
	public OhHaiSocket (BluetoothSocket sock)
		throws IOException
	{
		sockType = SocketType.BluetoothSocket;
		btSock = sock;
		in = new BufferedReader (new InputStreamReader (btSock.getInputStream()));
		out = new PrintWriter (btSock.getOutputStream(), true);
	}
	
	public String getAddress()
	{
		return (sockType == SocketType.InetSocket) ?
			inetSock.getInetAddress().getHostAddress() :
			btSock.getRemoteDevice().getAddress();
	}
	
	public BufferedReader getInputStream()
	{
		return in;
	}
	
	public PrintWriter getOutputStream()
	{
		return out;
	}
	
	public void close()
		throws IOException
	{
		if (sockType == SocketType.InetSocket)
		{
			inetSock.close();
		} else {
			btSock.close();
		}
	}
}
