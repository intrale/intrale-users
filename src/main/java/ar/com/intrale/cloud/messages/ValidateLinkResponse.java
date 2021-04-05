package ar.com.intrale.cloud.messages;

import ar.com.intrale.cloud.Response;

public class ValidateLinkResponse extends Response {
	private Boolean exists;

	public Boolean getExists() {
		return exists;
	}

	public void setExists(Boolean exists) {
		this.exists = exists;
	}
	
	
}
