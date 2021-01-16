package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class GetOwnersTest extends BunnyRegistryApiTest {

	@Test
	public void getOwners() throws IOException {
		
		Owner owner = mockOwner();
		
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		OwnerListDTO dto = api.getOwners().getOwners().get(0);
		
		assertOwner(dto, owner);
	}
	
	@Test
	public void getOwners_error() throws IOException {
		
		when(registry.findOwners(filterArgument.capture())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getOwners());
	}
}
