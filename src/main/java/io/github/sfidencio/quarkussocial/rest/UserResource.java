package io.github.sfidencio.quarkussocial.rest;

import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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

import io.github.sfidencio.quarkussocial.domain.model.User;
import io.github.sfidencio.quarkussocial.domain.repository.UserRepository;
import io.github.sfidencio.quarkussocial.rest.dto.CreateUserRequest;
import io.github.sfidencio.quarkussocial.rest.dto.FieldError;
import io.github.sfidencio.quarkussocial.rest.dto.ResponseError;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

	private UserRepository userRepository;
	private Validator validator;

	@Inject
	public UserResource(UserRepository userRespository, Validator validator) {
		this.userRepository = userRespository;
		this.validator = validator;
	}

	@POST
	@Transactional
	@Operation(summary = "Create a new user for quarkus social network")
	@APIResponse(responseCode = "201", //
			content = @Content(//
					mediaType = MediaType.APPLICATION_JSON, //
					schema = @Schema(//
							implementation = User.class, //
							type = SchemaType.OBJECT)))
	@APIResponse(responseCode = "422",description = "The 422 (Unprocessable Entity) status code means the server understands the content type of the request entity",
	content = @Content(//
			mediaType = MediaType.APPLICATION_JSON, //
			schema = @Schema(//
					implementation = ResponseError.class, //
					type = SchemaType.OBJECT))) 
	public Response createUser(CreateUserRequest createUserRequest) {

		Set<ConstraintViolation<CreateUserRequest>> violations = this.validator.validate(createUserRequest);
		if (!violations.isEmpty()) {
			return ResponseError.createFromValidation(violations)
					.withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
		}

		User user = new User();
		user.setName(createUserRequest.getName());
		user.setAge(createUserRequest.getAge());
		this.userRepository.persist(user);
		return Response.status(Status.CREATED).entity(user).build();
	}

	@GET
	@Operation(summary = "Lists all registered users on the quarkus social network")
	@APIResponse(responseCode = "200", description = "Returns a list of registered users", //
			content = @Content(//
					mediaType = MediaType.APPLICATION_JSON, //
					schema = @Schema(//
							implementation = User.class, //
							type = SchemaType.ARRAY)))
	public Response listAllUsers() {
		return Response.ok(this.userRepository.findAll().list()).build();
	}

	@DELETE
	@Path("{id}")
	@Transactional
	@Operation(summary = "Delete user by ID the quarkus social network")
	@APIResponse(responseCode = "204", description = "Returns no content. Only code 204")
	@APIResponse(responseCode = "404",description = "User not found to be deleted")
	public Response deleteUser(@PathParam("id") Long id) {
		User userDelete = this.userRepository.findById(id);
		if (Objects.nonNull(userDelete)) {
			this.userRepository.delete(userDelete);
			return Response.noContent().build();
		}
		return Response.status(Status.NOT_FOUND).entity(new FieldError("404", "User not found to be deleted")).build();
	}

	@PUT
	@Path("{id}")
	@Transactional
	@Operation(summary = "Update user by ID the quarkus social network")
	@APIResponse(responseCode = "204", description = "Returns no content. Only code 204")
	@APIResponse(responseCode = "422",description = "The 422 (Unprocessable Entity) status code means the server understands the content type of the request entity",
	content = @Content(//
			mediaType = MediaType.APPLICATION_JSON, //
			schema = @Schema(//
					implementation = ResponseError.class, //
					type = SchemaType.OBJECT))) 	@APIResponse(responseCode = "404",description = "User not found to be updated")
	public Response updateUser(@PathParam("id") Long id, CreateUserRequest createUserRequest) {

		
		Set<ConstraintViolation<CreateUserRequest>> violations = this.validator.validate(createUserRequest);
		if (!violations.isEmpty()) {
			return ResponseError.createFromValidation(violations)
					.withStatusCode(ResponseError.UNPROCESSABLE_ENTITY_STATUS);
		}

		User userUpdate = this.userRepository.findById(id);
		if (Objects.nonNull(userUpdate)) {
			userUpdate.setName(createUserRequest.getName());
			userUpdate.setAge(createUserRequest.getAge());
			return Response.noContent().build();
		}
		return Response.status(Status.NOT_FOUND).entity(new FieldError("404", "User not found to be updated")).build();
	}

}
