package httpserver.itf;

import java.io.BufferedReader;
import java.io.IOException;

import httpserver.itf.impl.HttpServer;

/*
 * Interface provided by an object representing a dynamic HTTP request 
 * A dynamic HTTP request involves an ricmlet
 */
public abstract class HttpRicmletRequest extends HttpRequest {
	protected BufferedReader m_br;
	public HttpRicmletRequest(HttpServer hs, String method, String ressname, BufferedReader br) throws IOException {
		super(hs,method,ressname); 
		m_br = br;
	}

	/*
	 * Returns the BufferedReader object attached to the current client
	 * used to read the cookies 
	 */
	public BufferedReader getBr() {
		return m_br;
	}

	/*
	 * Returns the session object attached to the current client
	 * Create a session if no session exist
	 */
	abstract public HttpSession getSession();
	
	/*
	 * Returns the value for the argument of the given name
	 * Returns null if there is no argument with that name
	 */
	abstract public String getArg(String name);

	/*
	 * Returns the value for the cookie of the given name
	 * Returns null if there is no cookie with that name
	 */
	abstract public String getCookie(String name);
	
	
}
