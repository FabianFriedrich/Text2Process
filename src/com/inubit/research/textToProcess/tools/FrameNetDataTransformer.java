/**
 * copyright
 * Inubit AG
 * Schoeneberger Ufer 89
 * 10785 Berlin
 * Germany
 */
package com.inubit.research.textToProcess.tools;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * removes the subCorpus elements in all framenet lu files 
 * this reduces the size and decreases the laoding time for
 * the NLP tools. as the xml can be read much quicker.
 * @author ff
 *
 */
public class FrameNetDataTransformer {
	
	private static String fnFolder = "FrameNet/fndata-1.5/lu/";
	private static String targetFolder = "FrameNet/fndata-1.5/lu-reduced/";
	private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private static DocumentBuilder db = null; 
	private static XMLSerializer serializer = new XMLSerializer();   
		
	static {
		try {
			db = dbf.newDocumentBuilder();
			File target = new File(targetFolder);
			if(!target.exists()) {
				target.mkdir();
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		parse(new File(fnFolder), ".*");
	}
			
	public static void parse(File directory, String pattern) {
		if (directory.isDirectory() && directory.canRead()) {
			for (File file : directory.listFiles()) {
				if (file.isFile() && !file.getName().endsWith(".xsl") && file.getName().matches(pattern))
					parseFile(file);
			}
		}
	}
	
	private static void parseFile(File file) {		
		System.out.println("Processing: "+file.getName());
		try {			
			Document doc = db.parse(file);
			Element _el = (Element) doc.getDocumentElement();
			NodeList _children = _el.getChildNodes();
			for(int i=0;i<_children.getLength();i++) {
				if(_children.item(i).getNodeName().equals("subCorpus")) {
					_el.removeChild(_children.item(i));
					i--;
				}
			}			
			serializer.setOutputCharStream(new java.io.FileWriter(new File(targetFolder+file.getName())));   
			serializer.serialize(doc); 
			serializer.endDocument();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}	
}
