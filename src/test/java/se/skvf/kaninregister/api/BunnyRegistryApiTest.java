package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.function.Predicate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.function.Executable;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import se.skvf.kaninregister.BunnyTest;
import se.skvf.kaninregister.addo.AddoSigningService;
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
	protected AddoSigningService signingService;
	
	@Mock
	protected Registry registry;
	
	@Captor
	protected ArgumentCaptor<Map<String, Predicate<String>>> filterArgument;
	@Captor
	protected ArgumentCaptor<Bunny> bunnyArgument;
	@Captor
	protected ArgumentCaptor<Owner> ownerArgument;
	@Mock
	protected HttpServletResponse response;
	@Captor
	private ArgumentCaptor<Cookie> cookie;
	
	@BeforeEach
	public void api() {
		
		api = impl;
		
		when(request.getCookies()).thenReturn(new Cookie[0]);
	}
	
	protected void setApprovalUrl(String url) {
		impl.setApprovalUrl(url);
	}
	
	protected String mockSession(String ownerId) {
		String sessionId = randomUUID().toString();
		Cookie cookie = new Cookie(BunnyRegistryApi.class.getSimpleName(), sessionId);
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

	protected Owner mockOwner(String ownerId) throws IOException {
		Owner owner = new Owner()
				.setId(ownerId)
				.setName(randomUUID().toString())
				.setEmail(randomUUID().toString())
				.setPublicOwner(true);
		when(registry.findOwners(singleton(owner.getId()))).thenReturn(singleton(owner));
		return owner;
	}

	protected Owner mockOwner() throws IOException {
		return mockOwner(randomUUID().toString());
	}
	
	protected Bunny mockBunny() throws IOException {
		Bunny bunny = new Bunny()
				.setId(randomUUID().toString())
				.setName(randomUUID().toString())
				.setOwner(randomUUID().toString())
				.setBreeder(randomUUID().toString());
		when(registry.findBunnies(singleton(bunny.getId()))).thenReturn(singleton(bunny));
		return bunny;
	}

	protected void assertCookie(String sessionId, boolean add) {
		verify(response).addCookie(cookie.capture());
		assertThat(cookie.getValue())
			.satisfies(c -> assertEquals(sessionId, c.getValue()))
			//.satisfies(c -> assertTrue(c.getSecure())) TODO restore secure
			.satisfies(c -> assertEquals(add ? -1 : 0, c.getMaxAge()))
			.satisfies(c -> assertEquals(BunnyRegistryApi.class.getSimpleName(), c.getName()));
	}

	protected static void assertBunny(BunnyDTO expected, Bunny actual) {
		assertAll(
				() -> assertThat(actual.getId()).isEqualTo(expected.getId()),
				() -> assertThat(actual.getName()).isEqualTo(expected.getName()),
				() -> assertThat(actual.getOwner()).isEqualTo(expected.getOwner()),
				() -> assertThat(actual.getPreviousOwner()).isEqualTo(expected.getPreviousOwner()),
				() -> assertThat(actual.getBreeder()).isEqualTo(expected.getBreeder()),
				() -> assertThat(actual.isNeutered()).isEqualTo(ofNullable(expected.getNeutered()).orElse(false)),
				() -> assertThat(actual.getBirthDate()).isEqualTo(expected.getBirthDate()),
				() -> assertThat(actual.getRace()).isEqualTo(expected.getRace()),
				() -> assertThat(actual.getCoat()).isEqualTo(expected.getCoat()),
				() -> assertThat(actual.getColourMarkings()).isEqualTo(expected.getColourMarkings()),
				() -> assertThat(actual.getFeatures()).isEqualTo(expected.getFeatures()),
				() -> assertThat(actual.getPicture()).isEqualTo(expected.getPicture()),
				() -> assertThat(actual.getLeftEar()).isEqualTo(expected.getLeftEar()),
				() -> assertThat(actual.getRightEar()).isEqualTo(expected.getRightEar()),
				() -> assertThat(actual.getChip()).isEqualTo(expected.getChip()),
				() -> assertThat(actual.getRing()).isEqualTo(expected.getRing())
				);
	}
	
	protected static void assertBunny(Bunny expected, BunnyListDTO actual) {
		assertAll(
				() -> assertThat(actual.getId()).isEqualTo(expected.getId()),
				() -> assertThat(actual.getName()).isEqualTo(expected.getName()),
				() -> assertThat(actual.getLeftEar()).isEqualTo(expected.getLeftEar()),
				() -> assertThat(actual.getRightEar()).isEqualTo(expected.getRightEar()),
				() -> assertThat(actual.getChip()).isEqualTo(expected.getChip()),
				() -> assertThat(actual.getRing()).isEqualTo(expected.getRing())
				);
	}

	protected static void assertOwner(OwnerDTO expected, Owner actual) {
		assertAll(
				() -> assertThat(actual.getId()).isEqualTo(expected.getId()),
				() -> assertThat(actual.getName()).isEqualTo(expected.getName()),
				() -> assertThat(actual.getUserName()).isEqualTo(expected.getUserName()),
				() -> assertThat(actual.getEmail()).isEqualTo(expected.getEmail()),
				() -> assertThat(actual.getAddress()).isEqualTo(expected.getAddress()),
				() -> assertThat(actual.getPhone()).isEqualTo(expected.getPhone()),
				() -> assertThat(actual.isPublicOwner()).isEqualTo(expected.getPublicOwner()),
				() -> assertThat(actual.getBreederName()).isEqualTo(expected.getBreederName()),
				() -> assertThat(actual.isPublicBreeder()).isEqualTo(expected.getPublicBreeder())
				);
	}
	
	protected static void assertOwner(Owner expected, BunnyOwnerDTO actual) {
		assertAll(
				() -> assertThat(actual.getName()).isEqualTo(expected.getName()),
				() -> assertThat(actual.getEmail()).isEqualTo(expected.getEmail()),
				() -> assertThat(actual.getAddress()).isEqualTo(expected.getAddress()),
				() -> assertThat(actual.getPhone()).isEqualTo(expected.getPhone())
				);
	}
	
	protected static void assertBreeder(Owner expected, BunnyBreederDTO actual) {
		
		if (expected.getBreederName() != null) {
			assertThat(actual.getName()).isEqualTo(expected.getBreederName());
		} else {
			assertThat(actual.getName()).isEqualTo(expected.getName());
		}
		
		if (expected.getBreederEmail() != null) {
			assertThat(actual.getEmail()).isEqualTo(expected.getBreederEmail());
		} else {
			assertThat(actual.getEmail()).isEqualTo(expected.getEmail());
		}
	}
}
