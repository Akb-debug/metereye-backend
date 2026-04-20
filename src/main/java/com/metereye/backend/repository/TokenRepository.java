// TokenRepository.java
package com.metereye.backend.repository;

import com.metereye.backend.entity.Token;
import com.metereye.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByToken(String token);
    List<Token> findAllByUserAndRevokedFalseAndExpiredFalse(User user);
}
