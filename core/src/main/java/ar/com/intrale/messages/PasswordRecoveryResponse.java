package ar.com.intrale.messages;

import javax.validation.constraints.NotBlank;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class PasswordRecoveryResponse extends Response{

    @NonNull
    @NotBlank
	private String email;

    @NonNull
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}


	
}
