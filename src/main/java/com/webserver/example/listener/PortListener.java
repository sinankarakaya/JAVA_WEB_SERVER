package com.webserver.example.listener;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.reflections.Reflections;
import com.webserver.example.annotation.Controller;
import com.webserver.example.annotation.RequestMapping;
import com.webserver.example.http.HttpRequest;
import com.webserver.example.http.HttpResponse;
import com.webserver.example.http.HttpVerb;
import com.webserver.example.http.SocketToRequest;
import com.webserver.example.http.StaticContentHandler;
import com.webserver.example.util.HttpServerDescription;

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
		System.out.println("Searching Controller...");
		this.prepareController();
		_status = true;
		System.out.println("Starting Listener...");
		while(_status) {
			try {
				Socket client = _serverSocket.accept();
				new Thread( () ->  {
					SocketToRequest req = new SocketToRequest(client);
					HttpRequest request =  req.processData();
					HttpResponse response = new HttpResponse(client, true);
					
					
					HttpVerb method = request.getMethod();
					String path = request.getPath();
					Method _method = null;
					
					
					try {
						switch (method) {
							case GET:
								 _method = getControllerMethod(path, RequestMapping.Method.GET);
								break;
							case POST:
								 _method = getControllerMethod(path, RequestMapping.Method.POST);
								break;
							case DELETE:
								 _method = getControllerMethod(path, RequestMapping.Method.DELETE);
								break;
							case PUT:
								 _method = getControllerMethod(path, RequestMapping.Method.PUT);
								break;
							default:
								break;
						}
						if(_method==null) {
							StaticContentHandler.getInstance().perform(request, response, true);
						}else {
							Class<?> _class = _method.getDeclaringClass(); 
							_method.invoke(_class.newInstance(),request, response); 	
						}
					}catch (Exception e) {
						response.setStatusCode(404);
						response.end("");
					}
				}).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopListening() {
		this._status = false;
	}
	
	
	private void prepareController() {
		 Reflections reflections = new Reflections(HttpServerDescription.CONTEX_PATH);
		 Set<Class<?>> annotated =   reflections.getTypesAnnotatedWith(Controller.class);
		
		 for (Class<?> _class : annotated) {
		     Method[] declaredMethods = _class.getDeclaredMethods();
		   
		     for (Method method : declaredMethods) {
		    	
		    	 if (method.isAnnotationPresent(RequestMapping.class)){
		    		RequestMapping rm = method.getAnnotation(RequestMapping.class);
		    		
		    		MethodData methodData = new MethodData();
		    		methodData.path = rm.path();
		    		methodData.requestMethod = rm.method();
		    		methodData.callBackMethod = method;
		    		
		    		switch (rm.method()) {
						case POST:
							_postMapping.put(rm.path(), methodData);
							break;
						case GET:
							_getMapping.put(rm.path(), methodData);
							break;
						case PUT:
							_putMapping.put(rm.path(), methodData);
							break;
						case DELETE:
							_deleteMapping.put(rm.path(),methodData);
							break;
						default:
							break;
					}
		    	}
			}
		}
	}
	
	public static Method getControllerMethod(String url, RequestMapping.Method method) {
		Method _mappingMethod = null ;
		switch (method) {
			case POST:
				if(_postMapping.get(url) != null)
					_mappingMethod = _postMapping.get(url).callBackMethod;
				break;
			case GET:
				if(_getMapping.get(url) != null)
					_mappingMethod = _getMapping.get(url).callBackMethod;
				break;
			case PUT:
				if(_putMapping.get(url) !=null)
					_mappingMethod = _putMapping.get(url).callBackMethod;
				break;
			case DELETE:
				if(_deleteMapping.get(url) !=null)
					_mappingMethod = _deleteMapping.get(url).callBackMethod;
				break;
			default:
				break;
		}
		return _mappingMethod;
	}
	
	public class MethodData {		
		private String path;
		private RequestMapping.Method requestMethod;
		private Method callBackMethod;
	}
}
