package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class DeactivateOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void deactivate() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString())
				.setPublicOwner(true)
				.setPublicBreeder(true)
				.setPassword(randomUUID().toString());
		String sessionId = mockSession(owner.getId());
		
		assertThat(owner.isActivated()).isTrue();
		
		api.deactivateOwner(owner.getId());
		assertCookie(sessionId, false);
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.getUserName()).isNull();
		assertThat(updatedOwner.isPublicOwner()).isFalse();
		assertThat(updatedOwner.isPublicBreeder()).isFalse();
		assertThat(updatedOwner.isActivated()).isFalse();
	}
	
	@Test
	public void deactivate_noSession() throws IOException {
		
		Owner owner = mockOwner();
		
		assertError(UNAUTHORIZED, () -> api.deactivateOwner(owner.getId()));
	}
	
	@Test
	public void deactivate_notFound() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		assertError(NOT_FOUND, () -> api.deactivateOwner(ownerId));
	}
}
