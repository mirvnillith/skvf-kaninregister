package se.skvf.kaninregister.addo;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static se.skvf.kaninregister.addo.OfflineSigning.OFFLINE_URL;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import net.vismaaddo.api.InitiateSigningRequestDTO;
import net.vismaaddo.api.LoginRequestDTO;
import net.vismaaddo.api.VismaAddoApi;
import se.skvf.kaninregister.BunnyTest;

public class AddoSigningServiceOfflineTest extends BunnyTest {
	
	private static final Optional<Boolean> SIGNING_ONGOING = Optional.empty();
	private static final Optional<Boolean> SIGNING_SUCCESSFUL = Optional.of(true);
	private static final Optional<Boolean> SIGNING_FAILED = Optional.of(false);

	@InjectMocks
	private AddoSigningService service;

	@Mock
	private VismaAddoApi addo;

	@Captor
	private ArgumentCaptor<LoginRequestDTO> loginRequest;
	
	@Captor
	private ArgumentCaptor<InitiateSigningRequestDTO> signingRequest;
	
	@BeforeEach
	public void setup() {
		service.setApprovalUrl("pdf");
	}
	
	@Test
	public void startSigning() throws Exception {
		
		Signing signing = service.startSigning();
		assertThat(signing.getToken())
			.isNotNull();
		assertThat(signing.getTransactionUrl())
			.startsWith(OFFLINE_URL)
			.endsWith(signing.getToken());
	}
	
	@Test
	public void noSigning() throws Exception {
		
		service.setApprovalUrl("");
		
		Signing signing = service.startSigning();
		assertThat(signing.getToken())
			.isNull();
		assertThat(signing.getTransactionUrl())
			.isNull();
	}
	
	@ParameterizedTest
	@MethodSource("signingStatusScenarios")
	public void checkSigning(Boolean state, Optional<Boolean> expectedStatus) throws Exception {
		
		String token = service.startSigning().getToken();
		
		if (state != null) {
			service.setSigningState(token, null, state);
		}
		
		assertThat(service.checkSigning(token)).isEqualTo(expectedStatus);
	}
	
	public static Stream<Arguments> signingStatusScenarios() {
		return Stream.of(
					Arguments.of(null, SIGNING_ONGOING),
					Arguments.of(TRUE, SIGNING_SUCCESSFUL),
					Arguments.of(FALSE, SIGNING_FAILED)
				);
	}

	@Test
	public void getSignature() throws Exception {
		
		String token = service.startSigning().getToken();
		String subject = randomUUID().toString();
		
		service.setSigningState(token, subject, true);
		
		assertThat(service.getSignature(token))
			.extracting("subject", "signature")
			.containsExactly(subject, token);
		
		assertThat(service.getSignature(token)).isNull();
	}
}
