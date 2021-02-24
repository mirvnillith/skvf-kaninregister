package se.skvf.kaninregister.addo;

import java.io.File;
import java.io.IOException;

class AddoRuntimeTest {

	void test(AddoSigningService service, String url) throws IOException {
		service.startSigning("PNR", new File("ADDO.pdf"));
	}
}
