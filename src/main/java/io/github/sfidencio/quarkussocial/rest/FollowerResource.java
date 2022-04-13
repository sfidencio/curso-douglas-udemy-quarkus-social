package io.github.sfidencio.quarkussocial.rest;

import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.github.sfidencio.quarkussocial.domain.model.Follower;
import io.github.sfidencio.quarkussocial.domain.repository.FollowerRepository;
import io.github.sfidencio.quarkussocial.domain.repository.UserRepository;
import io.github.sfidencio.quarkussocial.rest.dto.FieldError;
import io.github.sfidencio.quarkussocial.rest.dto.FollowerRequest;
import io.github.sfidencio.quarkussocial.rest.dto.FollowerResponse;
import io.github.sfidencio.quarkussocial.rest.dto.FollowersPerUserResponse;

@Path("/users/{userId}/followers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FollowerResource {

	private FollowerRepository followerRepository;
	private UserRepository userRepository;

	@Inject
	public FollowerResource(FollowerRepository followerRepository, UserRepository userRepository) {
		this.followerRepository = followerRepository;
		this.userRepository = userRepository;
	}

	@PUT
	@Operation(summary = "Follow user quarkus social network user")
	@Transactional
	@APIResponse(responseCode = "204", description = "Returns no content. Only code 204")
	@APIResponse(responseCode = "409", description = "You can't follow yourself")
	@APIResponse(responseCode = "404", description = "User not found")
	@APIResponse(responseCode = "404", description = "Follower not found")
	public Response followUser(@PathParam("userId") Long userId, FollowerRequest followResquest) {

		if (userId.equals(followResquest.getFollowerId()))
			return Response.status(Status.CONFLICT).entity(new FieldError("409", "You can't follow yourself")).build();

		// get user (seguido)
		var user = this.userRepository.findById(userId);
		if (Objects.isNull(user))
			return Response.status(Status.NOT_FOUND).entity(new FieldError("404", "User not found")).build();

		// get follower(seguidor)
		var follower = this.userRepository.findById(followResquest.getFollowerId());
		if (Objects.isNull(follower))
			return Response.status(Status.NOT_FOUND).entity(new FieldError("404", "Follower not found")).build();

		Boolean isFollower = this.followerRepository.isFollower(follower, user);
		if (!isFollower) {
			var entity = new Follower();
			// join the two,,follower and user..
			entity.setUser(user);
			entity.setFollower(follower);
			this.followerRepository.persist(entity);
		}

		// Anyway returns noContent(204)
		return Response.noContent().build();
	}

	@DELETE
	@Transactional
	@Operation(summary = "Unfollow quarkus social network user")
	@APIResponse(responseCode = "204", description = "Returns no content. Only code 204")
	@APIResponse(responseCode = "409", description = "You can't unfollow yourself")
	@APIResponse(responseCode = "404", description = "User followed not found")
	public Response unFollowUser(@PathParam("userId") Long userId, @QueryParam("followerId") Long followerId) {

		if (userId.equals(followerId))
			return Response.status(Status.CONFLICT).entity(new FieldError("409", "You can't unfollow yourself"))
					.build();

		// Checks if the followed user exists
		var user = this.userRepository.findById(userId);
		if (Objects.isNull(user))
			return Response.status(Status.NOT_FOUND).entity(new FieldError("404", "User followed not found")).build();
		this.followerRepository.deleteByFollowerAndUser(followerId, userId);
		// Anyway returns noContent(204)
		return Response.noContent().build();
	}

	@GET
	@Operation(summary = "List followers by quarkus social network user")
	@APIResponse(responseCode = "200", description = "Returns followers", //
			content = @Content(//
					mediaType = MediaType.APPLICATION_JSON, //
					schema = @Schema(//
							implementation = FollowersPerUserResponse.class, //
							type = SchemaType.ARRAY)))
	@APIResponse(responseCode = "404", description = "User not found")
	public Response listFollowers(@PathParam("userId") Long userId) {
		var user = this.userRepository.findById(userId);
		if (Objects.isNull(user))
			return Response.status(Status.NOT_FOUND).entity(new FieldError("404", "User not found")).build();
		var result = this.followerRepository.findByUser(userId);
		FollowersPerUserResponse responseObject = new FollowersPerUserResponse();
		responseObject.setFollowersCount(result.size());
		var followersList = result.stream().map(f -> new FollowerResponse(f)).collect(Collectors.toList());
		responseObject.setFollowers(followersList);
		return Response.ok(responseObject).build();
	}

}
