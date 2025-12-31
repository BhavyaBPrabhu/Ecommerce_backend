package com.ecommerce.ecommerce_backend.user;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

	private long id;

	@NotBlank(message = "Username cannot be blank")
	@Size(min = 4, max = 50, message = " Username should be between 4 and 50 characters")
	private String username;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank(message = "Password cannot be blank")
	@Size(min = 6, message = "Password must be at least 6 characters long")
	private String password;

	// @NotNull(message = "Authority is required")
	private Set<Authority> authorities;
}
