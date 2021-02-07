package se.skvf.kaninregister.model;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class Owner extends Entity {

	private static final PasswordEncryptor ENCRYPTOR = new StrongPasswordEncryptor();
	
	static final Collection<String> COLUMNS = asList(
			"Förnamn", 
			"Efternamn", 
			"Offentlig Ägare",
			"Uppfödare",
			"Uppfödarnamn",
			"Offentlig Uppfödare", 
			"Användarnamn",
			"Lösenord",
			"E-post");

	private String firstName;
	private String lastName;
	private boolean publicOwner;
	private boolean breeder;
	private String breederName;
	private boolean publicBreeder;
	private String userName;
	private String password;
	private String email;
	
	@Override
	public Owner setId(String id) {
		return (Owner) super.setId(id);
	}
	
	public String getUserName() {
		return userName;
	}
	
	public Owner setUserName(String userName) {
		this.userName = userName;
		return this;
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
	
	public Owner setEmail(String email) {
		this.email = email;
		return this;
	}
	
	public String getEmail() {
		return email;
	}
	
	@Override
	protected void toMap(Map<String, String> map) {
		if (firstName == null) {
			throw new IllegalStateException("Owner must have a first name");
		}
		if (lastName == null) {
			throw new IllegalStateException("Owner must have a last name");
		}
		map.put("Förnamn", firstName);
		map.put("Efternamn", lastName);
		map.put("Offentlig Ägare", toString(publicOwner));
		map.put("Uppfödare", toString(breeder));
		map.put("Uppfödarnamn", breederName);
		map.put("Offentlig Uppfödare", toString(publicBreeder));
		map.put("Användarnamn", userName);
		map.put("Lösenord", password);
		map.put("E-post", email);
	}
	
	public static Owner from(Map<String, String> map) {
		return new Owner().fromMap(map);
	}
	
	protected Owner fromMap(Map<String, String> map) {
		super.fromMap(map);
		firstName = map.get("Förnamn");
		lastName = map.get("Efternamn");
		publicOwner = booleanFromString(map.get("Offentlig Ägare"));
		breeder = booleanFromString(map.get("Uppfödare"));
		breederName = map.get("Uppfödarnamn");
		publicBreeder = booleanFromString(map.get("Offentlig Uppfödare"));
		userName = map.get("Användarnamn");
		password = map.get("Lösenord");
		email = map.get("E-post");
		return this;
	}
	
	@Override
	public String toString() {
		return super.toString() + ": " + firstName + " " + lastName;
	}

	public boolean isBreeder() {
		return breeder;
	}
	
	public Owner setBreeder(boolean breeder) {
		this.breeder = breeder;
		return this;
	}
	
	public String getBreederName() {
		return breederName;
	}
	
	public Owner setBreederName(String breederName) {
		this.breederName = breederName;
		return this;
	}
	
	public Owner setPublicBreeder(boolean publicBreeder) {
		this.publicBreeder = publicBreeder;
		return this;
	}
	
	public boolean isPublicBreeder() {
		return publicBreeder;
	}
	
	public boolean isNotPublicBreeder() {
		return !publicBreeder;
	}

	public Owner setPublicOwner(boolean publicOwner) {
		this.publicOwner = publicOwner;
		return this;
	}
	
	public boolean isPublicOwner() {
		return publicOwner;
	}
	
	public boolean isNotPublicOwner() {
		return !publicOwner;
	}

	public Owner setPassword(String password) {
		this.password = ENCRYPTOR.encryptPassword(password);
		return this;
	}
	
	public boolean validate(String password) {
		if (password == null) {
			return false;
		} else {
			return ENCRYPTOR.checkPassword(password, this.password);
		}
	}

	public static Map<String, Predicate<String>> byUserName(String userName) {
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put("Användarnamn", userName::equals);
		return filter;
	}

	public boolean isActivated() {
		return this.password != null;
	}
	
	public boolean isApproved() {
		return true;
	}

	public static Map<String, Predicate<String>> onlyBreeders(Boolean onlyBreeders) {
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put("Uppfödare", b -> TRUE.equals(onlyBreeders) ? booleanFromString(b) : true);
		return filter;
	}

	public Owner deactivate() {
		firstName = "Ny";
		lastName = "Ägare";
		email = null;
		userName = null;
		password = null;
		publicOwner = false;
		breeder = false;
		breederName = null;
		publicBreeder = false;
		return this;
	}
}
