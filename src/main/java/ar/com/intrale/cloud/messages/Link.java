package ar.com.intrale.cloud.messages;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Link {

	public static final String SEPARATOR = "-";

	private String businessName;
	
	private String email;

	public String getCompleteName() {
		return businessName + SEPARATOR + email;
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
	
	
	
}
