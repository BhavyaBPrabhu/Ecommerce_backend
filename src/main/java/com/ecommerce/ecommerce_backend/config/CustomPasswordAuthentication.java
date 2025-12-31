package com.ecommerce.ecommerce_backend.config;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecommerce.ecommerce_backend.security.CustomUserDetailsService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomPasswordAuthentication implements AuthenticationProvider {

	private final CustomUserDetailsService customUserDetailsService;
	private final PasswordEncoder passwordEncoder;
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		// TODO Auto-generated method stub
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		UserDetails userDetails= customUserDetailsService.loadUserByUsername(username);
		if(passwordEncoder.matches(password, userDetails.getPassword())) {
			return new UsernamePasswordAuthenticationToken(username,password,userDetails.getAuthorities());
		}
		else
			throw new BadCredentialsException("Invalid password");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		// TODO Auto-generated method stub
		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
	}

}
