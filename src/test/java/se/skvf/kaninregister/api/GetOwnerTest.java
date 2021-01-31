package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class GetOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void getOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(randomUUID().toString());
		
		OwnerDTO dto = api.getOwner(owner.getId());
		
		assertOwner(dto, owner);
	}
	
	@Test
	public void getOwner_nonPublicInSession() throws IOException {
		
		Owner owner = mockOwner();
		owner.setPublicOwner(false);
		mockSession(owner.getId());
		
		OwnerDTO dto = api.getOwner(owner.getId());
		
		assertOwner(dto, owner);
	}
	
	@Test
	public void getOwner_nonPublicNotInSession() throws IOException {
		
		Owner owner = mockOwner();
		owner.setPublicOwner(false);
		mockSession(randomUUID().toString());
		
		assertError(NO_CONTENT, () -> api.getOwner(owner.getId()));
	}
	
	@Test
	public void getOwner_noSession() throws IOException {
		
		assertError(UNAUTHORIZED, () -> api.getOwner(randomUUID().toString()));
	}
	
	@Test
	public void getOwner_error() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(randomUUID().toString());
		
		when(registry.findOwners(singleton(ownerId))).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getOwner(ownerId));
	}
	
	@Test
	public void getOwner_unknownId() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.getOwner(ownerId));
	}
}
