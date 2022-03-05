package se.skvf.kaninregister.data;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DelayedSetup<T> {

	public interface Source<T> {
		T setup() throws IOException;
	}
	
	private T setup;
	private IOException error;
	
	DelayedSetup(Source<T> source) {
		new Thread(() -> {
			try {
				setup(source.setup());
			} catch (IOException e) {
				setup(e);
			}
		}).start();
	}

	private synchronized void setup(IOException e) {
		error = e;
		notifyAll();
	}

	private synchronized void setup(T source) {
		setup = source;
		notifyAll();
	}

	public synchronized T setup() throws IOException {
		if (setup == null && error == null) {
			try {
				wait();
			} catch (InterruptedException e) {
				error = new IOException("Interrupted waiting for setup", e);
			}
		}
		
		if (setup != null) {
			return setup;
		} else {
			throw error;
		}
	}
}
