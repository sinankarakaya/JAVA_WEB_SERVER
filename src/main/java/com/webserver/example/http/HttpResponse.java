package com.webserver.example.http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.codec.Charsets;
import com.webserver.example.util.DateUtil;
import com.webserver.example.util.DynamicByteBuffer;
import com.webserver.example.util.HttpServerDescription;
import com.webserver.example.util.HttpUtil;

public class HttpResponse {

	private BufferedReader _in ;
	private PrintWriter _out;
	private BufferedOutputStream _bufferedOutput;
	private final Socket _clientSocket;
	
	private int statusCode = 200;	
	private final Map<String, String> headers = new HashMap<String, String>();
	private boolean headersCreated = false;
	private DynamicByteBuffer responseData = DynamicByteBuffer.allocate(HttpServerDescription.WRITE_BUFFER_SIZE);
	
	
	public HttpResponse(Socket _clientSocket,boolean keepAlive) {
		this._clientSocket = _clientSocket;
		try {
			_in = new BufferedReader(new InputStreamReader(_clientSocket.getInputStream()));
			_out = new PrintWriter(_clientSocket.getOutputStream());
			_bufferedOutput = new BufferedOutputStream(_clientSocket.getOutputStream());	
			headers.put("Server", "JAVA SERVER/0.1-SNAPSHOT");
			headers.put("Date", DateUtil.getCurrentAsString());
			headers.put("Connection", keepAlive ? "Keep-Alive" : "Close");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public  void setStatusCode(int sc) {
		statusCode = sc;
	}
	
	public  void setHeader(String header, String value) {
		headers.put(header, value);
	}
	
	private  void setEtagAndContentLength(String length) {
		if(statusCode != 200) {
			setHeader("Content-Length", "0");
		}else {
			setHeader("Content-Length", length);
		}
	}
	
	private  String createInitalLineAndHeaders() {
		StringBuilder sb = new StringBuilder(HttpUtil.createInitialLine(statusCode));
		for (Map.Entry<String, String> header : headers.entrySet()) {
			sb.append(header.getKey());
			sb.append(": ");
			sb.append(header.getValue());
			sb.append("\r\n");
		}
		sb.append("\r\n");
		return sb.toString();
	}

	public  void end(String data) {
		byte[] bytes = data.getBytes(Charsets.UTF_8);
		
		if (!headersCreated) {
			setEtagAndContentLength(String.valueOf(bytes.length));
			String initial = createInitalLineAndHeaders();		
			_out.println(initial);
			_out.println();
			_out.flush();
			headersCreated = true;
		}
		if(statusCode == 200) {
			_out.println(data);
			_out.println();
			_out.flush();	
		}
	}
	

}
