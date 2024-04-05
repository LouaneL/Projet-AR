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
		initClock();
	}
	
	private void initClock() {
		clock = new Timer();
		clock.schedule(new RemoveSession(this), TIME_BEFORE_DESTRUCTION);
	}
	
	@Override
	public String getId() {
		return m_id;
	}

	@Override
	public Object getValue(String key) {
		initClock();
		return userValue.get(key);
	}

	@Override
	public void setValue(String key, Object value) {
		userValue.put(key, value);
	}
	
	class RemoveSession extends TimerTask {
		
		Session m_s;
		public RemoveSession(Session s) {
			m_s = s; 
		}
		
		@Override
		public void run() {
			m_s.m_server.removeSession(m_s);
		}
		
	}

}
