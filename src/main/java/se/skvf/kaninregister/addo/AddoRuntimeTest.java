package se.skvf.kaninregister.addo;

import static java.lang.Thread.sleep;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

class AddoRuntimeTest {

	void test(AddoSigningService service) throws IOException {
		
		service.setApprovalUrl(new File("src/test/resources/minimal.pdf").toURI().toURL().toString());
		Signing signing = service.startSigning();
		System.out.println(signing.getTransactionUrl());
		
		Optional<Boolean> status = Optional.empty();
		while (status.isEmpty()) {
			try {
				sleep(10 * 1000L);
			} catch (InterruptedException ignored) {
			}
			status = service.checkSigning(signing.getToken());
		}
		
		if (status.get()) {
			System.out.println("Signature: " + service.getSignature(signing.getToken()).getSignature());
		} else {
			System.out.println("FAILED");
		}
	}
}
