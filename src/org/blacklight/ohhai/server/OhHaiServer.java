package org.blacklight.ohhai.server;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.*;

import android.content.ContentValues;
import android.net.Uri;
import android.telephony.SmsManager;
import org.blacklight.ohhai.xml.*;
import org.blacklight.ohhai.socket.*;

public class OhHaiServer extends Thread {
	private BufferedReader in;
	private PrintWriter out;
	private OhHaiSocket sock;
	private OhHaiService service;
	
	public OhHaiServer (OhHaiSocket sock, OhHaiService service)
		throws IOException
	{
		this.sock = sock;
		this.service = service;
		in = sock.getInputStream();
		out = sock.getOutputStream();
		OhHaiProgram.addMessage("Connection established from " + sock.getAddress(), out, null);
	}
	
	@Override
	public void run()
	{
		Pattern p = Pattern.compile("^\\s*Content-Length:\\s*([0-9]+)\\s*", Pattern.CASE_INSENSITIVE);
		String line = "";
		String xmlRequest = "";
		int contentLength = -1;
		
		try
		{
			do
			{
				line = in.readLine().trim();
				Matcher m = p.matcher(line);
				
				if (m.find())
				{
					contentLength = Integer.parseInt(m.group(1));
				}
			} while (line.length() != 0);
			
			if (contentLength < 0)
			{
				String msg = "Invalid content-length specified: " + contentLength;
				OhHaiProgram.addMessage(msg, out, null);
				return;
			}
		
			char[] requestBytes = new char[contentLength];
			in.read(requestBytes, 0, contentLength);
			xmlRequest = new String(requestBytes);
		}
			
		catch (Exception e)
		{
			String msg = "Error while reading from the client: " + e.toString();
			OhHaiProgram.addMessage(msg, out, e);
			return;
		}
		
		String number = "";
		String text = "";
		
		try
		{
			RequestParser parser = new RequestParser(xmlRequest);
			number = parser.getNumber();
			text = parser.getText();
		}
			
		catch (Exception e)
		{
			String msg = "Error while parsing the client's request: " + e.toString();
			OhHaiProgram.addMessage(msg, out, e);
			return;
		}
		
		try
		{
        	SmsManager sms = SmsManager.getDefault();
        	ArrayList<String> parts = sms.divideMessage(text);
        	sms.sendMultipartTextMessage(number, null, parts, null, null);
        	
        	ContentValues values = new ContentValues();
        	values.put("address", number);
        	values.put("date", new Date().getTime());
        	values.put("read", 1);
        	values.put("body", text);
        	service.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        	service.notifyMessage("OhHai SMS service", "SMS text successfully sent to " + number);
			
        	String msg = "Text successfully sent to " + number;
        	OhHaiProgram.addMessage(msg, out, null, true);
        	
        	in.close();
        	out.close();
        	sock.close();
		}
		
		catch (Exception e)
		{
			String msg = "Error while attempting to send the message to " + number + ": " + e.toString();
			OhHaiProgram.addMessage(msg, out, e, true);
		}
	}
}
