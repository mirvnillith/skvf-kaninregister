package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class GetBunnyBreederTest extends BunnyRegistryApiTest {

	@Test
	public void getBunnyBreeder() throws IOException {
		
		Owner breeder = mockOwner()
				.setEmail(randomUUID().toString())
				.setBreederName(randomUUID().toString())
				.setBreederEmail(randomUUID().toString())
				.setPublicBreeder(true);
		Bunny bunny = mockBunny()
				.setBreeder(breeder.getId());
		
		BunnyBreederDTO dto = api.getBunnyBreeder(bunny.getId());
		
		assertBreeder(breeder, dto);
	}
	
	@Test
	public void getBunnyBreeder_firstLastName() throws IOException {
		
		Owner breeder = mockOwner()
				.setEmail(randomUUID().toString())
				.setBreederEmail(randomUUID().toString())
				.setPublicBreeder(true);
		Bunny bunny = mockBunny()
				.setBreeder(breeder.getId());
		
		BunnyBreederDTO dto = api.getBunnyBreeder(bunny.getId());
		
		assertBreeder(breeder, dto);
	}
	
	@Test
	public void getBunnyBreeder_ownerEmail() throws IOException {
		
		Owner breeder = mockOwner()
				.setEmail(randomUUID().toString())
				.setBreederName(randomUUID().toString())
				.setPublicBreeder(true);
		Bunny bunny = mockBunny()
				.setBreeder(breeder.getId());
		
		BunnyBreederDTO dto = api.getBunnyBreeder(bunny.getId());
		
		assertBreeder(breeder, dto);
	}
	
	@Test
	public void getBunnyBreeder_error() throws IOException {
		
		when(registry.findBunnies(anyCollection())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getBunnyBreeder(randomUUID().toString()));
	}
	
	@Test
	public void getBunnyBreeder_noBunny() throws IOException {
		
		assertError(NOT_FOUND, () -> api.getBunnyBreeder(randomUUID().toString()));
	}
	
	@Test
	public void getBunnyBreeder_notSet() throws IOException {
		
		Bunny bunny = mockBunny()
				.setBreeder(null);
		
		assertThat(api.getBunnyBreeder(bunny.getId()).getName()).isNull();
	}
	
	@Test
	public void getBunnyBreeder_notFound() throws IOException {
		
		Bunny bunny = mockBunny();
		
		assertError(NOT_FOUND, () -> api.getBunnyBreeder(bunny.getId()));
	}
	
	@Test
	public void getBunnyBreeder_notPublic() throws IOException {
		
		Owner breeder = mockOwner()
				.setPublicBreeder(false);
		Bunny bunny = mockBunny()
				.setBreeder(breeder.getId());
		
		assertError(NO_CONTENT, () -> api.getBunnyBreeder(bunny.getId()));
	}
}
