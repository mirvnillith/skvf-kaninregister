package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class ApproveOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void approve_noApprovalUrl() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString())
				.setPassword(randomUUID().toString());
		mockSession(owner.getId());
		
		assertThat(owner.isActivated()).isTrue();
		assertThat(owner.isApproved()).isFalse();
		
		api.approveOwner(owner.getId());
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.getSignature()).isNotNull();
		assertThat(updatedOwner.isApproved()).isTrue();
		assertThat(updatedOwner.isActivated()).isTrue();
	}
	
	@Test
	public void approve_noSession() throws IOException {
		
		Owner owner = mockOwner();
		
		assertError(UNAUTHORIZED, () -> api.approveOwner(owner.getId()));
	}
	
	@Test
	public void approve_approved() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature("-");
		mockSession(owner.getId());
		
		api.approveOwner(owner.getId());
		
		verify(registry, never()).update(owner);
	}
	
	@Test
	public void approve_notFound() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		assertError(NOT_FOUND, () -> api.approveOwner(ownerId));
	}
}
