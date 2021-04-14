package ar.com.intrale.cloud.messages;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import ar.com.intrale.cloud.Request;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class PasswordRecoveryRequest extends Request{

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
