package httpserver.itf.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.Hashtable;

import httpserver.itf.HttpRequest;
import httpserver.itf.HttpRicmletResponse;

public class HttpRicmletResponseImpl implements HttpRicmletResponse {
	protected HttpServer m_hs;
	protected PrintStream m_ps;
	protected HttpRequest m_req;
	Hashtable<String, String> cookies;

	
	protected HttpRicmletResponseImpl(HttpServer hs, HttpRequest req, PrintStream ps) {
		m_hs = hs;
		m_req = req;
		m_ps = ps;
		cookies = new Hashtable<>();
	}
	
	@Override
	public void setReplyOk() throws IOException {
		m_ps.println("HTTP/1.0 200 OK");
		m_ps.println("Date: " + new Date());
		for (String nameCookie : cookies.keySet()) {
			m_ps.println("Set-Cookie: " + nameCookie + "=" + cookies.get(nameCookie));
		}
		
		// Création de l'id de la Session
		String session_id = ((HttpRicmletRequestImpl) m_req).getCookie("session-id");
		m_ps.println("Set-Cookie: " + "session-id" + "=" + session_id);

		m_ps.println("Server: ricm-http 1.0");
	}

	@Override
	public void setReplyError(int codeRet, String msg) throws IOException {
		m_ps.println("HTTP/1.0 "+codeRet+" "+msg);
		m_ps.println("Date: " + new Date());
		m_ps.println("Server: ricm-http 1.0");
		m_ps.println("Content-type: text/html");
		m_ps.println(); 
		m_ps.println("<HTML><HEAD><TITLE>"+msg+"</TITLE></HEAD>");
		m_ps.println("<BODY><H4>HTTP Error "+codeRet+": "+msg+"</H4></BODY></HTML>");
		m_ps.flush();
	}

	@Override
	public void setContentLength(int length) throws IOException {
		m_ps.println("Content-length: " + length); 
	}

	@Override
	public void setContentType(String type) throws IOException {
		m_ps.println("Content-type: " + type);
	}

	@Override
	public PrintStream beginBody() throws IOException {
		m_ps.println(); 
		return m_ps;
	}

	@Override
	public void setCookie(String name, String value) {
		cookies.put(name, value);
	}

}
