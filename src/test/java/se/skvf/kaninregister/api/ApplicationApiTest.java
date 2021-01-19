package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.apache.cxf.jaxrs.client.WebClient.getConfig;
import static org.apache.cxf.message.Message.MAINTAIN_SESSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.message.Message;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import se.skvf.kaninregister.data.Database;
import se.skvf.kaninregister.drive.GoogleDrive;
import se.skvf.kaninregister.model.Registry;

@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"}, webEnvironment = RANDOM_PORT)
public class ApplicationApiTest {

	@TestConfiguration
    static class ApplicationApiTestConfiguration {

		@MockBean
		GoogleDrive googleDrive;
		@MockBean
		Database database;
		
		@Bean
		@Primary
		public Registry registry() {
			return new RegistryStub();
		}
	}

	@LocalServerPort
	private int port;
	
	private BunnyRegistryApi api;
	
	@BeforeEach
	public void setup() {
		api = JAXRSClientFactory.create("http://localhost:" + port + "/api", BunnyRegistryApi.class, asList(new JacksonJaxbJsonProvider()));
		getConfig(api).getRequestContext().put(MAINTAIN_SESSION, true); 
	}
	
	@Test
	public void scenario() {
		
		// Breeder creates an account ...
		OwnerDTO breeder = new OwnerDTO();
		breeder.setEmail(randomUUID().toString());
		breeder.setFirstName(randomUUID().toString());
		breeder = api.createOwner(breeder);
		
		// ... and sets a password
		PasswordDTO breederPassword = new PasswordDTO();
		breederPassword.setEmail(breeder.getEmail());
		breederPassword.setNewPassword(randomUUID().toString());
		api.setPassword(breeder.getId(), breederPassword);
		
		// Breeder logs in ...
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setEmail(breeder.getEmail());
		loginDTO.setPassword(breederPassword.getNewPassword());
		api.login(loginDTO);
		
		// .. and creates a bunny
		BunnyDTO bunny = new BunnyDTO();
		bunny.setBreeder(breeder.getId());
		bunny.setName("Bunny");
		bunny.setOwner(breeder.getId());
		bunny = api.createBunny(breeder.getId(), bunny);
		
		// Time passes
		api.logout();
		api.login(loginDTO);
		
		// Breeder sells bunny to a new owner
		OwnerDTO owner = new OwnerDTO();
		owner.setEmail(randomUUID().toString());
		owner.setFirstName(randomUUID().toString());
		owner = api.createOwner(owner);
		BunnyDTO transfer = new BunnyDTO();
		transfer.setOwner(owner.getId());
		bunny = api.updateBunny(breeder.getId(), bunny.getId(), transfer);
		
		api.logout();
		
		// New owner sets a password ...
		PasswordDTO ownerPassword = new PasswordDTO();
		ownerPassword.setBunny(bunny.getId());
		ownerPassword.setNewPassword(randomUUID().toString());
		api.setPassword(owner.getId(), ownerPassword);
		
		// ... and renames bunny
		loginDTO.setEmail(owner.getEmail());
		loginDTO.setPassword(ownerPassword.getNewPassword());
		api.login(loginDTO);
		BunnyDTO rename = new BunnyDTO();
		rename.setName("My Bunny");
		bunny = api.updateBunny(owner.getId(), bunny.getId(), rename);
		api.logout();
		
		final String ownerId = owner.getId();
		final String breederId = breeder.getId();
		assertThat(api.getBunny(bunny.getId()))
			.satisfies(b -> assertThat(b.getOwner()).isEqualTo(ownerId))
			.satisfies(b -> assertThat(b.getBreeder()).isEqualTo(breederId))
			.satisfies(b -> assertThat(b.getName()).isEqualTo("My Bunny"));
	}
}
