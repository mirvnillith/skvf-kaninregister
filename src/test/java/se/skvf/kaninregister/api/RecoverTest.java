package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class RecoverTest extends BunnyRegistryApiTest {

	@Test
	public void recover() throws IOException {
		
		String oldPassword = randomUUID().toString();
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString())
				.setPassword(oldPassword);
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId());
		when(registry.findBunnies(filterArgument.capture())).thenReturn(new HashSet<>(singleton(bunny)));
		
		RecoveryDTO dto = new RecoveryDTO();
		dto.setBunnyIdentifiers(singletonList(bunnyIdentifier(randomUUID().toString())));
		dto.setNewPassword(randomUUID().toString());
		
		api.recoverOwner(owner.getUserName(), dto);
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.validate(dto.getNewPassword())).isTrue();
		assertThat(updatedOwner.validate(oldPassword)).isFalse();
		
		Map<String, Predicate<String>> ownerFilter = filterArgument.getAllValues().get(0);
		assertThat(ownerFilter.values().iterator().next()).accepts(owner.getUserName());
		
		Map<String, Predicate<String>> bunnyFilter = filterArgument.getValue();
		assertThat(bunnyFilter.values().iterator().next()).accepts(dto.getBunnyIdentifiers().get(0).getIdentifier());
	}
	
	private static BunnyIdentifierDTO bunnyIdentifier(String identifier) {
		BunnyIdentifierDTO dto = new BunnyIdentifierDTO();
		dto.setLocation(BunnyIdentifierLocation.values()[new Random().nextInt(BunnyIdentifierLocation.values().length)]);
		dto.setIdentifier(identifier);
		return dto;
	}

	@Test
	public void recover_unknownBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString());
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		RecoveryDTO dto = new RecoveryDTO();
		dto.setNewPassword(randomUUID().toString());
		dto.setBunnyIdentifiers(singletonList(bunnyIdentifier(randomUUID().toString())));
		
		assertError(NOT_FOUND, () -> api.recoverOwner(owner.getUserName(), dto));
		
		verify(registry, never()).update(owner);
	}
	
	@Test
	public void recover_unownedBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString());
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		Bunny bunny = mockBunny();
		when(registry.findBunnies(filterArgument.capture())).thenReturn(new HashSet<>(singleton(bunny)));
		
		RecoveryDTO dto = new RecoveryDTO();
		dto.setNewPassword(randomUUID().toString());
		dto.setBunnyIdentifiers(singletonList(bunnyIdentifier(randomUUID().toString())));
		
		assertError(NOT_FOUND, () -> api.recoverOwner(owner.getUserName(), dto));
		
		verify(registry, never()).update(owner);
	}
	
	@Test
	public void recover_invalidNewPassword() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString());

		RecoveryDTO dto = new RecoveryDTO();
		dto.setBunnyIdentifiers(asList(bunnyIdentifier(randomUUID().toString())));
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getUserName(), dto));
		dto.setNewPassword("");
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getUserName(), dto));
		dto.setNewPassword(" ");
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getUserName(), dto));
		dto.setNewPassword(" \t");
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getUserName(), dto));
		dto.setNewPassword(" \t\n");
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getId(), dto));
		
		verify(registry, never()).update(owner);
	}
	
	@Test
	public void recover_invalidIdentifiers() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString());
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		RecoveryDTO dto = new RecoveryDTO();
		dto.setBunnyIdentifiers(null);
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getUserName(), dto));
		dto.setBunnyIdentifiers(emptyList());
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getUserName(), dto));
		dto.setBunnyIdentifiers(singletonList(bunnyIdentifier(null)));
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getUserName(), dto));
		dto.setBunnyIdentifiers(singletonList(bunnyIdentifier(randomUUID().toString())));
		dto.getBunnyIdentifiers().get(0).setLocation(null);
		assertError(BAD_REQUEST, () -> api.recoverOwner(owner.getUserName(), dto));
		
		verify(registry, never()).update(owner);
	}
	
	@Test
	public void recover_unknownOwner() throws IOException {
		
		RecoveryDTO dto = new RecoveryDTO();
		dto.setNewPassword(randomUUID().toString());
		assertError(NOT_FOUND, () -> api.recoverOwner(randomUUID().toString(), dto));
	}
	
	@Test
	public void recover_inSession() throws IOException {
		
		mockSession(randomUUID().toString());
		
		RecoveryDTO dto = new RecoveryDTO();
		dto.setNewPassword(randomUUID().toString());
		assertError(UNAUTHORIZED, () -> api.recoverOwner(randomUUID().toString(), dto));
	}
}
