package se.skvf.kaninregister.api;

import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

public class RegistryStub extends Registry {

	private Map<String, Owner> owners;
	private Map<String, Bunny> bunnies;
	
	@Override
	public void setup() {
		owners = new HashMap<>();
		bunnies = new HashMap<>();
	}
	
	@Override
	public String add(Bunny bunny) {
		bunny.setId(randomUUID().toString());
		bunnies.put(bunny.getId(), bunny);
		return bunny.getId();
	}
	
	@Override
	public String add(Owner owner) {
		owner.setId(randomUUID().toString());
		owners.put(owner.getId(), owner);
		return owner.getId();
	}
	
	@Override
	public void update(Bunny bunny) {
		bunnies.put(bunny.getId(), bunny);
	}
	
	@Override
	public void update(Owner owner) {
		owners.put(owner.getId(), owner);
	}
	
	@Override
	public void remove(Bunny bunny) throws IOException {
		bunnies.remove(bunny.getId());
	}
	
	@Override
	public void remove(Owner owner) throws IOException {
		owners.remove(owner.getId());
	}
	
	@Override
	public Collection<Bunny> findBunnies(Collection<String> ids) {
		return ids.stream().map(bunnies::get).collect(toList());
	}
	
	@Override
	public Collection<Owner> findOwners(Collection<String> ids) {
		return ids.stream().map(owners::get).collect(toList());
	}
	
	@Override
	public Collection<Bunny> findBunnies(Map<String, Predicate<String>> filters) throws IOException {
		return bunnies.values().stream()
				.filter(b -> matches(b.toMap(), filters))
				.collect(toList());
	}
	
	@Override
	public Collection<Owner> findOwners(Map<String, Predicate<String>> filters) throws IOException {
		return owners.values().stream()
				.filter(b -> matches(b.toMap(), filters))
				.collect(toList());
	}

	private static boolean matches(Map<String, String> map, Map<String, Predicate<String>> filters) {
		for (Map.Entry<String, Predicate<String>> filter : filters.entrySet()) {
			if (!filter.getValue().test(map.get(filter.getKey()))) {
				return false;
			}
		}
		return true;
	}
}
