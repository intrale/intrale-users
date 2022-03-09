package ar.com.intrale.messages;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import ar.com.intrale.RequestRoot;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class PasswordRecoveryRequest extends RequestRoot{

    @NonNull
    @NotBlank
    @Email
	private String email;
    
    @NonNull
    @NotBlank
    @Email
	public String getEmail() {
		return email;
	}

	public void setEmail(@NonNull String email) {
		this.email = email;
	}

}
