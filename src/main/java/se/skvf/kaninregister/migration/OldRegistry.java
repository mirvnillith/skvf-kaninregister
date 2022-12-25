package se.skvf.kaninregister.migration;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import se.skvf.kaninregister.drive.GoogleDrive;
import se.skvf.kaninregister.drive.GoogleSheet;
import se.skvf.kaninregister.drive.GoogleSpreadsheet;
import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

@Component
public class OldRegistry {

	static final String OWNER_PUBLIC = "godkänner uppgiftsutlämning till privatperson";
	static final String BREEDER_PUBLIC = "Godkänner att privatpersoner får ta del av uppgifterna";
	static final String OWNER_EMAIL = "Mailadress";
	static final String BREEDER_EMAIL = "E-mail";
	static final String OWNER_PHONE = "telefon";
	static final String BREEDER_PHONE = "Telefonnr";
	static final String POSTAL_ADDRESS = "postadress";
	static final String POSTAL_CODE = "Postnr.";
	static final String POSTAL_CITY = "Ort";
	static final String OWNER_STREET_ADDRESS = "adress";
	static final String BREEDER_STREET_ADDRESS = "Adress";
	static final String OWNER_NAME = "ägare";
	static final String BREEDER_OWNERNAME = "Namn";
	static final String BREEDER_NAME = "Uppfödningens namn";
	static final String FEATURES = "kännetecken";
	static final String COLOUR_MARKINGS = "teckning och färg";
	static final String OWNER_COAT = "hårlag";
	static final String BREEDER_COAT = "pälsvariant";
	static final String RACE = "ras";
	static final String GENDER = "kön";
	static final String BUNNY_NAME = "kaninens namn";
	static final String BIRTHDATE = "födelsedatum";
	static final String OWNER_RING = "ringnr + uppfödarnummer";
	static final String BREEDER_RING = "ringnr";
	static final String CHIP = "mikrochipnr";
	static final String OWNER_EARS = "öronmärkning (V, H)";
	static final String BREEDER_EARS = "Örontatuering vö, hö";
	static final String PERSONNUMMER = "Personnummer";

	private static final Log LOG = LogFactory.getLog(OldRegistry.class);
	
	static final String OWNERS_SHEET = "Ägare";
	static final String BREEDERS_SHEET = "Uppfödare";
	
	@Autowired
	private GoogleDrive drive;
	
	@Autowired
	private Registry registry;

	private GoogleSheet owners;
	private GoogleSheet breeders;
	
	@Value("${skvf.dev.migration:}")
	private String oldRegistry;

	private static long pause = 2000L;
	
	static void setPause(long pause) {
		OldRegistry.pause = pause;
	}
	
	void setOldRegistry(String oldRegistry) {
		this.oldRegistry = oldRegistry;
	}
	
	@PostConstruct
	public void setup() throws IOException {
		if (isNotEmpty(oldRegistry)) {
			
			GoogleSpreadsheet spreadsheet = drive.getSpreadsheet(oldRegistry);
			LOG.info(spreadsheet);
			owners = spreadsheet.getSheet(OWNERS_SHEET);
			LOG.info(owners);
			breeders = spreadsheet.getSheet(BREEDERS_SHEET);
			LOG.info(breeders);
			
			migrate();
		}
	}

	void migrate() throws IOException {

		Map<String, Owner> ownerMap = new HashMap<>();
		Multimap<String, Bunny> bunnyMap = MultimapBuilder.hashKeys().arrayListValues().build();
		
		boolean validOwners = collectOwners(ownerMap, bunnyMap);
		boolean validBreeders = collectBreeders(ownerMap, bunnyMap);
		
		if (!validOwners || !validBreeders) {
			throw new IllegalStateException(oldRegistry + " is not valid");
		}
		
		LOG.info("Owners ...");
		int cnt = 1;
		for (Owner owner : ownerMap.values()) {
			LOG.info(cnt + " of " + ownerMap.size());
			cnt++;
			registry.add(owner);
			pause();
		}
		
		LOG.info("Bunnies ...");
		cnt = 1;
		for (Entry<String, Bunny> bunny : bunnyMap.entries()) {
			LOG.info(cnt + " of " + bunnyMap.size());
			cnt++;
			bunny.getValue().setOwner(ownerMap.get(bunny.getKey()).getId());
			registry.add(bunny.getValue());
			pause();
		}
	}

