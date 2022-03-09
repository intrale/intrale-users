package ar.com.intrale.messages;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import ar.com.intrale.RequestRoot;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class ConfirmPasswordRecoveryRequest extends RequestRoot{

    @NonNull
    @NotBlank
    @Email
	private String email;

    @NonNull
    @NotBlank
    private String code;
    
    @NonNull
    @NotBlank
    private String password;
    
    public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

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
