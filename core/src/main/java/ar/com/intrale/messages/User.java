package ar.com.intrale.messages;

import java.util.ArrayList;
import java.util.Collection;

public class User {
	
	private String email;
	private String name;
	private String familyName;
	private String status;
	private Collection<Group> groups = new ArrayList<Group>();
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public void addGroup(String name, String description) {
		Group group = new Group();
		group.setName(name);
		group.setDescription(description);
		groups.add(group);
	}
	
	public Collection<Group> getGroups() {
		return groups;
	}
	public void setGroups(Collection<Group> groups) {
		this.groups = groups;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
