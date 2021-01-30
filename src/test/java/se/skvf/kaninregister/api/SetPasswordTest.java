package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class SetPasswordTest extends BunnyRegistryApiTest {

	@Test
	public void setPassword() throws IOException {
		
		String oldPassword = randomUUID().toString();
		Owner owner = mockOwner();
		owner.setPassword(oldPassword);
		
		String newPassword = randomUUID().toString();
		
		PasswordDTO dto = new PasswordDTO();
		dto.setCurrentPassword(oldPassword);
		dto.setNewPassword(newPassword);
		api.setPassword(owner.getId(), dto);
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.validate(newPassword)).isTrue();
		assertThat(updatedOwner.validate(oldPassword)).isFalse();
	}
	
	@Test
	public void setPassword_incorrectCurrentPassword() throws IOException {
		
		Owner owner = mockOwner();
		
		PasswordDTO dto = new PasswordDTO();
		dto.setCurrentPassword(randomUUID().toString());
		dto.setNewPassword(randomUUID().toString());
		assertError(UNAUTHORIZED, () -> api.setPassword(owner.getId(), dto));
		dto.setCurrentPassword(null);
		assertError(UNAUTHORIZED, () -> api.setPassword(owner.getId(), dto));
		
		verify(registry, never()).update(owner);
	}
	
	@Test
	public void setPassword_invalidNewPassword() throws IOException {
		
		PasswordDTO dto = new PasswordDTO();
		assertError(BAD_REQUEST, () -> api.setPassword(randomUUID().toString(), dto));
		dto.setNewPassword("");
		assertError(BAD_REQUEST, () -> api.setPassword(randomUUID().toString(), dto));
		dto.setNewPassword(" ");
		assertError(BAD_REQUEST, () -> api.setPassword(randomUUID().toString(), dto));
		dto.setNewPassword(" \t");
		assertError(BAD_REQUEST, () -> api.setPassword(randomUUID().toString(), dto));
		dto.setNewPassword(" \t\n");
		assertError(BAD_REQUEST, () -> api.setPassword(randomUUID().toString(), dto));
		
		verifyNoMoreInteractions(registry);
	}
	
	@Test
	public void setPassword_unknownOwner() throws IOException {
		
		PasswordDTO dto = new PasswordDTO();
		dto.setNewPassword(randomUUID().toString());
		assertError(NOT_FOUND, () -> api.setPassword(randomUUID().toString(), dto));
		assertError(NOT_FOUND, () -> api.setPassword(null, dto));
	}
}
