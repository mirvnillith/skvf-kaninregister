package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.api.BunnyRegistryApiImpl.TRANSFER_OWNER;

import java.io.IOException;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Bunny.Gender;
import se.skvf.kaninregister.model.Owner;

public class GetOwnerBunniesTest extends BunnyRegistryApiTest {

	@Test
	public void getOwnerBunnies() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		Bunny bunny = mockBunny()
				.setOwner(owner.getId())
				.setChip(randomUUID().toString())
				.setLeftEar(randomUUID().toString())
				.setRightEar(randomUUID().toString())
				.setRing(randomUUID().toString());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny)).thenReturn(emptyList());
		
		OwnerBunnyList bunnies = api.getOwnerBunnies(owner.getId(), null);
		
		assertThat(bunnies.getBunnies())
			.hasSize(1)
			.allSatisfy(b -> assertBunny(b, bunny));
	}
	
	@Test
	public void getOwnerBunnies_sorting() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		Bunny first = mockBunny()
				.setOwner(owner.getId())
				.setName("A")
				.setBirthDate("1")
				.setRace("a")
				.setGender(Gender.MALE);
		Bunny second = mockBunny()
				.setOwner(owner.getId())
				.setName("B")
				.setBirthDate("2")
				.setRace("b")
				.setGender(Gender.FEMALE);
		Bunny third = mockBunny()
				.setOwner(owner.getId())
				.setName("C")
				.setBirthDate("3")
				.setRace("c");
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(asList(second, first, third));
		
		assertBunnyOrder(owner, null, "A", "B", "C");
		
		assertBunnyOrder(owner, "Name", "A", "B", "C");
		assertBunnyOrder(owner, "nAme_asc", "A", "B", "C");
		assertBunnyOrder(owner, "naMe_desc", "C", "B", "A");
		
		assertBunnyOrder(owner, "Birthdate", "A", "B", "C");
		assertBunnyOrder(owner, "bIrthdate_asc", "A", "B", "C");
		assertBunnyOrder(owner, "biRthdate_desc", "C", "B", "A");
		
		assertBunnyOrder(owner, "Race", "A", "B", "C");
		assertBunnyOrder(owner, "rAce_asc", "A", "B", "C");
		assertBunnyOrder(owner, "raCe_desc", "C", "B", "A");
		
		assertBunnyOrder(owner, "Gender", "A", "B", "C");
		assertBunnyOrder(owner, "gEnder_asc", "A", "B", "C");
		assertBunnyOrder(owner, "geNder_desc", "C", "B", "A");
		
		WebApplicationException expected = assertThrows(WebApplicationException.class, () -> assertBunnyOrder(owner, "ASC", "C", "B", "A"));
		assertThat(expected.getResponse().getStatus()).isEqualTo(BAD_REQUEST.getStatusCode());
	}
	
	private void assertBunnyOrder(Owner owner, String order, String... expected) {
		OwnerBunnyList bunnies = api.getOwnerBunnies(owner.getId(), order);
		
		assertThat(bunnies.getBunnies())
			.extracting(OwnerBunnyListDTO::getName)
			.containsExactly(expected);		
	}

	@Test
	public void getOwnerBunnies_transferred() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		Owner newOwner = mockOwner()
				.setName(TRANSFER_OWNER);
		Bunny bunny = mockBunny()
				.setOwner(newOwner.getId())
				.setPreviousOwner(owner.getId());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		assertThat(api.getOwnerBunnies(owner.getId(), null).getBunnies())
			.hasSize(2)
			.anySatisfy(own -> assertThat(own.getClaimToken()).isNull())
			.anySatisfy(transfer -> assertThat(transfer.getClaimToken()).isEqualTo(newOwner.getId()));
	}
	
	@Test
	public void getOwnerBunnies_transferredActivated() throws IOException {
		
		Owner owner = mockOwner();
		mockSession(owner.getId());
		
		Owner newOwner = mockOwner()
				.setPassword(randomUUID().toString());
		Bunny bunny = mockBunny()
				.setOwner(newOwner.getId())
				.setPreviousOwner(owner.getId());
		
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		assertThat(api.getOwnerBunnies(owner.getId(), null).getBunnies())
			.hasSize(1)
			.anySatisfy(own -> assertThat(own.getClaimToken()).isNull());
	}
	
	@Test
	public void getOwnerBunnies_noSession() throws IOException {
		
		String ownerId = randomUUID().toString();
		
		assertError(UNAUTHORIZED, () -> api.getOwnerBunnies(ownerId, null));
	}
	
	@Test
	public void getOwnerBunnies_oldSession() throws IOException {
		
		String ownerId = randomUUID().toString();
		String sessionId = mockSession(ownerId);
		reset(sessions);
		
		assertError(UNAUTHORIZED, () -> api.getOwnerBunnies(ownerId, null));
		
		assertCookies(sessionId, false);
	}
	
	@Test
	public void getOwnerBunnies_error() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		when(registry.findOwners(singleton(ownerId))).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getOwnerBunnies(ownerId, null));
	}
	
	@Test
	public void getOwnerBunnies_unknownId() throws IOException {
		
		String ownerId = randomUUID().toString();
		mockSession(ownerId);
		
		assertError(NOT_FOUND, () -> api.getOwnerBunnies(ownerId, null));
	}
}
