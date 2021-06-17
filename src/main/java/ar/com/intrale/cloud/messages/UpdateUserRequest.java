package ar.com.intrale.cloud.messages;

import java.util.ArrayList;
import java.util.Collection;

import ar.com.intrale.cloud.Request;

public class UpdateUserRequest extends Request {
	
	private String email;
	
	private String name;
	
    private String familyName;
	
	private Collection<String> groups = new ArrayList<String>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public Collection<String> getGroups() {
		return groups;
	}

	public void setGroups(Collection<String> groups) {
		this.groups = groups;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
