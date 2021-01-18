package se.skvf.kaninregister.api;

import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.UUID;

import javax.validation.Valid;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
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
	}
	
	@Test
	public void scenario() {
		
		OwnerDTO breeder = new OwnerDTO();
		breeder.setEmail(randomUUID().toString());
		breeder.setFirstName(randomUUID().toString());
		breeder = api.createOwner(breeder);
		
		PasswordDTO breederPassword = new PasswordDTO();
		breederPassword.setEmail(breeder.getEmail());
		breederPassword.setNewPassword(randomUUID().toString());
		api.setPassword(breeder.getId(), breederPassword);
	}
}
