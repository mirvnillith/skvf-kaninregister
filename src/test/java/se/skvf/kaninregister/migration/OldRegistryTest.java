package se.skvf.kaninregister.migration;

import static java.util.Collections.singleton;
import static java.util.UUID.randomUUID;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.skvf.kaninregister.migration.OldRegistry.BIRTHDATE;
import static se.skvf.kaninregister.migration.OldRegistry.BREEDERS_SHEET;
import static se.skvf.kaninregister.migration.OldRegistry.BUNNY_NAME;
import static se.skvf.kaninregister.migration.OldRegistry.CHIP;
import static se.skvf.kaninregister.migration.OldRegistry.COAT;
import static se.skvf.kaninregister.migration.OldRegistry.COLOUR_MARKINGS;
import static se.skvf.kaninregister.migration.OldRegistry.EARS;
import static se.skvf.kaninregister.migration.OldRegistry.EMAIL;
import static se.skvf.kaninregister.migration.OldRegistry.FEATURES;
import static se.skvf.kaninregister.migration.OldRegistry.GENDER;
import static se.skvf.kaninregister.migration.OldRegistry.OWNERS_SHEET;
import static se.skvf.kaninregister.migration.OldRegistry.OWNER_NAME;
import static se.skvf.kaninregister.migration.OldRegistry.PERSONNUMMER;
import static se.skvf.kaninregister.migration.OldRegistry.PHONE;
import static se.skvf.kaninregister.migration.OldRegistry.POSTAL_ADDRESS;
import static se.skvf.kaninregister.migration.OldRegistry.PUBLIC;
import static se.skvf.kaninregister.migration.OldRegistry.RACE;
import static se.skvf.kaninregister.migration.OldRegistry.RING;
import static se.skvf.kaninregister.migration.OldRegistry.STREET_ADDRESS;

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
		old.setOldRegistry("Old Registry");
		when(drive.getSpreadsheet("Old Registry")).thenReturn(spreadsheet);
		when(spreadsheet.getSheet(OWNERS_SHEET)).thenReturn(ownerSheet);
		when(spreadsheet.getSheet(BREEDERS_SHEET)).thenReturn(breederSheet);
		
		columns = new HashMap<>();
		columns.put(PERSONNUMMER, randomUUID().toString());
		columns.put(BIRTHDATE, randomUUID().toString());
		columns.put(BUNNY_NAME, randomUUID().toString());
		columns.put(CHIP, randomUUID().toString());
		columns.put(COAT, randomUUID().toString());
		columns.put(COLOUR_MARKINGS, randomUUID().toString());
		columns.put(EARS, randomUUID().toString());
		columns.put(EMAIL, randomUUID().toString());
		columns.put(FEATURES, randomUUID().toString());
		columns.put(GENDER, randomUUID().toString());
		columns.put(OWNER_NAME, randomUUID().toString());
		columns.put(PHONE, randomUUID().toString());
		columns.put(POSTAL_ADDRESS, randomUUID().toString());
		columns.put(PUBLIC, randomUUID().toString());
		columns.put(RACE, randomUUID().toString());
		columns.put(RING, randomUUID().toString());
		columns.put(STREET_ADDRESS, randomUUID().toString());
		
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
		
		assertBunny(COAT, Bunny::getCoat);
	}
	
	@Test
	public void bunnyColourMarkings() throws IOException {
		
		assertBunny(COLOUR_MARKINGS, Bunny::getColourMarkings);
	}
	
	@Test
	public void bunnyFeatures() throws IOException {
		
		assertBunny(FEATURES, Bunny::getFeatures);
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
		
		assertBunny(RING, Bunny::getRing);
	}
	
	@Test
	public void ownerName() throws IOException {
		
		assertOwner(OWNER_NAME, Owner::getName);
	}
	
	@Test
	public void ownerEmail() throws IOException {
		
		assertOwner(EMAIL, Owner::getEmail);
	}
	
	@Test
	public void ownerPhone() throws IOException {
		
		assertOwner(PHONE, Owner::getPhone);
	}
	
	@ParameterizedTest
	@MethodSource("addresses")
	public void ownerAddress(String streetAddress, String postalAddress, String address) throws IOException {
		
		Map<String, String> row = defaultRow();
		row.put(columns.get(STREET_ADDRESS), streetAddress);
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
		row.put(columns.get(PUBLIC), value);
		
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
		row.put(columns.get(EARS), ears);
		
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
		
		assertConflict(OWNER_NAME);
	}
	
	@Test
	public void streetAddressConflict() throws IOException {
		
		assertConflict(STREET_ADDRESS);
	}
	
	@Test
	public void postalAddressConflict() throws IOException {
		
		assertConflict(POSTAL_ADDRESS);
	}
	
	@Test
	public void phoneConflict() throws IOException {
		
		assertConflict(PHONE);
	}
	
	@Test
	public void emailConflict() throws IOException {
		
		assertConflict(EMAIL);
	}
	
	@Test
	public void publicConflict() throws IOException {
		
		Map<String, String> owner1 = defaultRow();
		owner1.put(columns.get(PUBLIC), "Ja");
		Map<String, String> owner2 = defaultRow();
		owner2.put(columns.get(PERSONNUMMER), owner1.get(columns.get(PERSONNUMMER)));
		owner2.put(columns.get(PUBLIC), "Nej");
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(owner1));
		when(breederSheet.findRows(anyMap())).thenReturn(singleton(owner2));
		
		assertThrows(IllegalStateException.class, () -> old.setup());
	}

	private void assertConflict(String attribute) throws IOException {
		Map<String, String> owner1 = defaultRow();
		owner1.put(columns.get(attribute), randomUUID().toString());
		Map<String, String> owner2 = defaultRow();
		owner2.put(columns.get(PERSONNUMMER), owner1.get(columns.get(PERSONNUMMER)));
		owner2.put(columns.get(attribute), randomUUID().toString());
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(owner1));
		when(breederSheet.findRows(anyMap())).thenReturn(singleton(owner2));
		
		assertThrows(IllegalStateException.class, () -> old.setup());
	}
	
	private void assertBunny(String attribute, Function<Bunny, String> getter) throws IOException {
		
		Map<String, String> row = defaultRow();
		
		String value = randomUUID().toString();
		row.put(columns.get(attribute), value);
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(bunny.capture());
		assertThat(getter.apply(bunny.getValue())).isEqualTo(value);
		
		reset(ownerSheet, registry);
		when(breederSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(bunny.capture());
		assertThat(getter.apply(bunny.getValue())).isEqualTo(value);
	}
	
	private void assertOwner(String attribute, Function<Owner, String> getter) throws IOException {
		
		Map<String, String> row = defaultRow();
		
		String value = randomUUID().toString();
		row.put(columns.get(attribute), value);
		
		when(ownerSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(owner.capture());
		assertThat(getter.apply(owner.getValue())).isEqualTo(value);
		
		reset(ownerSheet, registry);
		when(breederSheet.findRows(anyMap())).thenReturn(singleton(row));
		
		old.setup();
		
		verify(registry).add(owner.capture());
		assertThat(getter.apply(owner.getValue())).isEqualTo(value);
	}

	private Map<String, String> defaultRow() {
		Map<String, String> row = new HashMap<>();
		row.put(columns.get(PERSONNUMMER), randomUUID().toString());
		row.put(columns.get(EARS), randomUUID().toString());
		row.put(columns.get(GENDER), randomUUID().toString());
		return row;
	}
}
