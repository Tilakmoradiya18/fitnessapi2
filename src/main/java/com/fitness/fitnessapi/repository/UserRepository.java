// UserRepository.java
package com.fitness.fitnessapi.repository;

import com.fitness.fitnessapi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
