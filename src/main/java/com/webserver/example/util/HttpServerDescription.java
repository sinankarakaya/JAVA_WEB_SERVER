package com.webserver.example.util;

public class HttpServerDescription {

	/** The number of seconds gts will wait for subsequent socket activity before closing the connection */
	public static int KEEP_ALIVE_TIMEOUT = 15 * 1000;	// 30s
	public static int READ_BUFFER_SIZE = 1024;	// 1024 bytes
	public static int WRITE_BUFFER_SIZE = 1024;	// 1024 bytes
	public static int SERVER_PORT = 80;
	public static String ROOT_FOLDER = "";
	public static boolean USE_SSL;
	public static boolean MULTI_USER=false;
	public static int REQUEST_TIMEOUT = 45; // MS
	public static String CONTEX_PATH="";
	
}
