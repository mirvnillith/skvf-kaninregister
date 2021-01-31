package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class DeleteBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void deleteBunny() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		Bunny bunny = mockBunny()
				.setOwner(owner.getId());
		
		api.deleteBunny(owner.getId(), bunny.getId());
		
		verify(registry).remove(bunny);
	}
	
	@Test
	public void deleteBunny_notInSession() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.deleteBunny(owner.getId(), randomUUID().toString()));
	}
	
	@Test
	public void deleteBunny_noSession() throws IOException {
		
		assertError(UNAUTHORIZED, () -> api.deleteBunny(randomUUID().toString(), randomUUID().toString()));
	}
	
	@Test
	public void deleteBunny_error() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		when(registry.findOwners(singleton(ownerId))).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.deleteBunny(ownerId, randomUUID().toString()));
	}
	
	@Test
	public void deleteBunny_unknownBunny() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		assertError(NOT_FOUND, () -> api.deleteBunny(owner.getId(), randomUUID().toString()));
	}
}
