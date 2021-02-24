package se.skvf.kaninregister.addo;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static net.vismaaddo.api.DocumentDTO.MimeTypeEnum.APPLICATION_PDF;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.apache.commons.codec.digest.DigestUtils.digest;
import static org.apache.commons.codec.digest.DigestUtils.getSha512Digest;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import net.vismaaddo.api.DocumentDTO;
import net.vismaaddo.api.InitiateSigningRequestDTO;
import net.vismaaddo.api.LoginRequestDTO;
import net.vismaaddo.api.RecipientDTO;
import net.vismaaddo.api.RecipientDTO.DistributionMethodEnum;
import net.vismaaddo.api.RecipientDTO.SigningMethodEnum;
import net.vismaaddo.api.SenderDTO;
import net.vismaaddo.api.SigningDTO;
import net.vismaaddo.api.SigningDataDTO;
import net.vismaaddo.api.SigningRequestDTO;
import net.vismaaddo.api.SigningStatusDTO;
import net.vismaaddo.api.VismaAddoApi;

@Component
public class AddoSigningService {

	static final SigningMethodEnum SWEDISH_BANKID = RecipientDTO.SigningMethodEnum.NUMBER_5;

	static final DistributionMethodEnum NO_DISTRIBUTION = RecipientDTO.DistributionMethodEnum.NUMBER_1;

	static final String SIGNING_NAME = "Datahantering i SKVFs kaninregister";

	private static final Log LOG = LogFactory.getLog(AddoSigningService.class);
	
	private VismaAddoApi addo;
	
	@Value("${skvf.addo.url}")
	private String url;
	@Value("${skvf.addo.email}")
	private String email;
	@Value("${skvf.addo.password}")
	private String password;
	@Value("${skvf.dev.addo:false}")
	private boolean test;
	
	void setEmail(String email) {
		this.email = email;
	}
	
	void setPassword(String password) {
		this.password = password;
	}
	
	@PostConstruct
	public void setup() throws Exception {
		
		addo = JAXRSClientFactory.create(url, VismaAddoApi.class, asList(new JacksonJaxbJsonProvider()));
		
		if (test) {
			new AddoRuntimeTest().test(this, url);
		}
	}
	
	public Signing startSigning(String personnummer, File pdf) throws IOException {
		
		return inSession(session -> {

			SigningDataDTO signingData = new SigningDataDTO();
			signingData.setSender(createSender());
			signingData.setRecipients(singletonList(createRecipient(personnummer)));
			signingData.setDocuments(singletonList(createDocument(pdf)));
			signingData.setAllowInboundEnclosures(false);
			signingData.setAllowRecipientComment(false);
			
			SigningRequestDTO signingRequest = new SigningRequestDTO();
			signingRequest.setName(SIGNING_NAME);
			signingRequest.setSigningData(signingData);
			signingRequest.setStartDate("/Date(" + currentTimeMillis() + ")/");
			
			InitiateSigningRequestDTO request = new InitiateSigningRequestDTO();
			request.setToken(session);
			request.setRequest(signingRequest);
			
			LOG.info("initiateSigning(" + session + ")");
			System.out.println(request);
			String token = addo.initiateSigning(request).getSigningToken();
			LOG.info("initiateSigning(" + session + "): " + token);
			
			SigningStatusDTO status = getSigningStatus(session, token);
			
			return new Signing(token, status.getRecipients().get(0).getTransactions().get(0).getTransactionToken());
		});
	}

	private SigningStatusDTO getSigningStatus(String session, String token) {
		LOG.info("getSigningStatus(" + session + "," + token + ")");
		SigningStatusDTO status = addo.getSigningStatus(session, token);
		LOG.info("getSigningStatus(" + session + "," + token + "): " + status.getState());
		return status;
	}
	
	private static interface SessionCall<T> {
		T call(String session) throws IOException;
	}
	
	private <T> T inSession(SessionCall<T> call) throws IOException {
		String session = login();
		try {
			try {
				return call.call(session);
			} catch (WebApplicationException e) {
				LOG.error("Addo error", e);
				throw e;
			} catch (IOException e) {
				LOG.error("Unexpected error", e);
				throw e;
			} catch (Exception e) {
				LOG.error("Unexpected error", e);
				throw new IOException("Addo error", e);
			}
		} finally {
			logout(session);
		}
	}
	
	/**
	 * Checks signing status.
	 * @param token
	 * @return true if signing successful, false if signing failed and empty if signing ongoing
	 */
	public Optional<Boolean> checkSigning(String token) throws IOException {
		
		return inSession(session -> {
			
				SigningStatusDTO status = getSigningStatus(session, token);
				switch (status.getState()) {
				case SigningState.COMPLETED:
					return Optional.of(true);
				case SigningState.FAILED:
				case SigningState.REJECTED:
				case SigningState.STOPPED:
				case SigningState.EXPIRED:
					return Optional.of(false);
				default:
					return Optional.empty();
				}
		});
	}

	public String getSignature(String token) throws IOException {
		
		return inSession(session -> {
			
			LOG.info("getSigning(" + session + "," + token + ")");
			SigningDTO signing = addo.getSigning(session, token);
			LOG.info("getSigning(" + session + "," + token + "): " + signing.getName());
			
			return signing.getRecipients().get(0).getXmlData();
		});
	}
	
	private static RecipientDTO createRecipient(String personnummer) {
		RecipientDTO recipient = new RecipientDTO();
		recipient.setSendDistributionDocument(false);
		recipient.setSendDistributionNotification(false);
		recipient.setSendWelcomeNotification(false);
		recipient.setDistributionMethod(NO_DISTRIBUTION);
		recipient.setSigningMethod(SWEDISH_BANKID);
		recipient.setSSN(personnummer);
		return recipient;
	}

	static SenderDTO createSender() {
		SenderDTO sender = new SenderDTO();
		sender.setName("Kaninregistret");
		sender.setCompanyName("Sveriges Kaninvälfärdsförening");
		sender.setEmail("kaninregistret@skvf.se");
		return sender;
	}

	private static DocumentDTO createDocument(File file) throws IOException {
		DocumentDTO document = new DocumentDTO();
		document.setData(encodeBase64String(readFileToByteArray(file)));
		document.setId(file.getName());
		document.setMimeType(APPLICATION_PDF);
		document.setName(file.getName());
		document.setIsShared(false);
		return document;
	}

	private void logout(String session) throws IOException {
//		try {
//			//getService().logout(session);
//		} catch (SigningServiceLogoutValidationFaultFaultFaultMessage e) {
//			LOG.warn("Logout failed", e);
//		}
	}

	private String login() throws IOException {
		LoginRequestDTO request = new LoginRequestDTO();
		request.setEmail(email);
		request.setPassword(sha64(password));
		LOG.info("login()");
		String session = addo.login(request);
		if (session != null) {
			if (session.startsWith("\"")) {
				session = session.substring(1);
			}
			if (session.endsWith("\"")) {
				session = session.substring(0, session.length() - 1);
			}
		}
		if (isBlank(session)) {
			throw new IOException("Addo login failed");
		}
		LOG.info("login(): " + session);
		return session;
	}

	static String sha64(String string) {
		return new String(encodeBase64(digest(getSha512Digest(), string.getBytes())));
	}
}
