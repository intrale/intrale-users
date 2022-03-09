package ar.com.intrale;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;

import ar.com.intrale.IntraleFactory;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;

@Factory
@Requires(property = IntraleFactory.FACTORY, value = IntraleFactory.TRUE, defaultValue = IntraleFactory.TRUE)
public class UsersFactory extends IntraleFactory<AWSCognitoIdentityProvider>{

	@Bean @Requires(property = IntraleFactory.PROVIDER, value = IntraleFactory.TRUE, defaultValue = IntraleFactory.TRUE)
	@Override
	public AWSCognitoIdentityProvider provider() {
	      BasicAWSCredentials credentials = new BasicAWSCredentials(config.getCognito().getAccess(), config.getCognito().getSecret());
	 	 
	       return AWSCognitoIdentityProviderClientBuilder.standard()
	                      .withCredentials(new AWSStaticCredentialsProvider(credentials))
	                      .withRegion(config.getAws().getRegion())
	                             .build();
	}
	
	

}
