package se.skvf.kaninregister.data;

import java.io.IOException;

public class DelayedSetup<T> {

	public interface Source<T> {
		T setup() throws IOException;
	}
	
	private final Source<T> source;
	private T setup;
	
	DelayedSetup(Source<T> source) {
		this.source = source;
	}

	public synchronized T setup() throws IOException {
		if (setup == null) {
			setup = source.setup();
		}
		return setup;
	}
}
