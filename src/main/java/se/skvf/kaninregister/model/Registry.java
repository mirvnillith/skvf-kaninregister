package se.skvf.kaninregister.model;

import static java.util.Arrays.asList;
import static java.util.Comparator.naturalOrder;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import se.skvf.kaninregister.data.Database;
import se.skvf.kaninregister.data.Table;

@Component
public class Registry {

	private static final String BUNNIES_NAME = "Kaniner";
	private static final String OWNERS_NAME = "Ägare";

	@Autowired
	private Database database;
	
	private Table bunnies;
	private Table owners;

	private boolean test;
	private boolean performance;
	
	@Value("${skvf.dev.test}")
	public void setTest(boolean test) {
		this.test = test;
	}
	
	@Value("${skvf.dev.performance}")
	public void setPerformance(boolean performance) {
		this.performance = performance;
	}
	
	@PostConstruct
	public void setup() throws Exception {
		
		bunnies = database.getTable(BUNNIES_NAME, Bunny.COLUMNS);
		owners = database.getTable(OWNERS_NAME, Owner.COLUMNS);
		
		if (test) {
			test();
		}
		if (performance) {
			performance();
		}
	}

	private void performance() throws Exception {
		
		Set<String> ids = new HashSet<>();
		while (ids.size()<2000) {
			addOwners(ids, 100);
		}
		while (ids.size()>0) {
			removeOwners(ids, 100);
		}
	}
	
	private void addOwners(Set<String> ids, int batch) throws Exception {

		List<Long> durations = new ArrayList<>(batch);
		for (int i=0; i<batch;i++) {
			Owner owner = new Owner().setFirstName(randomUUID().toString()).setLastName(randomUUID().toString());
			long before = System.currentTimeMillis();
			ids.add(add(owner));
			durations.add(System.currentTimeMillis()-before);
			Thread.sleep(1000);
		}
		
		long min = durations.stream().min(naturalOrder()).orElse(0L);
		long max = durations.stream().max(naturalOrder()).orElse(0L);
		AtomicLong total = new AtomicLong();
		durations.forEach(total::addAndGet);
		long avg = total.get()/batch;
		System.out.println("add("+ids.size()+"): "+min+"-"+avg+"-"+max);
	}
	
	private void removeOwners(Set<String> ids, int batch) throws Exception {
		
		Set<String> batchIds = new HashSet<String>();
		Iterator<String> it = ids.iterator();
		while (batchIds.size()<batch) {
			batchIds.add(it.next());
		}
		List<Long> durations = new ArrayList<>(batch);
		for (String id : batchIds) {
			Owner owner = new Owner().setId(id);
			long before = System.currentTimeMillis();
			remove(owner);
			durations.add(System.currentTimeMillis()-before);
			Thread.sleep(1000);
		}
		
		long min = durations.stream().min(naturalOrder()).orElse(0L);
		long max = durations.stream().max(naturalOrder()).orElse(0L);
		AtomicLong total = new AtomicLong();
		durations.forEach(total::addAndGet);
		long avg = total.get()/batch;
		System.out.println("remove("+ids.size()+"): "+min+"-"+avg+"-"+max);
		ids.removeAll(batchIds);
	}

	private void test() throws IOException {
		
		Owner jonas = new Owner().setFirstName("Jonas").setLastName("Olsson");
		Owner maria = new Owner().setFirstName("Maria").setLastName("Wahlström");
		add(jonas);
		add(maria);
		findOwners(asList(maria.getId(), jonas.getId())).forEach(System.out::println);
		
		Map<String, Predicate<String>> ström = new HashMap<>();
		ström.put("Efternamn", n -> n.endsWith("ström"));
		System.out.println("%ström");
		findOwners(ström).forEach(System.out::println);
		
		Bunny bilbo = new Bunny().setOwner(maria.getId()).setName("Bilbo");
		Bunny pompom = new Bunny().setOwner(jonas.getId()).setName("PomPom");
		add(bilbo);
		add(pompom);
		findBunnies(asList(pompom.getId(), bilbo.getId())).forEach(System.out::println);
		
		Map<String, Predicate<String>> jonaso = new HashMap<>();
		jonaso.put("Ägare", ä -> jonas.getId().equals(ä));
		jonaso.put("Namn", n -> n.contains("o"));
		System.out.println("Jonas med o");
		findBunnies(jonaso).forEach(System.out::println);
		
		remove(bilbo);
		remove(pompom);
		remove(jonas);
		remove(maria);
	}

	public String add(Owner owner) throws IOException {
		return add(owners, owner);
	}
	
	public Collection<Owner> findOwners(Collection<String> ids) throws IOException {
		return owners.find(ids).stream().map(Owner::from).collect(toList());
	}
	
	public Collection<Owner> findOwners(Map<String, Predicate<String>> filters) throws IOException {
		return owners.find(filters).stream().map(Owner::from).collect(toList());
	}

	public void remove(Owner owner) throws IOException {
		remove(owners, owner);
	}
	
	public String add(Bunny bunny) throws IOException {
		return add(bunnies, bunny);
	}
	
	public Collection<Bunny> findBunnies(Collection<String> ids) throws IOException {
		return bunnies.find(ids).stream().map(Bunny::from).collect(toList());
	}
	
	public Collection<Bunny> findBunnies(Map<String, Predicate<String>> filters) throws IOException {
		return bunnies.find(filters).stream().map(Bunny::from).collect(toList());
	}
	
	public void remove(Bunny bunny) throws IOException {
		remove(bunnies, bunny);
	}
	
	private void remove(Table table, Entity entity) throws IOException {
		if (entity.getId() == null) {
			throw new IllegalStateException("Entity has no ID: " + entity);
		}
		table.remove(entity.getId());
	}
	
	private String add(Table table, Entity entity) throws IOException {
		if (entity.getId() != null) {
			throw new IllegalStateException("Entity already has ID: " + entity);
		}
		entity.setId(table.add(entity.toMap()));
		return entity.getId();
	}
}
