package ar.com.intrale.cloud.messages;

import ar.com.intrale.cloud.Request;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class ReadUserRequest extends Request {

	private String email;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
