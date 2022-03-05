package se.skvf.kaninregister.data;

public class DelayedSetupMock<T> extends DelayedSetup<T> {

	public DelayedSetupMock(T source) {
		super(() -> source);
	}
}
