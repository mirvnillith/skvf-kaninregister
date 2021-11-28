package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class ClaimBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void claimBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		Owner tempOwner = mockOwner();
		Owner previousOwner = mockOwner();
		
		Bunny bunny = mockBunny();
		bunny.setOwner(tempOwner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		BunnyClaimDTO dto = new BunnyClaimDTO();
		dto.setClaimToken(tempOwner.getId());
		BunnyDTO claimed = api.claimBunny(owner.getId(), dto);
		
		verify(registry).update(bunnyArgument.capture());
		verify(registry).remove(tempOwner);
		
		assertBunny(claimed, bunnyArgument.getValue());
		assertThat(claimed.getOwner()).isEqualTo(owner.getId());
		assertThat(claimed.getPreviousOwner()).isEqualTo(previousOwner.getId());
	}
	
	@Test
	public void claimBunny_error() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		Owner tempOwner = mockOwner();
		Owner previousOwner = mockOwner();
		
		Bunny bunny = mockBunny();
		bunny.setOwner(tempOwner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		doThrow(NullPointerException.class).when(registry).update(bunny);
		
		BunnyClaimDTO dto = new BunnyClaimDTO();
		dto.setClaimToken(tempOwner.getId());
		assertError(INTERNAL_SERVER_ERROR, () -> api.claimBunny(owner.getId(),  dto));
	}
	
	@Test
	public void claimBunny_activatedOwner() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		Owner tempOwner = mockOwner()
				.setPassword(randomUUID().toString());
		Owner previousOwner = mockOwner();
		
		Bunny bunny = mockBunny();
		bunny.setOwner(tempOwner.getId());
		bunny.setPreviousOwner(previousOwner.getId());
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		BunnyClaimDTO dto = new BunnyClaimDTO();
		dto.setClaimToken(tempOwner.getId());
		assertError(NOT_FOUND, () -> api.claimBunny(owner.getId(), dto));
	}
	
	@Test
	public void claimBunny_tokenWithoutBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		Owner tempOwner = mockOwner();
		
		BunnyClaimDTO dto = new BunnyClaimDTO();
		dto.setClaimToken(tempOwner.getId());
		assertError(NOT_FOUND, () -> api.claimBunny(owner.getId(), dto));
	}
		
	@Test
	public void claimBunny_unapproved() throws IOException {
		
		Owner owner = mockOwner();
		owner.setSignature(null);
		
		mockSession(owner.getId());
		
		Bunny bunny = mockBunny();
		bunny.setPreviousOwner(randomUUID().toString());
		
		assertError(PRECONDITION_FAILED, () -> api.claimBunny(owner.getId(), new BunnyClaimDTO()));
	}
	
	@Test
	public void claimBunny_noToken() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		
		assertError(NOT_FOUND, () -> api.claimBunny(owner.getId(), new BunnyClaimDTO()));
	}
	
	@Test
	public void claimBunny_unknownToken() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		
		BunnyClaimDTO dto = new BunnyClaimDTO();
		dto.setClaimToken(randomUUID().toString());
		assertError(NOT_FOUND, () -> api.claimBunny(owner.getId(), dto));
	}
	
	@Test
	public void claimBunny_noSession() throws IOException {
		
		assertError(UNAUTHORIZED, () -> api.claimBunny(randomUUID().toString(), new BunnyClaimDTO()));
	}
}
