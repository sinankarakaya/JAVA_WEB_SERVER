package com.webserver.example.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SocketToRequest {
	
	private BufferedReader in ;
	
	private Socket _clientSocket;
	
	public SocketToRequest(Socket _clientSocket) {
		// TODO Auto-generated constructor stub
		this._clientSocket = _clientSocket;
		try {
			this.in = new BufferedReader(new InputStreamReader(this._clientSocket.getInputStream()));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public HttpRequest processData() {
		HttpRequest request=null;
		try {
			String raw = in.readLine();
			if(raw!=null) {
				request = HttpRequest.of(raw);
				System.out.println(request);	
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	
	
}
