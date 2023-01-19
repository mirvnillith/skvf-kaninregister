package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.api.BunnyRegistryApiImpl.TRANSFER_OWNER;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class ActivateOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void activate_notUniqueUserName() throws IOException {
		
		Owner owner = mockOwner();
		
		String userName = randomUUID().toString();
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(userName);
		dto.setPassword(randomUUID().toString());
		
		assertError(CONFLICT, () -> api.activateOwner(owner.getId(), dto));
		
		assertThat(filterArgument.getValue().get("Användarnamn"))
			.accepts(userName)
			.rejects(userName.toUpperCase());
	}
	
	@Test
	public void activate_alreadyActivated() throws IOException {
		
		Owner owner = mockOwner()
				.setPassword(randomUUID().toString());
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(randomUUID().toString());
		dto.setPassword(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.activateOwner(owner.getId(), dto));
	}
	
	@Test
	public void activate_transferOwner() throws IOException {
		
		Owner owner = mockOwner()
				.setName(TRANSFER_OWNER);
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(randomUUID().toString());
		dto.setPassword(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.activateOwner(owner.getId(), dto));
	}
	
	@Test
	public void activate() throws IOException {
		
		String ownerId = mockOwner().getId();
		
		String username = randomUUID().toString();
		String password = randomUUID().toString();
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(username);
		dto.setPassword(password);
		
		OwnerDTO owner = api.activateOwner(ownerId, dto);
		
		assertThat(owner.getUserName()).isEqualTo(username);
		
		verify(registry).update(ownerArgument.capture());
		
		Owner activatedOwner = ownerArgument.getValue();
		assertThat(activatedOwner.getUserName()).isEqualTo(username);
		assertThat(activatedOwner.validate(password)).isTrue();
	}
	
	@Test
	public void activate_error() throws IOException {
		
		String ownerId = mockOwner().getId();
		
		doThrow(NullPointerException.class).when(registry).update(ownerArgument.capture());
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(randomUUID().toString());
		dto.setPassword(randomUUID().toString());
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.activateOwner(ownerId, dto));
	}
		
	@Test
	public void activate_invalidUsername() throws IOException {
		
		String ownerId = mockOwner().getId();
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setPassword(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		dto.setUserName("");
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		dto.setUserName(" ");
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		dto.setUserName(" \t");
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		dto.setUserName(" \t\n");
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		
		verify(registry, never()).update(ownerArgument.capture());
	}
	
	@Test
	public void activate_invalidPassword() throws IOException {
		
		String ownerId = mockOwner().getId();
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		dto.setPassword("");
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		dto.setPassword(" ");
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		dto.setPassword(" \t");
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		dto.setPassword(" \t\n");
		assertError(BAD_REQUEST, () -> api.activateOwner(ownerId, dto));
		
		verify(registry, never()).update(ownerArgument.capture());
	}
	
	@Test
	public void activate_inSession() throws IOException {
		
		mockSession(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.activateOwner(randomUUID().toString(), new CreateOwnerDTO()));
	}
	
	@Test
	public void activate_unknownOwner() throws IOException {
		
		assertError(NOT_FOUND, () -> api.activateOwner(randomUUID().toString(), new CreateOwnerDTO()));
	}
}
