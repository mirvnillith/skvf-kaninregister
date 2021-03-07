package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class GetOwnerBunniesTest extends BunnyRegistryApiTest {

	@Test
	public void getOwnerBunnies() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		Bunny bunny = mockBunny()
				.setOwner(owner.getId());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		BunnyListDTO dto = api.getOwnerBunnies(owner.getId()).getBunnies().get(0);
		
		assertBunny(dto, bunny);
	}
	
	@Test
	public void getOwnerBunnies_transferred() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		Owner newOwner = mockOwner();
		Bunny bunny = mockBunny()
				.setOwner(newOwner.getId())
				.setPreviousOwner(owner.getId());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		assertThat(api.getOwnerBunnies(owner.getId()).getBunnies())
			.hasSize(2)
			.anySatisfy(own -> assertThat(own.getClaimToken()).isNull())
			.anySatisfy(transfer -> assertThat(transfer.getClaimToken()).isEqualTo(newOwner.getId()));
	}
	
	@Test
	public void getOwnerBunnies_noSession() throws IOException {
		
		String ownerId = randomUUID().toString();
		
		assertError(UNAUTHORIZED, () -> api.getOwnerBunnies(ownerId));
	}
	
	@Test
	public void getOwnerBunnies_error() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		when(registry.findOwners(singleton(ownerId))).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getOwnerBunnies(ownerId));
	}
	
	@Test
	public void getOwnerBunnies_unknownId() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		assertError(NOT_FOUND, () -> api.getOwnerBunnies(ownerId));
	}
}
