package se.skvf.kaninregister.migration;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.migration.OldRegistry.BIRTHDATE;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDERS_SHEET;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_COAT;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_EARS;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_EMAIL;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_NAME;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_OWNERNAME;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_PHONE;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_PUBLIC;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_RING;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDER_STREET_ADDRESS;
import static se.skvf.kaninregister.migration.OldRegistry.BUNNY_NAME;
import static se.skvf.kaninregister.migration.OldRegistry.CHIP;
import static se.skvf.kaninregister.migration.OldRegistry.COLOUR_MARKINGS;
import static se.skvf.kaninregister.migration.OldRegistry.FEATURES;
import static se.skvf.kaninregister.migration.OldRegistry.GENDER;
import static se.skvf.kaninregister.migration.OldRegistry.OWNERS_SHEET;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_COAT;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_EARS;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_EMAIL;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_NAME;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_PHONE;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_PUBLIC;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_RING;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_STREET_ADDRESS;
import static se.skvf.kaninregister.migration.OldRegistry.PERSONNUMMER;
import static se.skvf.kaninregister.migration.OldRegistry.POSTAL_ADDRESS;
import static se.skvf.kaninregister.migration.OldRegistry.POSTAL_CITY;
import static se.skvf.kaninregister.migration.OldRegistry.POSTAL_CODE;
import static se.skvf.kaninregister.migration.OldRegistry.RACE;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import se.skvf.kaninregister.BunnyTest;
import se.skvf.kaninregister.drive.GoogleDrive;
import se.skvf.kaninregister.drive.GoogleSheet;
import se.skvf.kaninregister.drive.GoogleSpreadsheet;
import se.skvf.kaninregister.model.Bunny;
import se.skvf.kaninregister.model.Owner;
import se.skvf.kaninregister.model.Registry;

public class OldRegistryTest extends BunnyTest {

	@InjectMocks
	private OldRegistry old;
	
	@Mock
	private GoogleDrive drive;
	@Mock
	private GoogleSpreadsheet spreadsheet;
	@Mock
	private GoogleSheet ownerSheet;
	@Mock
	private GoogleSheet breederSheet;
	
	@Mock
	private Registry registry;
	@Captor
	private ArgumentCaptor<Bunny> bunny;
	@Captor
	private ArgumentCaptor<Owner> owner;
	
	private Map<String, String> columns;
	
	@BeforeEach
	public void setup() throws IOException {
		OldRegistry.setPause(0L);
		old.setOldRegistry("Old Registry");
		when(drive.getSpreadsheet("Old Registry")).thenReturn(spreadsheet);
		when(spreadsheet.getSheet(OWNERS_SHEET)).thenReturn(ownerSheet);
		when(spreadsheet.getSheet(BREEDERS_SHEET)).thenReturn(breederSheet);
		
		columns = new HashMap<>();
		columns.put(PERSONNUMMER, randomUUID().toString());
		columns.put(BIRTHDATE, randomUUID().toString());
		columns.put(BUNNY_NAME, randomUUID().toString());
		columns.put(CHIP, randomUUID().toString());
		columns.put(OWNER_COAT, randomUUID().toString());
		columns.put(BREEDER_COAT, randomUUID().toString());
		columns.put(COLOUR_MARKINGS, randomUUID().toString());
		columns.put(OWNER_EARS, randomUUID().toString());
		columns.put(BREEDER_EARS, randomUUID().toString());
		columns.put(OWNER_EMAIL, randomUUID().toString());
		columns.put(BREEDER_EMAIL, randomUUID().toString());
		columns.put(FEATURES, randomUUID().toString());
		columns.put(GENDER, randomUUID().toString());
		columns.put(OWNER_NAME, randomUUID().toString());
		columns.put(BREEDER_OWNERNAME, randomUUID().toString());
		columns.put(BREEDER_NAME, randomUUID().toString());
		columns.put(OWNER_PHONE, randomUUID().toString());
		columns.put(BREEDER_PHONE, randomUUID().toString());
		columns.put(POSTAL_ADDRESS, randomUUID().toString());
		columns.put(POSTAL_CODE, randomUUID().toString());
		columns.put(POSTAL_CITY, randomUUID().toString());
		columns.put(OWNER_PUBLIC, randomUUID().toString());
		columns.put(BREEDER_PUBLIC, randomUUID().toString());
		columns.put(RACE, randomUUID().toString());
		columns.put(OWNER_RING, randomUUID().toString());
		columns.put(BREEDER_RING, randomUUID().toString());
		columns.put(OWNER_STREET_ADDRESS, randomUUID().toString());
		columns.put(BREEDER_STREET_ADDRESS, randomUUID().toString());
		
		when(ownerSheet.getColumns(any())).thenAnswer(i -> {
			Collection<String> names = i.getArgument(0);
			return names.stream().collect(toMap(identity(), columns::get));
		});
		when(breederSheet.getColumns(any())).thenAnswer(i -> {
			Collection<String> names = i.getArgument(0);
			return names.stream().collect(toMap(identity(), columns::get));
		});
	}
	
