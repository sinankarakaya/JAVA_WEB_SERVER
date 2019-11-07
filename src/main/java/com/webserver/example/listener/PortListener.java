package com.webserver.example.listener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import com.webserver.example.annotation.RequestMapping;
import com.webserver.example.http.HttpRequest;
import com.webserver.example.http.SocketToRequest;

public class PortListener {

	private ServerSocket _serverSocket;
	private int _port = 1001;
	private boolean _status = false;
	private static Map<String,MethodData> _postMapping   = new HashMap<String, MethodData>();
	private static Map<String,MethodData> _getMapping    = new HashMap<String, MethodData>();
	private static Map<String,MethodData> _putMapping    = new HashMap<String, MethodData>();
	private static Map<String,MethodData> _deleteMapping = new HashMap<String, MethodData>();
	
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
	
	public class MethodData {		
		private String path;
		private RequestMapping.Method requestMethod;
		private Method callBackMethod;
	}
}
