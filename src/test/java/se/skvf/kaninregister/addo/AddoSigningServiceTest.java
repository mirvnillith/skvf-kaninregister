package se.skvf.kaninregister.addo;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static net.vismaaddo.api.DocumentDTO.MimeTypeEnum.APPLICATION_PDF;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.io.FileUtils.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.addo.AddoSigningService.NO_DISTRIBUTION;
import static se.skvf.kaninregister.addo.AddoSigningService.SWEDISH_BANKID;
import static se.skvf.kaninregister.addo.AddoSigningService.sha64;
import static se.skvf.kaninregister.addo.SigningState.CAMPAIGN_STARTED;
import static se.skvf.kaninregister.addo.SigningState.COMPLETED;
import static se.skvf.kaninregister.addo.SigningState.CREATED;
import static se.skvf.kaninregister.addo.SigningState.EXPIRED;
import static se.skvf.kaninregister.addo.SigningState.FAILED;
import static se.skvf.kaninregister.addo.SigningState.REJECTED;
import static se.skvf.kaninregister.addo.SigningState.STARTED;
import static se.skvf.kaninregister.addo.SigningState.STOPPED;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import javax.ws.rs.WebApplicationException;

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
import net.vismaaddo.api.InitiateSigningResponseDTO;
import net.vismaaddo.api.LoginRequestDTO;
import net.vismaaddo.api.RecipientStatusDTO;
import net.vismaaddo.api.SigningDTO;
import net.vismaaddo.api.SigningDataDTO;
import net.vismaaddo.api.SigningRecipientDTO;
import net.vismaaddo.api.SigningRequestDTO;
import net.vismaaddo.api.SigningStatusDTO;
import net.vismaaddo.api.TransactionStatusDTO;
import net.vismaaddo.api.VismaAddoApi;
import se.skvf.kaninregister.BunnyTest;

public class AddoSigningServiceTest extends BunnyTest {
	
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
	
	private String email;
	private String password;
	
	@BeforeEach
	public void setup() {
		email = randomUUID().toString();
		service.setEmail(email);
		password = randomUUID().toString();
		service.setPassword(password);
	}
	
	@Test
	public void startSigning_loginError() throws Exception {
		
		WebApplicationException error = mockLoginError();
		
		WebApplicationException exception = assertThrows(WebApplicationException.class, () -> service.startSigning(null, null));
		assertThat(exception).isSameAs(error);
		
		verifyLogin();
	}
	
	private void verifyLogin() {
		assertThat(loginRequest.getValue())
			.extracting("email", "password")
			.containsExactly(email, sha64(password));
	}

	@Test
	public void startSigning_signingError() throws Exception {
		
		String pnr = randomUUID().toString();
		File file = File.createTempFile("addo", "bin");
		file.deleteOnExit();
		String data = randomUUID().toString();
		write(file, data, defaultCharset());
		
		String session = mockSession();
		
		WebApplicationException error = new WebApplicationException(randomUUID().toString());
		when(addo.initiateSigning(signingRequest.capture())).thenThrow(error);
		try {

			WebApplicationException exception = assertThrows(WebApplicationException.class, () -> service.startSigning(pnr, file));
			assertThat(exception).isSameAs(error);
			
		} finally {
			verifySession(session);
		}
	}
	
	@Test
	public void startSigning_apiError() throws Exception {
		
		String pnr = randomUUID().toString();
		File file = File.createTempFile("addo", "bin");
		file.deleteOnExit();
		String data = randomUUID().toString();
		write(file, data, defaultCharset());
		
		String session = mockSession();
		
		NullPointerException error = new NullPointerException(randomUUID().toString());
		when(addo.initiateSigning(signingRequest.capture())).thenThrow(error);
		try {
			
			IOException exception = assertThrows(IOException.class, () -> service.startSigning(pnr, file));
			assertThat(exception.getCause()).isSameAs(error);
			
		} finally {
			verifySession(session);
		}
	}
	
	@Test
	public void startSigning() throws Exception {
		
		String pnr = randomUUID().toString();
		File file = File.createTempFile("addo", "bin");
		file.deleteOnExit();
		String data = randomUUID().toString();
		write(file, data, defaultCharset());
		
		String session = mockSession();
		
		InitiateSigningResponseDTO signingResponse = new InitiateSigningResponseDTO();
		signingResponse.setSigningToken(randomUUID().toString());
		
		when(addo.initiateSigning(signingRequest.capture())).thenReturn(signingResponse);
		
		SigningStatusDTO statusResponse = new SigningStatusDTO();
		RecipientStatusDTO recipient = new RecipientStatusDTO();
		TransactionStatusDTO transaction = new TransactionStatusDTO();
		transaction.setTransactionToken(randomUUID().toString());
		recipient.setTransactions(singletonList(transaction));
		statusResponse.setRecipients(singletonList(recipient));
		
		when(addo.getSigningStatus(session, signingResponse.getSigningToken())).thenReturn(statusResponse);

		try {
		
			Signing signing = service.startSigning(pnr, file);
			assertThat(signing.getToken())
				.isEqualTo(signingResponse.getSigningToken());
			assertThat(signing.getTransaction())
				.isEqualTo(transaction.getTransactionToken());

			InitiateSigningRequestDTO request = signingRequest.getValue();
			assertThat(request.getToken())
				.isEqualTo(session);
			
			SigningRequestDTO signingRequest = request.getRequest();
			assertThat(signingRequest.getName())
				.isEqualTo(AddoSigningService.SIGNING_NAME);
			assertThat(signingRequest.getStartDate())
				.startsWith("/Date(")
				.endsWith(")/");
			
			SigningDataDTO signingData = signingRequest.getSigningData();
			assertThat(signingData.getSender())
				.usingRecursiveComparison()
				.isEqualTo(AddoSigningService.createSender());
			assertThat(signingData.getAllowInboundEnclosures())
				.isFalse();
			assertThat(signingData.getAllowRecipientComment())
				.isFalse();
			
			assertThat(signingData.getRecipients())
				.hasSize(1)
				.allSatisfy(r -> {
					assertThat(r.getSendDistributionDocument()).isFalse();
					assertThat(r.getSendDistributionNotification()).isFalse();
					assertThat(r.getSendWelcomeNotification()).isFalse();
					assertThat(r.getDistributionMethod()).isEqualTo(NO_DISTRIBUTION);
					assertThat(r.getSigningMethod()).isEqualTo(SWEDISH_BANKID);
					assertThat(r.getSSN()).isEqualTo(pnr);
				});
			
			assertThat(signingData.getDocuments())
				.hasSize(1)
				.allSatisfy(d -> {
					assertThat(d.getName()).isEqualTo(file.getName());
					assertThat(d.getMimeType()).isEqualTo(APPLICATION_PDF);
					assertThat(d.getData()).isEqualTo(encodeBase64String(data.getBytes()));
				});
			
		} finally {
			verifySession(session);
		}
	}
	
