package se.skvf.kaninregister.model;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isAllEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static se.skvf.kaninregister.model.Bunny.Gender.ofValue;
import static se.skvf.kaninregister.model.Bunny.IdentifierLocation.CHIP;
import static se.skvf.kaninregister.model.Bunny.IdentifierLocation.LEFT_EAR;
import static se.skvf.kaninregister.model.Bunny.IdentifierLocation.RIGHT_EAR;
import static se.skvf.kaninregister.model.Bunny.IdentifierLocation.RING;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Bunny extends Entity<Bunny> {

	private static final String OWNER = "Ägare";
	private static final String PREVIOUS_OWNER = "Föregående Ägare";
	private static final String BREEDER = "Uppfödare";

	public enum IdentifierLocation {
		LEFT_EAR("Vänster Öra"), RIGHT_EAR("Höger Öra"), CHIP("Chipnummer"), RING("Ringnummer");
		
		private final String column;
		
		IdentifierLocation(String column) {
			this.column = column;
		}
		
		String getColumn() {
			return column;
		}
	}
	
	public enum Gender {
		FEMALE("Hona"), MALE("Hane");
		
		private final String value;
		
		Gender(String value) {
			this.value = value;
		}
		
		String getValue() {
			return value;
		}
		
		static Gender ofValue(String value) {
			return Stream.of(values())
			.filter(g -> g.value.equals(value))
			.findAny()
			.orElse(null);
		}
		
		@Override
		public String toString() {
			return value;
		}
	}

	static final List<String> COLUMNS = asList(
			"Namn", 
			OWNER, 
			PREVIOUS_OWNER, 
			BREEDER,
			"Kön",
			"Kastrerad",
			"Födelsedag",
			"Ras",
			"Hårlag",
			"Färgteckning",
			"Bild",
			LEFT_EAR.getColumn(),
			RIGHT_EAR.getColumn(),
			CHIP.getColumn(),
			RING.getColumn());

	public static final char WILDCARD = '?';

	private String name;
	private String owner;
	private String previousOwner;
	private String breeder;
	private Gender gender;
	private boolean neutered;
	private String birthDate;
	private String race;
	private String coat;
	private String colourMarkings;
	private String picture;
	private String leftEar;
	private String rightEar;
	private String chip;
	private String ring;
	
	@Override
	public Bunny setId(String id) {
		return (Bunny) super.setId(id);
	}
	
	public Gender getGender() {
		return gender;
	}
	
	public Bunny setGender(Gender gender) {
		this.gender = gender;
		return this;
	}
	
	public boolean isNeutered() {
		return neutered;
	}
	
	public Bunny setNeutered(boolean neutered) {
		this.neutered = neutered;
		return this;
	}
	
	public String getBirthDate() {
		return birthDate;
	}

	public Bunny setBirthDate(String birthDate) {
		this.birthDate = birthDate;
		return this;
	}

	public String getRace() {
		return race;
	}
	
	public Bunny setRace(String race) {
		this.race = race;
		return this;
	}
	
	public String getCoat() {
		return coat;
	}

	public Bunny setCoat(String coat) {
		this.coat = coat;
		return this;
	}

	public String getColourMarkings() {
		return colourMarkings;
	}

	public Bunny setColourMarkings(String colourMarkings) {
		this.colourMarkings = colourMarkings;
		return this;
	}

	public String getPicture() {
		return picture;
	}

	public Bunny setPicture(String picture) {
		this.picture = picture;
		return this;
	}

	public String getLeftEar() {
		return leftEar;
	}

	public Bunny setLeftEar(String leftEar) {
		this.leftEar = leftEar;
		return this;
	}

	public String getRightEar() {
		return rightEar;
	}

	public Bunny setRightEar(String rightEar) {
		this.rightEar = rightEar;
		return this;
	}

	public String getChip() {
		return chip;
	}

	public Bunny setChip(String chip) {
		this.chip = chip;
		return this;
	}

	public String getRing() {
		return ring;
	}

	public Bunny setRing(String ring) {
		this.ring = ring;
		return this;
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
		if (isEmpty(owner)) {
			throw new IllegalStateException("Bunny must have an owner");
		}
		if (isEmpty(name)) {
			throw new IllegalStateException("Bunny must have a name");
		}
		if (isAllEmpty(leftEar, rightEar, chip, ring)) {
			throw new IllegalStateException("Bunny must have at least one identifier");
		}

		List<String> values = new ArrayList<String>(COLUMNS.size());
		values.add(name);
		values.add(owner);
		values.add(previousOwner);
		values.add(breeder);
		values.add(ofNullable(gender).map(Gender::getValue).orElse(null));
		values.add(toString(neutered));
		values.add(birthDate);
		values.add(race);
		values.add(coat);
		values.add(colourMarkings);
		values.add(picture);
		values.add(leftEar);
		values.add(rightEar);
		values.add(chip);
		values.add(ring);
		
		addToMap(map, COLUMNS, values);
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
		
		List<BiConsumer<Bunny, String>> setters = asList(
				Bunny::setName,
				Bunny::setOwner,
				Bunny::setPreviousOwner,
				Bunny::setBreeder,
				(b,v) -> b.setGender(ofValue(v)),
				(b,v) -> b.setNeutered(booleanFromString(v)),
				Bunny::setBirthDate,
				Bunny::setRace,
				Bunny::setCoat,
				Bunny::setColourMarkings,
				Bunny::setPicture,
				Bunny::setLeftEar,
				Bunny::setRightEar,
				Bunny::setChip,
				Bunny::setRing
				);

		return setFromMap(map, COLUMNS, setters);
	}

	public static Map<String, Predicate<String>> byOwner(String id) {
		return Entity.by(OWNER, id);
	}
	
	public static Map<String, Predicate<String>> byPreviousOwner(String id) {
		return Entity.by(PREVIOUS_OWNER, id);
	}
	
	public static Map<String, Predicate<String>> byBreeder(String id) {
		return Entity.by(BREEDER, id);
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
		case RIGHT_EAR:
		case RING:
			return Entity.by(location.getColumn(), predicate);
		case CHIP: {
			return Entity.by(location.getColumn(), multiple(predicate));
		}
		default:
			throw new IOException("Unknown location: " + location);
		}
	}
	
	static Predicate<String> multiple(Predicate<String> predicate) {
		return v -> v != null && Stream.of(v.split(",")).map(String::trim).anyMatch(predicate);
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
