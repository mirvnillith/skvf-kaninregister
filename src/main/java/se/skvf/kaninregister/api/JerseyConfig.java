package se.skvf.kaninregister.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
@ApplicationPath("/api") 
public class JerseyConfig extends ResourceConfig {

	public JerseyConfig() {
		register(BunnyRegistryApiImpl.class);
	}
}
