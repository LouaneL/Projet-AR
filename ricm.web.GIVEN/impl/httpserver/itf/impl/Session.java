package httpserver.itf.impl;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import httpserver.itf.HttpSession;

public class Session implements HttpSession {
	public static final int TIME_BEFORE_DESTRUCTION = 5 * 1000;	
	
	String m_id;
	HashMap<String, Object> userValue;
	Timer clock;
	HttpServer m_server; 

	public Session(String id,HttpServer server) {
		m_id = id;
		userValue = new HashMap<>();
		m_server = server;
		clock = new Timer();
		clock.schedule(new RemoveSession(this), TIME_BEFORE_DESTRUCTION);
	}
	
	private void reinitClock() {
		clock.cancel();
		clock = new Timer();
		clock.schedule(new RemoveSession(this), TIME_BEFORE_DESTRUCTION);
	}
	
	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public Object getValue(String key) {
		reinitClock();
		return userValue.get(key);		
	}

	@Override
	public void setValue(String key, Object value) {
		reinitClock();
		userValue.put(key, value);
	}
	
	private class RemoveSession extends TimerTask {
		
		Session m_s;
		public RemoveSession(Session s) {
			m_s = s; 
		}
		
		@Override
		public void run() {
			m_s.m_server.removeSession(m_s.m_id);
		}
		
	}

}
