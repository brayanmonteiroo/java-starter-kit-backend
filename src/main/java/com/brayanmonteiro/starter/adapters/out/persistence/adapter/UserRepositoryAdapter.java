package com.brayanmonteiro.starter.adapters.out.persistence.adapter;

import com.brayanmonteiro.starter.adapters.out.persistence.entity.UserEntity;
import com.brayanmonteiro.starter.adapters.out.persistence.repository.UserJpaRepository;
import com.brayanmonteiro.starter.domain.identity.User;
import com.brayanmonteiro.starter.domain.identity.UserRepositoryPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository repo;

    public UserRepositoryAdapter(UserJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    public User save(User user) {
        UserEntity entity = user.getId() == null ? new UserEntity() :
                repo.findById(user.getId()).orElseGet(UserEntity::new);
        entity.setName(user.getName());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setEmailVerifiedAt(user.getEmailVerifiedAt());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setRoleIds(new HashSet<>(user.getRoleIds()));
        entity = repo.save(entity);
        if (user.getId() == null) {
            user.assignId(entity.getId());
        }
        return toDomain(entity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmailIgnoreCase(email).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repo.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return repo.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    public List<User> findAll() {
        return repo.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<User> findAll(int page, int size) {
        return repo.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size)).stream().map(this::toDomain).toList();
    }

    @Override
    public List<User> findCreatedSince(Instant fromInclusive) {
        return repo.findByCreatedAtGreaterThanEqualOrderByCreatedAtAsc(fromInclusive).stream().map(this::toDomain).toList();
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    private User toDomain(UserEntity e) {
        return new User(e.getId(), e.getName(), e.getEmail(), e.getPasswordHash(), e.getEmailVerifiedAt(),
                e.getCreatedAt(), e.getUpdatedAt(), e.getRoleIds());
    }
}
