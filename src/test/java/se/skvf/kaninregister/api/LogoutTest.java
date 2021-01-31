package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

public class LogoutTest extends BunnyRegistryApiTest {

	@Mock
	private HttpServletResponse response;
	@Captor
	private ArgumentCaptor<Cookie> cookie;
	
	@Test
	public void logout() throws IOException {
		
		String sessionId = mockSession(randomUUID().toString());
		
		api.logout();
		
		verify(sessions).endSession(sessionId);
		verify(response).addCookie(cookie.capture());
		assertThat(cookie.getValue())
			.satisfies(c -> assertEquals(sessionId, c.getValue()))
			.satisfies(c -> assertEquals(0, c.getMaxAge()))
			.satisfies(c -> assertEquals(api.getClass().getSimpleName(), c.getName()));
	}
	
	@Test
	public void logout_noSession() throws IOException {
		
		api.logout();
		
		verify(sessions).endSession(null);
		verifyNoMoreInteractions(response);
	}

}
