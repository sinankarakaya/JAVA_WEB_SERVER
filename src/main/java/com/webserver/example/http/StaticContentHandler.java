package com.webserver.example.http;

import java.io.File;
import com.webserver.example.util.HttpServerDescription;
import com.webserver.example.util.MimeTypes;

public class StaticContentHandler {

	private  final static StaticContentHandler INSTANCE = new StaticContentHandler();
	private  final MimeTypes mimeTypes = MimeTypes.INSTANCE;
	
	public static  StaticContentHandler getInstance() {
		return INSTANCE;
	}
	
	public void perform(final HttpRequest request, final HttpResponse response, boolean hasBody) throws Exception  {
		final String root = HttpServerDescription.ROOT_FOLDER;
		final String path = request.getPath();
		final File file = new File(root+"/"+path.substring(1));
		
		if (!file.exists()) {
			response.setStatusCode(404);
			response.end("");
			return;
		} else if (!file.isFile()) {
			response.setStatusCode(403);
			response.end("");
			return;
		}
		
		final long lastModified = file.lastModified();
		response.setHeader("Last-Modified", String.valueOf(lastModified));
		response.setHeader("Cache-Control", "max-age=360000");
		
		String mimeType = mimeTypes.getMimeTypes("bin");
		
		if (file.getName().indexOf('.') > -1) {
			String ext = file.getName().substring(file.getName().lastIndexOf('.') + 1).toLowerCase();
			if (mimeTypes.contains(ext)) {
				mimeType = mimeTypes.getMimeTypes(ext);
			}
	    }
		
		response.setHeader("Content-Type", mimeType);
		final String ifModifiedSince = request.getHeader("If-Modified-Since");
		if (ifModifiedSince != null) {
			long ims = Long.parseLong(ifModifiedSince);
			if (lastModified <= ims) {
				response.setStatusCode(304);//Not Modified
				response.end("");
				return;
			}
		}
		
		if(hasBody) {
			response.setStatusCode(200);
			response.sendFile(file);
			return;
		}
	}
}
