package httpserver.itf.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.UUID;

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
	 * @param br Used for the cookies
	 * @throws IOException
	 */

	static final String DEFAULT_FILE = "index.html";
	static final String RICMLETS_PATH = "/ricmlets/";
	Hashtable<String, String> cookies;
	Hashtable<String, String> args;

	public HttpRicmletRequestImpl(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
		super(hs, method, ressname, br);
		cookies = new Hashtable<>();
		args = new Hashtable<>();
		if (ressname.endsWith("/")) { // if the ressource is a directory
			m_ressname = ressname + DEFAULT_FILE;
		}
		setupArgs();
		setupCookies();
		if (!cookies.containsKey("session-id")) {
			String session_id = UUID.randomUUID().toString();
			cookies.put("session-id", session_id);
		}
	}
	
	private void setupArgs(){
		String[] ressnameWithoutRicmlets = this.getRessname().split("\\?");
		if (ressnameWithoutRicmlets.length == 1) return;
		String[] args = ressnameWithoutRicmlets[1].split("&");
		for (String arg : args) {
			String[] argSplit = arg.split("=");
			this.args.put(argSplit[0],argSplit[1]);
		}
	}

	private void setupCookies() {
		try {
			String line = m_br.readLine();
			while (!line.equals("")) {
				if (line.startsWith("Cookie: ")) {
					String[] newCookies = line.split(": ")[1].split("; ");
					for (String cookie : newCookies) {
						String[] cookieSplit = cookie.split("=");
						cookies.put(cookieSplit[0], cookieSplit[1]);
					}
				}
				line = m_br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public HttpSession getSession() {		
		String session_id = getCookie("session-id");
		return m_hs.getSession(session_id);
	}

	@Override
	public String getArg(String name) {
		return args.get(name);
	}

	@Override
	public String getCookie(String name) { 
		return cookies.get(name);
	}

	@Override
	public void process(HttpResponse resp) throws Exception {
		if (m_ressname.startsWith(RICMLETS_PATH)) { // if the ressource is a ricmlet

			// En changeant les / en . on obtient le nom de la classe
			String ressnameWithoutRicmlets = m_ressname.substring(RICMLETS_PATH.length());
			String ressnameBeforeSplit = ressnameWithoutRicmlets.split("\\?")[0];
			String clasname = ressnameBeforeSplit.replace( "/", ".");
			HttpRicmlet myRicmlet = m_hs.getInstance(clasname);
			myRicmlet.doGet(this, (HttpRicmletResponse) resp);
			return;
		} else {
			FileInputStream fis = null;
			try {
				// Open la ressource à partir du dossier FILES avec le IO
				fis = new FileInputStream(m_hs.getFolder().toString() + m_ressname);
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
			ps.flush();
			fis.close();
		}
	}
}
