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
import static se.skvf.kaninregister.api.BunnyRegistryApiImpl.TRANSFER_OWNER;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class ReclaimBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void reclaimBunny() throws IOException {
		
		Owner owner = mockOwner().setName(TRANSFER_OWNER);
		Owner previousOwner = mockOwner();
		mockSession(previousOwner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		
		BunnyDTO reclaimed = api.reclaimBunny(bunny.getId());
		
		verify(registry).update(bunnyArgument.capture());
		verify(registry).remove(owner);
		
		assertBunny(reclaimed, bunnyArgument.getValue());
		assertThat(reclaimed.getOwner()).isEqualTo(previousOwner.getId());
		assertThat(reclaimed.getPreviousOwner()).isNull();
	}
	
	@Test
	public void reclaimBunny_error() throws IOException {
		
		Owner owner = mockOwner().setName(TRANSFER_OWNER);
		Owner previousOwner = mockOwner();
		mockSession(previousOwner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		
		doThrow(NullPointerException.class).when(registry).update(bunny);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.reclaimBunny(bunny.getId()));
	}
	
	@Test
	public void reclaimBunny_notTransferOwner() throws IOException {
		
		Owner owner = mockOwner();
		Owner previousOwner = mockOwner();
		mockSession(previousOwner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setOwner(owner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		
		assertError(CONFLICT, () -> api.reclaimBunny(bunny.getId()));
	}
	
	@Test
	public void reclaimBunny_noPreviousOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		Bunny bunny = mockBunny();
		
		assertError(BAD_REQUEST, () -> api.reclaimBunny(bunny.getId()));
	}
	
	@Test
	public void reclaimBunny_notPreviousOwner() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setPreviousOwner(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.reclaimBunny(bunny.getId()));
	}
	
	@Test
	public void reclaimBunny_unknownBunny() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		assertError(NOT_FOUND, () -> api.reclaimBunny(randomUUID().toString()));
	}
	
	@Test
	public void reclaimBunny_noSession() throws IOException {
		
		assertError(UNAUTHORIZED, () -> api.reclaimBunny(randomUUID().toString()));
	}
}
