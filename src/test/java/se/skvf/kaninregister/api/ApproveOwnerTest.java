package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.ACCEPTED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.api.BunnyRegistryApiImpl.SESSION_SIGNING;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.addo.NoSigning;
import se.skvf.kaninregister.addo.Signature;
import se.skvf.kaninregister.addo.Signing;
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
		
		when(signingService.startSigning()).thenReturn(new NoSigning());
		
		api.approveOwner(owner.getId());
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.getSignature()).isNotNull();
		assertThat(updatedOwner.isApproved()).isTrue();
		assertThat(updatedOwner.isActivated()).isTrue();
	}
	
	@Test
	public void approve_success() throws IOException {
		
		URL url = new URL("http://localhost");
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString())
				.setPassword(randomUUID().toString());
		String session = mockSession(owner.getId());
		
		assertThat(owner.isApproved()).isFalse();
		
		Signing signing = assertStartSigning(session, owner);
		
		when(signingService.checkSigning(signing.getToken())).thenReturn(Optional.of(true));
		
		String subject = randomUUID().toString();
		String signature = randomUUID().toString();
		when(signingService.getSignature(signing.getToken())).thenReturn(new Signature(url.toString(), subject, signature));
		
		api.approveOwner(owner.getId());
		
		verify(response, atLeastOnce()).sendError(OK.getStatusCode(), "");
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.getSignature())
			.startsWith(url.toString())
			.contains(subject)
			.endsWith(signature);
		assertThat(updatedOwner.isApproved()).isTrue();
	}

	private Signing assertStartSigning(String session, Owner owner) throws IOException {
		Signing signing = new Signing(randomUUID().toString(), randomUUID().toString(), randomUUID().toString());
		when(signingService.startSigning()).thenReturn(signing);
		
		api.approveOwner(owner.getId());
		
		verify(response).setHeader("Location", signing.getTransactionUrl());
		verify(response).sendError(ACCEPTED.getStatusCode(), "");
		verify(sessions).setAttribute(session, SESSION_SIGNING, signing);
		when(sessions.getAttribute(session, SESSION_SIGNING)).thenReturn(signing);
		return signing;
	}
	
	@Test
	public void approve_failure() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString())
				.setPassword(randomUUID().toString());
		String session = mockSession(owner.getId());
		
		assertThat(owner.isApproved()).isFalse();
		
		Signing signing = assertStartSigning(session, owner);
		
		when(sessions.getAttribute(session, SESSION_SIGNING)).thenReturn(signing);
		when(signingService.checkSigning(signing.getToken())).thenReturn(Optional.of(false));
		
		assertError(NO_CONTENT, () -> api.approveOwner(owner.getId()));
		
		verify(registry, never()).update(ownerArgument.capture());
	}
	
	@Test
	public void approve_ongoing() throws IOException {
		
		Owner owner = mockOwner()
				.setUserName(randomUUID().toString())
				.setPassword(randomUUID().toString());
		String session = mockSession(owner.getId());
		
		assertThat(owner.isApproved()).isFalse();
		
		Signing signing = assertStartSigning(session, owner);
		
		when(sessions.getAttribute(session, SESSION_SIGNING)).thenReturn(signing);
		when(signingService.checkSigning(signing.getToken())).thenReturn(Optional.empty());
		
		api.approveOwner(owner.getId());
		
		verify(response, times(2)).sendError(ACCEPTED.getStatusCode(), "");
		verify(registry, never()).update(ownerArgument.capture());
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