	private static void pause() {
		try {
			Thread.sleep(pause);
		} catch (InterruptedException ignored) {
			ignored.printStackTrace();
		}
	}

	private boolean collectBreeders(Map<String, Owner> ownerMap, Multimap<String, Bunny> bunnyMap) throws IOException {
		
		LOG.info(breeders.getName() + " ...");
		Map<String, String> columns = mapColumns(breeders, 
				PERSONNUMMER, 
				BREEDER_EARS,
				CHIP,
				BREEDER_RING,
				BIRTHDATE,
				BUNNY_NAME,
				GENDER,
				RACE,
				BREEDER_COAT,
				COLOUR_MARKINGS,
				BREEDER_OWNERNAME,
				BREEDER_NAME,
				BREEDER_STREET_ADDRESS,
				POSTAL_CODE,
				POSTAL_CITY,
				BREEDER_PHONE,
				BREEDER_EMAIL,
				BREEDER_PUBLIC);
		return migrateOwners(loadAll(breeders, columns), columns, ownerMap, bunnyMap);
	}

	private static Collection<Map<String, String>> loadAll(GoogleSheet sheet, Map<String, String> columns) throws IOException {
		
		Map<String, Predicate<String>> allOwners = new HashMap<>();
		allOwners.put(columns.get(PERSONNUMMER), pnr -> true);
		Collection<Map<String, String>> rows = sheet.findRows(allOwners);
		return rows;
	}
	
	private boolean collectOwners(Map<String, Owner> ownerMap, Multimap<String, Bunny> bunnyMap) throws IOException {
		
		LOG.info(owners.getName() + " ...");
		Map<String, String> columns = mapColumns(owners,
				PERSONNUMMER, 
				OWNER_EARS,
				CHIP,
				OWNER_RING,
				BIRTHDATE,
				BUNNY_NAME,
				GENDER,
				RACE,
				OWNER_COAT,
				COLOUR_MARKINGS,
				FEATURES,
				OWNER_NAME,
				OWNER_STREET_ADDRESS,
				POSTAL_ADDRESS,
				OWNER_PHONE,
				OWNER_EMAIL,
				OWNER_PUBLIC);
		return migrateOwners(loadAll(owners, columns), columns, ownerMap, bunnyMap);
	}

	private static boolean migrateOwners(Collection<Map<String, String>> rows, Map<String, String> columns,
			Map<String, Owner> ownerMap, Multimap<String, Bunny> bunnyMap) {
		int cnt = 1;
		boolean valid = true;
		for (Map<String, String> row : rows) {
			LOG.info(cnt + " of " + rows.size());
			cnt++;			
			String pnr = row.get(columns.get(PERSONNUMMER));
			if (!add(pnr, owner(values(row, columns)), ownerMap)) {
				valid = false;
			}
			bunnyMap.put(pnr, bunny(values(row, columns)));
		}
		return valid;
	}
	
	private static Function<String, String> values(Map<String, String> row, Map<String, String> columns) {
		return c -> row.get(columns.get(c));
	}

	private static boolean add(String pnr, Owner owner, Map<String, Owner> map) {
		if (map.containsKey(pnr)) {
			Owner existing = map.get(pnr);
			boolean valid = true;
			if (!merge(pnr, existing, "name", existing.getName(), owner.getName(), Owner::setName)) {
				valid = false;
			}
			if (!merge(pnr, existing, "breederName", existing.getBreederName(), owner.getBreederName(), Owner::setBreederName)) {
				valid = false;
			}
			if (!merge(pnr, existing, "address", existing.getAddress(), owner.getAddress(), Owner::setAddress)) {
				valid = false;
			}
			if (!merge(pnr, existing, "phone", existing.getPhone(), owner.getPhone(), Owner::setPhone)) {
				valid = false;
			}
			if (!merge(pnr, existing, "email", existing.getEmail(), owner.getEmail(), Owner::setEmail)) {
				valid = false;
			}
			if (!merge(pnr, existing, "publicOwner", ""+existing.isPublicOwner(), ""+owner.isPublicOwner(), null)) {
				valid = false;
			}
			return valid;
		} else {
			map.put(pnr, owner);
			return true;
		}
	}

