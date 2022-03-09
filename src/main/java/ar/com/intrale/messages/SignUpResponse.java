package ar.com.intrale.messages;

import ar.com.intrale.Response;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class SignUpResponse extends Response{

	private String businessName;
	private String email;
	private String temporaryPassword;

	
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
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
