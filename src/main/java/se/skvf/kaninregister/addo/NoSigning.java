package se.skvf.kaninregister.addo;

public class NoSigning extends Signing {

	public NoSigning() {
		super(null, null, null);
	}

	@Override
	public String getTransactionUrl() {
		return null;
	}
}