	private static boolean merge(String pnr, Owner owner, String attribute, String existingValue, String additionalValue, BiFunction<Owner, String, Owner> setter) {
		if (isEmpty(existingValue)) {
			setter.apply(owner, additionalValue);
		} else if (isNotEmpty(additionalValue)) {
			if (!Objects.equals(existingValue, additionalValue)) {
				LOG.error(pnr + "." + attribute + ": " + existingValue + " differs from " + additionalValue);
				return false;
			}
		}
		
		return true;
	}

	private static Bunny bunny(Function<String, String> values) {
		 Bunny bunny = new Bunny()
				.setName(values.apply(BUNNY_NAME))
				.setChip(values.apply(CHIP))
				.setRing(ofNullable(values.apply(OWNER_RING)).orElse(values.apply(BREEDER_RING)))
				.setBirthDate(values.apply(BIRTHDATE))
				.setRace(values.apply(RACE))
				.setCoat(ofNullable(values.apply(OWNER_COAT)).orElse(values.apply(BREEDER_COAT)))
				.setColourMarkings(values.apply(COLOUR_MARKINGS))
				.setFeatures(values.apply(FEATURES));
		 
		 String earsValue = ofNullable(values.apply(OWNER_EARS)).orElse(values.apply(BREEDER_EARS));
		 String[] ears = ofNullable(earsValue).orElse("").split(",");
		 switch (ears.length) {
			case 1:
				bunny.setLeftEar(ears[0].replace("vö","").trim());
				break;
			case 2:
				bunny.setLeftEar(ears[0].replace("vö","").trim());
				bunny.setRightEar(ears[1].replace("hö","").trim());
				break;
		 }
		 
		 switch (values.apply(GENDER)) {
		 	case "hane":
		 		bunny.setGender(Bunny.Gender.MALE);
		 		break;
		 	case "hona":
		 		bunny.setGender(Bunny.Gender.FEMALE);
		 		break;
		 	case "hankastrat":
		 		bunny.setGender(Bunny.Gender.MALE);
		 		bunny.setNeutered(true);
		 		break;
		 	case "honkastrat":
		 		bunny.setGender(Bunny.Gender.FEMALE);
		 		bunny.setNeutered(true);
		 		break;
		 }
		 
		 return bunny;
	}

	private static Owner owner(Function<String, String> values) {
		
		Owner owner = new Owner()
				.setName(ofNullable(values.apply(OWNER_NAME)).orElse(values.apply(BREEDER_OWNERNAME)))
				.setPhone(ofNullable(values.apply(OWNER_PHONE)).orElse(values.apply(BREEDER_PHONE)))
				.setEmail(ofNullable(values.apply(OWNER_EMAIL)).orElse(values.apply(BREEDER_EMAIL)));
		owner.setBreederName(values.apply(BREEDER_NAME));
		String address = ofNullable(values.apply(OWNER_STREET_ADDRESS)).orElse(values.apply(BREEDER_STREET_ADDRESS));
		if (isNotBlank(values.apply(POSTAL_ADDRESS))) {
			address += ", " + values.apply(POSTAL_ADDRESS);
		}
		if (isNotBlank(values.apply(POSTAL_CODE))) {
			address += ", " + values.apply(POSTAL_CODE);
			if (isNotBlank(values.apply(POSTAL_CITY))) {
				address += " " + values.apply(POSTAL_CITY);
			}
		} else {
			if (isNotBlank(values.apply(POSTAL_CITY))) {
				address += ", " + values.apply(POSTAL_CITY);
			}
		}
		owner.setAddress(address);
		
		if ("ja".equalsIgnoreCase(values.apply(OWNER_PUBLIC)) ||
				"ja".equalsIgnoreCase(values.apply(BREEDER_PUBLIC))) {
			owner.setPublicOwner(true)
				.setPublicBreeder(true);
		}
		
		return owner;
	}

	private Map<String, String> mapColumns(GoogleSheet sheet, String... headers) throws IOException {
		return sheet.getColumns(asList(headers));
	}
}
