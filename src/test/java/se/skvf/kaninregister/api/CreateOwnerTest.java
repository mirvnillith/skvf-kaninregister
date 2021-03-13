package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class CreateOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void create_notUniqueUserName() throws IOException {
		
		Owner owner = mockOwner();
		
		String userName = randomUUID().toString();
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(userName);
		dto.setPassword(randomUUID().toString());
		
		assertError(CONFLICT, () -> api.createOwner(dto));
		
		assertThat(filterArgument.getValue().get("Användarnamn"))
			.accepts(userName)
			.rejects(userName.toUpperCase());
	}
	
	@Test
	public void create() throws IOException {
		
		String username = randomUUID().toString();
		String password = randomUUID().toString();
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(username);
		dto.setPassword(password);
		
		OwnerDTO owner = api.createOwner(dto);
		
		assertThat(owner.getUserName()).isEqualTo(username);
		
		verify(registry).add(ownerArgument.capture());
		
		Owner addedOwner = ownerArgument.getValue();
		assertThat(addedOwner.getUserName()).isEqualTo(username);
		assertThat(addedOwner.validate(password)).isTrue();
	}
	
	@Test
	public void create_error() throws IOException {
		
		doThrow(NullPointerException.class).when(registry).add(ownerArgument.capture());
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(randomUUID().toString());
		dto.setPassword(randomUUID().toString());
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.createOwner(dto));
	}
		
	@Test
	public void create_invalidUsername() throws IOException {
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setPassword(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		dto.setUserName("");
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		dto.setUserName(" ");
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		dto.setUserName(" \t");
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		dto.setUserName(" \t\n");
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		
		verify(registry, never()).add(ownerArgument.capture());
	}
	
	@Test
	public void create_invalidPassword() throws IOException {
		
		CreateOwnerDTO dto = new CreateOwnerDTO();
		dto.setUserName(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		dto.setPassword("");
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		dto.setPassword(" ");
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		dto.setPassword(" \t");
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		dto.setPassword(" \t\n");
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
		
		verify(registry, never()).add(ownerArgument.capture());
	}
	
	@Test
	public void create_inSession() throws IOException {
		
		mockSession(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.createOwner(new CreateOwnerDTO()));
	}
}
