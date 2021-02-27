package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class UnapproveOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void unapprove() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString())
				.setPublicOwner(true)
				.setPublicBreeder(true)
				.setPassword(randomUUID().toString())
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		
		assertThat(owner.isActivated()).isTrue();
		assertThat(owner.isApproved()).isTrue();
		
		api.unapproveOwner(owner.getId());
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.getUserName()).isNotNull();
		assertThat(updatedOwner.isPublicOwner()).isFalse();
		assertThat(updatedOwner.isPublicBreeder()).isFalse();
		assertThat(updatedOwner.isApproved()).isFalse();
		assertThat(updatedOwner.isActivated()).isTrue();
	}
	
	@Test
	public void unapprove_noSession() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.unapproveOwner(owner.getId()));
	}
	
	@Test
	public void unapprove_notApproved() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		assertError(UNAUTHORIZED, () -> api.unapproveOwner(owner.getId()));
	}
	
	@Test
	public void unapprove_notFound() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		assertError(NOT_FOUND, () -> api.unapproveOwner(ownerId));
	}
}
