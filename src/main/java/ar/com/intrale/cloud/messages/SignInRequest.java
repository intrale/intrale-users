package ar.com.intrale.cloud.messages;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import ar.com.intrale.cloud.Request;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class SignInRequest extends Request {

    @NonNull
    @NotBlank
    @Email
	private String email;
	
	private String name;
	
    private String familyName;
   
    @NonNull
    @NotBlank
	private String password;
	private String newPassword;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	@NonNull
	@NotBlank
	public String getPassword() {
		return password;
	}

	public void setPassword(@NonNull String password) {
		this.password = password;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
