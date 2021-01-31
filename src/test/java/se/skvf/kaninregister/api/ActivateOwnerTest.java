package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class ActivateOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void activate_byBunny() throws IOException {
		
		Owner owner = mockOwner();
		Bunny bunny = mockBunny().setOwner(owner.getId());	
		
		String userName = randomUUID().toString();
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setBunny(bunny.getId());
		dto.setUserName(userName);
		dto.setPassword(password);
		api.activateOwner(owner.getId(), dto);
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.getUserName()).isEqualTo(userName);
		assertThat(updatedOwner.validate(password)).isTrue();
	}
	
	@Test
	public void activate_notOwnedBunny() throws IOException {
		
		Owner owner = mockOwner();
		Bunny bunny = mockBunny();	
		
		ActivationDTO dto = new ActivationDTO();
		dto.setBunny(bunny.getId());
		dto.setUserName(randomUUID().toString());
		dto.setPassword(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.activateOwner(owner.getId(), dto));
	}
	
	@Test
	public void activate_notUniqueUserName() throws IOException {
		
		Owner owner = mockOwner();
		Bunny bunny = mockBunny();	
		bunny.setOwner(owner.getId());
		
		String userName = randomUUID().toString();
		when(registry.findOwners(filterArgument.capture())).thenReturn(singleton(owner));
		
		ActivationDTO dto = new ActivationDTO();
		dto.setBunny(bunny.getId());
		dto.setUserName(userName);
		dto.setPassword(randomUUID().toString());
		
		assertError(CONFLICT, () -> api.activateOwner(owner.getId(), dto));
		
		assertThat(filterArgument.getValue().get("Användarnamn"))
			.accepts(userName)
			.rejects(userName.toUpperCase());
	}
	
	@Test
	public void activate_byUserName() throws IOException {
		
		Owner owner = mockOwner()
			.setUserName(randomUUID().toString());
		
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(owner.getUserName());
		dto.setPassword(password);
		api.activateOwner(owner.getId(), dto);
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.validate(password)).isTrue();
	}
	
	@Test
	public void activate_otherUserName() throws IOException {
		
		Owner owner = mockOwner();
		owner.setUserName(randomUUID().toString());
		
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(randomUUID().toString());
		dto.setPassword(password);
		
		assertError(BAD_REQUEST, () -> api.activateOwner(owner.getId(), dto));
	}
	
	@Test
	public void activate_alreadyActivated() throws IOException {
		
		Owner owner = mockOwner();
		owner.setUserName(randomUUID().toString());
		
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(owner.getUserName());
		dto.setPassword(password);
		api.activateOwner(owner.getId(), dto);
		
		assertError(NO_CONTENT, () -> api.activateOwner(owner.getId(), dto));
	}
	
	@Test
	public void activate_error() throws IOException {
		
		Owner owner = mockOwner();
		doThrow(NullPointerException.class).when(registry).update(owner);
		
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(randomUUID().toString());
		dto.setPassword(password);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.activateOwner(owner.getId(), dto));
	}
		
	@Test
	public void activate_invalidPassword() throws IOException {
		
		Owner owner = mockOwner();
		owner.setUserName(randomUUID().toString());

		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(owner.getUserName());
		
		assertError(BAD_REQUEST, () -> api.activateOwner(owner.getId(), dto));
		dto.setPassword("");
		assertError(BAD_REQUEST, () -> api.activateOwner(owner.getId(), dto));
		dto.setPassword(" ");
		assertError(BAD_REQUEST, () -> api.activateOwner(owner.getId(), dto));
		dto.setPassword(" \t");
		assertError(BAD_REQUEST, () -> api.activateOwner(owner.getId(), dto));
		dto.setPassword(" \t\n");
		assertError(BAD_REQUEST, () -> api.activateOwner(owner.getId(), dto));
		
		verify(registry, never()).update(owner);
	}
	
	@Test
	public void activate_unknownOwner() throws IOException {
		
		ActivationDTO dto = new ActivationDTO();
		assertError(NOT_FOUND, () -> api.activateOwner(randomUUID().toString(), dto));
		assertError(NOT_FOUND, () -> api.activateOwner(null, dto));
	}
	
	@Test
	public void activate_inSession() throws IOException {
		
		mockSession(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.activateOwner(randomUUID().toString(), new ActivationDTO()));
	}
	
	@Test
	public void activate_unknownBunny() throws IOException {
		
		Owner owner = mockOwner();
		
		String userName = randomUUID().toString();
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setBunny(randomUUID().toString());
		dto.setUserName(userName);
		dto.setPassword(password);

		assertError(NOT_FOUND, () -> api.activateOwner(owner.getId(), dto));
	}
}
