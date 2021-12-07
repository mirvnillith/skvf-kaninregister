package se.skvf.kaninregister.addo;

public class Signature {

	private final String url;
	private final String subject;
	private final String signature;
	
	public Signature(String url, String subject, String signature) {
		this.url = url;
		this.subject = subject;
		this.signature = signature;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getSignature() {
		return signature;
	}
}
