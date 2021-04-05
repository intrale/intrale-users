package ar.com.intrale.cloud.messages;

import javax.validation.constraints.NotBlank;

import ar.com.intrale.cloud.Request;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class ReadUserRequest extends Request {
    @NonNull
    @NotBlank
	private String email;
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
