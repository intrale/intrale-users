package ar.com.intrale.cloud.test.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import ar.com.intrale.cloud.CredentialsGenerator;
import ar.com.intrale.cloud.IntraleFactory;
import ar.com.intrale.cloud.IntraleFunction;
import ar.com.intrale.cloud.Lambda;
import ar.com.intrale.cloud.Request;
import ar.com.intrale.cloud.TemporaryPasswordConfig;
import ar.com.intrale.cloud.functions.DeleteFunction;
import ar.com.intrale.cloud.functions.PasswordRecoveryFunction;
import ar.com.intrale.cloud.functions.ReadGroupFunction;
import ar.com.intrale.cloud.functions.SignInFunction;
import ar.com.intrale.cloud.functions.SignUpFunction;
import ar.com.intrale.cloud.functions.ValidateTokenFunction;
import ar.com.intrale.cloud.messages.DeleteRequest;
import ar.com.intrale.cloud.messages.DeleteResponse;
import ar.com.intrale.cloud.messages.Group;
import ar.com.intrale.cloud.messages.PasswordRecoveryRequest;
import ar.com.intrale.cloud.messages.ReadGroupResponse;
import ar.com.intrale.cloud.messages.ReadUserRequest;
import ar.com.intrale.cloud.messages.ReadUserResponse;
import ar.com.intrale.cloud.messages.SignInRequest;
import ar.com.intrale.cloud.messages.SignInResponse;
import ar.com.intrale.cloud.messages.SignUpRequest;
import ar.com.intrale.cloud.messages.SignUpResponse;
import ar.com.intrale.cloud.messages.UpdateUserRequest;
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
    	signUpRequest.setRequestId(DUMMY_VALUE);
    	signUpRequest.setEmail(DUMMY_EMAIL);

    	APIGatewayProxyRequestEvent requestEvent = makeRequestEvent(signUpRequest, SignUpFunction.FUNCTION_NAME);
		Map<String, String> headers = requestEvent.getHeaders();
		headers.remove(Lambda.HEADER_BUSINESS_NAME);
		responseEvent = lambda.execute(requestEvent);
		assertEquals(HttpStatus.NOT_FOUND.getCode(), responseEvent.getStatusCode());
		
		requestEvent = makeRequestEvent(signUpRequest, SignUpFunction.FUNCTION_NAME);
    	responseEvent = lambda.execute(requestEvent);
        SignUpResponse signupResponse  = mapper.readValue(responseEvent.getBody(), SignUpResponse.class);
    	
        assertEquals(DUMMY_EMAIL.toLowerCase(), signupResponse.getEmail().toLowerCase());
        
        // Hacemos el primer Signin con el email registrado
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setRequestId(DUMMY_VALUE);
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
        
        // signin con la nueva password
        signInRequest.setPassword(newPassword);
        
    	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
        signinResponse  = mapper.readValue(responseEvent.getBody(), SignInResponse.class);
        
        assertNotNull(signinResponse.getAccessToken());
        assertNotNull(signinResponse.getIdToken());
        assertNotNull(signinResponse.getRefreshToken());
        
        // signin con password incorrecta
        signInRequest.setPassword(DUMMY_VALUE);

       	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
        assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());
        
        // se trata de hacer signin con otro negocio
        signInRequest.setPassword(newPassword);
        requestEvent = makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME);
        requestEvent.getHeaders().put(Lambda.HEADER_BUSINESS_NAME, DUMMY_VALUE + "_OTHER2");
    	responseEvent = lambda.execute(requestEvent);

    	assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());
    	
        requestEvent = makeRequestEvent(signUpRequest, SignUpFunction.FUNCTION_NAME);
        requestEvent.getHeaders().put(Lambda.HEADER_BUSINESS_NAME, DUMMY_VALUE + "_OTHER");
    	responseEvent = lambda.execute(requestEvent);

    	signupResponse  = mapper.readValue(responseEvent.getBody(), SignUpResponse.class);
    	
        assertEquals(DUMMY_EMAIL.toLowerCase(), signupResponse.getEmail().toLowerCase());
        
    	APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent = makeRequestEvent(new Request(), ReadGroupFunction.FUNCTION_NAME);
    	apiGatewayProxyRequestEvent.getHeaders().put(Lambda.HEADER_ID_TOKEN, signinResponse.getIdToken());
    	apiGatewayProxyRequestEvent.getHeaders().put(Lambda.HEADER_AUTHORIZATION, signinResponse.getAccessToken());
    	responseEvent = lambda.execute(apiGatewayProxyRequestEvent);
    	ReadGroupResponse readGroupResponse = mapper.readValue(responseEvent.getBody(), ReadGroupResponse.class);

    	assertTrue(!readGroupResponse.getGroups().isEmpty());

    	
    	List<String> groups = new ArrayList<String>();
    	readGroupResponse.getGroups().forEach(new Consumer<Group>() {

			@Override
			public void accept(Group group) {
				groups.add(group.getName());
			}
		});
    	
    	UpdateUserRequest updateUserRequest = new UpdateUserRequest();
    	updateUserRequest.setRequestId(DUMMY_VALUE);
    	updateUserRequest.setEmail(DUMMY_EMAIL);
    	updateUserRequest.setGroups(groups);
    	updateUserRequest.setFamilyName(DUMMY_VALUE);
    	updateUserRequest.setName(DUMMY_VALUE);
    	
    	responseEvent = lambda.execute(makeRequestEvent(updateUserRequest, IntraleFunction.UPDATE));
    	assertEquals(HttpStatus.OK.getCode(), responseEvent.getStatusCode());
    	
    	updateUserRequest.setGroups(groups.subList(1, groups.size()));
    	
    	responseEvent = lambda.execute(makeRequestEvent(updateUserRequest, IntraleFunction.UPDATE));
    	assertEquals(HttpStatus.OK.getCode(), responseEvent.getStatusCode());
        
    	
    	ReadUserRequest readUserRequest = new ReadUserRequest();
    	readUserRequest.setRequestId(DUMMY_VALUE);
    	
    	responseEvent = lambda.execute(makeRequestEvent(readUserRequest, IntraleFunction.READ));
    	ReadUserResponse readUserResponse = mapper.readValue(responseEvent.getBody(), ReadUserResponse.class);   
    
    	assertEquals(1, readUserResponse.getUsers().size());
    	
    	readUserRequest.setEmail(signInRequest.getEmail());
    	
    	responseEvent = lambda.execute(makeRequestEvent(readUserRequest, IntraleFunction.READ));
    	readUserResponse = mapper.readValue(responseEvent.getBody(), ReadUserResponse.class);
    	
    	assertEquals(1, readUserResponse.getUsers().size());
    	
    	User user = readUserResponse.getUsers().iterator().next();
    	assertEquals(DUMMY_VALUE, user.getName());
    	assertEquals(DUMMY_VALUE, user.getFamilyName());
    	
    	PasswordRecoveryRequest passwordRecoveryRequest = new PasswordRecoveryRequest();
    	passwordRecoveryRequest.setRequestId("001");
    	passwordRecoveryRequest.setEmail(DUMMY_EMAIL);
    	
    	responseEvent = lambda.execute(makeRequestEvent(passwordRecoveryRequest, PasswordRecoveryFunction.FUNCTION_NAME));
    	//PasswordRecoveryResponse passwordRecoveryResponse = mapper.readValue(responseEvent.getBody(), PasswordRecoveryResponse.class); 
    	
    	deleteUser();   
        
    	responseEvent = lambda.execute(makeRequestEvent(signInRequest, SignInFunction.FUNCTION_NAME));
    	
    	assertEquals(HttpStatus.UNAUTHORIZED.getCode(), responseEvent.getStatusCode());
    	
    	Request request = new Request();
    	request.setRequestId(DUMMY_VALUE);
    	apiGatewayProxyRequestEvent = makeRequestEvent(request, ValidateTokenFunction.FUNCTION_NAME);
    	apiGatewayProxyRequestEvent.getHeaders().put(Lambda.HEADER_ID_TOKEN, signinResponse.getIdToken());
    	apiGatewayProxyRequestEvent.getHeaders().put(Lambda.HEADER_AUTHORIZATION, signinResponse.getAccessToken());
    	
    	responseEvent = lambda.execute(apiGatewayProxyRequestEvent);
    	assertEquals(HttpStatus.OK.getCode(), responseEvent.getStatusCode());
    	
    }

	private void deleteUser() throws Exception, JsonProcessingException, JsonMappingException {
		APIGatewayProxyResponseEvent responseEvent;
		DeleteRequest deleteRequest = new DeleteRequest();
    	deleteRequest.setRequestId(DUMMY_VALUE);
    	deleteRequest.setEmail(DUMMY_EMAIL);
    	
    	APIGatewayProxyRequestEvent requestEvent = makeRequestEvent(deleteRequest, DeleteFunction.FUNCTION_NAME);
    	requestEvent.getHeaders().put(Lambda.HEADER_BUSINESS_NAME, DUMMY_VALUE);
    	responseEvent = lambda.execute(requestEvent);
    	DeleteResponse deleteResponse  = mapper.readValue(responseEvent.getBody(), DeleteResponse.class);    	

    	requestEvent = makeRequestEvent(deleteRequest, DeleteFunction.FUNCTION_NAME);
    	requestEvent.getHeaders().put(Lambda.HEADER_BUSINESS_NAME, DUMMY_VALUE + "_OTHER");
    	responseEvent = lambda.execute(requestEvent);
    	deleteResponse  = mapper.readValue(responseEvent.getBody(), DeleteResponse.class);
	}




    
    



}
