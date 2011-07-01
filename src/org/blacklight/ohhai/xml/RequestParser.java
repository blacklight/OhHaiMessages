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
	
	public RequestParser(String xmlString)
		throws IOException, SAXException, ParserConfigurationException, ParserException
	{
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlString));
		boolean hasNumber = false, hasText = false, hasSetPassword = false;
		
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
		
		if (!(hasNumber && hasText) && !hasSetPassword)
		{
			throw new ParserException ("The fields number, text or setpassword were not correctly filled");
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
}
