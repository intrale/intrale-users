package ar.com.intrale.messages;

import java.util.ArrayList;
import java.util.Collection;

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

}


