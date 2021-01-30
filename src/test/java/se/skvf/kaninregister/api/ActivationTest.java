package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class ActivationTest extends BunnyRegistryApiTest {

	@Test
	public void activate_byBunny() throws IOException {
		
		Owner owner = mockOwner();
		Bunny bunny = mockBunny().setOwner(owner.getId());	
		
		String userName = randomUUID().toString();
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setBunny(bunny.getId());
		dto.setUserName(userName);
		dto.setPassword(password);
		api.activate(owner.getId(), dto);
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.getUserName()).isEqualTo(userName);
		assertThat(updatedOwner.validate(password)).isTrue();
	}
	
	@Test
	public void activate_notOwnedBunny() throws IOException {
		
		Owner owner = mockOwner();
		Bunny bunny = mockBunny();	
		
		String userName = randomUUID().toString();
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setBunny(bunny.getId());
		dto.setUserName(userName);
		dto.setPassword(password);
		
		assertError(BAD_REQUEST, () -> api.activate(owner.getId(), dto));
	}
	
	@Test
	public void activate_byUserName() throws IOException {
		
		Owner owner = mockOwner();
		owner.setUserName(randomUUID().toString());
		
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(owner.getUserName());
		dto.setPassword(password);
		api.activate(owner.getId(), dto);
		
		verify(registry).update(ownerArgument.capture());
		
		Owner updatedOwner = ownerArgument.getValue();
		assertThat(updatedOwner.validate(password)).isTrue();
	}
	
	@Test
	public void activate_otherUserName() throws IOException {
		
		Owner owner = mockOwner();
		owner.setUserName(randomUUID().toString());
		
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(randomUUID().toString());
		dto.setPassword(password);
		
		assertError(BAD_REQUEST, () -> api.activate(owner.getId(), dto));
	}
	
	@Test
	public void activate_alreadyActivated() throws IOException {
		
		Owner owner = mockOwner();
		owner.setUserName(randomUUID().toString());
		
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(owner.getUserName());
		dto.setPassword(password);
		api.activate(owner.getId(), dto);
		
		assertError(Status.UNAUTHORIZED, () -> api.activate(owner.getId(), dto));
	}
	
	@Test
	public void activate_invalidPassword() throws IOException {
		
		Owner owner = mockOwner();
		owner.setUserName(randomUUID().toString());

		ActivationDTO dto = new ActivationDTO();
		dto.setUserName(owner.getUserName());
		
		assertError(BAD_REQUEST, () -> api.activate(owner.getId(), dto));
		dto.setPassword("");
		assertError(BAD_REQUEST, () -> api.activate(owner.getId(), dto));
		dto.setPassword(" ");
		assertError(BAD_REQUEST, () -> api.activate(owner.getId(), dto));
		dto.setPassword(" \t");
		assertError(BAD_REQUEST, () -> api.activate(owner.getId(), dto));
		dto.setPassword(" \t\n");
		assertError(BAD_REQUEST, () -> api.activate(owner.getId(), dto));
		
		verify(registry, never()).update(owner);
	}
	
	@Test
	public void activate_unknownOwner() throws IOException {
		
		ActivationDTO dto = new ActivationDTO();
		assertError(NOT_FOUND, () -> api.activate(randomUUID().toString(), dto));
		assertError(NOT_FOUND, () -> api.activate(null, dto));
	}
	
	@Test
	public void activate_unknownBunny() throws IOException {
		
		Owner owner = mockOwner();
		
		String userName = randomUUID().toString();
		String password = randomUUID().toString();
		
		ActivationDTO dto = new ActivationDTO();
		dto.setBunny(randomUUID().toString());
		dto.setUserName(userName);
		dto.setPassword(password);

		assertError(NOT_FOUND, () -> api.activate(owner.getId(), dto));
	}
}
