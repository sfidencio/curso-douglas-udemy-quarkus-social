package io.github.sfidencio.quarkussocial.rest.dto;

import javax.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CreatePostRequest {
	@NotBlank(message = "Post text is required")
	private String text;
}