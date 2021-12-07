package se.skvf.kaninregister.addo;

public class Signing {

	private static String PATH = "/SigningPortal/?token=";
	
	protected final String url;
	protected final String token;
	protected final String transaction;
	
	public Signing(String url, String token, String transaction) {
		this.url = url;
		this.token = token;
		this.transaction = transaction;
	}
	
	public String getToken() {
		return token;
	}
	
	public String getTransactionUrl() {
		return url + PATH + transaction;
	}
}
