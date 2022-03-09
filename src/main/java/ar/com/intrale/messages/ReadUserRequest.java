package ar.com.intrale.messages;

import ar.com.intrale.RequestRoot;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class ReadUserRequest extends RequestRoot {

	private String email;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
