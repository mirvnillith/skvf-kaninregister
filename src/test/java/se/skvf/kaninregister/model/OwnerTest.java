package se.skvf.kaninregister.model;

import static java.util.UUID.randomUUID;

import org.junit.jupiter.api.Test;

public class OwnerTest extends EntityTest<Owner> {

	public OwnerTest() {
		super(Owner.class, Owner::from);
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
}
