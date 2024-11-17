package se.skvf.kaninregister.api;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.model.Bunny.BREEDER;
import static se.skvf.kaninregister.model.Bunny.PREVIOUS_OWNER;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class DeleteOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void deleteOwner() throws IOException {
		
		Owner owner = mockOwner();
		String sessionId = mockSession(owner.getId());
		
		api.deleteOwner(owner.getId());
		
		verify(registry).remove(owner);
		assertCookies(sessionId, false);
	}
	
	@Test
	public void deleteBreeder() throws IOException {
		
		Owner owner = mockOwner();
		String sessionId = mockSession(owner.getId());
		Bunny bunny = mockBunny()
				.setBreeder(owner.getId());
		
		when(registry.findBunnies(filterArgument.capture())).thenAnswer(i -> {
			Map<String, Object> filter = i.getArgument(0);
			if (filter.containsKey(BREEDER)) {
				return singleton(bunny);
			}
			return emptyList();
		});
		
		api.deleteOwner(owner.getId());
		
		Predicate<String> breederFilter = filterArgument.getAllValues().stream()
			.map(m -> m.get(BREEDER))
			.filter(Objects::nonNull)
			.findAny()
			.orElseThrow(() -> new NullPointerException("Filter " + BREEDER + " not found"));
		assertThat(breederFilter).accepts(owner.getId());
		
		verify(registry).update(bunny);
		assertThat(bunny.getBreeder()).isNull();
		
		verify(registry).remove(owner);
		assertCookies(sessionId, false);
	}
	
	@Test
	public void deletePreviousOwner() throws IOException {
		
		Owner owner = mockOwner();
		String sessionId = mockSession(owner.getId());
		Bunny bunny = mockBunny()
				.setPreviousOwner(owner.getId());
		
		when(registry.findBunnies(filterArgument.capture())).thenAnswer(i -> {
			Map<String, Object> filter = i.getArgument(0);
			if (filter.containsKey(PREVIOUS_OWNER)) {
				return singleton(bunny);
			}
			return emptyList();
		});
		
		api.deleteOwner(owner.getId());
		
		Predicate<String> previousOwnerFilter = filterArgument.getAllValues().stream()
				.map(m -> m.get(PREVIOUS_OWNER))
				.filter(Objects::nonNull)
				.findAny()
				.orElseThrow(() -> new NullPointerException("Filter " + PREVIOUS_OWNER + " not found"));
			assertThat(previousOwnerFilter).accepts(owner.getId());

		verify(registry).update(bunny);
		assertThat(bunny.getPreviousOwner()).isNull();
		
		verify(registry).remove(owner);
		assertCookies(sessionId, false);
	}
	
	@Test
	public void deleteOwner_hasBunnies() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(null));
		
		assertError(BAD_REQUEST, () -> api.deleteOwner(owner.getId()));
	}
	
	@Test
	public void deleteOwner_notInSession() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.deleteOwner(owner.getId()));
	}
	
	@Test
	public void deleteOwner_noSession() throws IOException {
		
		assertError(UNAUTHORIZED, () -> api.deleteOwner(randomUUID().toString()));
	}
	
	@Test
	public void deleteOwner_error() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		when(registry.findOwners(singleton(ownerId))).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.deleteOwner(ownerId));
	}
	
	@Test
	public void deleteOwner_unknownId() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		assertError(NOT_FOUND, () -> api.deleteOwner(ownerId));
	}
}
