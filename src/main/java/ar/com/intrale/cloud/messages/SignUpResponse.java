package ar.com.intrale.cloud.messages;

import ar.com.intrale.cloud.Response;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class SignUpResponse extends Response{

	private String email;
	private String temporaryPassword;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getTemporaryPassword() {
		return temporaryPassword;
	}
	public void setTemporaryPassword(String temporaryPassword) {
		this.temporaryPassword = temporaryPassword;
	}
	
	
}
