package se.skvf.kaninregister.model;

import static java.util.UUID.randomUUID;

import org.junit.jupiter.api.Test;

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
}
