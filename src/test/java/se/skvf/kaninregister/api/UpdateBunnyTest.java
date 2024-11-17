package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.api.BunnyGender.FEMALE;
import static se.skvf.kaninregister.api.BunnyGender.UNKNOWN;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;

public class UpdateBunnyTest extends BunnyRegistryApiTest {

	@Test
	public void updateBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setBirthDate(randomUUID().toString());
		dto.setChip(randomUUID().toString());
		dto.setCoat(randomUUID().toString());
		dto.setColourMarkings(randomUUID().toString());
		dto.setFeatures(randomUUID().toString());
		dto.setGender(FEMALE);
		dto.setLeftEar(randomUUID().toString());
		dto.setNeutered(true);
		dto.setPicture(randomUUID().toString());
		dto.setRace(randomUUID().toString());
		dto.setRightEar(randomUUID().toString());
		dto.setRing(randomUUID().toString());
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertBunny(dto, bunny);
	}
	
	@Test
	public void updateBunny_unknownGender() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setGender(Bunny.Gender.MALE);
		
		BunnyDTO dto = new BunnyDTO();
		dto.setGender(UNKNOWN);
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getGender()).isEqualTo(UNKNOWN);
		assertThat(bunny.getGender()).isNull();
	}
	
	@Test
	public void updateBunny_newOwnerIgnored() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setOwner(randomUUID().toString());
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getOwner()).isEqualTo(owner.getId());
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getOwner()).isEqualTo(owner.getId());
	}
	
	@Test
	public void updateBunny_sameBreeder() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setBreeder(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setBreeder(owner.getId());
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getOwner()).isEqualTo(owner.getId());
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getBreeder()).isEqualTo(owner.getId());
	}
	
	@Test
	public void updateBunny_sameId() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setBreeder(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setId(bunny.getId());
		dto.setName(randomUUID().toString());
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getOwner()).isEqualTo(owner.getId());
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getName()).isEqualTo(dto.getName());
	}
	
	@Test
	public void updateBunny_noBreeder() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setBreeder(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setBreeder("");
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getBreeder()).isEmpty();
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getBreeder()).isEmpty();
	}
	
	@Test
	public void updateBunny_unknownBreeder() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(singleton(owner.getId()))).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setBreeder(randomUUID().toString());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setBreeder(bunny.getBreeder());
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getBreeder()).isEmpty();
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getBreeder()).isEmpty();
	}
	
	@Test
	public void updateBunny_noPreviousOwner() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setPreviousOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setPreviousOwner("");
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getPreviousOwner()).isEmpty();
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getPreviousOwner()).isEmpty();
	}
	
	@Test
	public void updateBunny_unknownPreviousOwner() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(singleton(owner.getId()))).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setPreviousOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		dto.setPreviousOwner(randomUUID().toString());
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(dto.getPreviousOwner()).isEmpty();
		
		verify(registry).update(bunnyArgument.capture());
		assertThat(bunnyArgument.getValue().getPreviousOwner()).isEmpty();
	}
	
	@Test
	public void updateBunny_duplicateIdentifierOtherBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature("-");
		mockSession(owner.getId());
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId());

		BunnyDTO dto = new BunnyDTO();
		dto.setChip(randomUUID().toString());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(new Bunny()));
		
		assertError(CONFLICT, () -> api.updateBunny(owner.getId(), bunny.getId(), dto));
		
		assertThat(filterArgument.getValue().get(Bunny.IdentifierLocation.CHIP.getColumn())).accepts(dto.getChip());
		verify(registry, never()).add(bunnyArgument.capture());
	}
	
	@Test
	public void updateBunny_duplicateIdentifierSameBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature("-");
		mockSession(owner.getId());
		
		Bunny bunny = mockBunny()
				.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setChip(randomUUID().toString());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		dto = api.updateBunny(owner.getId(), bunny.getId(), dto);
		
		assertThat(filterArgument.getValue().get(Bunny.IdentifierLocation.CHIP.getColumn())).accepts(dto.getChip());
		verify(registry).update(bunnyArgument.capture());
	}

	@Test
	public void updateBunny_unknownBunny() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());;
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.updateBunny(owner.getId(), randomUUID().toString(), dto));
	}
	
	@Test
	public void updateBunny_notSameId() throws IOException {
		
		Owner owner = mockOwner()
				.setSignature(randomUUID().toString());;
		mockSession(owner.getId());
		reset(registry);
		when(registry.findOwners(anyCollection())).thenReturn(asList(owner));
		
		Bunny bunny = mockBunny();
		String bunnyId = bunny.getId();
		bunny.setOwner(owner.getId());
		
		BunnyDTO dto = new BunnyDTO();
		dto.setId(randomUUID().toString());
		dto.setName(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.updateBunny(owner.getId(), bunnyId, dto));
	}
	
	@Test
	public void updateBunny_noSession() throws IOException {
		
		Owner owner = mockOwner();
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		
		assertError(UNAUTHORIZED, () -> api.updateBunny(owner.getId(), randomUUID().toString(), dto));
	}
	
	@Test
	public void updateBunny_unknownOwner() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		Bunny bunny = mockBunny();
		bunny.setOwner(ownerId);
		
		BunnyDTO dto = new BunnyDTO();
		dto.setName(randomUUID().toString());
		
		assertError(NOT_FOUND, () -> api.updateBunny(ownerId, bunny.getId(), dto));
	}
	
	@Test
	public void updateBunny_otherBreeder() throws IOException {
		
		String ownerId = mockOwner()
				.setSignature("-")
				.getId();
		mockSession(ownerId);
		
		Bunny bunny = mockBunny();
		bunny.setOwner(ownerId);
		
		BunnyDTO dto = new BunnyDTO();
		dto.setBreeder(randomUUID().toString());
		
		assertError(BAD_REQUEST, () -> api.updateBunny(ownerId, bunny.getId(), dto));
	}
}
