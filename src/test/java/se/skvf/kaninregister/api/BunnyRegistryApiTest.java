package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import se.skvf.kaninregister.BunnyTest;
import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

public abstract class BunnyRegistryApiTest extends BunnyTest {

	@InjectMocks
	private BunnyRegistryApiImpl impl;
	protected BunnyRegistryApi api;
	
	@Mock
	private HttpServletRequest request;
	@Mock
	protected SessionManager sessions;
	
	@Mock
	protected Registry registry;
	
	@Captor
	protected ArgumentCaptor<Map<String, Predicate<String>>> filterArgument;
	@Captor
	protected ArgumentCaptor<Bunny> bunnyArgument;
	@Captor
	protected ArgumentCaptor<Owner> ownerArgument;
	
	@BeforeEach
	public void api() {
		
		impl.request = request;
		api = impl;
		
		when(request.getCookies()).thenReturn(new Cookie[0]);
	}
	
	protected String mockSession(String ownerId) {
		String sessionId = randomUUID().toString();
		Cookie cookie = new Cookie(impl.getClass().getSimpleName(), sessionId);
		reset(request);
		when(request.getCookies()).thenReturn(new Cookie[] { cookie });
		when(sessions.isSession(sessionId, ownerId)).thenReturn(true);
		return sessionId;
	}
	
	protected void assertError(Status expected, Executable executable) {
		WebApplicationException actual = assertThrows(WebApplicationException.class, executable);
		assertThat(actual.getResponse().getStatus())
			.isEqualTo(expected.getStatusCode());
	}

	protected Owner mockOwner() throws IOException {
		Owner owner = new Owner()
				.setId(randomUUID().toString())
				.setFirstName(randomUUID().toString())
				.setLastName(randomUUID().toString())
				.setEmail(randomUUID().toString());
		when(registry.findOwners(anyCollection())).thenReturn(singleton(owner));
		return owner;
	}
	
	protected Bunny mockBunny() throws IOException {
		Bunny bunny = new Bunny()
				.setId(randomUUID().toString())
				.setName(randomUUID().toString())
				.setOwner(randomUUID().toString())
				.setBreeder(randomUUID().toString());
		when(registry.findBunnies(anyCollection())).thenReturn(singleton(bunny));
		return bunny;
	}

	protected static void assertBunny(BunnyDTO expected, Bunny actual) {
		assertAll(
				() -> assertThat(actual.getId()).isEqualTo(expected.getId()),
				() -> assertThat(actual.getName()).isEqualTo(expected.getName()),
				() -> assertThat(actual.getOwner()).isEqualTo(expected.getOwner()),
				() -> assertThat(actual.getBreeder()).isEqualTo(expected.getBreeder())
				);
	}
	
	protected static void assertBunny(BunnyListDTO expected, Bunny actual) {
		assertAll(
				() -> assertThat(actual.getId()).isEqualTo(expected.getId()),
				() -> assertThat(actual.getName()).isEqualTo(expected.getName()),
				() -> assertThat(actual.getOwner()).isEqualTo(expected.getOwner())
				);
	}

	protected static void assertOwner(OwnerDTO expected, Owner actual) {
		assertAll(
				() -> assertThat(actual.getId()).isEqualTo(expected.getId()),
				() -> assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName()),
				() -> assertThat(actual.getLastName()).isEqualTo(expected.getLastName()),
				() -> assertThat(actual.getEmail()).isEqualTo(expected.getEmail())
				);
	}
	
	protected static void assertOwner(OwnerListDTO expected, Owner actual) {
		assertAll(
				() -> assertThat(actual.getId()).isEqualTo(expected.getId()),
				() -> assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName()),
				() -> assertThat(actual.getLastName()).isEqualTo(expected.getLastName())
				);
	}
}