	@Test
	public void empty() throws IOException {
		old.setup();
	}
	
	@Test
	public void bunnyBirthdate() throws IOException {
		
		assertBunny(BIRTHDATE, Bunny::getBirthDate);
	}
	
	@Test
	public void bunnyChip() throws IOException {
		
		assertBunny(CHIP, Bunny::getChip);
	}
	
	@Test
	public void bunnyCoat() throws IOException {
		
		assertBunny(OWNER_COAT, BREEDER_COAT, Bunny::getCoat);
	}
	
	@Test
	public void bunnyColourMarkings() throws IOException {
		
		assertBunny(COLOUR_MARKINGS, Bunny::getColourMarkings);
	}
	
	@Test
	public void bunnyFeatures() throws IOException {
		
		assertBunny(FEATURES, null, Bunny::getFeatures);
	}
	
	@Test
	public void bunnyName() throws IOException {
		
		assertBunny(BUNNY_NAME, Bunny::getName);
	}
	
	@Test
	public void bunnyRace() throws IOException {
		
		assertBunny(RACE, Bunny::getRace);
	}
	
	@Test
	public void bunnyRing() throws IOException {
		
		assertBunny(OWNER_RING, BREEDER_RING, Bunny::getRing);
	}
	
	@Test
	public void ownerName() throws IOException {
		
		assertOwner(OWNER_NAME, BREEDER_OWNERNAME, Owner::getName);
	}
	
	@Test
	public void ownerEmail() throws IOException {
		
		assertOwner(OWNER_EMAIL, BREEDER_EMAIL, Owner::getEmail);
	}
	
	@Test
	public void ownerPhone() throws IOException {
		
		assertOwner(OWNER_PHONE, BREEDER_PHONE, Owner::getPhone);
	}
	
	@ParameterizedTest
	@MethodSource("addresses")
	public void ownerAddress(String streetAddress, String postalAddress, String address) throws IOException {
		
		Map<String, String> row = defaultRow();
		row.put(columns.get(OWNER_STREET_ADDRESS), streetAddress);
		row.put(columns.get(POSTAL_ADDRESS), postalAddress);
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(owner.capture());
		assertThat(owner.getValue().getAddress()).isEqualTo(address);
	}
	
	public static Stream<Arguments> addresses() {
		return Stream.of(
				Arguments.of("", "", ""),
				Arguments.of("123", "", "123"),
				Arguments.of("123", "456", "123, 456")
				);
	}
	
	@ParameterizedTest
	@MethodSource("publicOwner")
	public void ownerPublic(String value, boolean expected) throws IOException {
		
		Map<String, String> row = defaultRow();
		row.put(columns.get(OWNER_PUBLIC), value);
		row.put(columns.get(BREEDER_PUBLIC), value);
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(row));
		when(breederSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(owner.capture());
		assertThat(owner.getValue().isPublicOwner()).isEqualTo(expected);
		assertThat(owner.getValue().isPublicBreeder()).isEqualTo(expected);
	}
	
