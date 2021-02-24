package se.skvf.kaninregister.addo;

public interface AuthenticationMethod {
	
	static final int NONE = 0;
	static final int NEMID_PRIVATE = 1;
	static final int NEMID_PRIVATE_NO_SSN = 2;
	static final int TWO_FACTOR_VERIFICATION = 3;
	static final int SITHS = 4;
	static final int NEMID_EMPLOYEE = 7;
	static final int NORWEGIAN_BANKID = 8;
	static final int SWEDISH_BANKID = 9;
	static final int NORWEGIAN_BANKID_MOBILE = 10;
	static final int SECRET_CODE = 11;
}
