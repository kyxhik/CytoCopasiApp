package org.cytoscape.CytoCopasiApp.Query;


import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SoapTrial {
	String[] ecNumbers;
	String parameters;
	String organisms;
	String resultString;
	
	public static void main(String[] args) throws Exception{       
		Service service = new Service();
              Call call;
              
		
				call = (Call) service.createCall();
			
              String endpoint = "https://www.brenda-enzymes.org/soap/brenda_server.php";
              String password = "Kayhik_1992";
              MessageDigest md = MessageDigest.getInstance("SHA-256");
              md.update(password.getBytes());
              byte byteData[] = md.digest();
              StringBuffer sb = new StringBuffer();
              StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < byteData.length; i++){
                    String hex = Integer.toHexString(0xff & byteData[i]);
                    if(hex.length()==1) hexString.append('0');
                    hexString.append(hex);
              }  
              call.setTargetEndpointAddress( new java.net.URL(endpoint) );
              
              String parameters = "kyxhik001@myuct.ac.za,"+hexString+",758645";
              call.setOperationName(new QName("http://soapinterop.org/", "getReferenceById"));
              String resultString = (String) call.invoke( new Object[] {parameters} );
              System.out.println(resultString);
      
}}
              
              
             
	             