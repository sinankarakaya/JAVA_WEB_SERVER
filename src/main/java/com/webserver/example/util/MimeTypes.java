package com.webserver.example.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class MimeTypes {

	public static Map<String,String> mimeTypes;
	public static final MimeTypes INSTANCE = new MimeTypes();
	
	
	public  MimeTypes() {
		if ( mimeTypes == null ){
			mimeTypes = new HashMap<String,String>();
			
			InputStream inputStream = MimeTypes.class.getResourceAsStream("mimetypes.cfg");
			String mimes="";
			
			try {
				mimes = readFromInputStream(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String[] lines = mimes.split("\n");
			for(int i=0;i<lines.length;i++) {
				String line = lines[i];
				String[] params = line.split(",");
				if ( params.length > 1 ){
					mimeTypes.put(params[0].trim(), params[1].trim());
				}
			}
		}
	}
	
	
	public  String getMimeTypes(String type) {
		if(type==null || type.equals(""))
			return mimeTypes.get("bin");
		else
			return mimeTypes.get(type);
	}
	
	public  boolean contains(String type) {
		if(mimeTypes.containsKey(type))
			return true;
		else
			return false;
	}
	
	private String readFromInputStream(InputStream inputStream)  throws IOException {
	    StringBuilder resultStringBuilder = new StringBuilder();
	    try (BufferedReader br  = new BufferedReader(new InputStreamReader(inputStream))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            resultStringBuilder.append(line).append("\n");
	        }
	    }
	  return resultStringBuilder.toString();
	}
}
