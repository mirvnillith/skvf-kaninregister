package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class LoginTest extends BunnyRegistryApiTest {

	@Mock
	private HttpServletResponse response;
	@Captor
	private ArgumentCaptor<Cookie> cookie;
	
	@Test
	public void login_email() throws IOException {
		
		String password = randomUUID().toString();
		Owner owner = mockOwner();
		owner.setPassword(password);
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		String sessionId = randomUUID().toString();
		when(sessions.startSession(owner.getId())).thenReturn(sessionId);
		
		LoginDTO dto = new LoginDTO();
		dto.setEmail(owner.getEmail());
		dto.setPassword(password);
		api.login(dto);
		
		assertThat(filterArgument.getValue().get("E-post"))
			.accepts(dto.getEmail());
		verify(response).addCookie(cookie.capture());
		assertThat(cookie.getValue())
			.satisfies(c -> assertEquals(api.getClass().getSimpleName(), c.getName()))
			.satisfies(c -> assertEquals(sessionId, c.getValue()));
	}
	
	@Test
	public void login_incorrectPassword() throws IOException {
		
		String password = randomUUID().toString();
		Owner owner = mockOwner();
		owner.setPassword(password);
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		String sessionId = randomUUID().toString();
		when(sessions.startSession(owner.getId())).thenReturn(sessionId);
		
		LoginDTO dto = new LoginDTO();
		dto.setEmail(owner.getEmail());
		dto.setPassword(password + password);
		assertError(UNAUTHORIZED, () -> api.login(dto));
	}
	
	@Test
	public void login_error() throws IOException {
		
		when(registry.findOwners(filterArgument.capture())).thenThrow(IOException.class);
		
		LoginDTO dto = new LoginDTO();
		dto.setEmail("");
		assertError(INTERNAL_SERVER_ERROR, () -> api.login(dto));
	}
	
	@Test
	public void login_unknownEmail() throws IOException {
		
		LoginDTO dto = new LoginDTO();
		dto.setEmail("");
		assertError(UNAUTHORIZED, () -> api.login(dto));
	}
	
	@Test
	public void login_unknownBunny() throws IOException {
		
		LoginDTO dto = new LoginDTO();
		dto.setBunny("");
		assertError(UNAUTHORIZED, () -> api.login(dto));
	}
	
	@Test
	public void login_none() throws IOException {
		
		assertError(UNAUTHORIZED, () -> api.login(new LoginDTO()));
	}
	
	@Test
	public void login_bunny() throws IOException {
		
		String password = randomUUID().toString();
		Owner owner = mockOwner();
		owner.setPassword(password);
		Bunny bunny = mockBunny();
		
		String sessionId = randomUUID().toString();
		when(sessions.startSession(owner.getId())).thenReturn(sessionId);
		
		LoginDTO dto = new LoginDTO();
		dto.setBunny(bunny.getId());
		dto.setPassword(password);
		api.login(dto);
		
		verify(response).addCookie(cookie.capture());
		assertThat(cookie.getValue())
			.satisfies(c -> assertEquals(api.getClass().getSimpleName(), c.getName()))
			.satisfies(c -> assertEquals(sessionId, c.getValue()));
	}
	
	@Test
	public void login_unknownOwner() throws IOException {
		
		Bunny bunny = mockBunny();
		
		LoginDTO dto = new LoginDTO();
		dto.setBunny(bunny.getId());
		assertError(UNAUTHORIZED, () -> api.login(dto));
	}
}
