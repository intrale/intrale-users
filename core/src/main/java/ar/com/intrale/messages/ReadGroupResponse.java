package ar.com.intrale.messages;

import java.util.ArrayList;
import java.util.Collection;

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
