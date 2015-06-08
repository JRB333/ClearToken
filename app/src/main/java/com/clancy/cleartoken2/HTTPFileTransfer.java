package com.clancy.cleartoken2;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.ByteArrayBuffer;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class HTTPFileTransfer {
		 
	  //private final String PATH = "";

        public static long HTTPGetFileSize(String URLFile, Context ct) 
        {
        	long HTTPFileSize = 0;
            try {
            		URL url = new URL(URLFile);
                    URLConnection ucon = url.openConnection();
                    InputStream is = ucon.getInputStream();
                    BufferedInputStream bis = new BufferedInputStream(is);

                    ByteArrayBuffer baf = new ByteArrayBuffer(50);
                    int current = 0;
                    while ((current = bis.read()) != -1) {
                            baf.append((byte) current);                    }
                    HTTPFileSize = baf.length();

                   // FileOutputStream fos = new FileOutputStream(file);
                   // fos.write(baf.toByteArray());
                   // fos.close(); 
            } 
    		catch(FileNotFoundException e) { 
    			//Toast.makeText(ct, "Ex: "+e.toString(), 2000).show();
    			//Do not display anything for this exception
    		}               
    		catch(Throwable t) { 
    			Toast.makeText(ct, "GetFileSize Ex: "+t.toString(), 2000).show();
    			
    		}   
    		return HTTPFileSize;
        }
        
        public static boolean GetValidation(Context ct)
        {
        	File file = new File("/data/data/com.clancysystems.eventparking/files/","CONFIG.A");  
            if(file.exists())   
            	{return true;}
            Toast.makeText(ct,"No config exists", Toast.LENGTH_LONG).show();       	
        	String phone="blabla";

        	String ConfigJ=HTTPGetPageContent("http://www.cleartoken.com/"+phone+".J", ct);
        	        	
        	if(ConfigJ.substring(0,13).equals("clancysystems"))
        		return true;
        	 Toast.makeText(ct,"No validation exists", Toast.LENGTH_LONG).show();
        	return false;
        }
        
		public static String HTTPGetPageContent(String URLFile, Context ct) {
    			    		
    			String inLine = "ERROR CONNECTING";    			
    			HttpParams httpParameters = new BasicHttpParams();     			
    			try {
    				int timeoutConnection = 10000; 
    				HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);     			
    				int timeoutSocket = 20000; 
    				HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket); 
    			 
    				HttpClient httpclient = new DefaultHttpClient(httpParameters); 

    			//HttpClient httpclient = new DefaultHttpClient();  	      
    				while(URLFile.indexOf(" ")>0){
    					URLFile=URLFile.replace(" ","%20");    	        	
    				}
    				HttpGet httpget = new HttpGet(URLFile);  
    				HttpResponse response;    	        
    	        	
    	            response = httpclient.execute(httpget);    	       	         
    	            HttpEntity entity = response.getEntity();
    	 
    	            if (entity != null) {
    	                InputStream instream = entity.getContent();
    	                inLine = convertStreamToString(instream);
    	                instream.close();
    	            }
    	        } catch(Throwable t) { 
        			Toast.makeText(ct, "GetPageContent Ex: "+t.toString(), 2000).show();
        			inLine = "ERROR CONNECTING";
        		}   
    	        return inLine;
        }
		public static String convertURL(String str) { 
			 
		    String url = null; 
		    try{ 
		    url = new String(str.trim().replace(" ", "%20").replace("&", "%26") 
		            .replace(",", "%2c").replace("(", "%28").replace(")", "%29") 
		            .replace("!", "%21").replace("=", "%3D").replace("<", "%3C") 
		            .replace(">", "%3E").replace("#", "%23").replace("$", "%24") 
		            .replace("'", "%27").replace("*", "%2A").replace("-", "%2D") 
		            .replace(".", "%2E").replace("/", "%2F").replace(":", "%3A") 
		            .replace(";", "%3B").replace("?", "%3F").replace("@", "%40") 
		            .replace("[", "%5B").replace("\\", "%5C").replace("]", "%5D") 
		            .replace("_", "%5F").replace("`", "%60").replace("{", "%7B") 
		            .replace("|", "%7C").replace("}", "%7D")); 
		    }catch(Exception e){ 
		        e.printStackTrace(); 
		    } 
		    return url; 
		} 
        
        
        private static String convertStreamToString(InputStream is) {

            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
     
            String line = null;
            try {
                while ((line = reader.readLine()) != null) {
                    //sb.append(line + "\n");
                	sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }  
    	public static String GetPhoneNumber(Context ct)
    	{
    		String mPhoneNumber = "";
    		//TelephonyManager tMgr =(TelephonyManager) ct.getSystemService(Context.TELEPHONY_SERVICE);
    		TelephonyManager tMgr =(TelephonyManager) ct.getSystemService("phone");
    		mPhoneNumber = tMgr.getLine1Number();
    		if(mPhoneNumber == null)
    		{    			
    		}else
    		{
    			if (mPhoneNumber.equals("")) // get the serial number of the sim as a second best thing
    			{
    				mPhoneNumber = tMgr.getSimSerialNumber();			
    			}
    		}
    		return mPhoneNumber;
    	}
}