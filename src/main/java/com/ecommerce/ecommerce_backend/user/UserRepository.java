package com.ecommerce.ecommerce_backend.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {

	Optional<Users> findByUsername(String username);
	@Query("SELECT u FROM Users u LEFT JOIN FETCH u.authorities")
	List<Users> findAllWithAuthorities();
}
