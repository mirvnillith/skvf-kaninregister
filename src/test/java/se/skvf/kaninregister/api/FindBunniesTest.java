package se.skvf.kaninregister.api;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.api.BunnyIdentifierLocation.CHIP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import se.skvf.kaninregister.model.Bunny;

public class FindBunniesTest extends BunnyRegistryApiTest {

	@Test
	public void findBunnies() throws IOException {
		
		Bunny bunny = mockBunny();
		when(registry.findBunnies(filterArgument.capture())).thenReturn(singleton(bunny));
		
		String identifier = randomUUID().toString();
		BunnyList bunnies = api.findBunnies(CHIP, identifier);
		
		assertThat(bunnies.getBunnies())
			.hasSize(1)
			.allSatisfy(b -> assertBunny(bunny, b));
		assertThat(filterArgument.getValue().values())
			.allSatisfy(f -> assertThat(f).accepts(identifier));
	}
	
	@Test
	public void findBunnies_error() throws IOException {
		
		when(registry.findBunnies(filterArgument.capture())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.findBunnies(CHIP, randomUUID().toString()));
	}
	
	@Test
	public void findBunnies_noLocation() throws IOException {
		
		assertError(BAD_REQUEST, () -> api.findBunnies(null, randomUUID().toString()));
	}
	
	@Test
	public void findBunnies_noIdentifier() throws IOException {
		
		assertError(BAD_REQUEST, () -> api.findBunnies(CHIP, null));
	}
	
	@Test
	public void findBunnies_tooManyWildcards() throws IOException {
		
		api.findBunnies(CHIP, "?");
		api.findBunnies(CHIP, "??");
		assertError(BAD_REQUEST, () -> api.findBunnies(CHIP, "???"));
	}
	
	@Test
	public void findBunnies_tooManyBunnies() throws IOException {
		
		List<Bunny> bunnies = new ArrayList<>();
		when(registry.findBunnies(filterArgument.capture())).thenReturn(bunnies);
		while (bunnies.size() <= 10) {
			api.findBunnies(CHIP, randomUUID().toString());
			bunnies.add(mockBunny());
		}
		assertError(NO_CONTENT, () -> api.findBunnies(CHIP, randomUUID().toString()));
	}
}
