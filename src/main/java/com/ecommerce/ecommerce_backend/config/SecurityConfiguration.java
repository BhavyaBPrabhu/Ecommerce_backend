package com.ecommerce.ecommerce_backend.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import com.ecommerce.ecommerce_backend.filter.JWTTokenValidatorFilter;
import com.ecommerce.ecommerce_backend.handlers.CustomAuthenticationEntryPoint;
import com.ecommerce.ecommerce_backend.security.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

	private final Environment env;

	@Bean
	SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, AuthenticationManager authManager)
			throws Exception {

		JWTTokenValidatorFilter validatorFilter = new JWTTokenValidatorFilter(env);

		http.csrf(csrfConfig -> csrfConfig.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(validatorFilter, BasicAuthenticationFilter.class)
				.authorizeHttpRequests((requests) -> requests.requestMatchers("/cart", "/cart/**")
						.hasAnyRole("USER", "ADMIN").requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
						// public endpoints
						.requestMatchers("/users/login", "/error", "/users/register", "/products", "/category","/products/health",
								"/products/**", "/category/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
						.permitAll().requestMatchers("/users/**").authenticated()

						.anyRequest().authenticated())
				.exceptionHandling(e -> e.authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
				.formLogin(t -> t.disable()).httpBasic(withDefaults());
		return http.build();

	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(CustomUserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		CustomPasswordAuthentication authenticationProvider = new CustomPasswordAuthentication(userDetailsService,
				passwordEncoder);
		ProviderManager providerManager = new ProviderManager(authenticationProvider);
		// Do not erase credentials after authentication
		providerManager.setEraseCredentialsAfterAuthentication(false);
		return providerManager;
	}
}
