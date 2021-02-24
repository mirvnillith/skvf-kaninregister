package se.skvf.kaninregister.addo;

public interface SigningState {

    static final int FAILED = -1;
	static final int CREATED = 1;
	static final int STARTED = 2;
	static final int COMPLETED = 3;
	static final int EXPIRED = 4;
	static final int STOPPED = 5;
	static final int CAMPAIGN_STARTED = 6;
	static final int REJECTED = 7;
}
