package io.github.sfidencio.quarkussocial.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.sfidencio.quarkussocial.rest.dto.CreateUserRequest;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserResourceTest {

	@TestHTTPResource("/users")
	URL apiURL;
	
	@Test
	@DisplayName("should create an user successfuly")
	@Order(1)
	public void testCreateUser() {
		var user = new CreateUserRequest();
		user.setName("Fulano");
		user.setAge(50);
		
		//request 
		//given this scenario - dado este cenário
		var response = given()
			.contentType(ContentType.JSON)
			.body(user)
		.when()
			.post(apiURL)
		.then()
			.extract().response();
		
		assertEquals(201, response.getStatusCode());
		assertNotNull(response.jsonPath().getString("id")); //get id, cannot be null in JSON return
			
	}
	
	
	@Test
	@DisplayName("should return error when json is not valid")
	@Order(2)
	public void testCreaterUserValidationErrors() {
		var user = new CreateUserRequest();
		user.setName(null);
		user.setAge(190);
		
		var response = given()
				.contentType(ContentType.JSON)
				.body(user)
			.when()
				.post(apiURL)
			.then()
				.extract().response();
	
		assertEquals(422, response.getStatusCode());
		
		System.out.println(response.jsonPath().getString("field"));
		
		//deepening level of errors - aprofundando nivel de teste 
		//assertEquals("Validation Error", response.jsonPath().getString("field"));
		
		//Check if field message is null
		List<Map<String,String>> errors = response.jsonPath().getList("errors");	
		assertNotNull(errors.get(0).get("message"));
		assertNotNull(errors.get(1).get("message"));
		
		
		//deepening level of errors - aprofundando nivel de teste 
		/*List<Map<String,String>> errors = response.jsonPath().getList("errors");	
		System.out.println(response.jsonPath().getString("errors"));
		
		assertEquals("Name is required", errors.get(0).get("message"));
		assertEquals("name", errors.get(0).get("field"));
		System.out.println(errors.get(0).get("field"));
		System.out.println(errors.get(0).get("message"));
		
		
		assertEquals("Name is required", errors.get(1).get("message"));
		assertEquals("name", errors.get(1).get("field"));
		System.out.println(errors.get(1).get("field"));
		System.out.println(errors.get(1).get("message"));*/
	} 
	
	@Test
	@DisplayName("should list all users with successfuly")
	@Order(3)
	public void testListAllUsers() {
	     //request 
		//given this scenario - dado este cenário
		var response = given()
				.contentType(ContentType.JSON)
		.when()
			.get(apiURL)
		.then()
			.statusCode(200)
			.body("size()", Matchers.is(1));
		

			
	}
	

}
