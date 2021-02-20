package se.skvf.kaninregister.addo;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.UUID.randomUUID;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.distributionmethodenum.DistributionMethodEnum.NONE;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingmethodenum.SigningMethodEnum.SWEDISH_BANK_ID;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum.CAMPAIGN_STARTED;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum.COMPLETED;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum.CREATED;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum.EXPIRED;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum.FAILED;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum.REJECTED;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum.STARTED;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum.STOPPED;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.io.FileUtils.write;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.util.MimeTypeUtils.APPLICATION_OCTET_STREAM;
import static se.skvf.kaninregister.addo.AddoSigningService.sha64;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.Random;
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

import net.vismaaddo.schemas.services.signingservice.v2_0.SigningService;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceGetSigningStatusValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceGetSigningValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceInitiateSigningValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceLoginValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceLogoutValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingstateenum.SigningStateEnum;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.generatedocumentresponse.GetSigningResponse;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.generatedocumentresponse.recipient.ArrayOfGetSigningResponseRecipient;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.generatedocumentresponse.recipient.GetSigningResponseRecipient;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.getsigningstatus.GetSigningStatus;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.initiatesigningrequest.InitiateSigningRequest;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.initiatesigningrequest.signing.Signing;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.initiatesigningresponse.InitiateSigningResponse;
import se.skvf.kaninregister.BunnyTest;

public class AddoSigningServiceTest extends BunnyTest {
	
	private static final Optional<Boolean> SIGNING_ONGOING = Optional.empty();
	private static final Optional<Boolean> SIGNING_SUCCESSFUL = Optional.of(true);
	private static final Optional<Boolean> SIGNING_FAILED = Optional.of(false);

	@InjectMocks
	private AddoSigningService service;

	@Mock
	private SigningService addo;

	@Captor
	private ArgumentCaptor<InitiateSigningRequest> signingRequest;
	
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
		
		SigningServiceLoginValidationFaultFaultFaultMessage error = mockLoginError();
		
