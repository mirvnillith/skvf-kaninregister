package se.skvf.kaninregister.api;

import static java.lang.System.currentTimeMillis;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class SessionManager {

	class Session {
		private final String ownerId;
		private long lastUsed;
		
		Session(String ownerId) {
			this.ownerId = ownerId;
			lastUsed = currentTimeMillis();
		}
		
		boolean is(String ownerId) {
			if (ownerId == null || this.ownerId.equals(ownerId)) {
				lastUsed = currentTimeMillis();
				return true;
			}
			return false;
		}
		
		boolean hasTimedOut() {
			return (currentTimeMillis() - lastUsed) > timeoutSeconds * 1000;
		}
	}
	
	private final Map<String, Session> sessions = new HashMap<>();
	private long timeoutSeconds = 3600;
	
	public synchronized String startSession(String ownerId) {
		purgeSessions();
		
		if (ownerId == null) {
			return null;
		}
		String sessionId = UUID.randomUUID().toString();
		sessions.put(sessionId, new Session(ownerId));
		return sessionId;
	}
	
	public synchronized void endSession(String sessionId) {
		purgeSessions();

		if (sessionId != null) {
			sessions.remove(sessionId);
		}
	}
	
	public synchronized boolean isSession(String sessionId, String ownerId) {
		purgeSessions();
		
		if (sessionId == null) {
			return false;
		}
		
		Session session = sessions.get(sessionId);
		if (session == null) {
			return false;
		}
		
		return session.is(ownerId);
	}

	private void purgeSessions() {
		sessions.entrySet().stream()
				.filter(e -> e.getValue().hasTimedOut())
				.map(Entry::getKey)
				.collect(Collectors.toSet())
				.forEach(sessions::remove);
	}

	void setTimeout(int seconds) {
		timeoutSeconds = seconds;
	}
}
