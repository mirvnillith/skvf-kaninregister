package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;

import se.skvf.kaninregister.model.Bunny;

public class CreateBunnyTest extends BunnyRegistryApiTest {

	@Captor
	private ArgumentCaptor<Bunny> bunny;
	
	@Test
	public void createBunny() throws IOException {
		
		String ownerId = mockOwner().getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		
		String bunnyId = randomUUID().toString();
		when(registry.add(bunny.capture())).thenReturn(bunnyId);
		
		dto = api.createBunny(ownerId, dto);
		
		assertThat(dto.getId()).isEqualTo(bunnyId);
		assertThat(dto.getOwner()).isEqualTo(ownerId);
		
		assertBunny(dto, bunny.getValue().setId(bunnyId));
	}
	
	@Test
	public void createBunny_sameDtoOwner() throws IOException {
		
		String ownerId = mockOwner().getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		dto.setOwner(ownerId);
		
		String bunnyId = randomUUID().toString();
		when(registry.add(bunny.capture())).thenReturn(bunnyId);
		
		dto = api.createBunny(ownerId, dto);
		
		assertThat(dto.getId()).isEqualTo(bunnyId);
		assertThat(dto.getOwner()).isEqualTo(ownerId);
		
		assertBunny(dto, bunny.getValue().setId(bunnyId));
	}
	
	@Test
	public void createBunny_error() throws IOException {
		
		String ownerId = mockOwner().getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		
		when(registry.add(bunny.capture())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.createBunny(ownerId, dto));
	}
	
	@Test
	public void createBunny_differentDtoOwner() throws IOException {
		
		String ownerId = mockOwner().getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		dto.setOwner(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.createBunny(ownerId, dto));
	}
	
	@Test
	public void createBunny_dtoId() throws IOException {
		
		String ownerId = mockOwner().getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		dto.setId(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.createBunny(ownerId, dto));
	}
	
	@Test
	public void createBunny_noOwner() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		
		assertError(NOT_FOUND, () -> api.createBunny(ownerId, dto));
	}
	
	@Test
	public void createBunny_noSession() throws IOException {
		
		String ownerId = mockOwner().getId();
		BunnyDTO dto = new BunnyDTO();
		
		assertError(UNAUTHORIZED, () -> api.createBunny(ownerId, dto));
	}
}
