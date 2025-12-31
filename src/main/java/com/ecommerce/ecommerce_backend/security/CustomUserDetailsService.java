package com.ecommerce.ecommerce_backend.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_backend.user.UserRepository;
import com.ecommerce.ecommerce_backend.user.Users;

import lombok.RequiredArgsConstructor;

	@Service
	@RequiredArgsConstructor
	public class CustomUserDetailsService implements UserDetailsService {
	
		private final UserRepository userRepository;
		
		@Override
		public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
			
			Users user = userRepository.findByUsername(username)
					.orElseThrow(() -> new UsernameNotFoundException(" Username not found for the user :"+ username) );
			List<GrantedAuthority> authorities = user.getAuthorities().stream()
													.map(authority -> new SimpleGrantedAuthority(authority.getName())).collect(Collectors.toList())	;	
			
			return new  User(user.getUsername(),user.getPassword(),authorities);
		}
			
				
		}


