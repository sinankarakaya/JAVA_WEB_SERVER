package com.webserver.example.http;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.net.URLCodec;
import com.google.common.collect.ImmutableMultimap;
import com.webserver.example.util.ArrayUtil;

public class HttpRequest {

	private final String requestLine;
	private final HttpVerb method;
	private final String requestedPath;	// correct name?
	private final String version; 
	private Map<String, String> headers;
	private final String path;
	
	private ImmutableMultimap<String, String> parameters;
	private String body;
	private boolean keepAlive;
	private InetAddress remoteHost;
	private InetAddress serverHost;
	private int remotePort;
	private int serverPort;
	private static URLCodec urlcodec = new URLCodec("utf-8");

	/** Regex to parse HttpRequest Request Line */
	public static final Pattern REQUEST_LINE_PATTERN = Pattern.compile(" ") ;
	/** Regex to parse out QueryString from HttpRequest */
	public static final Pattern QUERY_STRING_PATTERN = Pattern.compile("\\?") ;
	/** Regex to parse out parameters from query string */
	public static final Pattern PARAM_STRING_PATTERN = Pattern.compile("\\&|;");  //Delimiter is either & or ;
	/** Regex to parse out key/value pairs */
	public static final Pattern KEY_VALUE_PATTERN = Pattern.compile("=");
	/** Regex to parse raw headers and body */
	public static final Pattern RAW_VALUE_PATTERN = Pattern.compile("\\r\\n\\r\\n"); //TODO fix a better regexp for this
	/** Regex to parse raw headers from body */
	public static final Pattern HEADERS_BODY_PATTERN = Pattern.compile("\\r\\n");
	/** Regex to parse header name and value */
	public static final Pattern HEADER_VALUE_PATTERN = Pattern.compile(": ");
	
	/**
	 * Creates a new HttpRequest 
	 * @param requestLine The Http request text line
	 * @param headers The Http request headers
	 */
	public  HttpRequest(String requestLine, Map<String, String> headers) {
		this.requestLine = requestLine;
		String[] elements = REQUEST_LINE_PATTERN.split(requestLine);
		method = HttpVerb.valueOf(elements[0]);
		String[] pathFrags = QUERY_STRING_PATTERN.split(elements[1]);
		requestedPath = pathFrags[0];
		version = elements[2];
		this.headers = headers;	
		body = null;
		initKeepAlive();
		parameters = parseParameters(elements[1]);
		
		StringTokenizer parse = new StringTokenizer(requestLine);
		String method = parse.nextToken();
		String path = parse.nextToken();
		this.path = path;
		
	}
	
	public static  HttpRequest of(String raw) {
		try {
			//String raw = new String(buffer.array(), 0, buffer.limit(),Charsets.ISO_8859_1);
			
			String[] headersAndBody = RAW_VALUE_PATTERN.split(raw); 
			String[] headerFields = HEADERS_BODY_PATTERN.split(headersAndBody[0]);
			headerFields = ArrayUtil.dropFromEndWhile(headerFields, "");

			String requestLine = headerFields[0];
			Map<String, String> generalHeaders = new HashMap<String, String>();
			for (int i = 1; i < headerFields.length; i++) {
				String[] header = HEADER_VALUE_PATTERN.split(headerFields[i]);
				generalHeaders.put(header[0].toLowerCase(), header[1]);
			}

			String body = "";
			for (int i = 1; i < headersAndBody.length; ++i) { //First entry contains headers
				body += headersAndBody[i]+"~";
			}
			
			if (requestLine.contains("POST")) {
				int contentLength = Integer.parseInt(generalHeaders.get("content-length"));
				if (contentLength > body.length()) {
					return new PartialHttpRequest(requestLine, generalHeaders, body);
				}else {
					body = urlcodec.decode(body);
				}
			}else {
				requestLine = urlcodec.decode(requestLine);
			}
			
			return new HttpRequest(requestLine, generalHeaders, body);
		} catch (Exception t) {
			//System.out.println(t);
			return MalFormedHttpRequest.instance;
		}
	}
	
	/**
	 * Creates a new HttpRequest
	 * @param requestLine The Http request text line
	 * @param headers The Http request headers
	 * @param body The Http request posted body
	 */
	public  HttpRequest(String requestLine, Map<String, String> headers, String body) {
		this(requestLine, headers);
		this.body = body;
	}
	
	
	public static  HttpRequest continueParsing(ByteBuffer buffer, PartialHttpRequest unfinished) {
		try {
			String nextChunk = new String(buffer.array(), 0, buffer.limit(),Charsets.ISO_8859_1);
			unfinished.appendBody(nextChunk);
			
			int contentLength = Integer.parseInt(unfinished.getHeader("Content-Length"));
			if (contentLength > unfinished.getBody().length()) {
				return unfinished;
			} else {
				
				String body = unfinished.getBody();
				String contentType = unfinished.getHeader("Content-Type");
				if(!contentType.startsWith("multipart/form-data"))
					body = urlcodec.decode(body);
				
				unfinished.setBody(body);
				return new HttpRequest(unfinished.getRequestLine(), unfinished.getHeaders(), unfinished.getBody());
			}
		}catch (Exception t) {
			System.out.println(t.getMessage());
			return MalFormedHttpRequest.instance;
		}
	}
	
	
	private  void initKeepAlive() {
		String connection = getHeader("Connection");
		if ("keep-alive".equalsIgnoreCase(connection)) { 
			keepAlive = true;
		} else if ("close".equalsIgnoreCase(connection) || requestLine.contains("1.0")) {
			keepAlive = false;
		} else {
			keepAlive = true;
		}
	}
	
	public  String getHeader(String name) {
		return headers.get(name.toLowerCase());
	}
	
	private  ImmutableMultimap<String, String> parseParameters(String requestLine) {
		ImmutableMultimap.Builder<String, String> builder = ImmutableMultimap.builder();
		String[] str = QUERY_STRING_PATTERN.split(requestLine);
		
		//Parameters exist
		if (str.length > 1) {
			String[] paramArray = PARAM_STRING_PATTERN.split(str[1]); 
			for (String keyValue : paramArray) {
				String[] keyValueArray = KEY_VALUE_PATTERN.split(keyValue);				
				//We need to check if the parameter has a value associated with it.
				if (keyValueArray.length > 1) {
					builder.put(keyValueArray[0], keyValueArray[1]); //name, value
				}
			}
		}
		return builder.build();
	}

	public  Collection<String> getParameterValues(String name) {
		return parameters.get(name);
	}
	
	public  boolean isKeepAlive() {
		return keepAlive;
	}
	
	public  String getRequestLine() {
		return requestLine;
	}
	
	public  String getRequestedPath() {
		return requestedPath;
	}

	public  String getVersion() {
		return version;
	}
	
	public  Map<String, String> getHeaders() {
		return Collections.unmodifiableMap(headers);
	}
	
	
	public  HttpVerb getMethod() {
		return method;
	}
	
	public String getPath() {
		return path;
	}
	
	@Override
	public  String toString() {
		String result = "METHOD: " + method + "\n";
		result += "VERSION: " + version + "\n";
		result += "PATH: " + requestedPath + "\n";
		
		result += "--- HEADER --- \n";
		for (String key : headers.keySet()) {
			String value = headers.get(key);
			result += key + ":" + value + "\n";
		}
		
		result += "--- PARAMETERS --- \n";
		for (String key : parameters.keySet()) {
			Collection<String> values = parameters.get(key);
			for (String value : values) {
				result += key + ":" + value + "\n";
			}
		}
		return result;
	}
}
