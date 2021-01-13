package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class CreateOwnerTest extends BunnyRegistryApiTest {

	@Captor
	private ArgumentCaptor<Owner> owner;
	
	@Test
	public void createOwner() throws IOException {
		
		OwnerDTO dto = new OwnerDTO();
		
		String ownerId = randomUUID().toString();
		when(registry.add(owner.capture())).thenReturn(ownerId);
		
		dto = api.createOwner(dto);
		
		assertThat(dto.getId()).isEqualTo(ownerId);
		
		assertOwner(dto, owner.getValue().setId(ownerId));
	}
	
	private void assertOwner(OwnerDTO expected, Owner actual) {
		assertAll(
				() -> assertThat(actual.getId()).isEqualTo(expected.getId()),
				() -> assertThat(actual.getFirstName()).isEqualTo(expected.getFirstName()),
				() -> assertThat(actual.getLastName()).isEqualTo(expected.getLastName())
				);
	}

	@Test
	public void createOwner_error() throws IOException {
		
		OwnerDTO dto = new OwnerDTO();
		
		when(registry.add(owner.capture())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.createOwner(dto));
	}
	
	@Test
	public void createOwner_dtoId() throws IOException {
		
		OwnerDTO dto = new OwnerDTO();
		dto.setId(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
	}
}
