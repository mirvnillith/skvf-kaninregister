package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class getBunnyOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void getBunnyOwner() throws IOException {
		
		Bunny bunny = mockBunny();
		Owner owner = mockOwner();
		owner.setPublicOwner(true);
		bunny.setOwner(owner.getId());
		
		OwnerDTO dto = api.getBunnyOwner(bunny.getId());
		
		assertOwner(dto, owner);
	}
	
	@Test
	public void getBunnyOwner_error() throws IOException {
		
		when(registry.findBunnies(anyCollection())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getBunnyOwner(randomUUID().toString()));
	}
	
	@Test
	public void getBunnyOwner_noBunny() throws IOException {
		
		assertError(NOT_FOUND, () -> api.getBunnyOwner(randomUUID().toString()));
	}
	
	@Test
	public void getBunnyOwner_notSet() throws IOException {
		
		Bunny bunny = mockBunny();
		bunny.setOwner(null);
		
		assertError(NOT_FOUND, () -> api.getBunnyOwner(bunny.getId()));
	}
	
	@Test
	public void getBunnyOwner_notFound() throws IOException {
		
		Bunny bunny = mockBunny();
		
		assertError(NOT_FOUND, () -> api.getBunnyOwner(bunny.getId()));
	}
	
	@Test
	public void getBunnyOwner_notPublic() throws IOException {
		
		Bunny bunny = mockBunny();
		Owner owner = mockOwner();
		owner.setPublicOwner(false);
		bunny.setOwner(owner.getId());
		
		assertError(NO_CONTENT, () -> api.getBunnyOwner(bunny.getId()));
	}
}
