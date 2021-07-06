package ar.com.intrale.cloud.messages;

import java.util.ArrayList;
import java.util.Collection;

import ar.com.intrale.cloud.Response;

public class ReadGroupResponse extends Response {

	private Collection<Group> groups = new ArrayList<Group>();

	public Collection<Group> getGroups() {
		return groups;
	}

	public void setGroups(Collection<Group> groups) {
		this.groups = groups;
	}
	
	public void addGroup(Group group) {
		groups.add(group);
	}
	
}