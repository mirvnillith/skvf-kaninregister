package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class TransferBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void transferBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		
		BunnyTransferDTO dto = api.transferBunny(owner.getId(), bunny.getId());
		
		assertThat(dto.getClaimToken()).isEqualTo(bunny.getOwner());
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getOwner()).isEqualTo(dto.getClaimToken());
		assertThat(bunnyArgument.getValue().getPreviousOwner()).isEqualTo(owner.getId());
	}
	
	@Test
	public void transferBunny_otherOwner() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny();
		
		assertError(NOT_FOUND, () -> api.transferBunny(owner.getId(), bunny.getId()));
	}
	
	@Test
	public void transferBunny_unknownBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());;
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		assertError(NOT_FOUND, () -> api.transferBunny(owner.getId(), randomUUID().toString()));
	}
	
	@Test
	public void transferBunny_noSession() throws IOException {
		
		Owner owner = mockOwner();
		
		assertError(UNAUTHORIZED, () -> api.transferBunny(owner.getId(), randomUUID().toString()));
	}
	
	@Test
	public void transferBunny_notApproved() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		assertError(UNAUTHORIZED, () -> api.transferBunny(owner.getId(), randomUUID().toString()));
	}
	
	@Test
	public void transferBunny_unknownOwner() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		Bunny bunny = mockBunny();
		
		assertError(NOT_FOUND, () -> api.transferBunny(ownerId, bunny.getId()));
	}
}
