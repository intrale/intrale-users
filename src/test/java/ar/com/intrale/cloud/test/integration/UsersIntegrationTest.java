package ar.com.intrale.cloud.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ar.com.intrale.cloud.CredentialsGenerator;
import ar.com.intrale.cloud.Function;
import ar.com.intrale.cloud.IntraleFactory;
import ar.com.intrale.cloud.TemporaryPasswordConfig;
import ar.com.intrale.cloud.functions.DeleteFunction;
import ar.com.intrale.cloud.functions.PasswordRecoveryFunction;
import ar.com.intrale.cloud.functions.SignInFunction;
import ar.com.intrale.cloud.functions.SignUpFunction;
import ar.com.intrale.cloud.messages.DeleteRequest;
import ar.com.intrale.cloud.messages.DeleteResponse;
import ar.com.intrale.cloud.messages.PasswordRecoveryRequest;
import ar.com.intrale.cloud.messages.PasswordRecoveryResponse;
import ar.com.intrale.cloud.messages.ReadUserRequest;
import ar.com.intrale.cloud.messages.ReadUserResponse;
import ar.com.intrale.cloud.messages.SignInRequest;
import ar.com.intrale.cloud.messages.SignInResponse;
import ar.com.intrale.cloud.messages.SignUpRequest;
import ar.com.intrale.cloud.messages.SignUpResponse;
import ar.com.intrale.cloud.messages.User;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpStatus;
import io.micronaut.test.annotation.MicronautTest;


@MicronautTest(rebuildContext = true)
@Property(name = IntraleFactory.FACTORY, value = "true")
@Property(name = IntraleFactory.PROVIDER, value = "true")
public class UsersIntegrationTest extends ar.com.intrale.cloud.Test{
	
	@Inject
	private TemporaryPasswordConfig temporaryPasswordConfig;
	
	@Inject
	private CredentialsGenerator credentialGenerator;
	
	@Override
    public void beforeEach() {
    }
    
	@Override
	public void afterEach() {
	}
    
    @Test
    public void test() throws Exception {

    	APIGatewayProxyResponseEvent responseEvent = null;

		deleteUser(); 
    	
    	// Registramos un email
    	SignUpRequest signUpRequest = new SignUpRequest();
    	signUpRequest.setBusinessName(DUMMY_VALUE);
    	signUpRequest.setRequestId(DUMMY_VALUE);
    	signUpRequest.setEmail(DUMMY_EMAIL);

    	responseEvent = lambda.execute(makeRequestEvent(signUpRequest, SignUpFunction.FUNCTION_NAME));
        SignUpResponse signupResponse  = mapper.readValue(responseEvent.getBody(), SignUpResponse.class);
    	
        assertEquals(DUMMY_EMAIL.toLowerCase(), signupResponse.getEmail().toLowerCase());
        
        // Hacemos el primer Signin con el email registrado
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setRequestId(DUMMY_VALUE);
        signInRequest.setBusinessName(DUMMY_VALUE);
        signInRequest.setEmail(DUMMY_EMAIL);
        signInRequest.setPassword(signupResponse.getTemporaryPassword());
        signInRequest.setFamilyName(DUMMY_VALUE);
        signInRequest.setName(DUMMY_VALUE);

        // signin que luego requerira cambio de password
    	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
    	assertEquals(HttpStatus.UPGRADE_REQUIRED.getCode(), responseEvent.getStatusCode());
    	
    	// Cambio de password durante el signin
    	String newPassword =  credentialGenerator.generate(temporaryPasswordConfig.length);
    	signInRequest.setNewPassword(newPassword);
    	
    	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
        SignInResponse signinResponse  = mapper.readValue(responseEvent.getBody(), SignInResponse.class);
        
        assertNotNull(signinResponse.getAccessToken());
        assertNotNull(signinResponse.getIdToken());
        assertNotNull(signinResponse.getRefreshToken());
        
        signInRequest.setPassword(newPassword);
        
    	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
        signinResponse  = mapper.readValue(responseEvent.getBody(), SignInResponse.class);
        
        assertNotNull(signinResponse.getAccessToken());
        assertNotNull(signinResponse.getIdToken());
        assertNotNull(signinResponse.getRefreshToken());
        
        signInRequest.setPassword(DUMMY_VALUE);

       	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
        assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());
        
        // se trata de hacer signin con otro negocio
        signInRequest.setPassword(newPassword);
        signInRequest.setBusinessName(DUMMY_VALUE + "_OTHER2");
    	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));

    	assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());
    	
        signUpRequest.setBusinessName(DUMMY_VALUE + "_OTHER");

    	responseEvent = lambda.execute(makeRequestEvent(signUpRequest, SignUpFunction.FUNCTION_NAME));
        signupResponse  = mapper.readValue(responseEvent.getBody(), SignUpResponse.class);
    	
        assertEquals(DUMMY_EMAIL.toLowerCase(), signupResponse.getEmail().toLowerCase());
    	
    	ReadUserRequest readUserRequest = new ReadUserRequest();
    	readUserRequest.setRequestId(DUMMY_VALUE);
    	readUserRequest.setBusinessName(DUMMY_VALUE);
    	readUserRequest.setEmail(signInRequest.getEmail());
    	
    	responseEvent = lambda.execute(makeRequestEvent(readUserRequest, Function.READ));
    	ReadUserResponse readUserResponse = mapper.readValue(responseEvent.getBody(), ReadUserResponse.class);   
    
    	assertEquals(1, readUserResponse.getUsers().size());
    	
    	User user = readUserResponse.getUsers().iterator().next();
    	assertEquals(DUMMY_VALUE, user.getName());
    	assertEquals(DUMMY_VALUE, user.getFamilyName());
    	
    	PasswordRecoveryRequest passwordRecoveryRequest = new PasswordRecoveryRequest();
    	passwordRecoveryRequest.setRequestId("001");
    	passwordRecoveryRequest.setBusinessName(DUMMY_VALUE);
    	passwordRecoveryRequest.setEmail(DUMMY_EMAIL);
    	
    	responseEvent = lambda.execute(makeRequestEvent(passwordRecoveryRequest, PasswordRecoveryFunction.FUNCTION_NAME));
    	PasswordRecoveryResponse passwordRecoveryResponse = mapper.readValue(responseEvent.getBody(), PasswordRecoveryResponse.class); 
    	
    	deleteUser();   
        
        signInRequest.setBusinessName(DUMMY_VALUE);
    	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
    	
    	assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());

    }

	private void deleteUser() throws Exception, JsonProcessingException, JsonMappingException {
		APIGatewayProxyResponseEvent responseEvent;
		DeleteRequest deleteRequest = new DeleteRequest();
    	deleteRequest.setBusinessName(DUMMY_VALUE);
    	deleteRequest.setRequestId(DUMMY_VALUE);
    	deleteRequest.setEmail(DUMMY_EMAIL);
    	
    	responseEvent = lambda.execute(makeRequestEvent(deleteRequest, DeleteFunction.FUNCTION_NAME));
    	DeleteResponse deleteResponse  = mapper.readValue(responseEvent.getBody(), DeleteResponse.class);    	

    	deleteRequest.setBusinessName(DUMMY_VALUE + "_OTHER");

    	responseEvent = lambda.execute(makeRequestEvent(deleteRequest, DeleteFunction.FUNCTION_NAME));
    	deleteResponse  = mapper.readValue(responseEvent.getBody(), DeleteResponse.class);
	}




    
    



}
