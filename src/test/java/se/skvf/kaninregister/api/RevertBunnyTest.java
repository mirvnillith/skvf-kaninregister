package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class RevertBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void revertBunny() throws IOException {
		
		Owner owner = mockOwner();
		Owner previousOwner = mockOwner();
		mockSession(previousOwner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		
		BunnyDTO reverted = api.revertBunnyOwner(bunny.getId());
		
		verify(registry).update(bunnyArgument.capture());
		verify(registry).remove(owner);
		
		assertBunny(reverted, bunnyArgument.getValue());
		assertThat(reverted.getOwner()).isEqualTo(previousOwner.getId());
		assertThat(reverted.getPreviousOwner()).isNull();
	}
	
	@Test
	public void revertBunny_error() throws IOException {
		
		Owner owner = mockOwner();
		Owner previousOwner = mockOwner();
		mockSession(previousOwner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		
		doThrow(NullPointerException.class).when(registry).update(bunny);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.revertBunnyOwner(bunny.getId()));
	}
	
	@Test
	public void revertBunny_activatedOwner() throws IOException {
		
		Owner owner = mockOwner();
		owner.setPassword(randomUUID().toString());
		Owner previousOwner = mockOwner();
		mockSession(previousOwner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		
		assertError(CONFLICT, () -> api.revertBunnyOwner(bunny.getId()));
	}
	
	@Test
	public void revertBunny_noPreviousOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		Bunny bunny = mockBunny();
		
		assertError(BAD_REQUEST, () -> api.revertBunnyOwner(bunny.getId()));
	}
	
	@Test
	public void revertBunny_notPreviousOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setPreviousOwner(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.revertBunnyOwner(bunny.getId()));
	}
	
	@Test
	public void revertBunny_unknownBunny() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		assertError(NOT_FOUND, () -> api.revertBunnyOwner(randomUUID().toString()));
	}
	
	@Test
	public void revertBunny_noSession() throws IOException {
		
		assertError(UNAUTHORIZED, () -> api.revertBunnyOwner(randomUUID().toString()));
	}
}
