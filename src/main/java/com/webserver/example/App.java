package com.webserver.example;



import com.webserver.example.listener.PortListener;
import com.webserver.example.util.HttpServerDescription;

/**
 * @author Sinan KARAKAYA
 *
 */
public class App 
{
	private static String _resources="";
	private static int _port = 1001;
	
    public static void main( String[] args ){
    	
    	for (String arg : args) {
			String[] param = arg.split("=");
			if( (param[0]).contains("--port") ) {
				_port = Integer.valueOf(param[1]);
			}
			if( (param[0].contains("--resources")) ) {
				_resources = param[1];
			}
		}
    	
    	HttpServerDescription.ROOT_FOLDER = _resources;
    	HttpServerDescription.SERVER_PORT = _port;
    	
    	PortListener listener = new PortListener(_port);
    	listener.startListening();
    }
}
