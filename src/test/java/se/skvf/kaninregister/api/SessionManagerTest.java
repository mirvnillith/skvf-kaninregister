package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SessionManagerTest {

	private SessionManager manager;
	
	@BeforeEach
	public void setup() {
		manager = new SessionManager();
	}
	
	@Test
	public void session() {
		
		assertThat(manager.startSession(null)).isNull();
		
		String ownerId = randomUUID().toString();
		String sessionId = manager.startSession(ownerId);
		
		assertThat(manager.isSession(sessionId, ownerId)).isTrue();
		assertThat(manager.isSession(sessionId, null)).isTrue();
		assertThat(manager.isSession(ownerId, ownerId)).isFalse();
		assertThat(manager.isSession(ownerId, null)).isFalse();
		assertThat(manager.isSession(null, ownerId)).isFalse();
		assertThat(manager.isSession(null, null)).isFalse();
		
		manager.endSession(sessionId);
		
		assertThat(manager.isSession(sessionId, ownerId)).isFalse();
		assertThat(manager.isSession(sessionId, null)).isFalse();
		assertThat(manager.isSession(ownerId, ownerId)).isFalse();
		assertThat(manager.isSession(ownerId, null)).isFalse();
		assertThat(manager.isSession(null, ownerId)).isFalse();
		assertThat(manager.isSession(null, null)).isFalse();
		
		manager.endSession(null);
	}
}
