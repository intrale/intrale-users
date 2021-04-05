package ar.com.intrale.cloud;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("app.signup.temporaryPassword")
public class TemporaryPasswordConfig {


	public Integer length;
	public Boolean returned;
	
}
