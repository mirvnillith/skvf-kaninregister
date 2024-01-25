package se.skvf.kaninregister.model;

import static java.lang.Boolean.parseBoolean;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static se.skvf.kaninregister.data.Table.ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import se.skvf.kaninregister.data.Table;

public class Owner extends Entity<Owner> {

	private static final PasswordEncryptor ENCRYPTOR = new StrongPasswordEncryptor();
	
	static final List<String> COLUMNS = asList(
			"Namn", 
			"Offentlig Ägare",
			"Uppfödarnamn",
			"Offentlig Uppfödare", 
			"Användarnamn",
			"Lösenord",
			"E-post",
			"Signatur",
			"Adress",
			"Telefon",
			"Uppfödarepost",
			"Uppfödartelefon",
			"Uppfödaradress");

	private String name;
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
	private String breederPhone;
	private String breederAddress;
	
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
	
	public String getName() {
		return name;
	}
	
	public Owner setName(String name) {
		this.name = name;
		if (this.name != null) {
			this.name = this.name.trim();
		}
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
	
	public Owner setBreederPhone(String breederPhone) {
		this.breederPhone = breederPhone;
		return this;
	}
	
	public String getBreederPhone() {
		return breederPhone;
	}
	
	public Owner setBreederAddress(String breederAddress) {
		this.breederAddress = breederAddress;
		return this;
	}
	
	public String getBreederAddress() {
		return breederAddress;
	}
	
	@Override
	protected void toMap(Map<String, String> map) {
		if (name == null) {
			throw new IllegalStateException("Owner must have a name");
		}
		
		List<String> values = new ArrayList<String>(COLUMNS.size());
		values.add(name);
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
		values.add(breederPhone);
		values.add(breederAddress);
		
		addToMap(map, COLUMNS, values);
	}

	public static Owner from(Map<String, String> map) {
		return new Owner().fromMap(map);
	}
	
	protected Owner fromMap(Map<String, String> map) {
		super.fromMap(map);
		
		List<BiConsumer<Owner, String>> setters = asList(
				Owner::setName,
				(o,v) -> o.setPublicOwner(booleanFromString(v)),
				Owner::setBreederName,
				(o,v) -> o.setPublicBreeder(booleanFromString(v)),
				Owner::setUserName,
				(o,v) -> o.password = v,
				Owner::setEmail,
				Owner::setSignature,
				Owner::setAddress,
				Owner::setPhone,
				Owner::setBreederEmail,
				Owner::setBreederPhone,
				Owner::setBreederAddress
				);

		return setFromMap(map, COLUMNS, setters);
	}

	@Override
	public String toString() {
		return super.toString() + ": " + name;
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
	
	public Owner setPublicOwner(String publicOwner) {
		return setPublicOwner(parseBoolean(publicOwner));
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
	
	public static Map<String, Predicate<String>> otherByUserName(String originalId, String userName) {
		Map<String, Predicate<String>> filter = new HashMap<>();
		filter.put(ID, id -> !id.equals(originalId));
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
		name = "Okänd";
		publicOwner = false;
		breederName = null;
		breederEmail = null;
		breederPhone = null;
		breederAddress = null;
		publicBreeder = false;
		email = null;
		signature = null;
		address = null;
		phone = null;
		return this;
	}

	public static Owner newOwner() {
		return new Owner()
				.setName("Ny");
	}
}
