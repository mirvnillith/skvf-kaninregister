package se.skvf.kaninregister.model;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class BunnyTest extends EntityTest<Bunny> {

	public BunnyTest() {
		super(Bunny::from);
	}
	
	@Override
	protected Bunny create() {
		return new Bunny()
				.setOwner(randomUUID().toString())
				.setName(randomUUID().toString());
	}
	
	@Test
	public void owner() throws Exception {
		assertAttribute("Ägare", Bunny::setOwner, Bunny::getOwner);
	}
	
	@Test
	public void previousOwner() throws Exception {
		assertAttribute("Föregående Ägare", Bunny::setPreviousOwner, Bunny::getPreviousOwner);
	}
	
	@Test
	public void name() throws Exception {
		assertAttribute("Namn", Bunny::setName, Bunny::getName);
	}
	
	@Test
	public void breeder() throws Exception {
		assertAttribute("Uppfödare", Bunny::setBreeder, Bunny::getBreeder);
	}
	
	@Test
	public void birthDate() throws Exception {
		assertAttribute("Födelsedag", Bunny::setBirthDate, Bunny::getBirthDate);
	}
	
	@Test
	public void race() throws Exception {
		assertAttribute("Ras", Bunny::setRace, Bunny::getRace);
	}
	
	@Test
	public void coat() throws Exception {
		assertAttribute("Hårlag", Bunny::setCoat, Bunny::getCoat);
	}
	
	@Test
	public void colourMarkings() throws Exception {
		assertAttribute("Färgteckning", Bunny::setColourMarkings, Bunny::getColourMarkings);
	}
	
	@Test
	public void picture() throws Exception {
		assertAttribute("Bild", Bunny::setPicture, Bunny::getPicture);
	}
	
	@Test
	public void leftEar() throws Exception {
		assertAttribute("Vänster Öra", Bunny::setLeftEar, Bunny::getLeftEar);
	}
	
	@Test
	public void rightEar() throws Exception {
		assertAttribute("Höger Öra", Bunny::setRightEar, Bunny::getRightEar);
	}
	
	@Test
	public void chip() throws Exception {
		assertAttribute("Chipnummer", Bunny::setChip, Bunny::getChip);
	}
	
	@Test
	public void ring() throws Exception {
		assertAttribute("Ringnummer", Bunny::setRing, Bunny::getRing);
	}
	
	@Test
	public void neutered() throws Exception {
		assertBooleanAttribute("Kastrerad", Bunny::setNeutered, Bunny::isNeutered);
	}
	
	@Test
	public void gender() throws Exception {
		assertEnumAttribute("Kön", Bunny::setGender, Bunny::getGender, Bunny.Gender.FEMALE, Bunny.Gender.MALE);
	}
	
	@Test
	public void testToString() {
		Bunny bunny = new Bunny()
				.setId(randomUUID().toString())
				.setOwner(randomUUID().toString())
				.setName(randomUUID().toString());
		assertToString(bunny, ": "+bunny.getName()+"@"+bunny.getOwner());
	}
	
	@Test
	public void mandatoryOwner() {
		assertMandatoryAttribute("Ägare");
	}
	
	@Test
	public void mandatoryName() {
		assertMandatoryAttribute("Namn");
	}
	
	@ParameterizedTest
	@MethodSource("wildcardScenarios")
	public void wildcards(String identifier, String wildcards, boolean match) {
		if (match) {
			assertThat(Bunny.wildcard(wildcards)).accepts(identifier);
		} else {
			assertThat(Bunny.wildcard(wildcards)).rejects(identifier);
		}
	}
	
	public static Stream<Arguments> wildcardScenarios() {
		return Stream.of(
				Arguments.of("1234567890", "1234567890", true),
				Arguments.of(null, "", false),
				Arguments.of("", "", false),
				Arguments.of("", null, false),
				Arguments.of("1234567890", "123456789", false),
				Arguments.of("1234567890", "123456789?", true),
				Arguments.of("1234567890", "?234567890", true),
				Arguments.of("1234567890", "?23456789?", true),
				Arguments.of("1234567890", "??????????", true)
				);
	}
	
	@ParameterizedTest
	@MethodSource("multipleScenarios")
	public void multiple(Predicate<String> predicate, String value, boolean match) {
		
		predicate = Bunny.multiple(predicate);
		
		if (match) {
			assertThat(predicate).accepts(value);
		} else {
			assertThat(predicate).rejects(value);
		}
	}
	
	public static Stream<Arguments> multipleScenarios() {
		
		String value = randomUUID().toString();
		
		return Stream.of(
				Arguments.of((Predicate<String>)value::equals, value, true),
				Arguments.of((Predicate<String>)value::equals, "", false),
				Arguments.of((Predicate<String>)value::equals, null, false),
				Arguments.of((Predicate<String>)value::equals, value+value, false),
				Arguments.of((Predicate<String>)value::equals, value+",value", true),
				Arguments.of((Predicate<String>)value::equals, value+","+value, true),
				Arguments.of((Predicate<String>)value::equals, value+" , "+value, true),
				Arguments.of((Predicate<String>)value::equals, value+" , value", true),
				Arguments.of((Predicate<String>)value::equals, "value , "+value, true)
				);
	}
	
}
