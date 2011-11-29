package org.blacklight.ohhai.server;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.*;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
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
		
		// Read Content-Length
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
				in.close();
				out.close();
				sock.close();
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
			out.close();
			return;
		}
		
		// Read the request
		String number = "";
		String text = "";
		
		try
		{
			RequestParser parser = new RequestParser(xmlRequest);
			String curPassword = OhHaiProgram.getPasswd();
			
			// Manage the password
			if (curPassword != null)
			{
				String password = parser.getPassword();
				
				if (password == null)
				{
					OhHaiProgram.addMessage("A password is required in order to use OhHai service " +
						"but no password was provided", out, null);
					
					in.close();
					out.close();
					sock.close();
					return;
				}
				
				if (!password.equals(curPassword))
				{
					OhHaiProgram.addMessage("The provided password is wrong", out, null);
					in.close();
					out.close();
					sock.close();
					return;
				}
			}
			
			// Manage password change
			String newPassword = parser.getSetPassword();
			
			if (newPassword != null)
			{
				try
				{
					OhHaiProgram.changePasswd(newPassword);
					OhHaiProgram.addMessage("Password successfully modified", out, null);
				}
				
				catch (IOException e)
				{
					OhHaiProgram.addMessage("Unable to change the password", out, e);
				}
				
				in.close();
				out.close();
				sock.close();
				return;
			}
			
			// Manage get messages
			String readQuery = parser.getReadQuery();
			
			if (readQuery != null)
			{
				int readCount = parser.getReadCount();
				String orderQuery = parser.getOrderQuery();
				
				if (readQuery.equals("*"))
					readQuery = null;
				
				if (orderQuery == null)
					orderQuery = "date DESC";
				
				Cursor cur = service.getApplicationContext().getContentResolver().query (
					Uri.parse("content://sms/inbox"),
					new String[] { "_id", "thread_id", "address", "person", "date", "body" },
					readQuery,
					null,
					orderQuery
				);
				
				
				if (cur != null)
				{
					try
					{
						cur.moveToFirst();
						int i = 0;
						
						while (cur != null)
						{
							//long msgId = cur.getLong(0);
							//long threadId = cur.getLong(1);
							String address = cur.getString(2);
							//long contactId = cur.getLong(3);
							String timestamp = new SimpleDateFormat("yyyy.MM.dd, HH:mm:ss").format(new Date(cur.getLong(4)));
							String body = cur.getString(5);
							OhHaiProgram.addMessage(
								"\nReceived from: " + address + " at " + timestamp + "\n\t" + body, out, null);
							
							if (!cur.moveToNext() || (readCount > 0 && ++i >= readCount))
								break;
						}
						
						return;
					}
					
					finally
					{
						cur.close();
						in.close();
						out.close();
						sock.close();
					}
				} else {
					return;
				}
			}
			
			// Manage get contact info
			String contactByDisplayName = parser.getContactByDisplayName();
			String contactByNumber = parser.getContactByNumber();
			
			if (contactByDisplayName != null || contactByNumber != null)
			{
				Uri lookup = null;
				
				if (contactByDisplayName != null)
					lookup = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactByDisplayName));
				else
					lookup = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(contactByNumber));
				
				Cursor cur = service.getApplicationContext().getContentResolver().
					query(lookup, new String[]{
						PhoneLookup.DISPLAY_NAME, PhoneLookup.HAS_PHONE_NUMBER, PhoneLookup.NUMBER},
						null, null, null);
				
				try
				{
					while (cur.moveToNext())
					{
				        String dispName = cur.getString(cur.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
				        String hasNumber = cur.getString(cur.getColumnIndexOrThrow(PhoneLookup.HAS_PHONE_NUMBER));
				        String phoneNumber = null;
				        
				        if (!hasNumber.equals("0"))
				        {
				        	phoneNumber = cur.getString(cur.getColumnIndexOrThrow(PhoneLookup.NUMBER));
				        }
				        
				        OhHaiProgram.addMessage("Display name: " + dispName + "\n" +
				        	"Number: " + ((phoneNumber == null) ? "(none)" : phoneNumber) + "\n",
				        	out, null);
					}
				}
				
				finally
				{
					cur.close();
				}
				
				return;
			}
			
			// Manage send SMS
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
