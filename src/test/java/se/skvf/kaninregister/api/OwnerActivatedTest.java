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

public class OwnerActivatedTest extends BunnyRegistryApiTest {

	@Test
	public void activated_transferOwner() throws IOException {
		
		Owner owner = mockOwner()
				.setName(TRANSFER_OWNER);
		
		assertError(NOT_FOUND, () -> api.isOwnerActivated(owner.getId()));
	}
	
	@Test
	public void activated() throws IOException {
		
		Owner owner = mockOwner();
		
		ActivatedStatusDTO dto = api.isOwnerActivated(owner.getId());
		
		assertThat(dto.getActivated()).isFalse();
		
		owner.setUserName(randomUUID().toString());
		owner.setPassword(randomUUID().toString());
		
		dto = api.isOwnerActivated(owner.getId());
		
		assertThat(dto.getActivated()).isTrue();
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
	public void activated_inSession() throws IOException {
		
		mockSession(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.isOwnerActivated(randomUUID().toString()));
	}
	
	@Test
	public void activate_unknownOwner() throws IOException {
		
		assertError(NOT_FOUND, () -> api.isOwnerActivated(randomUUID().toString()));
	}
}
