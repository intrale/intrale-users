package ar.com.intrale;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;

import ar.com.intrale.IntraleFactory;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Factory
@Requires(property = IntraleFactory.FACTORY, value = IntraleFactory.TRUE, defaultValue = IntraleFactory.TRUE)
public class UsersFactory extends IntraleFactory<AWSCognitoIdentityProvider>{

	private static final Logger LOGGER = LoggerFactory.getLogger(UsersFactory.class);

	@Bean @Requires(property = IntraleFactory.PROVIDER, value = IntraleFactory.TRUE, defaultValue = IntraleFactory.TRUE)
	@Override
	public AWSCognitoIdentityProvider provider() {

			LOGGER.info("access:" + config.getCognito().getAccess());
			LOGGER.info("secret:" + config.getCognito().getSecret());
			LOGGER.info("region:" + config.getAws().getRegion());

	      BasicAWSCredentials credentials = new BasicAWSCredentials(config.getCognito().getAccess(), config.getCognito().getSecret());
	 	 
	       return AWSCognitoIdentityProviderClientBuilder.standard()
	                      .withCredentials(new AWSStaticCredentialsProvider(credentials))
	                      .withRegion(config.getAws().getRegion())
	                             .build();
	}
	
	

}
