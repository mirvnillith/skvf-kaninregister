package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;

public class GetBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void getBunny() throws IOException {
		
		Bunny bunny = mockBunny();
		
		BunnyDTO dto = api.getBunny(bunny.getId());
		
		assertBunny(dto, bunny);
	}
	
	@Test
	public void getBunny_error() throws IOException {
		
		String bunnyId = randomUUID().toString();
		
		when(registry.findBunnies(singleton(bunnyId))).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getBunny(bunnyId));
	}
	
	@Test
	public void getBunny_unknownId() throws IOException {
		
		String bunnyId = randomUUID().toString();
		
		assertError(NOT_FOUND, () -> api.getBunny(bunnyId));
	}
}
