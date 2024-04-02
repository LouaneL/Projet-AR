package httpserver.itf.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
	 * @param br Used for the cookies
	 * @throws IOException
	 */

	static final String DEFAULT_FILE = "index.html";
	static final String RICMLETS_PATH = "/ricmlets/";

	public HttpRicmletRequestImpl(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
		super(hs, method, ressname, br);
		if (ressname.endsWith("/")) { // if the ressource is a directory

			m_ressname = ressname + DEFAULT_FILE;

		} 
	}

	@Override
	public HttpSession getSession() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getArg(String name) {
		String[] ressnameWithoutRicmlets = this.getRessname().split("\\?");
		String[] args = ressnameWithoutRicmlets[1].split("&");
		for (String arg : args) {
			String[] argSplit = arg.split("=");
			if (argSplit[0].equals(name)) {
				return (argSplit.length == 1) ? "" : argSplit[1];
			}
		}
		return null;
	}

	@Override
	public String getCookie(String name) { 
		try {
			String cookie = m_br.readLine();
			while (cookie != null) {
				String[] cookieSplit = cookie.split("=");
				if (cookieSplit[0].equals(name)) {
					return (cookieSplit.length == 1) ? "" : cookieSplit[1];
				}
				cookie = m_br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
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
				System.out.println(m_hs.getFolder().toString() + m_ressname);
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


	private void updateCookie() {
		try {
			String line = m_br.readLine();
			while (line != null) {
				if (line.startsWith("Set-Cookie: ")) {
					String[] cookies = line.split(": ")[1].split(";");
					for (String cookie : cookies) {
						String[] cookieSplit = cookie.split("=");
						if (cookieSplit.length == 2) {
							addCookie(cookieSplit[0], cookieSplit[1]);
						} else {
							removeCookie(cookieSplit[0]);
						}
					}
				}
				line = m_br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Add a cookie to the cookies.txt file
	 * 
	 * @param name
	 * @param value
	 */
	private void addCookie(String name, String value) {
		// Ouvrir le fichier de cookie et ajouter le cookie
		// Lire toute les lignes du fichier
		// Si le cookie existe déjà, le remplacer
		// Sinon ajouter le cookie à la fin 
		File file = new File(m_hs.getFolder().toString() + "/cookies.txt");
		String cookie = name + "=" + value;
		try {
			FileWriter fw = new FileWriter(file);
			String line = m_br.readLine();
			boolean cookieExist = false;
			while (line != null) {
				if (line.startsWith(name)) {
					fw.write(cookie + "\n");
					cookieExist = true;
				} else {
					fw.write(line + "\n");
				}
				line = m_br.readLine();
			}
			if (!cookieExist)
				fw.write(cookie + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeCookie(String name) {
		File file = new File(m_hs.getFolder().toString() + "/cookies.txt");
		try {
			FileWriter fw = new FileWriter(file, true);
			String line = m_br.readLine();
			while (line != null) {
				if (!line.startsWith(name)) {
					fw.write(line + "\n");
				}
				line = m_br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
