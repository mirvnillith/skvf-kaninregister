package se.skvf.kaninregister.addo;

import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.distributionmethodenum.DistributionMethodEnum.NONE;
import static net.vismaaddo.schemas.services.signingservice.v2_0.messages.enums.signingmethodenum.SigningMethodEnum.BANK_ID;
import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.digest.DigestUtils.digest;
import static org.apache.commons.codec.digest.DigestUtils.getSha512Digest;
import static org.apache.commons.io.FileUtils.readFileToByteArray;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.stream.Stream;

import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.datacontract.schemas._2004._07.visma_addo_webservice_contracts_v2_0.ArrayOfSigningDocument;
import org.datacontract.schemas._2004._07.visma_addo_webservice_contracts_v2_0.SenderData;
import org.datacontract.schemas._2004._07.visma_addo_webservice_contracts_v2_0.SigningDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

import net.vismaaddo.schemas.services.signingservice.v2_0.SigningService;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceGetSigningStatusValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceGetSigningValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceInitiateSigningValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceLoginValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.SigningServiceLogoutValidationFaultFaultFaultMessage;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.generatedocumentresponse.recipient.GetSigningResponseRecipient;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.getsigningstatus.GetSigningStatus;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.initiatesigningrequest.InitiateSigningRequest;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.initiatesigningrequest.signing.Signing;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.recipientdata.ArrayOfRecipientData;
import net.vismaaddo.schemas.services.signingservice.v2_0.messages.recipientdata.RecipientData;

@Component
public class AddoSigningService {

	private static final DatatypeFactory DATATYPE_FACTORY = DatatypeFactory.newDefaultInstance();

	private static final Log LOG = LogFactory.getLog(AddoSigningService.class);
	
	private SigningService service;
	
	@Value("${skvf.addo.email}")
	private String email;
	@Value("${skvf.addo.password}")
	private String password;
	
	private synchronized SigningService getService() {
		if (service == null) {
			
		}
		return service;
	}
	
	public String startSigning(String personnummer, File file, MimeType type) throws IOException {
		
		return inSession(session -> {

			Signing signing = new Signing();
			signing.setSender(createSender());
			signing.setRecipients(recipients(createRecipient(personnummer)));
			signing.setDocuments(documents(createDocument(file, type)));
			signing.setAllowInboundEnclosures(false);
			signing.setAllowRecipientComment(false);
			
			InitiateSigningRequest request = new InitiateSigningRequest();
			request.setName("Datahantering i SKVFs kaninregister");
			request.setStartDate(DATATYPE_FACTORY.newXMLGregorianCalendar(new GregorianCalendar()));
			request.setSigningData(signing);
			
			try {
				return getService().initiateSigning(session, request, null).getSigningToken();
			} catch (SigningServiceInitiateSigningValidationFaultFaultFaultMessage e) {
				throw new IOException("Addo signing creation failed", e);
			}
		});
	}
	
	private static interface SessionCall<T> {
		T call(String session) throws IOException;
	}
	
	private <T> T inSession(SessionCall<T> call) throws IOException {
		String session = login();
		try {
			try {
				return call.call(session);
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
			
			try {
				GetSigningStatus status = getService().getSigningStatus(session, token);
				switch (status.getState()) {
				case COMPLETED:
					return Optional.of(true);
				case EXPIRED:
				case FAILED:
				case REJECTED:
				case STOPPED:
					return Optional.of(false);
				default:
					return Optional.empty();
				}
			} catch (SigningServiceGetSigningStatusValidationFaultFaultFaultMessage e) {
				throw new IOException("Addo signing status failed", e);
			}
		});
	}

	public String getSignature(String token) throws IOException {
		
		return inSession(session -> {
			
				return getSigner(token, session).getXmlData();
		});
	}

	private GetSigningResponseRecipient getSigner(String token, String session) throws IOException {
		try {
			return getService().getSigning(session, token).getRecipients().getGetSigningResponseRecipients().get(0);
		} catch (SigningServiceGetSigningValidationFaultFaultFaultMessage e) {
			throw new IOException("Addo signer failed", e);
		}
	}
	
	private static ArrayOfRecipientData recipients(RecipientData... recipient) {
		ArrayOfRecipientData recipients = new ArrayOfRecipientData();
		Stream.of(recipient).forEach(recipients.getRecipientDatas()::add);
		return recipients;	
	}

	private static RecipientData createRecipient(String personnummer) {
		RecipientData recipient = new RecipientData();
		recipient.setSendDistributionDocument(false);
		recipient.setSendDistributionNotification(false);
		recipient.setSendWelcomeNotification(false);
		recipient.setDistributionMethod(NONE);
		recipient.setSigningMethod(BANK_ID);
		recipient.setSwedishSsn(personnummer);
		return recipient;
	}

	private static ArrayOfSigningDocument documents(SigningDocument... document) {
		ArrayOfSigningDocument documents = new ArrayOfSigningDocument();
		Stream.of(document).forEach(documents.getSigningDocuments()::add);
		return documents;
	}

	private static SenderData createSender() {
		SenderData sender = new SenderData();
		sender.setName("Kaninregistret");
		sender.setCompanyName("Sveriges Kaninvälfärdsförening");
		sender.setEmail("kaninregistret@skvf.se");
		return sender;
	}

	private static SigningDocument createDocument(File file, MimeType type) throws IOException {
		SigningDocument document = new SigningDocument();
		document.setData(new String(encodeBase64(readFileToByteArray(file))));
		document.setId(file.getName());
		document.setMimeType(type.toString());
		document.setName(file.getName());
		document.setIsShared(false);
		return document;
	}

	private void logout(String session) {
		try {
			getService().logout(session);
		} catch (SigningServiceLogoutValidationFaultFaultFaultMessage e) {
			LOG.warn("Logout failed", e);
		}
	}

	private String login() throws IOException {
		String session;
		try {
			session = getService().login(email, sha64(password));
			if (isBlank(session)) {
				throw new IOException("Addo login failed");
			}
		} catch (SigningServiceLoginValidationFaultFaultFaultMessage e) {
			throw new IOException("Addo login failed", e);
		}
		return session;
	}

	private static String sha64(String string) {
		return new String(encodeBase64(digest(getSha512Digest(), string.getBytes())));
	}
}
