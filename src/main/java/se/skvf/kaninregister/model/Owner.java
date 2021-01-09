package se.skvf.kaninregister.model;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.Map;

public class Owner extends Entity {

	static final Collection<String> COLUMNS = asList("Förnamn", "Efternamn");

	private String firstName;
	private String lastName;
	
	@Override
	public Owner setId(String id) {
		return (Owner) super.setId(id);
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public Owner setFirstName(String firstName) {
		this.firstName = firstName;
		return this;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public Owner setLastName(String lastName) {
		this.lastName = lastName;
		return this;
	}
	
	@Override
	protected void toMap(Map<String, String> map) {
		map.put("Förnamn", firstName);
		map.put("Efternamn", lastName);
	}
}