	public static Stream<Arguments> publicOwner() {
		return Stream.of(
				Arguments.of("", false),
				Arguments.of("-", false),
				Arguments.of("nej", false),
				Arguments.of("Nej", false),
				Arguments.of("ja", true),
				Arguments.of("Ja", true),
				Arguments.of("JA", true)
				);
	}
	
	@ParameterizedTest
	@MethodSource("ears")
	public void bunnyEars(String ears, String leftEar, String rightEar) throws IOException {
		
		Map<String, String> row = defaultRow();
		row.put(columns.get(OWNER_EARS), ears);
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(bunny.capture());
		assertThat(bunny.getValue().getLeftEar()).isEqualTo(leftEar);
		assertThat(bunny.getValue().getRightEar()).isEqualTo(rightEar);
	}
	
	public static Stream<Arguments> ears() {
		return Stream.of(
				Arguments.of("", "", null),
				Arguments.of("123", "123", null),
				Arguments.of(" 123 ", "123", null),
				Arguments.of("vö123", "123", null),
				Arguments.of("vö 123", "123", null),
				Arguments.of("123,456", "123", "456"),
				Arguments.of(" 123 , 456 ", "123", "456"),
				Arguments.of("vö123,hö456", "123", "456"),
				Arguments.of("vö 123, hö 456", "123", "456")
				);
	}
	
