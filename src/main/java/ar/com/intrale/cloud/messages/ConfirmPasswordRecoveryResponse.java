package ar.com.intrale.cloud.messages;

import javax.validation.constraints.NotBlank;

import ar.com.intrale.cloud.Response;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class ConfirmPasswordRecoveryResponse extends Response{

    @NonNull
    @NotBlank
	private String username;

	@NonNull
	public String getUsername() {
		return username;
	}
    
	public void setUsername(@NonNull String username) {
		this.username = username;
	}
	
}
