package io.github.sfidencio.quarkussocial.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.github.sfidencio.quarkussocial.domain.model.Follower;
import io.github.sfidencio.quarkussocial.domain.model.Post;
import io.github.sfidencio.quarkussocial.domain.model.User;
import io.github.sfidencio.quarkussocial.domain.repository.FollowerRepository;
import io.github.sfidencio.quarkussocial.domain.repository.PostRepository;
import io.github.sfidencio.quarkussocial.domain.repository.UserRepository;
import io.github.sfidencio.quarkussocial.rest.dto.CreatePostRequest;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
/* With this configuration, we do not need to configure the URL */
@TestHTTPEndpoint(PostResource.class)
class PostResourceTest {

	@Inject
	private UserRepository userRepository;
	@Inject
	private FollowerRepository followerRepository;
	@Inject
	private PostRepository postRepository;
	
	private Long userId;
	private Long userNotFollowerId;
	private Long userFollowerId;

	@BeforeEach
	@Transactional
	public void setUP() {
		User user = new User();
		user.setName("Fulano");
		user.setAge(35);
		this.userRepository.persist(user);
		userId = user.getId();
		
		//Post for user => "user"
		Post post = new Post();
		post.setText("Hello");
		post.setUser(user);
		this.postRepository.persist(post);
		
		
		//user who doesn't follow anyone - 
		//usuário que não segue ninguém
		User userNotFollower = new User();
		userNotFollower.setName("Ciclano");
		userNotFollower.setAge(58);
		this.userRepository.persist(userNotFollower);
		userNotFollowerId = userNotFollower.getId();
		
		
		//user who follows- usuário que segue
		User userFollower = new User();
		userFollower.setName("Beltrano");
		userFollower.setAge(25);
		this.userRepository.persist(userFollower);
		userFollowerId = userFollower.getId();
		
		Follower follower = new Follower();
		follower.setUser(user);
		follower.setFollower(userFollower);
		this.followerRepository.persist(follower);

	}
	

	@Test
	@DisplayName("should return 201 when trying to make a post  for an user")
	@Order(1)
	public void testCreateUser() {
		var postRequest = new CreatePostRequest();
		postRequest.setText("Some text");
		
		given().
			contentType(ContentType.JSON)
			.body(postRequest)
			.pathParam("userId", userId)
		.when()
			.post()
		.then()
			.statusCode(201);

	}

	
	@Test
	@DisplayName("should return 404 when trying to make a post  for an inexistent user")
	@Order(2)
	public void testPostForAnInexistentUser() {
		var postRequest = new CreatePostRequest();
		postRequest.setText("Some text");
		
		given().
			contentType(ContentType.JSON)
			.body(postRequest)
			.pathParam("userId", 999)
		.when()
			.post()
		.then()
			.statusCode(404);

	}
	
	
	@Test
	@DisplayName("should return 404 when user doesn's exist")
	@Order(3)
	public void testListPostUserNotFound() {
		var inexistentUserId = 999;
		given()
			.pathParam("userId", inexistentUserId)
		.when()
			.get()
		.then()
			.statusCode(404);
	}
	
	@Test
	@DisplayName("should return 400 when followerId header not present")
	@Order(4)
	public void testListPostFollowerHeaderNotSend() {
		/*Note that the header was not passed on on purpose
   		 *Observe que o cabeçalho não foi passado de proposito
		 */
		
		var response = given()
			.pathParam("userId", userId)
		.when()
			.get()
		.then()
			.statusCode(400)
			//.body(Matchers.is("You forgot to inform the FollowerId in the request header")) /*Use this line, only if the message comes in the body, without being in a JSON array object. - Utilizar esta linha, apenas se a mensagem vier no corpo, sem estar em um objeto array JSON.*/
			
			/*
			unnecessary commands below, if you use the direct test in the body of the return.
			*/
			.extract().response();
	    	 assertEquals("You forgot to inform the FollowerId in the request header", response.jsonPath().getString("message"));
	}
	
	@Test
	@DisplayName("should return 400 when follower doesn's exist")
	@Order(5)
	public void testListPostFollowerNotFound() {
		var inexistentFollowerId = 999;
		var response = given()
			.pathParam("userId", userId)
			.header("followerId",inexistentFollowerId)
		.when()
			.get()
		.then()
			.statusCode(400)
			//.body(Matchers.is("Non-existent follower"));
		 /*
		 unnecessary commands below, if you use the direct test in the body of the return.
		 */
		.extract().response();
    	 assertEquals("Non-existent follower", response.jsonPath().getString("message"));
		

	}
	
	
	@Test
	@DisplayName("should return 403 when follower isn't a follower")
	@Order(6)
	public void testListPostNotAFollower() {
		var response = given()
			.pathParam("userId", userId)
			.header("followerId",userNotFollowerId)
		.when()
			.get()
		.then()
			.statusCode(403)
			//.body(Matchers.is("Non-existent follower"));
		 /*
		 unnecessary commands below, if you use the direct test in the body of the return.
		 */
		.extract().response();
    	 assertEquals("You can't see these posts", response.jsonPath().getString("message"));
	}
	
	@Test
	@DisplayName("should return posts")
	@Order(7)
	public void testListPosts() {
			given()
				.pathParam("userId", userId)
				.header("followerId",userFollowerId)
			.when()
				.get()
			.then()
				.statusCode(200);
				//.body("size()",Matchers.is("1"));
	}
	

}
