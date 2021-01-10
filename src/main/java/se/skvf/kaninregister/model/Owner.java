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
	
	public static Owner from(Map<String, String> map) {
		return new Owner().fromMap(map);
	}
	
	protected Owner fromMap(Map<String, String> map) {
		super.fromMap(map);
		firstName = map.get("Förnamn");
		lastName = map.get("Efternamn");
		return this;
	}
	
	@Override
	public String toString() {
		return super.toString()+": "+firstName+" "+lastName;
	}
}
