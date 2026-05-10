package com.invernadero.invernadero_inteligente_backend.modules.users.repository;

import com.invernadero.invernadero_inteligente_backend.modules.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
