package ar.com.intrale.cloud.messages;

import javax.validation.constraints.NotBlank;

import ar.com.intrale.cloud.Response;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class DeleteLinkResponse extends Response{

    @NonNull
    @NotBlank
	private String username;
    
    private Integer linksCounts;
	
    public Integer getLinksCounts() {
		return linksCounts;
	}

	public void setLinksCounts(Integer linksCounts) {
		this.linksCounts = linksCounts;
	}

	@NonNull
	public String getUsername() {
		return username;
	}
    
	public void setUsername(@NonNull String username) {
		this.username = username;
	}
	
}
