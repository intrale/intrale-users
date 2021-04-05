package ar.com.intrale.cloud.messages;

import ar.com.intrale.cloud.Request;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class GetLinkRequest extends Request{

	private String email;
    

	public String getEmail() {
		return email;
	}

	public void setEmail(@NonNull String email) {
		this.email = email;
	}

}
