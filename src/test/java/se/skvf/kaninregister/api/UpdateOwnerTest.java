package se.skvf.kaninregister.api;

import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class UpdateOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void updateOwner() throws IOException {
		
		String userName = randomUUID().toString();
		Owner owner = mockOwner()
				.setUserName(userName)
				.setSignature("-");
		String ownerId = owner.getId();
		mockSession(ownerId);
		
		OwnerDTO dto = new OwnerDTO();
		dto.setEmail(randomUUID().toString());
		dto.setFirstName(randomUUID().toString());
		dto.setLastName(randomUUID().toString());
		dto.setUserName(randomUUID().toString());
		dto.setPublicOwner(false);
		dto.setBreeder(true);
		dto.setBreederName(randomUUID().toString());
		dto.setPublicBreeder(true);
		
		doNothing().when(registry).update(ownerArgument.capture());
		OwnerDTO updated = api.updateOwner(ownerId, dto);
		
		assertUpdate(updated, dto, ownerId);
		assertThat(updated.getUserName()).isEqualTo(userName);
		assertOwner(updated, ownerArgument.getValue());
	}
	
	@Test
	public void updateOwner_sameId() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature("-");
		String ownerId = owner.getId();
		mockSession(ownerId);
		
		OwnerDTO dto = new OwnerDTO();
		dto.setId(ownerId);
		dto.setEmail(randomUUID().toString());
		
		doNothing().when(registry).update(ownerArgument.capture());
		OwnerDTO updated = api.updateOwner(ownerId, dto);
		
		assertUpdate(updated, dto, ownerId);
		assertOwner(updated, ownerArgument.getValue());
	}

	private static void assertUpdate(OwnerDTO updated, OwnerDTO dto, String ownerId) {
		assertThat(updated.getId()).isEqualTo(ownerId);
		ofNullable(dto.getEmail()).ifPresent(assertThat(updated.getEmail())::isEqualTo);
		ofNullable(dto.getFirstName()).ifPresent(assertThat(updated.getFirstName())::isEqualTo);
		ofNullable(dto.getLastName()).ifPresent(assertThat(updated.getLastName())::isEqualTo);
		ofNullable(dto.getPublicOwner()).ifPresent(assertThat(updated.getPublicOwner())::isEqualTo);
		ofNullable(dto.getBreeder()).ifPresent(assertThat(updated.getBreeder())::isEqualTo);
		ofNullable(dto.getBreederName()).ifPresent(assertThat(updated.getBreederName())::isEqualTo);
		ofNullable(dto.getPublicBreeder()).ifPresent(assertThat(updated.getPublicBreeder())::isEqualTo);
	}
	
	@Test
	public void updateOwner_unknownOwner() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		OwnerDTO dto = new OwnerDTO();
		dto.setEmail(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.updateOwner(ownerId, dto));
	}
	
	@Test
	public void updateOwner_notSameId() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		OwnerDTO dto = new OwnerDTO();
		dto.setId(randomUUID().toString());
		dto.setEmail(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.updateOwner(owner.getId(), dto));
	}
	
	@Test
	public void updateOwner_noSession() throws IOException {
		
		Owner owner = mockOwner();
		
		OwnerDTO dto = new OwnerDTO();
		dto.setEmail(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.updateOwner(owner.getId(), dto));
	}
	
	@Test
	public void updateOwner_notApproved() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		OwnerDTO dto = new OwnerDTO();
		dto.setEmail(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.updateOwner(owner.getId(), dto));
	}
}