		IOException exception = assertThrows(IOException.class, () -> service.startSigning(null, null, null));
		assertThat(exception.getCause()).isSameAs(error);
	}
	
	@Test
	public void startSigning_signingError() throws Exception {
		
		String pnr = randomUUID().toString();
		File file = File.createTempFile("addo", "bin");
		file.deleteOnExit();
		String data = randomUUID().toString();
		write(file, data, defaultCharset());
		
		String session = mockSession();
		
		SigningServiceInitiateSigningValidationFaultFaultFaultMessage error = new SigningServiceInitiateSigningValidationFaultFaultFaultMessage(randomUUID().toString(), null);
		when(addo.initiateSigning(eq(session), signingRequest.capture(), isNull())).thenThrow(error);
		try {

			IOException exception = assertThrows(IOException.class, () -> service.startSigning(pnr, file, APPLICATION_OCTET_STREAM));
			assertThat(exception.getCause()).isSameAs(error);
			
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
		
		WebApplicationException error = new WebApplicationException(randomUUID().toString());
		when(addo.initiateSigning(eq(session), signingRequest.capture(), isNull())).thenThrow(error);
		try {
			
			IOException exception = assertThrows(IOException.class, () -> service.startSigning(pnr, file, APPLICATION_OCTET_STREAM));
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
		
		InitiateSigningResponse signingResponse = new InitiateSigningResponse();
		signingResponse.setSigningToken(randomUUID().toString());
		
		when(addo.initiateSigning(eq(session), signingRequest.capture(), isNull())).thenReturn(signingResponse);
		try {
		
			assertThat(service.startSigning(pnr, file, APPLICATION_OCTET_STREAM))
				.isEqualTo(signingResponse.getSigningToken());

			InitiateSigningRequest request = signingRequest.getValue();
			assertThat(request.getName())
				.isEqualTo(AddoSigningService.SIGNING_NAME);
			assertThat(request.getStartDate().toGregorianCalendar())
				.isLessThanOrEqualTo(new GregorianCalendar());
			
			Signing signing = request.getSigningData();
			assertThat(signing.getSender())
				.usingRecursiveComparison()
				.isEqualTo(AddoSigningService.createSender());
			assertThat(signing.isAllowInboundEnclosures())
				.isFalse();
			assertThat(signing.isAllowRecipientComment())
				.isFalse();
			
			assertThat(signing.getRecipients().getRecipientDatas())
				.hasSize(1)
				.allSatisfy(r -> {
					assertThat(r.isSendDistributionDocument()).isFalse();
					assertThat(r.isSendDistributionNotification()).isFalse();
					assertThat(r.isSendWelcomeNotification()).isFalse();
					assertThat(r.getDistributionMethod()).isEqualTo(NONE);
					assertThat(r.getSigningMethod()).isEqualTo(SWEDISH_BANK_ID);
					assertThat(r.getSwedishSsn()).isEqualTo(pnr);
				});
			
			assertThat(signing.getDocuments().getSigningDocuments())
				.hasSize(1)
				.allSatisfy(d -> {
					assertThat(d.getName()).isEqualTo(file.getName());
					assertThat(d.getMimeType()).isEqualTo(APPLICATION_OCTET_STREAM.toString());
					assertThat(d.getData()).isEqualTo(encodeBase64String(data.getBytes()));
				});
			
		} finally {
			verifySession(session);
		}
	}
	
	@Test
	public void checkSigning_loginError() throws Exception {
		
		SigningServiceLoginValidationFaultFaultFaultMessage error = mockLoginError();
		
		IOException exception = assertThrows(IOException.class, () -> service.checkSigning(null));
		assertThat(exception.getCause()).isSameAs(error);
	}
	
	@Test
	public void checkSigning_statusError() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		SigningServiceGetSigningStatusValidationFaultFaultFaultMessage error = new SigningServiceGetSigningStatusValidationFaultFaultFaultMessage(token, null);
		when(addo.getSigningStatus(session, token)).thenThrow(error);
		try {
			
			IOException exception = assertThrows(IOException.class, () -> service.checkSigning(token));
			assertThat(exception.getCause()).isSameAs(error);
			
		} finally {
			verifySession(session);
		}
	}
	
	@Test
	public void checkSigning_apiError() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		WebApplicationException error = new WebApplicationException(token);
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
	public void checkSigning(SigningStateEnum state, Optional<Boolean> expectedStatus) throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		GetSigningStatus statusResponse = new GetSigningStatus();
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
		
		SigningServiceLoginValidationFaultFaultFaultMessage error = mockLoginError();
		
		IOException exception = assertThrows(IOException.class, () -> service.getSignature(null));
		assertThat(exception.getCause()).isSameAs(error);
	}
	
	@Test
	public void getSignature_getError() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		SigningServiceGetSigningValidationFaultFaultFaultMessage error = new SigningServiceGetSigningValidationFaultFaultFaultMessage(token, null);
		when(addo.getSigning(session, token)).thenThrow(error);
		try {
			
			IOException exception = assertThrows(IOException.class, () -> service.getSignature(token));
			assertThat(exception.getCause()).isSameAs(error);
			
		} finally {
			verifySession(session);
		}
	}
	
	@Test
	public void getSignature_apiError() throws Exception {
		
		String token = randomUUID().toString();
		
		String session = mockSession();
		
		WebApplicationException error = new WebApplicationException(token);
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
		
		GetSigningResponse signingResponse = new GetSigningResponse();
		ArrayOfGetSigningResponseRecipient recipients = new ArrayOfGetSigningResponseRecipient();
		GetSigningResponseRecipient recipient = new GetSigningResponseRecipient();
		recipient.setXmlData(randomUUID().toString());
		recipients.getGetSigningResponseRecipients().add(recipient);
		signingResponse.setRecipients(recipients);
		
		when(addo.getSigning(session, token)).thenReturn(signingResponse);
		try {
			
			assertThat(service.getSignature(token)).isEqualTo(recipient.getXmlData());
			
		} finally {
			verifySession(session);
		}
	}
	
	private void verifySession(String session) throws Exception {
		verify(addo).logout(session);
	}

	private String mockSession() throws Exception {
		String session = randomUUID().toString();
		when(addo.login(email, sha64(password))).thenReturn(session);
		if (new Random().nextBoolean()) {
			doThrow(new SigningServiceLogoutValidationFaultFaultFaultMessage(session, null)).when(addo).logout(session);
		}
		return session;
	}
	
	private SigningServiceLoginValidationFaultFaultFaultMessage mockLoginError() throws Exception {
		SigningServiceLoginValidationFaultFaultFaultMessage error = new SigningServiceLoginValidationFaultFaultFaultMessage(randomUUID().toString(), null);
		when(addo.login(email, sha64(password))).thenThrow(error);
		return error;
	}
}
