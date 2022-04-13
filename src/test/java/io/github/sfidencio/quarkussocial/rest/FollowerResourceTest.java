package io.github.sfidencio.quarkussocial.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import static io.restassured.RestAssured.given;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.core.Response;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.sfidencio.quarkussocial.domain.model.Follower;
import io.github.sfidencio.quarkussocial.domain.model.User;
import io.github.sfidencio.quarkussocial.domain.repository.FollowerRepository;
import io.github.sfidencio.quarkussocial.domain.repository.UserRepository;
import io.github.sfidencio.quarkussocial.rest.dto.FollowerRequest;
import io.github.sfidencio.quarkussocial.rest.dto.FollowerResponse;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestHTTPEndpoint(FollowerResource.class)
class FollowerResourceTest {

	 	@Inject
	    private UserRepository userRepository;
	    @Inject
	    private FollowerRepository followerRepository;

	    Long userId;
	    Long followerId;

	    @BeforeEach
	    @Transactional
	    void setUp() {
	        //user followed
	        User user = new User();
	        user.setAge(35);
	        user.setName("Fulano");
	        this.userRepository.persist(user);
	        this.userId = user.getId();

	        //follower
	        User follower = new User();
	        follower.setName("Beltrano");
	        follower.setAge(25);
	        this.userRepository.persist(follower);
	        followerId = follower.getId();

	        //cria um follower
	        Follower followerEntity = new Follower();
	        followerEntity.setFollower(follower);
	        followerEntity.setUser(user);
	        this.followerRepository.persist(followerEntity);
	    }

	    @Test
	    @DisplayName("should return 409 when Follower Id is equal to User id")
	    public void  testSameUserAsFollower(){

	        var body = new FollowerRequest();
	        body.setFollowerId(userId);

	        var response = given()
	            .contentType(ContentType.JSON)
	            .body(body)
	            .pathParam("userId", userId)
	        .when()
	            .put()
	        .then()
	            .statusCode(Response.Status.CONFLICT.getStatusCode())
	            //.body(Matchers.is("You can't follow yourself"));
	            .extract().response();
	    	 assertEquals("You can't follow yourself", response.jsonPath().getString("message"));
	    }

	    @Test
	    @DisplayName("should return 404 on follow a user when User id doen't exist")
	    public void userNotFoundWhenTryingToFollowTest(){

	        var body = new FollowerRequest();
	        body.setFollowerId(userId);

	        var inexistentUserId = 999;

	        given()
	            .contentType(ContentType.JSON)
	            .body(body)
	            .pathParam("userId", inexistentUserId)
	        .when()
	            .put()
	        .then()
	            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
	    }

	    @Test
	    @DisplayName("should follow a user")
	    public void followUserTest(){

	        var body = new FollowerRequest();
	        body.setFollowerId(followerId);

	        given()
	            .contentType(ContentType.JSON)
	            .body(body)
	            .pathParam("userId", userId)
	        .when()
	            .put()
	        .then()
	            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
	    }

	    @Test
	    @DisplayName("should return 404 on list user followers and User id doen't exist")
	    public void userNotFoundWhenListingFollowersTest(){
	        var inexistentUserId = 999;

	        given()
	            .contentType(ContentType.JSON)
	            .pathParam("userId", inexistentUserId)
	        .when()
	            .get()
	        .then()
	            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
	    }

	    @Test
	    @DisplayName("should list a user's followers")
	    public void listFollowersTest(){
	        var response =
	                given()
	                    .contentType(ContentType.JSON)
	                    .pathParam("userId", userId)
	                .when()
	                    .get()
	                .then()
	                    .extract().response();

	        var followersCount = response.jsonPath().get("followersCount");
	        var followersContent = response.jsonPath().getList("followers");
	        
	
	        assertEquals(Response.Status.OK.getStatusCode(), response.statusCode());
	        assertEquals(1, followersCount);
	        assertEquals(1, followersContent.size());

	    }

	    @Test
	    @DisplayName("should return 404 on unfollow user and User id doen't exist")
	    public void userNotFoundWhenUnfollowingAUserTest(){
	        var inexistentUserId = 999;

	        given()
	            .pathParam("userId", inexistentUserId)
	            .queryParam("followerId", followerId)
	        .when()
	            .delete()
	        .then()
	            .statusCode(Response.Status.NOT_FOUND.getStatusCode());
	    }

	    @Test
	    @DisplayName("should Unfollow an user")
	    public void unfollowUserTest(){
	        given()
	            .pathParam("userId", userId)
	            .queryParam("followerId", followerId)
	        .when()
	            .delete()
	        .then()
	            .statusCode(Response.Status.NO_CONTENT.getStatusCode());
	    }
	
	
	
}
