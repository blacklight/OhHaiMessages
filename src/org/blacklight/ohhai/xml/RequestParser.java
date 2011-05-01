package org.blacklight.ohhai.xml;

import java.io.StringReader;
import java.io.IOException;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;

public class RequestParser {
	private String dstNumber;
	private String msgText;
	
	public RequestParser(String xmlString)
		throws IOException, SAXException, ParserConfigurationException, ParserException
	{
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlString));
		
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		
		try
		{
			Element numberNode = (Element) document.getElementsByTagName("number").item(0);
			dstNumber = numberNode.getTextContent();
		}
		
		catch (Exception e)
		{
			throw new ParserException("Error while parsing the <number> tag in the client's request: " + e.toString());
		}
		
		try
		{
			Element numberNode = (Element) document.getElementsByTagName("text").item(0);
			msgText = numberNode.getTextContent();
		}
		
		catch (Exception e)
		{
			throw new ParserException("Error while parsing the <text> tag in the client's request: " + e.toString());
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
}
