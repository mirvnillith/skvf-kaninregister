package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class GetBunnyPreviousOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void getBunnyPreviousOwner() throws IOException {
		
		Bunny bunny = mockBunny();
		Owner owner = mockOwner()
				.setEmail(randomUUID().toString())
				.setAddress(randomUUID().toString())
				.setPhone(randomUUID().toString())
				.setBreederName(randomUUID().toString())
				.setPublicOwner(true);
		bunny.setOwner(owner.getId());
		bunny.setPreviousOwner(owner.getId());
		
		mockSession(owner.getId());
		BunnyOwnerDTO dto = api.getBunnyPreviousOwner(bunny.getId());
		
		assertOwner(owner, dto);
	}
	
	@Test
	public void getBunnyPreviousOwner_error() throws IOException {
		
		when(registry.findBunnies(anyCollection())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getBunnyPreviousOwner(randomUUID().toString()));
	}
	
	@Test
	public void getBunnyPreviousOwner_noBunny() throws IOException {
		
		assertError(NOT_FOUND, () -> api.getBunnyPreviousOwner(randomUUID().toString()));
	}
	
	@Test
	public void getBunnyPreviousOwner_notSet() throws IOException {
		
		Bunny bunny = mockBunny();
		mockSession(bunny.getOwner());
		
		assertThat(api.getBunnyPreviousOwner(bunny.getId()).getName()).isNull();
	}
	
	@Test
	public void getBunnyPreviousOwner_notLoggedIn() throws IOException {
		
		Bunny bunny = mockBunny();
		
		assertError(UNAUTHORIZED, () -> api.getBunnyPreviousOwner(bunny.getId()));
	}
	
	@Test
	public void getBunnyPreviousOwner_notLoggedInOwner() throws IOException {
		
		Bunny bunny = mockBunny();
		mockSession(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.getBunnyPreviousOwner(bunny.getId()));
	}
	
	@Test
	public void getBunnyPreviousOwner_notFound() throws IOException {
		
		Bunny bunny = mockBunny()
				.setPreviousOwner(randomUUID().toString());
		mockSession(bunny.getOwner());
		
		assertError(NOT_FOUND, () -> api.getBunnyPreviousOwner(bunny.getId()));
	}
	
	@Test
	public void getBunnyPreviousOwner_notPublic() throws IOException {
		
		Bunny bunny = mockBunny();
		Owner owner = mockOwner();
		owner.setPublicOwner(false);
		bunny.setPreviousOwner(owner.getId());
		mockSession(bunny.getOwner());
		
		assertError(NO_CONTENT, () -> api.getBunnyPreviousOwner(bunny.getId()));
	}
}