	@ParameterizedTest
	@MethodSource("gender")
	public void bunnyGender(String value, Bunny.Gender gender, boolean neutered) throws IOException {
		
		Map<String, String> row = defaultRow();
		row.put(columns.get(GENDER), value);
		
		when(breederSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(bunny.capture());
		assertThat(bunny.getValue().getGender()).isEqualTo(gender);
		assertThat(bunny.getValue().isNeutered()).isEqualTo(neutered);
	}
	
	public static Stream<Arguments> gender() {
		return Stream.of(
				Arguments.of("", null, false),
				Arguments.of("hona", Bunny.Gender.FEMALE, false),
				Arguments.of("honkastrat", Bunny.Gender.FEMALE, true),
				Arguments.of("hane", Bunny.Gender.MALE, false),
				Arguments.of("hankastrat", Bunny.Gender.MALE, true)
				);
	}
	
	@Test
	public void nameConflict() throws IOException {
		
		assertOwnerConflict(OWNER_NAME);
		assertBreederConflict(BREEDER_OWNERNAME);
		assertBreederConflict(BREEDER_NAME);
	}
	
	@Test
	public void streetAddressConflict() throws IOException {
		
		assertOwnerConflict(OWNER_STREET_ADDRESS);
		assertBreederConflict(BREEDER_STREET_ADDRESS);
	}
	
	@Test
	public void postalAddressConflict() throws IOException {
		
		assertOwnerConflict(POSTAL_ADDRESS);
		assertBreederConflict(POSTAL_CODE);
		assertBreederConflict(POSTAL_CITY);
	}
	
	@Test
	public void phoneConflict() throws IOException {
		
		assertOwnerConflict(OWNER_PHONE);
		assertBreederConflict(BREEDER_PHONE);
	}
	
	@Test
	public void emailConflict() throws IOException {
		
		assertOwnerConflict(OWNER_EMAIL);
		assertBreederConflict(BREEDER_EMAIL);
	}
	
	@Test
	public void publicConflict() throws IOException {
		
		Map<String, String> owner1 = defaultRow();
		owner1.put(columns.get(OWNER_PUBLIC), "Ja");
		owner1.put(columns.get(BREEDER_PUBLIC), "Ja");
		Map<String, String> owner2 = defaultRow();
		owner2.put(columns.get(PERSONNUMMER), owner1.get(columns.get(PERSONNUMMER)));
		owner2.put(columns.get(OWNER_PUBLIC), "Nej");
		owner2.put(columns.get(BREEDER_PUBLIC), "Nej");
		
		when(ownerSheet.findRows(anyMap())).thenReturn(asList(owner1, owner2));
		
		assertThrows(IllegalStateException.class, () -> old.setup());
		
		when(ownerSheet.findRows(anyMap())).thenReturn(emptyList());
		when(breederSheet.findRows(anyMap())).thenReturn(asList(owner1, owner2));
		
		assertThrows(IllegalStateException.class, () -> old.setup());
	}

	private void assertOwnerConflict(String attribute) throws IOException {
		Map<String, String> owner1 = defaultRow();
		owner1.put(columns.get(attribute), randomUUID().toString());
		Map<String, String> owner2 = defaultRow();
		owner2.put(columns.get(PERSONNUMMER), owner1.get(columns.get(PERSONNUMMER)));
		owner2.put(columns.get(attribute), randomUUID().toString());
		
		when(ownerSheet.findRows(anyMap())).thenReturn(asList(owner1, owner2));
		
		assertThrows(IllegalStateException.class, () -> old.setup());
	}
	
	private void assertBreederConflict(String attribute) throws IOException {
		Map<String, String> owner1 = defaultRow();
		owner1.put(columns.get(attribute), randomUUID().toString());
		Map<String, String> owner2 = defaultRow();
		owner2.put(columns.get(PERSONNUMMER), owner1.get(columns.get(PERSONNUMMER)));
		owner2.put(columns.get(attribute), randomUUID().toString());
		
		when(breederSheet.findRows(anyMap())).thenReturn(asList(owner1, owner2));
		
		assertThrows(IllegalStateException.class, () -> old.setup());
	}
	
	private void assertBunny(String attribute, Function<Bunny, String> getter) throws IOException {
		assertBunny(attribute, attribute, getter);
	}
	
	private void assertBunny(String ownerAttribute, String breederAttribute, Function<Bunny, String> getter) throws IOException {
		
		Map<String, String> row = defaultRow();
		
		String ownerValue = randomUUID().toString();
		row.put(columns.get(ownerAttribute), ownerValue);
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(bunny.capture());
		assertThat(getter.apply(bunny.getValue())).isEqualTo(ownerValue);
		
		if (breederAttribute != null) {
			reset(ownerSheet, registry);
			
			String breederValue = randomUUID().toString();
			row.put(columns.get(breederAttribute), breederValue);
			
			when(breederSheet.findRows(anyMap())).thenReturn(singleton(row));
			
			old.setup();
			
			verify(registry).add(bunny.capture());
			assertThat(getter.apply(bunny.getValue())).isEqualTo(breederValue);
		}
	}
	
	private void assertOwner(String attribute, Function<Owner, String> getter) throws IOException {
		assertOwner(attribute, attribute, getter);
	}
	
	private void assertOwner(String ownerAttribute, String breederAttribute, Function<Owner, String> getter) throws IOException {
		
		Map<String, String> row = defaultRow();
		
		String ownerValue = randomUUID().toString();
		row.put(columns.get(ownerAttribute), ownerValue);
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(owner.capture());
		assertThat(getter.apply(owner.getValue())).isEqualTo(ownerValue);
		
		String breederValue = randomUUID().toString();
		row.put(columns.get(breederAttribute), breederValue);
		
		reset(ownerSheet, registry);
		when(breederSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(owner.capture());
		assertThat(getter.apply(owner.getValue())).isEqualTo(breederValue);
	}

	private Map<String, String> defaultRow() {
		Map<String, String> row = new HashMap<>();
		row.put(columns.get(PERSONNUMMER), randomUUID().toString());
		row.put(columns.get(OWNER_EARS), randomUUID().toString());
		row.put(columns.get(BREEDER_EARS), randomUUID().toString());
		row.put(columns.get(GENDER), randomUUID().toString());
		return row;
	}
}
