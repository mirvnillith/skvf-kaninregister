package se.skvf.kaninregister.model;

import static java.util.Arrays.asList;
import static java.util.Comparator.naturalOrder;
import static java.util.UUID.randomUUID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

class RegistryRuntimeTest {

	void test(Registry registry) throws IOException {
		
		Owner jonas = new Owner().setName("Jonas Olsson");
		Owner maria = new Owner().setName("Maria Wahlström");
		registry.add(jonas);
		registry.add(maria);
		registry.findOwners(asList(maria.getId(), jonas.getId())).forEach(System.out::println);
		
		jonas.setName("Carl Jonas Olsson");
		registry.update(jonas);
		registry.findOwners(asList(jonas.getId())).forEach(System.out::println);
		
		Map<String, Predicate<String>> ström = new HashMap<>();
		ström.put("Namn", n -> n.endsWith("ström"));
		System.out.println("%ström");
		registry.findOwners(ström).forEach(System.out::println);
		
		Bunny bilbo = new Bunny().setOwner(maria.getId()).setName("Bilbo");
		Bunny pompom = new Bunny().setOwner(jonas.getId()).setName("PomPom");
		registry.add(bilbo);
		registry.add(pompom);
		registry.findBunnies(asList(pompom.getId(), bilbo.getId())).forEach(System.out::println);
		
		Map<String, Predicate<String>> jonaso = new HashMap<>();
		jonaso.put("Ägare", ä -> jonas.getId().equals(ä));
		jonaso.put("Namn", n -> n.contains("o"));
		System.out.println("Jonas med o");
		registry.findBunnies(jonaso).forEach(System.out::println);
		
		registry.remove(bilbo);
		registry.remove(pompom);
		registry.remove(jonas);
		registry.remove(maria);
	}
}
