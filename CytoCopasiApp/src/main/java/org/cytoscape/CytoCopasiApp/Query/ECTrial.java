package org.cytoscape.CytoCopasiApp.Query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import org.apache.axis.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class ECTrial {
   
    public static void main(String[] args){
	   URL url;
	   String[][] ecData = null;
	   String[] ecNumbers = null;
	   String[] recName = null;
	   String[] synonym = null;
	   String enzymeName = "st6bok";
	try {
		url = new URL("https://www.brenda-enzymes.org/search_result.php?quicksearch=1&noOfResults=10&a=9&W[2]=" +enzymeName + "&T[2]=2&V[8]=1");
	
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
	   String body = new String(baos.toByteArray(), encoding);	
	   
	   
	   ecNumbers = StringUtils.substringsBetween(body, "enzyme.php?ecno=", "\"");
	   System.out.println("ecNumbers: " + ecNumbers);

	   recName = StringUtils.substringsBetween(body, "</div><div class=\"cell\">", "</div>");
	   System.out.println(ecNumbers.length);

	   synonym = StringUtils.substringsBetween(body, "</div>\n"
	   		+ "<div class=\"cell\">","</div>");
	   ecData = new String[3][ecNumbers.length];
	   
	   if (body.contains("Sorry, no results could be found for your query")) {
		   ecData = null;
	   } else {
	   for (int i = 0; i< ecNumbers.length; i++) {
		   ecData[0][i] = ecNumbers[i];
		   System.out.println(ecData[0][i]);
		   ecData[1][i] = recName[i];
		   System.out.println(ecData[1][i]);
		   ecData[2][i] = synonym[i];
		   System.out.println(ecData[2][i]);
		   
	   }
	   }
	   String result = "ecNumber*2.4.99.3#kmValue*0.119#kmValueMaximum*#substrate*CMP-N-acetyl-beta-neuraminate#commentary*pH and temperature not specified in the publication#organism*Homo sapiens#ligandStructureId*87575#literature*758645#!ecNumber*2.4.99.3#kmValue*0.086#kmValueMaximum*#substrate*cytidine-5'-yl-5-(acetamido)-9-(3-[4-chloro-1,2,3-oxadiazol-3-ium-5-olate]benzamido)-3,5,9-trideoxy-beta-D-glycero-D-galacto-non-2-ulopyranosid-2''-yl phosphate#commentary*pH and temperature not specified in the publication#organism*Homo sapiens#ligandStructureId*250293#literature*758645#!ecNumber*2.4.99.3#kmValue*0.385#kmValueMaximum*#substrate*cytidine-5'-yl-5-(acetamido)-9-azido-3,5,9-trideoxy-beta-D-glycero-D-galacto-non-2-ulopyranosid-2''-yl phosphate#commentary*pH and temperature not specified in the publication#organism*Homo sapiens#ligandStructureId*250294#literature*758645#";
	   String[] resultSplit = result.split("!");
	   for (int i = 0 ; i<resultSplit.length; i++) {
		   System.out.println(resultSplit[i]);
	   }
	} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	System.out.println(ecData[0].length);
	
	
   }
}

