package httpserver.itf.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import httpserver.itf.HttpResponse;
import httpserver.itf.HttpRicmlet;
import httpserver.itf.HttpRicmletRequest;
import httpserver.itf.HttpRicmletResponse;
import httpserver.itf.HttpSession;

public class HttpRicmletRequestImpl extends HttpRicmletRequest {

	/**
	 * 
	 * @param hs
	 * @param method
	 * @param ressname
	 * @param br Used for the ARG 
	 * @throws IOException
	 */

	static final String DEFAULT_FILE = "index.html";
	static final String PATH_TO_FILE_DIR = ".";
	static final String RICMLETS_PATH = "/ricmlets/";

	public HttpRicmletRequestImpl(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
		super(hs, method, ressname, br);
	}

	@Override
	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getArg(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCookie(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void process(HttpResponse resp) throws Exception {
		String ressname = this.getRessname();
		System.out.println(ressname);
		if (ressname.equals("/FILES/")) {
			ressname = ressname + DEFAULT_FILE;
		} else if (ressname.startsWith(RICMLETS_PATH)) {
			String clasname = ressname.substring(RICMLETS_PATH.length());
			Class<?> clas = Class.forName(clasname);
			HttpRicmlet myRicmlet = (HttpRicmlet) clas.getDeclaredConstructor().newInstance();
			myRicmlet.doGet(this, (HttpRicmletResponse) resp);
		} else {
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

}
