package io.github.sfidencio.quarkussocial.rest;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.github.sfidencio.quarkussocial.domain.model.Post;
import io.github.sfidencio.quarkussocial.domain.model.User;
import io.github.sfidencio.quarkussocial.domain.repository.FollowerRepository;
import io.github.sfidencio.quarkussocial.domain.repository.PostRepository;
import io.github.sfidencio.quarkussocial.domain.repository.UserRepository;
import io.github.sfidencio.quarkussocial.rest.dto.CreatePostRequest;
import io.github.sfidencio.quarkussocial.rest.dto.FieldError;
import io.github.sfidencio.quarkussocial.rest.dto.PostResponse;
import io.github.sfidencio.quarkussocial.rest.dto.ResponseError;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;

@Path("/users/{userId}/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostResource {
	private PostRepository postRepository;
	private UserRepository userRepository;
	private FollowerRepository followerRepository;
	private Validator validator;

	@Inject
	public PostResource(PostRepository postRespository, UserRepository userRespository,FollowerRepository followerRepository, Validator validator) {
		super();
		this.postRepository = postRespository;
		this.userRepository = userRespository;
		this.followerRepository = followerRepository;
		this.validator = validator;
	}

	@POST
	@Transactional
	@Operation(summary = "Create a new post for the social network user quarkus")
	@APIResponse(responseCode = "201", //
			content = @Content(//
					mediaType = MediaType.APPLICATION_JSON, //
					schema = @Schema(//
							implementation = CreatePostRequest.class, //
							type = SchemaType.OBJECT)))
	@APIResponse(responseCode = "422",description = "The 422 (Unprocessable Entity) status code means the server understands the content type of the request entity",
			content = @Content(//
					mediaType = MediaType.APPLICATION_JSON, //
					schema = @Schema(//
							implementation = ResponseError.class, //
							type = SchemaType.OBJECT))) 
	@APIResponse(responseCode = "404",description = "Post user to be registered not found")
	public Response savePost(@PathParam(value = "userId") Long idUser, CreatePostRequest createPostRequest) {

		if (createPostRequest.getText().length() < 5 || createPostRequest.getText().length() > 144)
			return Response.status(ResponseError.UNPROCESSABLE_ENTITY_STATUS)
					.entity(new FieldError("422", "Post requires a minimum of 5 and a maximum of 144 characters"))
					.build();

		Set<ConstraintViolation<CreatePostRequest>> violations = this.validator.validate(createPostRequest);
		if (!violations.isEmpty()) {
			return ResponseError.createFromValidation(violations)
					.withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
		}

		User user = this.userRepository.findById(idUser);
		if (Objects.nonNull(user)) {
			Post post = new Post();
			post.setText(createPostRequest.getText());
			post.setUser(user);
			this.postRepository.persist(post);
			return Response.status(Status.CREATED).entity(post).build();
		}
		return Response.status(Status.NOT_FOUND).entity(new FieldError("404", "Post user to be registered not found"))
				.build();
	}

	@GET
	@Operation(summary = "List all posts the user by ID for the social network user quarkus")
	@APIResponse(responseCode = "200", //
			content = @Content(//
					mediaType = MediaType.APPLICATION_JSON, //
					schema = @Schema(//
							implementation = CreatePostRequest.class, //
							type = SchemaType.ARRAY)))
	@APIResponse(responseCode = "422",description = "The 422 (Unprocessable Entity) status code means the server understands the content type of the request entity",
	content = @Content(//
			mediaType = MediaType.APPLICATION_JSON, //
			schema = @Schema(//
					implementation = ResponseError.class, //
					type = SchemaType.OBJECT))) 	@APIResponse(responseCode = "404",description = "Post user to be registered not found")
	@APIResponse(responseCode = "400",description = "You forgot to inform the FollowerId in the request header")
	@APIResponse(responseCode = "400",description = "Non-existent follower")
	@APIResponse(responseCode = "403",description = "You can't see these posts")
	public Response listPosts(@PathParam(value = "userId") Long idUser,
							  @HeaderParam("followerId") Long followerId) {
		User user = this.userRepository.findById(idUser);
		if (Objects.isNull(user))
			return Response.status(Status.NOT_FOUND)
					.entity(new FieldError("404", "Post user to be registered not found")).build();

		
		if(Objects.isNull(followerId))
			return Response.status(Status.BAD_REQUEST)
					.entity(new FieldError("400", "You forgot to inform the FollowerId in the request header")).build();

		
		
		User follower = this.userRepository.findById(followerId);
		if(Objects.isNull(follower))	{
			return Response.status(Status.BAD_REQUEST)
					.entity(new FieldError("400", "Non-existent follower")).build();

		}
		
		
		  Boolean isFollower = this.followerRepository.isFollower(follower, user);
	        if(!isFollower){
	            return Response.status(Response.Status.FORBIDDEN)
	                    .entity(new FieldError("403","You can't see these posts"))
	                    .build();
	        }
		
		
		
		/*Get list of posts sorted by most recent.*/
		var listPosts = this.postRepository
				.find("user", Sort.by("dateTime", Direction.Descending), user)
				.list();

		/*
		 * Return a response (PostResponse) with only the fields of the API contract text and date and time.
    	 */
		var postResponseList = listPosts.stream()
				// .map(post -> PostResponse.fromEntity(post))
				.map(PostResponse::fromEntity).collect(Collectors.toList());
		return Response.ok(postResponseList).build();
	}

}
