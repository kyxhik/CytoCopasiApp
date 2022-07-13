package org.cytoscape.CytoCopasiApp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;

public class KGMLFixer {
	static String kgmlString; 
	static String[] entries;
	static String[] reactions;
	static String[] entriesNew;
	static String[] reactionsNew;
	static Boolean isConnected;
	File betterKgml;
	public File fixedKgml (File kgmlFile) {
		
		try {
			kgmlString = new Scanner (new File(kgmlFile.getAbsolutePath())).useDelimiter("\\Z").next();
			entries = StringUtils.substringsBetween(kgmlString, "<entry", "</entry>");
			//reactions = StringUtils.substringsBetween(kgmlString, "<reaction", "</reaction>");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int i = 0; i< entries.length; i++) {
			if (entries[i].contains("type=\"line\"")==true || entries[i].contains("type=\"rectangle\"")==true) {
				kgmlString = kgmlString.replace("<entry"+entries[i].toString()+"</entry>", " ");
			}
		}
		for (int i = 0; i< entries.length; i++) {
			
			if (entries[i].contains("type=\"map\"")==true) {
				kgmlString = kgmlString.replace("<entry"+entries[i].toString()+"</entry>", "");
				
			} else if (entries[i].contains("type=\"compound\"")==true) {
				String link = StringUtils.substringBetween(entries[i], "link=\"", "\">");
				String actualName = getActualName(link);
				String weirdName= StringUtils.substringBetween(entries[i], "<graphics name=\"", "\" fgcolor=");
				kgmlString = kgmlString.replace(weirdName, weirdName+":"+actualName);
				//entries[i] = entries[i].replace(weirdName, actualName+"_"+weirdName);
				
				
			} else if (entries[i].contains("type=\"gene\"")==true) {
				String reactionName = StringUtils.substringBetween(entries[i], "reaction=\"", "\"");
				
				String desiredReactionName = StringUtils.substringBetween(entries[i], "<graphics name=\"", ",");
				//entries[i] = entries[i].replace(reactionName, desiredReactionName+"_"+i);
				kgmlString = kgmlString.replace(reactionName, desiredReactionName+"_"+i);
				
			}
			
		}
		
		entriesNew = StringUtils.substringsBetween(kgmlString, "<entry", "</entry>");
		reactionsNew = StringUtils.substringsBetween(kgmlString, "<reaction", "</reaction>");
		for (int i = 0; i< entriesNew.length; i++) {
			
			String compoundName = StringUtils.substringBetween(entriesNew[i], "<graphics name=\"", "\" fgcolor=");
			//System.out.println(compoundName);
			isConnected = false;
			for (int j = 0; j< reactionsNew.length; j++) {
				//System.out.println(reactions[j]);
				if (reactionsNew[j].contains(compoundName)==true) {
					isConnected = true;
				}
			}
			if (isConnected == false && entriesNew[i].contains("type=\"compound\"")==true ) {
				
				kgmlString = kgmlString.replace("<entry"+entriesNew[i].toString()+"</entry>", " ");
			}
		
		}
		
		//String newPath = kgmlFile.getAbsolutePath().replace(kgmlFile.getName(), "new"+kgmlFile.getName());
		//betterKgml = new File(newPath);
		
		try {
			FileWriter kgmlWriter = new FileWriter(kgmlFile);
			kgmlWriter.write(kgmlString);
			kgmlWriter.flush();
			//kgmlWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return kgmlFile;
		
	}
	
	static String getActualName(String link) {
		 URL url;
		 String body = null;
		 String actualName = null;
	
		try {
			url = new URL(link);
		   URLConnection con;
		
			con = url.openConnection();
		
		   con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
		   con.connect();
		   InputStream in = con.getInputStream();
		   String encoding = con.getContentEncoding();  // ** WRONG: should use "con.getContentType()" instead but it returns something like "text/html; charset=UTF-8" so this value must be parsed to extract the actual encoding
		   encoding = encoding == null ? "UTF-8" : encoding;
		   ByteArrayOutputStream baos = new ByteArrayOutputStream();
		   byte[] buf = new byte[8192];
		   int len = 0;
		   while ((len = in.read(buf)) != -1) {
		       baos.write(buf, 0, len);
		   }
		  body = new String(baos.toByteArray(), encoding);	
	
} catch (MalformedURLException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
} catch (IOException e) {
	// TODO Auto-generated catch block
	e.printStackTrace();
}finally {
	
}
		if (body.contains("\"nowrap\">Name")==true) {
		actualName = StringUtils.substringBetween(body, "Name</span></th>\n"
				+ "<td class=\"td21 defd\"><div class=\"cel\"><div class=\"cel\">", "<");
		} else {
			actualName = StringUtils.substringBetween(body,"Composition</span></th>\n"
					+ "<td class=\"td21 defd\"><div class=\"cel\">", "<");
		}
		
		return actualName;
	}
}
