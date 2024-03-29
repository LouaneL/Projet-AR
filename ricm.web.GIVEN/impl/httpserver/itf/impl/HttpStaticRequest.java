package httpserver.itf.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import httpserver.itf.HttpRequest;
import httpserver.itf.HttpResponse;

/*
 * This class allows to build an object representing an HTTP static request
 */
public class HttpStaticRequest extends HttpRequest {
	static final String DEFAULT_FILE = "index.html";
	static final String PATH_TO_FILE_DIR = ".";

	public HttpStaticRequest(HttpServer hs, String method, String ressname) throws IOException {
		super(hs, method, ressname);
	}

	public void process(HttpResponse resp) throws Exception {
		String ressname = this.getRessname();
		System.out.println(ressname);
		if (ressname.equals("/FILES/")) {
			ressname = ressname + DEFAULT_FILE;
		}
		
		BufferedReader br = null;
		FileInputStream fis = null;
		try {
			// Open la ressource à partir du dossier FILES avec le IO
			fis = new FileInputStream(PATH_TO_FILE_DIR + ressname);
			InputStreamReader isr = new InputStreamReader(fis);
			br = new BufferedReader(isr);
		} catch (FileNotFoundException e) {
			// close stream
			resp.setReplyError(404, "Not Found");		
			return;
		}

		resp.setReplyOk();
		resp.setContentType(getContentType(ressname));

		
		if (getContentType(ressname).contains("text")) {
			String line = br.readLine();
			String content = "";
			while (line != null) {
				content += line;
				line = br.readLine();
			}

			resp.setContentLength(content.length());
			PrintStream pt = resp.beginBody();
			pt.print(content);

		} else {
			resp.setContentLength(fis.available());
			PrintStream pt = resp.beginBody();
			byte[] buffer = new byte[1024];
			int nbRead;
			while ((nbRead = fis.read(buffer)) != -1) {
				pt.write(buffer, 0, nbRead);
			}
		}

		// close stream
		br.close();
		fis.close();
	}

}
