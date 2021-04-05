package ar.com.intrale.cloud.messages;

import java.util.ArrayList;
import java.util.Collection;

import ar.com.intrale.cloud.Response;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class ReadUserResponse extends Response {
	
	private Collection<User> users = new ArrayList<User>();

	public Collection<User> getUsers() {
		return users;
	}

	public void setUsers(Collection<User> users) {
		this.users = users;
	}
	
	public void addUser(User user) {
		users.add(user);
	}

/*		private String username;
		private String name;
		private String familyName;
		private String status;
		private String email;
		private Collection<Group> groups = new ArrayList<Group>();
		
		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
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
*/
}


