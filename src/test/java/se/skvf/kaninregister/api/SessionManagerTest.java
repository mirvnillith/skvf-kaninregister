package se.skvf.kaninregister.api;

import static java.lang.Thread.sleep;
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
		assertThat(manager.isSession(sessionId, sessionId)).isFalse();
		assertThat(manager.isSession(ownerId, ownerId)).isFalse();
		assertThat(manager.isSession(ownerId, null)).isFalse();
		assertThat(manager.isSession(null, ownerId)).isFalse();
		assertThat(manager.isSession(null, null)).isFalse();
		
		manager.endSession(sessionId);
		
		assertThat(manager.isSession(sessionId, ownerId)).isFalse();
		assertThat(manager.isSession(sessionId, null)).isFalse();
		assertThat(manager.isSession(sessionId, sessionId)).isFalse();
		assertThat(manager.isSession(ownerId, ownerId)).isFalse();
		assertThat(manager.isSession(ownerId, null)).isFalse();
		assertThat(manager.isSession(null, ownerId)).isFalse();
		assertThat(manager.isSession(null, null)).isFalse();
		
		manager.endSession(null);
	}

	@Test
	public void session_ownerIdForSession() {

		String ownerId = randomUUID().toString();
		String sessionId = manager.startSession(ownerId);

		assertThat(manager.getOwnerIdForSession(sessionId)).isEqualTo(ownerId);
		assertThat(manager.getOwnerIdForSession(null)).isNull();;

		manager.endSession(sessionId);

		assertThat(manager.getOwnerIdForSession(sessionId)).isNull();
	}

	@Test
	public void timeout() throws Exception {
		
		manager.setTimeout(1);
		
		String ownerId = randomUUID().toString();
		String sessionId = manager.startSession(ownerId);
		
		sleep(500);
		
		assertThat(manager.isSession(sessionId, ownerId)).isTrue();
		
		sleep(1100);
		
		assertThat(manager.isSession(sessionId, ownerId)).isFalse();
	}
}
