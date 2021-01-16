package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class LogoutTest extends BunnyRegistryApiTest {

	@Test
	public void logout() throws IOException {
		
		String sessionId = mockSession(randomUUID().toString());
		
		api.logout();
		
		verify(sessions).endSession(sessionId);
	}
	
	@Test
	public void logout_noSession() throws IOException {
		
		api.logout();
		
		verify(sessions).endSession(null);
	}

}
