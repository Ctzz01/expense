package com.auth.repository;

import com.auth.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailAndApplicationId(String email, String applicationId);

    Optional<User> findByPhoneNumberAndApplicationId(String phoneNumber, String applicationId);

    Optional<User> findByIdAndApplicationId(Long id, String applicationId);

    boolean existsByEmailAndApplicationId(String email, String applicationId);

    boolean existsByPhoneNumberAndApplicationId(String phoneNumber, String applicationId);
}
