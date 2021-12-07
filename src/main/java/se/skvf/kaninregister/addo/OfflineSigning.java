package se.skvf.kaninregister.addo;

import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;

public class OfflineSigning extends Signing {

	static final String OFFLINE_URL="/signOffline/";
	
	private Boolean state;
	private String subject;
	
	public OfflineSigning(String url) {
		super(url, randomUUID().toString(), null);
	}
	
	@Override
	public String getTransactionUrl() {
		return OFFLINE_URL + getToken();
	}
	
	public void setState(String subject, boolean state) {
		this.state = state;
		this.subject = subject;
	}
	
	public Boolean getState() {
		return state;
	}

	public Signature getSignature() {
		return new Signature(url, subject, token);
	}
}
