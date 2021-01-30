package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class UpdateBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void updateBunny_newOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		Owner newOwner = mockOwner();
		Owner breeder = mockOwner();
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner, newOwner, breeder));
		
		Bunny bunny = mockBunny();
		String bunnyId = bunny.getId();
		bunny.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setId(bunnyId);
		dto.setName(randomUUID().toString());
		dto.setBreeder(breeder.getId());
		dto.setOwner(newOwner.getId());
		
		doNothing().when(registry).update(bunnyArgument.capture());
		BunnyDTO updated = api.updateBunny(owner.getId(), bunnyId, dto);
		
		assertUpdate(updated, dto, bunnyId);
		assertBunny(updated, bunnyArgument.getValue());
		assertThat(updated.getPreviousOwner()).isEqualTo(owner.getId());
	}

	private static void assertUpdate(BunnyDTO updated, BunnyDTO dto, String bunnyId) {
		assertThat(updated.getId()).isEqualTo(bunnyId);
		ofNullable(dto.getOwner()).ifPresent(assertThat(updated.getOwner())::isEqualTo);
		ofNullable(dto.getBreeder()).ifPresent(assertThat(updated.getBreeder())::isEqualTo);
		ofNullable(dto.getName()).ifPresent(assertThat(updated.getName())::isEqualTo);
	}
	
	@Test
	public void updateBunny_unknownNewOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setOwner(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.updateBunny(owner.getId(), bunny.getId(), dto));
	}
	
	@Test
	public void updateBunny_anonymousNewOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		String anonymousId = randomUUID().toString();
		when(registry.add(ownerArgument.capture())).thenReturn(anonymousId);
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setOwner("");
		
		BunnyDTO updated = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(updated.getOwner()).isEqualTo(anonymousId);
		assertThat(updated.getPreviousOwner()).isEqualTo(owner.getId());
		assertThat(ownerArgument.getValue().isPublicOwner()).isFalse();
	}
	
	@Test
	public void updateBunny_unknownBreeder() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setBreeder(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.updateBunny(owner.getId(), bunny.getId(), dto));
	}
	
	@Test
	public void updateBunny_unknownBunny() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.updateBunny(owner.getId(), randomUUID().toString(), dto));
	}
	
	@Test
	public void updateBunny_sameOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		Owner breeder = mockOwner();
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner, breeder));
		
		Bunny bunny = mockBunny();
		String bunnyId = bunny.getId();
		bunny.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setBreeder(breeder.getId());
		
		doNothing().when(registry).update(bunnyArgument.capture());
		BunnyDTO updated = api.updateBunny(owner.getId(), bunnyId, dto);
		
		assertUpdate(updated, dto, bunnyId);
		assertBunny(updated, bunnyArgument.getValue());
	}
	
	@Test
	public void updateBunny_notSameId() throws IOException {
		
		Owner owner = mockOwner();
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
