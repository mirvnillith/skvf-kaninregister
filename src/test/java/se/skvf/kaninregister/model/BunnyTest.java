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
	public void name() throws Exception {
		assertAttribute("Namn", Bunny::setName, Bunny::getName);
	}
	
	@Test
	public void breeder() throws Exception {
		assertAttribute("Uppfödare", Bunny::setBreeder, Bunny::getBreeder);
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
	@MethodSource("orScenarios")
	public void orPredicate(Predicate<String> predicate, String firstValue, String secondValue, boolean match) {
		
		predicate = new Bunny.OrPredicate(predicate);
		
		assertThat(predicate.test(firstValue)).isTrue();
		if (match) {
			assertThat(predicate).accepts(secondValue);
		} else {
			assertThat(predicate).rejects(secondValue);
		}
	}
	
	public static Stream<Arguments> orScenarios() {
		
		String value = randomUUID().toString();
		
		return Stream.of(
				Arguments.of((Predicate<String>)value::equals, value, value, true),
				Arguments.of((Predicate<String>)value::equals, value, null, true),
				Arguments.of((Predicate<String>)value::equals, null, value, true),
				Arguments.of((Predicate<String>)value::equals, null, null, false)
				);
	}
	
}
