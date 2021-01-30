package se.skvf.kaninregister.model;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class OwnerTest extends EntityTest<Owner> {

	public OwnerTest() {
		super(Owner::from);
	}
	
	@Override
	protected Owner create() {
		return new Owner()
				.setFirstName(randomUUID().toString())
				.setLastName(randomUUID().toString());
	}
	
	@Test
	public void firstName() throws Exception {
		assertAttribute("Förnamn", Owner::setFirstName, Owner::getFirstName);
	}
	
	@Test
	public void lastName() throws Exception {
		assertAttribute("Efternamn", Owner::setLastName, Owner::getLastName);
	}
	
	@Test
	public void email() throws Exception {
		assertAttribute("E-post", Owner::setEmail, Owner::getEmail);
	}
	
	@Test
	public void password() {
		
		String password = randomUUID().toString();
		Owner owner = create()
				.setPassword(password);
		
		assertThat(owner.validate(password)).isTrue();
		assertThat(owner.validate(null)).isFalse();
		
		Map<String, String> map = owner.toMap();
		assertThat(map)
			.containsKey("Lösenord")
			.doesNotContainEntry("Lösenord", password);
		assertThat(owner.validate(map.get("Lösenord"))).isFalse();
		
		owner = Owner.from(map);
		assertThat(owner.validate(password)).isTrue();
	}
	
	@Test
	public void publicBreeder() throws Exception {
		assertBooleanAttribute("Offentlig Uppfödare", Owner::setPublicBreeder, Owner::isPublicBreeder);
	}
	
	@Test
	public void publicOwner() throws Exception {
		assertBooleanAttribute("Offentlig Ägare", Owner::setPublicOwner, Owner::isPublicOwner);
	}
	
	@Test
	public void testToString() {
		Owner owner = new Owner()
				.setId(randomUUID().toString())
				.setFirstName(randomUUID().toString())
				.setLastName(randomUUID().toString());
		assertToString(owner, ": "+owner.getFirstName()+" "+owner.getLastName());
	}
	
	@Test
	public void mandatoryFirstName() {
		assertMandatoryAttribute("Förnamn");
	}
	
	@Test
	public void mandatoryLastName() {
		assertMandatoryAttribute("Efternamn");
	}
}
