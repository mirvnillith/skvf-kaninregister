package se.skvf.kaninregister.addo;

public class Signature {

	private final String identifier;
	private final String subject;
	
	public Signature(String identifier, String subject) {
		this.identifier = identifier;
		this.subject = subject;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getSubject() {
		return subject;
	}
}
