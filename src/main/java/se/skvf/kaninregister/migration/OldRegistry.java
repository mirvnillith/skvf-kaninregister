package se.skvf.kaninregister.migration;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
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

	private static final String PERSONNUMMER = "Personnummer";

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
		
		boolean valid = collectOwners(ownerMap, bunnyMap) &&
				collectBreeders(ownerMap, bunnyMap);
		
		if (!valid) {
			throw new IllegalStateException(oldRegistry + " is not valid");
		}
		
		LOG.info("Owners ...");
		int cnt = 1;
		for (Owner owner : ownerMap.values()) {
			LOG.info(cnt + " of " + ownerMap.size());
			cnt++;
			registry.add(owner);
		}
		
		LOG.info("Bunnies ...");
		cnt = 1;
		for (Entry<String, Bunny> bunny : bunnyMap.entries()) {
			LOG.info(cnt + " of " + bunnyMap.size());
			cnt++;
			bunny.getValue().setOwner(ownerMap.get(bunny.getKey()).getId());
			registry.add(bunny.getValue());
		}
	}

	private boolean collectBreeders(Map<String, Owner> ownerMap, Multimap<String, Bunny> bunnyMap) throws IOException {
		
		LOG.info(breeders.getName() + " ...");
		Map<String, String> columns = mapColumns(PERSONNUMMER, "Förnamn", "Efternamn", "Namn", "Kön");
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
		Map<String, String> columns = mapColumns(PERSONNUMMER, "Förnamn", "Efternamn", "Namn", "Kön");
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
			if (!merge(pnr, existing, "firstName", existing.getFirstName(), owner.getFirstName(), Owner::setFirstName)) {
				valid = false;
			}
			if (!merge(pnr, existing, "lastName", existing.getLastName(), owner.getLastName(), Owner::setLastName)) {
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
				.setName(values.apply("kaninens namn"))
				.setChip(values.apply("mikrochipnr"))
				.setRing(values.apply("ringnr + uppfödarnummer"))
				.setBirthDate(values.apply("födelsedatum"))
				.setRace(values.apply("ras"))
				.setCoat(values.apply("hårlag"))
				.setColourMarkings(values.apply("teckning och färg"))
				.setFeatures(values.apply("kännetecken"));
		 
		 String[] ears = values.apply("öronnummer, vä först").split(",");
		 switch (ears.length) {
			case 1:
				bunny.setLeftEar(ears[0].trim());
				break;
			case 2:
				bunny.setLeftEar(ears[0].trim());
				bunny.setRightEar(ears[1].trim());
				break;
		 }
		
		 
		 switch (values.apply("kön")) {
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
				.setPhone(values.apply("telefon"))
				.setEmail(values.apply("Mailadress"));
		
		String[] names = values.apply("ägare").split(" ");
		switch (names.length) {
			case 0:
				owner.setFirstName("Utan");
				owner.setLastName("Namn");
				break;
			case 1:
				owner.setFirstName(names[0]);
				owner.setLastName("-");
				break;
			case 2:
				owner.setFirstName(names[0]);
				owner.setLastName(names[1]);
				break;
			default:
				owner.setFirstName(names[0]);
				owner.setLastName(values.apply("ägare").substring(names[0].length()+1));
				break;
		}
		
		String address = values.apply("adress");
		if (isNotBlank(values.apply("postadress"))) {
			address += ", " + values.apply("postadress");
		}
		owner.setAddress(address);
		
		if (values.apply("godkänner uppgiftsutlämning till privatperson").equalsIgnoreCase("ja")) {
			owner.setPublicOwner(true)
				.setPublicBreeder(true);
		}
		
		return owner;
	}

	private Map<String, String> mapColumns(String... headers) throws IOException {
		Map<String, String> columns = new HashMap<>();
		for (String header : headers) {
			columns.put(header, owners.getColumn(header));
		}
		return columns;
	}
}
