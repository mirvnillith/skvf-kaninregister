package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
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

public class UpdateBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void updateBunny_newOwnerIgnored() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setOwner(randomUUID().toString());
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getOwner()).isEqualTo(owner.getId());
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getOwner()).isEqualTo(owner.getId());
	}
	
	@Test
	public void updateBunny_otherBreeder() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setBreeder(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setBreeder(randomUUID().toString());
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getBreeder()).isEmpty();
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getBreeder()).isEmpty();
	}
	
	@Test
	public void updateBunny_unknownBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());;
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.updateBunny(owner.getId(), randomUUID().toString(), dto));
	}
	
	@Test
	public void updateBunny_notSameId() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());;
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny();
		String bunnyId = bunny.getId();
		bunny.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setId(randomUUID().toString());
		dto.setName(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.updateBunny(owner.getId(), bunnyId, dto));
	}
	
	@Test
	public void updateBunny_noSession() throws IOException {
		
		Owner owner = mockOwner();
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.updateBunny(owner.getId(), randomUUID().toString(), dto));
	}
	
	@Test
	public void updateBunny_unknownOwner() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		Bunny bunny = mockBunny();
		bunny.setOwner(ownerId);
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.updateBunny(ownerId, bunny.getId(), dto));
	}
}
