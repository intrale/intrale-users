package ar.com.intrale.cloud;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

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
	
	
	@Bean @Requires(property = IntraleFactory.PROVIDER, value = IntraleFactory.TRUE, defaultValue = IntraleFactory.TRUE)
	public AmazonDynamoDB providerDB() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(config.getDatabase().getAccess(), config.getDatabase().getSecret());
    	
        AmazonDynamoDB amazonDynamoDB = AmazonDynamoDBClientBuilder.standard()
          .withCredentials(new AWSStaticCredentialsProvider(credentials))
          .withRegion(config.getAws().getRegion())
          .build();
         
        return amazonDynamoDB;
	}
}
