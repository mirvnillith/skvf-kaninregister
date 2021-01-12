package se.skvf.kaninregister;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public abstract class BunnyTest {

	private AutoCloseable mocks;
	
	@BeforeEach
	public void openMocks() {
		mocks = MockitoAnnotations.openMocks(this);
	}
	
	@AfterEach
	public void closeMocks() throws Exception {
		mocks.close();
	}
}
