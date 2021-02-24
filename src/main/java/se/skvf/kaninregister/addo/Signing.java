package se.skvf.kaninregister.addo;

public class Signing {

	private String token;
	private String transaction;
	
	public Signing(String token, String transaction) {
		this.token = token;
		this.transaction = transaction;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getTransaction() {
		return transaction;
	}
}
