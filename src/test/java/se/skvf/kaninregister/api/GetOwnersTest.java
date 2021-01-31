package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Owner;

public class GetOwnersTest extends BunnyRegistryApiTest {

	@Test
	public void getOwners_all() throws IOException {
		
		mockSession(randomUUID().toString());
		Owner owner = mockOwner();
		Owner breeder = mockOwner()
				.setBreeder(true)
				.setBreederName(randomUUID().toString());
		
		when(registry.findOwners(filterArgument.capture())).thenReturn(asList(owner, breeder));
		
		OwnerList list = api.getOwners(null);
		
		assertOwner(list.getOwners().get(0), owner);
		assertOwner(list.getOwners().get(1), breeder);
		
		assertThat(filterArgument.getValue().get("Uppfödare"))
			.accepts("Ja", "Nej", "");
	}
	
	@Test
	public void getOwners_includeBreeders() throws IOException {
		
		mockSession(randomUUID().toString());
		Owner owner = mockOwner();
		Owner breeder = mockOwner().setBreeder(true);
		
		when(registry.findOwners(filterArgument.capture())).thenReturn(asList(owner, breeder));
		
		OwnerList list = api.getOwners(false);
		
		assertOwner(list.getOwners().get(0), owner);
		assertOwner(list.getOwners().get(1), breeder);
		
		assertThat(filterArgument.getValue().get("Uppfödare"))
			.accepts("Ja", "Nej", "");
	}
	
	@Test
	public void getOwners_onlyBreeders() throws IOException {
		
		mockSession(randomUUID().toString());
		Owner breeder = mockOwner().setBreeder(true);
		
		when(registry.findOwners(filterArgument.capture())).thenReturn(asList(breeder));
		
		OwnerListDTO dto = api.getOwners(true).getOwners().get(0);
		
		assertOwner(dto, breeder);
		
		assertThat(filterArgument.getValue().get("Uppfödare"))
			.accepts("Ja")
			.rejects("Nej", "");
	}
	
	@Test
	public void getOwners_noSession() throws IOException {
		
		assertError(UNAUTHORIZED, () -> api.getOwners(null));
	}
	
	@Test
	public void getOwners_error() throws IOException {
		
		mockSession(randomUUID().toString());
		when(registry.findOwners(filterArgument.capture())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.getOwners(false));
	}
}
