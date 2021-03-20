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

public class Owner extends Entity<Owner> {

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
			"Signatur",
			"Adress",
			"Telefon",
			"Uppfödarepost");

	private String firstName;
	private String lastName;
	private boolean publicOwner;
	private String breederName;
	private boolean publicBreeder;
	private String userName;
	private String password;
	private String email;
	private String signature;
	private String address;
	private String phone;
	private String breederEmail;
	
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
	
	public String getAddress() {
		return address;
	}
	
	public Owner setAddress(String address) {
		this.address = address;
		return this;
	}
	
	public String getPhone() {
		return phone;
	}
	
	public Owner setPhone(String phone) {
		this.phone = phone;
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
	
	public Owner setBreederEmail(String breederEmail) {
		this.breederEmail = breederEmail;
		return this;
	}
	
	public String getBreederEmail() {
		return breederEmail;
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
		values.add(address);
		values.add(phone);
		values.add(breederEmail);
		
		addToMap(map, COLUMNS, values);
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
				Owner::setSignature,
				Owner::setAddress,
				Owner::setPhone,
				Owner::setBreederEmail
				);

		return setFromMap(map, COLUMNS, setters);
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
		address = null;
		phone = null;
		return this;
	}

	public static Owner newOwner() {
		return new Owner()
				.setFirstName("Ny")
				.setLastName("Ägare");
	}
}
