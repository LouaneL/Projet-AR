package httpserver.itf.impl;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import httpserver.itf.HttpRequest;
import httpserver.itf.HttpResponse;

/*
 * This class allows to build an object representing an HTTP static request
 */
public class HttpStaticRequest extends HttpRequest {
	static final String DEFAULT_FILE = "index.html";

	public HttpStaticRequest(HttpServer hs, String method, String ressname) throws IOException {
		super(hs, method, ressname);
		if (ressname.endsWith("/")) { // if the ressource is a directory
			
			m_ressname = ressname + DEFAULT_FILE;
			
		} 
	}

	public void process(HttpResponse resp) throws Exception {
		FileInputStream fis = null;
		try {
			// Open la ressource à partir du dossier FILES avec le IO
			fis = new FileInputStream(m_hs.getFolder() + m_ressname);
		} catch (FileNotFoundException e) {
			// close stream
			resp.setReplyError(404, "Not Found");		
			return;
		}

		resp.setReplyOk();
		resp.setContentLength(fis.available());
		resp.setContentType(getContentType(m_ressname));

		PrintStream ps = resp.beginBody();
		ps.write(fis.readAllBytes());
		fis.close();
	}

}
