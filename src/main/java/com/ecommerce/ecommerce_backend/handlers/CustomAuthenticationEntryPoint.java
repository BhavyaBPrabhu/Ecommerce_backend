package com.ecommerce.ecommerce_backend.handlers;

import java.io.IOException;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {

        // Set 401 for unauthorized access
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String message = "Unauthorized";

        if (authException instanceof UsernameNotFoundException) {
            message = "Username does not exist";
        } 
        else if (authException instanceof BadCredentialsException) {
            message = "Invalid password";
        }

     
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    

	}

}
