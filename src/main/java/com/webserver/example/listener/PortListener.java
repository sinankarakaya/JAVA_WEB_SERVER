package com.webserver.example.listener;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.webserver.example.http.HttpRequest;
import com.webserver.example.http.SocketToRequest;

public class PortListener {

	private ServerSocket _serverSocket;
	private int _port = 1001;
	private boolean _status = false;
	
	public PortListener(int _port) {
		try {
			System.out.println("Creating Server Socket...");
			this._port = _port;
			_serverSocket = new ServerSocket(_port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void startListening() {
		System.out.println("Starting Listener...");
		_status = true;
		while(_status) {
			try {
				Socket client = _serverSocket.accept();
				new Thread( () ->  {
					SocketToRequest req = new SocketToRequest(client);
					HttpRequest request =  req.processData();
					
				}).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopListening() {
		this._status = false;
	}
}
