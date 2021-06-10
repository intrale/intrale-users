package ar.com.intrale.cloud.functions;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;

import ar.com.intrale.cloud.Error;
import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.exceptions.FunctionException;
import ar.com.intrale.cloud.exceptions.NewPasswordRequiredException;
import ar.com.intrale.cloud.exceptions.UnauthorizeExeption;
import ar.com.intrale.cloud.messages.SignInRequest;
import ar.com.intrale.cloud.messages.SignInResponse;
import ar.com.intrale.cloud.messages.ValidateLinkRequest;
import ar.com.intrale.cloud.messages.ValidateLinkResponse;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

@Singleton
@Named(SignInFunction.FUNCTION_NAME)
@Requires(property = IntraleFunction.APP_INSTANTIATE + SignInFunction.FUNCTION_NAME , value = IntraleFunction.TRUE, defaultValue = IntraleFunction.TRUE)
public class SignInFunction extends IntraleFunction<SignInRequest, SignInResponse, AWSCognitoIdentityProvider> {
	
	private static final String NOT_LINKED_WITH_BUSINESS_MESSAGE = "NOT LINKED WITH BUSINESS ";

	private static final String EMAIL_VERIFIED_ATTRIBUTE = "email_verified";

	private static final String NEW_PASSWORD_PARAM = "NEW_PASSWORD";

	private static final String PASS_WORD_PARAM = "PASS_WORD";

	private static final String PASSWORD_PARAM = "PASSWORD";

	private static final String USERNAME_PARAM = "USERNAME";

	private static final Logger LOGGER = LoggerFactory.getLogger(SignInFunction.class);
	
	public static final String FUNCTION_NAME = "signin";

	public static final String NEW_PASSWORD_REQUIRED = "NEW_PASSWORD_REQUIRED";
	
	public static final String FAMILY_NAME = "family_name";
	public static final String NAME = "name";
	
	@Inject
	private ValidateLinkFunction validateLinkFunction;
	
	@Override
	public SignInResponse execute(SignInRequest request) throws FunctionException {
		LOGGER.info("INTRALE: LOGIN INITIALIZING ");
		try {
			LOGGER.info("INTRALE: Link validation ");
			SignInResponse response = new SignInResponse();
			
			ValidateLinkRequest validateLinkRequest = new ValidateLinkRequest();
			validateLinkRequest.setHeaders(request.getHeaders());;
			validateLinkRequest.setEmail(request.getEmail());
			validateLinkRequest.setRequestId(request.getRequestId());
			
			ValidateLinkResponse validateLinkResponse = validateLinkFunction.execute(validateLinkRequest);
			LOGGER.info("INTRALE: Link validation finishing");
			
			if (validateLinkResponse.getExists()) {
			    final Map<String, String>authParams = new HashMap();
			    authParams.put(USERNAME_PARAM, request.getEmail());  
			    authParams.put(PASSWORD_PARAM, request.getPassword());
			    
			    LOGGER.info("INTRALE: Pre Autenticacion ");
			 
			   final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
			       authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
			       .withClientId(config.getCognito().getClientId())
			       .withUserPoolId(config.getCognito().getUserPoolId())
			       .withAuthParameters(authParams);
			       
			   AdminInitiateAuthResult result = provider.adminInitiateAuth(authRequest);
			   AuthenticationResultType authenticationResult = result.getAuthenticationResult();
			   
			   LOGGER.info("INTRALE: Post Autenticacion ");
			   
			   if(NEW_PASSWORD_REQUIRED.equals(result.getChallengeName())){
				   LOGGER.info("INTRALE: NEW PASSWORD REQUIRED ");
				   // La autenticacion solicita una nueva password
				   if (StringUtils.isEmpty(request.getNewPassword())) {
					   LOGGER.info("INTRALE: NEW PASSWORD IS EMPTY ");
					   throw new NewPasswordRequiredException(new Error(NEW_PASSWORD_REQUIRED, NEW_PASSWORD_REQUIRED), mapper);
				   } else {
					   LOGGER.info("INTRALE: NEW PASSWORD DETECTED "); 
					   final Map<String, String> challengeResponses = new HashMap();
				       challengeResponses.put(USERNAME_PARAM, request.getEmail());
				       challengeResponses.put(PASS_WORD_PARAM, request.getPassword());
				       //add the new password to the params map
				       challengeResponses.put(NEW_PASSWORD_PARAM, request.getNewPassword());
				       //populate the challenge response
				        final AdminRespondToAuthChallengeRequest requestChallenge = new AdminRespondToAuthChallengeRequest();
				        requestChallenge.withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
				          .withChallengeResponses(challengeResponses)
				          .withClientId(config.getCognito().getClientId())
					      .withUserPoolId(config.getCognito().getUserPoolId())
				          .withSession(result.getSession());
				 
				      AdminRespondToAuthChallengeResult responseChallenge = provider.adminRespondToAuthChallenge(requestChallenge);
				      authenticationResult = responseChallenge.getAuthenticationResult();
				      
				      AdminUpdateUserAttributesRequest adminUpdateUserAttributesRequest = new AdminUpdateUserAttributesRequest()
				        .withUserPoolId(config.getCognito().getUserPoolId())
						.withUsername(request.getEmail());
				      
				      if (!StringUtils.isEmpty(request.getName())) {
				    	  adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(NAME).withValue(request.getName()));
			      	  }
				      if (!StringUtils.isEmpty(request.getFamilyName())) {
				    	  adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(FAMILY_NAME).withValue(request.getFamilyName()));
				      }
				      
				      adminUpdateUserAttributesRequest.withUserAttributes(new AttributeType().withName(EMAIL_VERIFIED_ATTRIBUTE).withValue(TRUE));
				      
				      provider.adminUpdateUserAttributes(adminUpdateUserAttributesRequest);
				      
				   }
			   }
			
			   LOGGER.info("INTRALE: LOGIN FINALIZING ");
			   
			   response.setIdToken(authenticationResult.getIdToken());
			   response.setAccessToken(authenticationResult.getAccessToken());
			   response.setRefreshToken(authenticationResult.getRefreshToken());
			} else {
				throw new UnauthorizeExeption(new Error(UNAUTHORIZED, NOT_LINKED_WITH_BUSINESS_MESSAGE + request.getHeaders().get(Lambda.HEADER_BUSINESS_NAME)), mapper);
			}
		   LOGGER.info("finalizando handler");
	       return response;
	   } catch (NotAuthorizedException e) {
			throw new UnauthorizeExeption(new Error(UNAUTHORIZED, UNAUTHORIZED), mapper);
	   }
	}

	
}
