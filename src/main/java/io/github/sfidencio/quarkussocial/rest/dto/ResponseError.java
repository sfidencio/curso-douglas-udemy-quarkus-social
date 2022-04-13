package io.github.sfidencio.quarkussocial.rest.dto;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseError {
	public static final int UNPROCESSABLE_ENTITY_STATUS = 422;
	private String field;
	private Collection<FieldError> errors;

	/*
	 * public static <T> ResponseError createFromValidation(
	 * Set<ConstraintViolation<T>> violations){ List<FieldError> errors = violations
	 * .stream() .map(cv -> new FieldError(cv.getPropertyPath().toString(),
	 * cv.getMessage())) .collect(Collectors.toList());
	 * 
	 * String message = "Validation Error";
	 * 
	 * var responseError = new ResponseError(message, errors); return responseError;
	 * }
	 */

	public static <T> ResponseError createFromValidation(Set<ConstraintViolation<T>> violations) {
		List<FieldError> errors = violations.stream()
				.map(cv -> new FieldError(cv.getPropertyPath().toString(), cv.getMessage()))
				.collect(Collectors.toList());

		String message = "Validation Error";

		// feature do JAVA 11
		var responseError = new ResponseError(message, errors);
		return responseError;

	}

	public Response withStatusCode(int code) {
		return Response.status(code).entity(this).build();
	}

}
