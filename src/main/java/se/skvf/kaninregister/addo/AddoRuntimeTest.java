package se.skvf.kaninregister.addo;

import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

import java.io.IOException;
import java.util.Optional;

class AddoRuntimeTest {

	void test(AddoSigningService service) throws IOException {
		
		Signing signing = service.startSigning("PNR", currentThread().getContextClassLoader().getResourceAsStream("minimal.pdf"), "minimal.pdf");
		System.out.println(signing.getTransactionUrl());
		
		Optional<Boolean> status = null;
		while (status == null) {
			try {
				sleep(10*1000);
			} catch (InterruptedException ignored) {
			}
			status = service.checkSigning(signing.getToken());
		}
		
		if (status.get()) {
			System.out.println("XML: " + service.getSignature(signing.getToken()));
		} else {
			System.out.println("FAILED");
		}
	}
}
