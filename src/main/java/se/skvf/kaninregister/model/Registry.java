package se.skvf.kaninregister.model;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import se.skvf.kaninregister.data.Database;
import se.skvf.kaninregister.data.Table;

@Component
public class Registry {

	static final String BUNNIES_TABLE = "Kaniner";
	static final String OWNERS_TABLE = "Ägare";

	@Autowired
	private Database database;
	
	private Table bunnies;
	private Table owners;

	@Value("${skvf.dev.test:false}")
	private boolean test;
	
	@PostConstruct
	public void setup() throws Exception {
		
		bunnies = database.getTable(BUNNIES_TABLE, Bunny.COLUMNS);
		owners = database.getTable(OWNERS_TABLE, Owner.COLUMNS);
		
		RegistryRuntimeTest runtimeTest = new RegistryRuntimeTest();
		if (test) {
			runtimeTest.test(this);
		}
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

	public void update(Owner owner) throws IOException {
		update(owners, owner);
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
	
	public void update(Bunny bunny) throws IOException {
		update(bunnies, bunny);
	}
	
	public void remove(Bunny bunny) throws IOException {
		remove(bunnies, bunny);
	}
	
	private void remove(Table table, Entity<?> entity) throws IOException {
		if (entity.getId() == null) {
			throw new IllegalStateException("Entity has no ID: " + entity);
		}
		table.remove(entity.getId());
	}
	
	private void update(Table table, Entity<?> entity) throws IOException {
		if (entity.getId() == null) {
			throw new IllegalStateException("Entity has no ID: " + entity);
		}
		table.update(entity.toMap());
	}
	
	private String add(Table table, Entity<?> entity) throws IOException {
		if (entity.getId() != null) {
			throw new IllegalStateException("Entity already has ID: " + entity);
		}
		entity.setId(table.add(entity.toMap()));
		return entity.getId();
	}
}
