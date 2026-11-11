package com.brayanmonteiro.starter.domain.identity;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {
    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<User> findAll();
    List<User> findAll(int page, int size);
    List<User> findCreatedSince(Instant fromInclusive);
    long count();
    void deleteById(Long id);
}
