package se.skvf.kaninregister.model;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
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
	
	@PostConstruct
	public void setup() throws IOException {
		bunnies = database.getTable(BUNNIES_NAME, Bunny.COLUMNS);
		owners = database.getTable(OWNERS_NAME, Owner.COLUMNS);
		Owner jonas = new Owner().setFirstName("Jonas").setLastName("Olsson");
		Owner maria = new Owner().setFirstName("Maria").setLastName("Wahlström");
		add(jonas);
		add(maria);
		Bunny bilbo = new Bunny().setOwner(maria.getId()).setName("Bilbo");
		Bunny pompom = new Bunny().setOwner(jonas.getId()).setName("PomPom");
		add(bilbo);
		add(pompom);
		
		remove(bilbo);
		remove(pompom);
		remove(jonas);
		remove(maria);
	}

	public String add(Owner owner) throws IOException {
		return add(owners, owner);
	}
	
	public void remove(Owner owner) throws IOException {
		remove(owners, owner);
	}
	
	public String add(Bunny bunny) throws IOException {
		return add(bunnies, bunny);
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
