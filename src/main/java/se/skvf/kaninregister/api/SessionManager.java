package se.skvf.kaninregister.api;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SessionManager {

	private final Map<String, String> sessions = new ConcurrentHashMap<>();
	
	public String startSession(String ownerId) {
		if (ownerId == null) {
			return null;
		}
		String sessionId = UUID.randomUUID().toString();
		sessions.put(sessionId, ownerId);
		return sessionId;
	}
	
	public void endSession(String sessionId) {
		if (sessionId != null) {
			sessions.remove(sessionId);
		}
	}
	
	public boolean isSession(String sessionId, String ownerId) {
		return sessionId != null &&
				sessions.containsKey(sessionId) &&
				(ownerId == null || ownerId.equals(sessions.get(sessionId)));
	}
}
