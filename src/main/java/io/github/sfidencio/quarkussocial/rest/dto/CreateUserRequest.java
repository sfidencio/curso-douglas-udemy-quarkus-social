package io.github.sfidencio.quarkussocial.rest.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class CreateUserRequest {
	@NotBlank(message = "Name is required")
	private String name;
	@NotNull(message = "Age is required")
	@Max(message = "Age max 90",value = 90)
	@Min(message = "Age min 18",value = 18)
	private Integer age;
}