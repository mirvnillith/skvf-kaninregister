package se.skvf.kaninregister.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Bunny extends Entity {

	public enum IdentifierLocation {
		LEFT_EAR, RIGHT_EAR, CHIP, RING
	}

	static final Collection<String> COLUMNS = asList("Ägare", "Tidigare Ägare", "Namn", "Uppfödare");

	public static final char WILDCARD = '?';

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
		return Entity.by("Ägare", id);
	}
	
	public static Map<String, Predicate<String>> byPreviousOwner(String id) {
		return Entity.by("Tidigare Ägare", id);
	}
	
	public static Map<String, Predicate<String>> byBreeder(String id) {
		return Entity.by("Uppfödare", id);
	}
	
	public static Map<String, Predicate<String>> byExactIdentifier(IdentifierLocation location, String identifier) throws IOException {
		if (location == null || identifier == null) {
			return emptyMap();
		}
		return byIdentifier(location, identifier::equals);
	}
	
	public static Map<String, Predicate<String>> byWildcardIdentifier(IdentifierLocation location, String identifier) throws IOException {
		if (location == null || identifier == null) {
			return emptyMap();
		}
		return byIdentifier(location, wildcard(identifier));
	}

	private static Map<String, Predicate<String>> byIdentifier(IdentifierLocation location,
			Predicate<String> predicate) throws IOException {
		switch (location) {
		case LEFT_EAR:
			return Entity.by("Vänster Öra", predicate);
		case RIGHT_EAR:
			return Entity.by("Höger Öra", predicate);
		case CHIP: {
			Map<String, Predicate<String>> chips = new HashMap<>();
			Predicate<String> orPredicate = new OrPredicate(predicate);
			chips.put("Chip 1", orPredicate);
			chips.put("Chip 2", orPredicate);
			return chips;
		}
		case RING:
			return Entity.by("Ring", predicate);
		default:
			throw new IOException("Unknown location: " + location);
		}
	}
	
	static class OrPredicate implements Predicate<String> {
		
		Predicate<String> predicate;
		Boolean firstValue;
		
		public OrPredicate(Predicate<String> predicate) {
			this.predicate = predicate;
		}
		
		@Override
		public boolean test(String value) {
			if (firstValue == null) {
				firstValue = predicate.test(value);
				return true;
			} else {
				return firstValue || predicate.test(value);
			}
		}
	}

	static Predicate<String> wildcard(String identifier) {
		
		if (isEmpty(identifier)) {
			return value -> false;
		}
		
		List<Integer> wildcards = new ArrayList<>();
		for (int i=0; i<identifier.length(); i++) {
			if (identifier.charAt(i) == WILDCARD) {
				wildcards.add(i);
			}
		}
		
		return value -> {
			if (value == null) {
				return false;
			}
			char[] valueChars = value.toCharArray();
			for (int i : wildcards) {
				if (i >= valueChars.length) {
					return false;
				} else {
					valueChars[i] = WILDCARD;
				}
			}
			return new String(valueChars).equalsIgnoreCase(identifier);
		};
	}
}
