package org.blacklight.ohhai.xml;

public class ParserException extends Exception {
	private static final long serialVersionUID = 12345L;
	private String errMsg;
	
	public ParserException (String msg)
	{
		errMsg = msg;
	}
	
	@Override
	public String toString()
	{
		return errMsg;
	}
}
