package se.skvf.kaninregister.addo;

public class Signature {

	private final String subject;
	private final String signature;
	
	public Signature(String subject, String signature) {
		this.subject = subject;
		this.signature = signature;
	}
	
	public String getSubject() {
		return subject;
	}
	
	public String getSignature() {
		return signature;
	}
}
