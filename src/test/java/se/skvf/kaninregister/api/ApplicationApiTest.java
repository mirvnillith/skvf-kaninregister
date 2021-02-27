package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.apache.cxf.jaxrs.client.WebClient.getConfig;
import static org.apache.cxf.message.Message.MAINTAIN_SESSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static se.skvf.kaninregister.data.Table.ALL;

import java.io.IOException;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import se.skvf.kaninregister.addo.AddoSigningService;
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
		AddoSigningService addoSigningService;
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
	
	@Autowired
	private Registry registry;
	
	private BunnyRegistryApi api;
	
	@BeforeEach
	public void setup() {
		api = JAXRSClientFactory.create("http://localhost:" + port + "/api", BunnyRegistryApi.class, asList(new JacksonJaxbJsonProvider()));
		getConfig(api).getRequestContext().put(MAINTAIN_SESSION, true); 
	}
	
	@Test
	public void scenario() throws IOException {
		
		// Breeder creates an account ...
		OwnerDTO breeder = new OwnerDTO();
		breeder.setUserName(randomUUID().toString());
		breeder.setFirstName(randomUUID().toString());
		breeder.setLastName(randomUUID().toString());
		breeder = api.createOwner(breeder);
		
		// ... and activates by user name
		ActivationDTO activation = new ActivationDTO();
		activation.setUserName(breeder.getUserName());
		activation.setPassword(randomUUID().toString());
		api.activateOwner(breeder.getId(), activation);
		
		// Breeder logs in ...
		LoginDTO loginDTO = new LoginDTO();
		loginDTO.setUserName(breeder.getUserName());
		String breederPassword = activation.getPassword();
		loginDTO.setPassword(breederPassword);
		api.login(loginDTO);
		
		// ... approves ...
		api.approveOwner(breeder.getId());
		
		// .. and creates a bunny
		BunnyDTO bunny = new BunnyDTO();
		bunny.setBreeder(breeder.getId());
		bunny.setName("Bunny");
		bunny.setOwner(breeder.getId());
		bunny = api.createBunny(breeder.getId(), bunny);
		
		// Time passes
		api.logout();
		api.login(loginDTO);
		
		// Breeder sells bunny to a new owner ...
		BunnyDTO transfer = new BunnyDTO();
		transfer.setOwner("");
		bunny = api.updateBunny(breeder.getId(), bunny.getId(), transfer);
		// ... reverts ...
		bunny = api.revertBunnyOwner(bunny.getId());
		// ... and sells again
		bunny = api.updateBunny(breeder.getId(), bunny.getId(), transfer);
		
		api.logout();
		
		// New owner activates by bunny ...
		activation = new ActivationDTO();
		activation.setBunny(bunny.getId());
		activation.setUserName(randomUUID().toString());
		String ownerPassword = randomUUID().toString();
		activation.setPassword(ownerPassword);
		api.activateOwner(bunny.getOwner(), activation);
		
		// ... and renames bunny
		loginDTO.setUserName(activation.getUserName());
		loginDTO.setPassword(activation.getPassword());
		OwnerDTO owner = api.login(loginDTO);
		api.approveOwner(owner.getId());
		BunnyDTO rename = new BunnyDTO();
		rename.setName("My Bunny");
		bunny = api.updateBunny(owner.getId(), bunny.getId(), rename);
		api.logout();
		
		final String ownerId = owner.getId();
		final String breederId = breeder.getId();
		assertThat(api.getBunny(bunny.getId()))
			.satisfies(b -> assertThat(b.getOwner()).isEqualTo(ownerId))
			.satisfies(b -> assertThat(b.getPreviousOwner()).isEqualTo(breederId))
			.satisfies(b -> assertThat(b.getBreeder()).isEqualTo(breederId))
			.satisfies(b -> assertThat(b.getName()).isEqualTo("My Bunny"));
		
		// Delete breeder
		loginDTO.setUserName(breeder.getUserName());
		loginDTO.setPassword(breederPassword);
		api.login(loginDTO);
		api.unapproveOwner(breeder.getId());
		assertThat(api.getOwner(breeder.getId()).getFirstName()).isEqualTo("Okänd");
		api.deleteOwner(breeder.getId());
		
		// Delete bunny ...
		loginDTO.setUserName(owner.getUserName());
		loginDTO.setPassword(ownerPassword);
		api.login(loginDTO);
		api.deleteBunny(owner.getId(), bunny.getId());
		// ... and owner
		api.deleteOwner(owner.getId());
		
		assertThat(registry.findBunnies(ALL)).isEmpty();
		assertThat(registry.findOwners(ALL)).isEmpty();
	}
}
