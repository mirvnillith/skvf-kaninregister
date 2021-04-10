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

	void performance(Registry registry) throws Exception {
		
		Set<String> ids = new HashSet<>();
		while (ids.size()<2000) {
			addOwners(registry, ids, 100);
		}
		while (ids.size()>0) {
			removeOwners(registry, ids, 100);
		}
	}
	
	private void addOwners(Registry registry, Set<String> ids, int batch) throws Exception {

		List<Long> durations = new ArrayList<>(batch);
		for (int i=0; i<batch;i++) {
			Owner owner = new Owner().setName(randomUUID().toString());
			long before = System.currentTimeMillis();
			ids.add(registry.add(owner));
			durations.add(System.currentTimeMillis()-before);
			Thread.sleep(1000);
		}
		
		long min = durations.stream().min(naturalOrder()).orElse(0L);
		long max = durations.stream().max(naturalOrder()).orElse(0L);
		AtomicLong total = new AtomicLong();
		durations.forEach(total::addAndGet);
		long avg = total.get()/batch;
		System.out.println("add(" + ids.size() + "): " + min + "-" + avg + "-"+max);
	}
	
	private void removeOwners(Registry registry, Set<String> ids, int batch) throws Exception {
		
		Set<String> batchIds = new HashSet<String>();
		Iterator<String> it = ids.iterator();
		while (batchIds.size()<batch) {
			batchIds.add(it.next());
		}
		List<Long> durations = new ArrayList<>(batch);
		for (String id : batchIds) {
			Owner owner = new Owner().setId(id);
			long before = System.currentTimeMillis();
			registry.remove(owner);
			durations.add(System.currentTimeMillis()-before);
			Thread.sleep(1000);
		}
		
		long min = durations.stream().min(naturalOrder()).orElse(0L);
		long max = durations.stream().max(naturalOrder()).orElse(0L);
		AtomicLong total = new AtomicLong();
		durations.forEach(total::addAndGet);
		long avg = total.get()/batch;
		System.out.println("remove(" + ids.size() + "): " + min + "-" + avg+"-"+max);
		ids.removeAll(batchIds);
	}

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
