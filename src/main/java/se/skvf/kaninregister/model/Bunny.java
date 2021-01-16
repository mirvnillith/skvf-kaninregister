package se.skvf.kaninregister.model;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Map;

public class Bunny extends Entity {

	static final Collection<String> COLUMNS = asList("Ägare", "Namn", "Uppfödare");

	private String owner;
	private String name;
	private String breeder;
	
	@Override
	public Bunny setId(String id) {
		return (Bunny) super.setId(id);
	}
	
	public String getOwner() {
		return owner;
	}

	public Bunny setOwner(String owner) {
		this.owner = owner;
		return this;
	}

	public String getName() {
		return name;
	}

	public Bunny setName(String name) {
		this.name = name;
		return this;
	}

	public String getBreeder() {
		return breeder;
	}
	
	public Bunny setBreeder(String breeder) {
		this.breeder = breeder;
		return this;
	}
	
	@Override
	protected void toMap(Map<String, String> map) {
		map.put("Ägare", owner);
		map.put("Namn", name);
		map.put("Uppfödare", breeder);
	}

	@Override
	public String toString() {
		return super.toString() + ": " + name + "@" + owner;
	}

	public static Bunny from(Map<String, String> map) {
		return new Bunny().fromMap(map);
	}

	protected Bunny fromMap(Map<String, String> map) {
		super.fromMap(map);
		owner = map.get("Ägare");
		name = map.get("Namn");
		breeder = map.get("Uppfödare");
		return this;
	}
}
