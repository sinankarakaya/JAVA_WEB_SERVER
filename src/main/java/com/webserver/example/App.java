package com.webserver.example;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

import com.webserver.example.listener.PortListener;

/**
 * @author Sinan KARAKAYA
 *
 */
public class App 
{
	private static final File _ROOT = new File(".");
	
    public static void main( String[] args )
    {
    	
    	PortListener listener = new PortListener(1001);
    	listener.startListening();
    	
    	/*
    	ServerSocket _serverSocket;
    	try {
			_serverSocket = new ServerSocket(8080);
			while(true) {
				Socket client = _serverSocket.accept();
				new Thread( () ->  {
					BufferedReader in ;
					PrintWriter out;
					BufferedOutputStream bOut;
					
					try {
						in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						out = new PrintWriter(client.getOutputStream());
						bOut = new BufferedOutputStream(client.getOutputStream());
						
						String receiveData = in.readLine();
						StringTokenizer parse = new StringTokenizer(receiveData);
						String method = parse.nextToken().toUpperCase();
						
						if(!method.equals("GET") && method.equals("HEAD")) {
							File file = new File(_ROOT,"error.html");
							int length = (int) file.length();
							byte[] data = readData(file,length);
						
							String responceCode = "HTTP/1.1 501 Not Implemented";
							sendResponse(out, bOut, data, length,responceCode);
						}else {
							File file = new File(_ROOT, "index.html");
							int length = (int) file.length();
							if (method.equals("GET")) { // GET method so we return content
								byte[] data = readData(file, length);
								sendResponse(out, bOut, data, length,"HTTP/1.1 200 OK");
							}
						}
					}catch (Exception e) {
						e.printStackTrace();
					}
				}).start();  
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
    }
    
    private static void sendResponse(PrintWriter out,BufferedOutputStream bOut,
    									byte[] fileData,int fileLength,String responseCode) {
    	try {
    		out.println(responseCode);
    		out.println("Server: Java Server");
    		out.println("Date: " + new Date());
    		out.println("Content-type: text/html");
    		out.println("Content-length: " + fileLength);
    		out.println(); 
    		out.flush();
    		
    		bOut.write(fileData, 0, fileLength);
    		bOut.flush();
    	}catch (Exception e) {
			// TODO: handle exception
    		e.printStackTrace();
		}
    	
    }
    
    private static byte[] readData(File file,int length) {
    	FileInputStream fis = null;
    	byte[] data = null;
    	try {
        	data = new byte[length];
        	fis = new FileInputStream(file);
        	fis.read(data);	
    	}catch (Exception e) {
			e.printStackTrace();
		}
    	return data;
    }
}
