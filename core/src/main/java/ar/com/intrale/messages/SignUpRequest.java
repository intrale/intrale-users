package ar.com.intrale.messages;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class SignUpRequest extends RequestRoot{

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
