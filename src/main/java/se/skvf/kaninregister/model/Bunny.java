package se.skvf.kaninregister.model;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class Bunny extends Entity {

	static final Collection<String> COLUMNS = asList("Ägare", "Tidigare Ägare", "Namn", "Uppfödare");

	private String name;
	private String owner;
	private String previousOwner;
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
	
	public String getPreviousOwner() {
		return previousOwner;
	}
	
	public Bunny setPreviousOwner(String previousOwner) {
		this.previousOwner = previousOwner;
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
		if (owner == null) {
			throw new IllegalStateException("Bunny must have an owner");
		}
		if (name == null) {
			throw new IllegalStateException("Bunny must have a name");
		}
		map.put("Ägare", owner);
		map.put("Tidigare Ägare", previousOwner);
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
		previousOwner = map.get("Tidigare Ägare");
		name = map.get("Namn");
		breeder = map.get("Uppfödare");
		return this;
	}

	public static Map<String, Predicate<String>> byOwner(String id) {
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put("Ägare", id::equals);
		return filter;
	}
	
	public static Map<String, Predicate<String>> byPreviousOwner(String id) {
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put("Tidigare Ägare", id::equals);
		return filter;
	}
	
	public static Map<String, Predicate<String>> byBreeder(String id) {
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put("Uppfödare", id::equals);
		return filter;
	}
}
