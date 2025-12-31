package com.ecommerce.ecommerce_backend.user;

import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_backend.constants.ApplicationConstants;
import com.ecommerce.ecommerce_backend.exceptions.EmptyListException;
import com.ecommerce.ecommerce_backend.exceptions.ResourceAlreadyExistsException;
import com.ecommerce.ecommerce_backend.exceptions.ResourceNotFoundException;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final Environment env;

	private AuthUserPrincipal getPrincipal() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !(auth.getPrincipal() instanceof AuthUserPrincipal principal)) {
			throw new ResourceNotFoundException("User not authenticated");
		}

		return principal;
	}

	public List<UserDTO> getAllUsers() {
		// TODO Auto-generated method stub
		List<Users> userList = userRepository.findAllWithAuthorities();
		if (userList.isEmpty())
			throw new EmptyListException("No users found");
		else
			return userList.stream().map(user -> userMapper.toDTO(user)).toList();
	}

	public UserDTO getUserById(Long id) {
		Users user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));
		return userMapper.toDTO(user);
	}

	public UserDTO createUser(UserDTO userDTO) {
		// TODO Auto-generated method stub
		Optional<Users> savedUser = userRepository.findByUsername(userDTO.getUsername());
		if (savedUser.isPresent())
			throw new ResourceAlreadyExistsException("User already exists with username: " + userDTO.getUsername());
		else {
			Users user = userMapper.toEntity(userDTO);
			user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
			// ALWAYS assign default role
			Authority authority = new Authority();
			authority.setName("ROLE_USER");
			authority.setUser(user);

			user.setAuthorities(Set.of(authority));

			Users newUser = userRepository.save(user);
			return userMapper.toDTO(newUser);
		}

	}

	public UserDTO updateUser(Long id, @Valid UserDTO userDTO) throws AccessDeniedException {

		AuthUserPrincipal principal = getPrincipal();

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

		// Only ADMIN or self can update
		if (!isAdmin && !principal.id().equals(id)) {
			throw new AccessDeniedException("You cannot update another user");
		}
		Users savedUser = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User does not exist with id: " + id));

		savedUser.setUsername(userDTO.getUsername());
		if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
			savedUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
		}
		// ONLY ADMIN can update authorities
		if (isAdmin && userDTO.getAuthorities() != null) {

			Set<String> existingRoles = savedUser.getAuthorities().stream().map(Authority::getName)
					.collect(Collectors.toSet());

			// add new authorities
			for (Authority authority : userDTO.getAuthorities()) {
				String role = authority.getName().toUpperCase();

				// Ensure ROLE_ prefix
				if (!role.startsWith("ROLE_")) {
					role = "ROLE_" + role;
				}
				if (!existingRoles.contains(role)) {
					Authority newAuthority = new Authority();
					newAuthority.setName(role);
					newAuthority.setUser(savedUser);
					savedUser.getAuthorities().add(newAuthority);
				}
			}
		}

		return userMapper.toDTO(userRepository.save(savedUser));

	}

	public void deleteUser(Long id) {
		// TODO Auto-generated method stub
		Users user = userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

		userRepository.delete(user);

	}

	public String generateTokenToLogin(Authentication authentication) {
		String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
				ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);

		SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

		String authorities = authentication.getAuthorities().stream().map(a -> a.getAuthority())
				.collect(Collectors.joining(","));

		Users user = userRepository.findByUsername(authentication.getName()).orElseThrow();

		return Jwts.builder().claim("userId", user.getId()).claim("username", authentication.getName())
				.claim("authorities", authorities).setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 30 * 60 * 1000)).signWith(key).compact();
	}
}
