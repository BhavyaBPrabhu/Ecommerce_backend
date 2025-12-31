package com.ecommerce.ecommerce_backend.user;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Tag(name = "Users", description = "User management, registration and authentication APIs")
public class UserController {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;

	@Operation(summary = "Get all users", description = "Fetches list of all registered users. Admin access only.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Users fetched successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@GetMapping
	public ResponseEntity<List<UserDTO>> getAllUsers() {
		List<UserDTO> userList = userService.getAllUsers();
		return ResponseEntity.ok(userList);
	}

	@Operation(summary = "Get user by ID", description = "Fetches user details by user ID. Accessible by admin or the same user.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "User fetched successfully"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@GetMapping("/{id}")
	public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
		UserDTO userDTO = userService.getUserById(id);
		return ResponseEntity.ok(userDTO);
	}

	@Operation(summary = "Register a new user", description = "Creates a new user account.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "User registered successfully"),
			@ApiResponse(responseCode = "400", description = "Validation error") })
	@PostMapping("/register")
	public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) {
		UserDTO savedUser = userService.createUser(userDTO);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(savedUser.getId())
				.toUri();
		return ResponseEntity.created(location).body(savedUser);
	}

	@Operation(summary = "Update user", description = "Updates user details. Admin or the same user can update.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "User updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input"),
			@ApiResponse(responseCode = "403", description = "Access denied"),
			@ApiResponse(responseCode = "404", description = "User not found") })
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or #id == principal.id")
	public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO)
			throws AccessDeniedException {
		UserDTO updatedUser = userService.updateUser(id, userDTO);
		return ResponseEntity.ok(updatedUser);
	}

	@Operation(summary = "Delete user", description = "Deletes a user by ID. Admin access only.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "User deleted successfully"),
			@ApiResponse(responseCode = "404", description = "User not found"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@PreAuthorize("hasRole('ADMIN')")
	@DeleteMapping("/{id}")
	public ResponseEntity<String> deleteUser(@PathVariable Long id) {
		userService.deleteUser(id);
		return ResponseEntity.ok("User deleted successfully");
	}

	@Operation(summary = "User Login", description = "Authenticates user credentials and returns JWT token.")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Login successful"),
			@ApiResponse(responseCode = "401", description = "Invalid credentials") })
	@PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
	public ResponseEntity<LoginResponseDTO> apiLogin(@RequestBody LoginRequestDTO loginRequestDTO) {

		// build authentication token
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
				loginRequestDTO.username(), loginRequestDTO.password());

		Authentication authentication = authenticationManager.authenticate(authToken);

		String token = userService.generateTokenToLogin(authentication);

		return ResponseEntity.ok(new LoginResponseDTO("Login successful", token));
	}
}
