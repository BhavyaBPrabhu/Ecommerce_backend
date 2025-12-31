package com.ecommerce.ecommerce_backend.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ecommerce.ecommerce_backend.constants.ApplicationConstants;
import com.ecommerce.ecommerce_backend.user.AuthUserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTTokenValidatorFilter extends OncePerRequestFilter {

	private final Environment env;

	public JWTTokenValidatorFilter(Environment env) {
		this.env = env;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String authHeader = request.getHeader(ApplicationConstants.JWT_HEADER);
		if (authHeader != null && authHeader.startsWith("Bearer ")
				&& SecurityContextHolder.getContext().getAuthentication() == null) {

			try {

				String jwt = authHeader.substring(7); // remove "Bearer "

				String secret = env.getProperty(ApplicationConstants.JWT_SECRET_KEY,
						ApplicationConstants.JWT_SECRET_DEFAULT_VALUE);
				SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

				Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwt).getBody();
				String username = String.valueOf(claims.get("username"));
				String authorities = String.valueOf(claims.get("authorities"));
				Long userId = Long.valueOf(claims.get("userId").toString());

				AuthUserPrincipal principal = new AuthUserPrincipal(userId, username);

				Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null,
						AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));

				SecurityContextHolder.getContext().setAuthentication(authentication);

			}

			catch (ExpiredJwtException ex) {
				throw new BadCredentialsException("JWT token has expired!", ex);

			} catch (UnsupportedJwtException ex) {
				throw new BadCredentialsException("Unsupported JWT token!", ex);

			} catch (MalformedJwtException ex) {
				throw new BadCredentialsException("Malformed JWT token!", ex);

			} catch (SignatureException ex) {
				throw new BadCredentialsException("Invalid JWT signature!", ex);

			} catch (IllegalArgumentException ex) {
				throw new BadCredentialsException("Invalid JWT token!", ex);

			} catch (Exception ex) {
				throw new BadCredentialsException("Invalid Token received!", ex);
			}
		}

		filterChain.doFilter(request, response);

	}

	// Skip validation for public endpoints (login/register/products/category/error)
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getServletPath();
		if (path == null)
			return false;
		return path.equals("/users/login") || path.equals("/users/register") || path.equals("/error");
	}
}
