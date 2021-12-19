package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.api.BunnyGender.FEMALE;
import static se.skvf.kaninregister.api.BunnyGender.UNKNOWN;
import static se.skvf.kaninregister.model.Bunny.IdentifierLocation.RING;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;

public class CreateBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void createBunny() throws IOException {
		
		String ownerId = mockOwner()
				.setSignature("-")
				.getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setChip(randomUUID().toString());
		dto.setCoat(randomUUID().toString());
		dto.setColourMarkings(randomUUID().toString());
		dto.setFeatures(randomUUID().toString());
		dto.setGender(FEMALE);
		dto.setBreeder(ownerId);
		dto.setLeftEar(randomUUID().toString());
		dto.setNeutered(true);
		dto.setPicture(randomUUID().toString());
		dto.setRace(randomUUID().toString());
		dto.setRightEar(randomUUID().toString());
		dto.setRing(randomUUID().toString());
		
		String bunnyId = randomUUID().toString();
		when(registry.add(bunnyArgument.capture())).thenReturn(bunnyId);
		
		dto = api.createBunny(ownerId, dto);
		
		assertThat(dto.getId()).isEqualTo(bunnyId);
		assertThat(dto.getOwner()).isEqualTo(ownerId);
		
		assertBunny(dto, bunnyArgument.getValue().setId(bunnyId));
	}
	
	@Test
	public void createBunny_unknownGender() throws IOException {
		
		String ownerId = mockOwner()
				.setSignature("-")
				.getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		dto.setGender(UNKNOWN);
		
		String bunnyId = randomUUID().toString();
		when(registry.add(bunnyArgument.capture())).thenReturn(bunnyId);
		
		dto = api.createBunny(ownerId, dto);
		
		assertThat(dto.getGender()).isEqualTo(UNKNOWN);
		assertThat(bunnyArgument.getValue().getGender()).isNull();
	}
	
	@Test
	public void createBunny_sameDtoOwner() throws IOException {
		
		String ownerId = mockOwner()
				.setSignature("-")
				.getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		dto.setOwner(ownerId);
		
		String bunnyId = randomUUID().toString();
		when(registry.add(bunnyArgument.capture())).thenReturn(bunnyId);
		
		dto = api.createBunny(ownerId, dto);
		
		assertThat(dto.getId()).isEqualTo(bunnyId);
		assertThat(dto.getOwner()).isEqualTo(ownerId);
		
		assertBunny(dto, bunnyArgument.getValue().setId(bunnyId));
	}
	
	@Test
	public void createBunny_duplicateIdentifier() throws IOException {
		
		String ownerId = mockOwner()
				.setSignature("-")
				.getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		dto.setOwner(ownerId);
		dto.setRing(randomUUID().toString());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(new Bunny()));
		
		assertError(CONFLICT, () -> api.createBunny(ownerId, dto));
		
		assertThat(filterArgument.getValue().get(RING.getColumn())).accepts(dto.getRing());
		verify(registry, never()).add(bunnyArgument.capture());
	}
	
	@Test
	public void createBunny_error() throws IOException {
		
		String ownerId = mockOwner()
				.setSignature("-")
				.getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		
		when(registry.add(bunnyArgument.capture())).thenThrow(IOException.class);
		
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
	public void createBunny_otherBreeder() throws IOException {
		
		String ownerId = mockOwner()
				.setSignature("-")
				.getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		dto.setBreeder(randomUUID().toString());
		
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
	
	@Test
	public void createBunny_notApproved() throws IOException {
		
		String ownerId = mockOwner().getId();
		mockSession(ownerId);
		BunnyDTO dto = new BunnyDTO();
		
		assertError(PRECONDITION_FAILED, () -> api.createBunny(ownerId, dto));
	}
}
