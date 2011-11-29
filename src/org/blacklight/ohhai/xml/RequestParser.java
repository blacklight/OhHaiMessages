package org.blacklight.ohhai.xml;

import java.io.StringReader;
import java.io.IOException;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class RequestParser {
	private String dstNumber = null;
	private String msgText = null;
	private String password = null;
	private String setPassword = null;
	private String readQuery = null;
	private String orderQuery = null;
	private String contactByDisplayName = null;
	private String contactByNumber = null;
	private int readCount = 0;
	
	public RequestParser(String xmlString)
		throws IOException, SAXException, ParserConfigurationException, ParserException
	{
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlString));
		boolean hasNumber = false, hasText = false, hasSetPassword = false, hasGetMessages = false;
		
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		
		try
		{
			Element numberNode = (Element) document.getElementsByTagName("number").item(0);
			dstNumber = numberNode.getTextContent();
			hasNumber = true;
		}
		
		catch (Exception e)  {}
		
		try
		{
			Element numberNode = (Element) document.getElementsByTagName("text").item(0);
			msgText = numberNode.getTextContent();
			hasText = true;
		}
		
		catch (Exception e)  {}
		
		try
		{
			Element numberNode = (Element) document.getElementsByTagName("password").item(0);
			password = numberNode.getTextContent();
		}
		
		catch (Exception e)  {}
		
		try
		{
			Element numberNode = (Element) document.getElementsByTagName("setpassword").item(0);
			setPassword = numberNode.getTextContent();
			hasSetPassword = true;
		}
		
		catch (Exception e)  {}
		
		try
		{
			hasGetMessages = true;
			readQuery = null;
			Element numberNode = (Element) document.getElementsByTagName("getmessages").item(0);
			readQuery = numberNode.getTextContent();
		}
		
		catch (Exception e)  { readQuery = null; hasGetMessages = false; }
		
		try
		{
			orderQuery = null;
			Element numberNode = (Element) document.getElementsByTagName("order").item(0);
			orderQuery = numberNode.getTextContent();
		}
		
		catch (Exception e)  { orderQuery = null; }
		
		try
		{
			contactByDisplayName = null;
			Element numberNode = (Element) document.getElementsByTagName("get_contact_info_by_display_name").item(0);
			contactByDisplayName = numberNode.getTextContent();
		}
		
		catch (Exception e)  { contactByDisplayName = null; }
		
		try
		{
			contactByNumber = null;
			Element numberNode = (Element) document.getElementsByTagName("get_contact_info_by_number").item(0);
			contactByNumber = numberNode.getTextContent();
		}
		
		catch (Exception e)  { contactByNumber = null; }
		
		try
		{
			Element numberNode = (Element) document.getElementsByTagName("count").item(0);
			readCount = Integer.parseInt(numberNode.getTextContent());
		}
		
		catch (Exception e)  {}
		
		if (!(hasNumber && hasText) && !hasSetPassword && !hasGetMessages && contactByDisplayName == null && contactByNumber == null)
		{
			throw new ParserException ("The fields number, text, getmessages or setpassword were not correctly filled");
		}
	}
	
	public String getNumber()
	{
		return dstNumber;
	}
	
	public String getText()
	{
		return msgText;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public String getSetPassword()
	{
		return setPassword;
	}
	
	public String getReadQuery()
	{
		return readQuery;
	}
	
	public String getOrderQuery()
	{
		return orderQuery;
	}
	
	public int getReadCount()
	{
		return readCount;
	}
	
	public String getContactByDisplayName()
	{
		return contactByDisplayName;
	}
	
	public String getContactByNumber()
	{
		return contactByNumber;
	}
}