	@Test
	public void checkSigning_loginError() throws Exception {
		
		WebApplicationException error = mockLoginError();
		
		WebApplicationException exception = assertThrows(WebApplicationException.class, () -> service.checkSigning(null));
		assertThat(exception).isSameAs(error);
		
		verifyLogin();
	}
	
	@Test
	public void checkSigning_statusError() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		WebApplicationException error = new WebApplicationException(token);
		when(addo.getSigningStatus(session, token)).thenThrow(error);
		try {
			
			WebApplicationException exception = assertThrows(WebApplicationException.class, () -> service.checkSigning(token));
			assertThat(exception).isSameAs(error);
			
		} finally {
			verifySession(session);
		}
	}
	
	@Test
	public void checkSigning_apiError() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		NullPointerException error = new NullPointerException(token);
		when(addo.getSigningStatus(session, token)).thenThrow(error);
		try {
			
			IOException exception = assertThrows(IOException.class, () -> service.checkSigning(token));
			assertThat(exception.getCause()).isSameAs(error);
			
		} finally {
			verifySession(session);
		}
	}
	
	@ParameterizedTest
	@MethodSource("signingStatusScenarios")
	public void checkSigning(int state, Optional<Boolean> expectedStatus) throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		SigningStatusDTO statusResponse = new SigningStatusDTO();
		statusResponse.setState(state);
		
		when(addo.getSigningStatus(session, token)).thenReturn(statusResponse);
		try {
			
			assertThat(service.checkSigning(token)).isEqualTo(expectedStatus);
			
		} finally {
			verifySession(session);
		}
	}
	
	public static Stream<Arguments> signingStatusScenarios() {
		return Stream.of(
					Arguments.of(CAMPAIGN_STARTED, SIGNING_ONGOING),
					Arguments.of(COMPLETED, SIGNING_SUCCESSFUL),
					Arguments.of(CREATED, SIGNING_ONGOING),
					Arguments.of(EXPIRED, SIGNING_FAILED),
					Arguments.of(FAILED, SIGNING_FAILED),
					Arguments.of(REJECTED, SIGNING_FAILED),
					Arguments.of(STARTED, SIGNING_ONGOING),
					Arguments.of(STOPPED, SIGNING_FAILED)
				);
	}

	@Test
	public void getSignature_loginError() throws Exception {
		
		WebApplicationException error = mockLoginError();
		
		WebApplicationException exception = assertThrows(WebApplicationException.class, () -> service.getSignature(null));
		assertThat(exception).isSameAs(error);
		
		verifyLogin();
	}
	
	@Test
	public void getSignature_getError() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		WebApplicationException error = new WebApplicationException(token);
		when(addo.getSigning(session, token)).thenThrow(error);
		try {
			
			WebApplicationException exception = assertThrows(WebApplicationException.class, () -> service.getSignature(token));
			assertThat(exception).isSameAs(error);
			
		} finally {
			verifySession(session);
		}
	}
	
	@Test
	public void getSignature_apiError() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		NullPointerException error = new NullPointerException(token);
		when(addo.getSigning(session, token)).thenThrow(error);
		try {
			
			IOException exception = assertThrows(IOException.class, () -> service.getSignature(token));
			assertThat(exception.getCause()).isSameAs(error);
			
		} finally {
			verifySession(session);
		}
	}

	@Test
	public void getSignature() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		SigningDTO signingResponse = new SigningDTO();
		SigningRecipientDTO recipient = new SigningRecipientDTO();
		recipient.setXmlData(randomUUID().toString());
		signingResponse.setRecipients(singletonList(recipient));
		
		when(addo.getSigning(session, token)).thenReturn(signingResponse);
		try {
			
			assertThat(service.getSignature(token)).isEqualTo(recipient.getXmlData());
			
		} finally {
			verifySession(session);
		}
	}
	
	private void verifySession(String session) throws Exception {
		verifyLogin();
	}

	private String mockSession() throws Exception {
		String session = randomUUID().toString();
		when(addo.login(loginRequest.capture())).thenReturn(session);
		return session;
	}
	
	private WebApplicationException mockLoginError() throws Exception {
		WebApplicationException error = new WebApplicationException(randomUUID().toString());
		when(addo.login(loginRequest.capture())).thenThrow(error);
		return error;
	}
}
