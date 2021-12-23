package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.api.BunnyIdentifierLocation.CHIP;
import static se.skvf.kaninregister.api.BunnyIdentifierLocation.LEFT_EAR;
import static se.skvf.kaninregister.api.BunnyIdentifierLocation.RIGHT_EAR;

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
		BunnyList bunnies = api.findBunnies(asList(CHIP), asList(identifier));
		
		assertThat(bunnies.getBunnies())
			.hasSize(1)
			.allSatisfy(b -> assertBunny(b, bunny));
		assertThat(filterArgument.getValue().values())
			.allSatisfy(f -> assertThat(f).accepts(identifier));
	}
	
	@Test
	public void findBunnies_error() throws IOException {
		
		when(registry.findBunnies(filterArgument.capture())).thenThrow(IOException.class);
		
		assertError(INTERNAL_SERVER_ERROR, () -> api.findBunnies(asList(CHIP), asList(randomUUID().toString())));
	}
	
	@Test
	public void findBunnies_mismatch() throws IOException {
		
		assertError(BAD_REQUEST, () -> api.findBunnies(asList(LEFT_EAR, RIGHT_EAR), asList(randomUUID().toString())));
	}
	@Test
	public void findBunnies_noLocation() throws IOException {
		
		assertError(BAD_REQUEST, () -> api.findBunnies(null, asList(randomUUID().toString())));
		assertError(BAD_REQUEST, () -> api.findBunnies(emptyList(), asList(randomUUID().toString())));
	}
	
	@Test
	public void findBunnies_noIdentifier() throws IOException {
		
		assertError(BAD_REQUEST, () -> api.findBunnies(asList(CHIP), null));
		assertError(BAD_REQUEST, () -> api.findBunnies(asList(CHIP), emptyList()));
	}
	
	@Test
	public void findBunnies_tooManyWildcards() throws IOException {
		
		api.findBunnies(asList(CHIP), asList("?"));
		api.findBunnies(asList(CHIP), asList("??"));
		assertError(BAD_REQUEST, () -> api.findBunnies(asList(CHIP), asList("???")));
	}
	
	@Test
	public void findBunnies_tooManyBunnies() throws IOException {
		
		List<Bunny> bunnies = new ArrayList<>();
		when(registry.findBunnies(filterArgument.capture())).thenReturn(bunnies);
		while (bunnies.size() <= 10) {
			api.findBunnies(asList(CHIP), asList(randomUUID().toString()));
			bunnies.add(mockBunny());
		}
		assertError(NO_CONTENT, () -> api.findBunnies(asList(CHIP), asList(randomUUID().toString())));
	}
}
