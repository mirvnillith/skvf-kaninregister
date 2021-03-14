package se.skvf.kaninregister.model;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class Owner extends Entity {

	private static final PasswordEncryptor ENCRYPTOR = new StrongPasswordEncryptor();
	
	static final List<String> COLUMNS = asList(
			"Förnamn", 
			"Efternamn", 
			"Offentlig Ägare",
			"Uppfödarnamn",
			"Offentlig Uppfödare", 
			"Användarnamn",
			"Lösenord",
			"E-post",
			"Signatur");

	private String firstName;
	private String lastName;
	private boolean publicOwner;
	private String breederName;
	private boolean publicBreeder;
	private String userName;
	private String password;
	private String email;
	private String signature;
	
	@Override
	public Owner setId(String id) {
		return (Owner) super.setId(id);
	}
	
	public String getSignature() {
		return signature;
	}
	
	public Owner setSignature(String signature) {
		this.signature = signature;
		return this;
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
		
		List<String> values = new ArrayList<String>(COLUMNS.size());
		values.add(firstName);
		values.add(lastName);
		values.add(toString(publicOwner));
		values.add(breederName);
		values.add(toString(publicBreeder));
		values.add(userName);
		values.add(password);
		values.add(email);
		values.add(signature);
		
		if (values.size() != COLUMNS.size()) {
			throw new IllegalStateException("Values do not match columns: "+values+" vs "+COLUMNS);			
		}
		for (int i=0; i<COLUMNS.size(); i++) {
			map.put(COLUMNS.get(i), values.get(i));
		}
	}
	
	public static Owner from(Map<String, String> map) {
		return new Owner().fromMap(map);
	}
	
	protected Owner fromMap(Map<String, String> map) {
		super.fromMap(map);
		
		List<BiConsumer<Owner, String>> setters = asList(
				Owner::setFirstName,
				Owner::setLastName,
				(o,v) -> o.setPublicOwner(booleanFromString(v)),
				Owner::setBreederName,
				(o,v) -> o.setPublicBreeder(booleanFromString(v)),
				Owner::setUserName,
				(o,v) -> o.password = v,
				Owner::setEmail,
				Owner::setSignature
				);

		if (setters.size() != COLUMNS.size()) {
			throw new IllegalStateException("Values do not match columns: "+COLUMNS);			
		}
		for (int i=0; i<COLUMNS.size(); i++) {
			setters.get(i).accept(this, map.get(COLUMNS.get(i)));
		}
		
		return this;
	}
	
	@Override
	public String toString() {
		return super.toString() + ": " + firstName + " " + lastName;
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
		return isNotEmpty(password);
	}
	
	public boolean isApproved() {
		return isNotEmpty(signature);
	}

	public Owner deactivate() {
		unapprove();
		userName = null;
		password = null;
		return this;
	}

	public Owner unapprove() {
		firstName = "Okänd";
		lastName = "Ägare";
		publicOwner = false;
		breederName = null;
		publicBreeder = false;
		email = null;
		signature = null;
		return this;
	}

	public static Owner newOwner() {
		return new Owner()
				.setFirstName("Ny")
				.setLastName("Ägare");
	}
}
