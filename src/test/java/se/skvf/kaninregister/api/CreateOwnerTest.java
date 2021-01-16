package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class CreateOwnerTest extends BunnyRegistryApiTest {

	@Test
	public void createOwner() throws IOException {
		
		OwnerDTO dto = new OwnerDTO();
		
		String ownerId = randomUUID().toString();
		when(registry.add(ownerArgument.capture())).thenReturn(ownerId);
		
		dto = api.createOwner(dto);
		
		assertThat(dto.getId()).isEqualTo(ownerId);
		
		assertOwner(dto, ownerArgument.getValue().setId(ownerId));
	}
	
	@Test
	public void createOwner_error() throws IOException {
		
		OwnerDTO dto = new OwnerDTO();
		
		when(registry.add(ownerArgument.capture())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.createOwner(dto));
	}
	
	@Test
	public void createOwner_dtoId() throws IOException {
		
		OwnerDTO dto = new OwnerDTO();
		dto.setId(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.createOwner(dto));
	}
}
