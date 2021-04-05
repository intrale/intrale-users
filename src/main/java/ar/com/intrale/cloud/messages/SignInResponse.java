package ar.com.intrale.cloud.messages;

import javax.validation.constraints.NotBlank;

import ar.com.intrale.cloud.Response;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class SignInResponse extends Response {
    @NonNull
    @NotBlank
	private String idToken;
    
    @NonNull
    @NotBlank
	private String accessToken;
    
    @NonNull
    @NotBlank
	private String refreshToken;

    @NonNull
    public String getIdToken() {
		return idToken;
	}

	public void setIdToken(@NonNull String idToken) {
		this.idToken = idToken;
	}

	@NonNull
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(@NonNull String accessToken) {
		this.accessToken = accessToken;
	}

	@NonNull
	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(@NonNull String refreshToken) {
		this.refreshToken = refreshToken;
	}

}
