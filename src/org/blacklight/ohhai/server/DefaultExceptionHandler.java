package org.blacklight.ohhai.server;

import java.lang.Thread.UncaughtExceptionHandler;
import java.io.*;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {
	private UncaughtExceptionHandler handler;
	private static final String defaultStackTraceFile = "/mnt/sdcard/OhHaiMessagesStacktrace.txt";
	
	public DefaultExceptionHandler ()  {}
	
	@Override
	public void uncaughtException (Thread t, Throwable e)
	{
		StringWriter result = new StringWriter();
		PrintWriter writer = new PrintWriter(result);
		e.printStackTrace(writer);
		
		String stacktrace = result.toString();
		writer.close();
		
		try
		{
			BufferedWriter file = new BufferedWriter(
				new FileWriter(defaultStackTraceFile)
			);
			
			file.write(stacktrace);
			file.flush();
			file.close();
		}
		
		catch (Exception ex)  {}
		
		finally
		{
			handler.uncaughtException(t, e);
			System.exit(1);
		}
	}
}
