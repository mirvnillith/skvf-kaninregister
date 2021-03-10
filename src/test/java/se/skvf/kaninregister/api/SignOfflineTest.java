package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class SignOfflineTest extends BunnyRegistryApiTest {

	@Test
	public void signOffline() throws IOException {
		
		String token = randomUUID().toString();
		OfflineSignatureDTO dto = new OfflineSignatureDTO();
		dto.setSubject(randomUUID().toString());
		dto.setSuccess(true);
		
		api.signOffline(token, dto);
		
		verify(signingService).setSigningState(token, dto.getSubject(), dto.getSuccess());
	}
}
