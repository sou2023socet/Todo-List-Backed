package com.example.todolist.repository;

import com.example.todolist.model.UserAccount;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserAccount, String> {
    Optional<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByUsernameOrEmailAddress(String username, String emailAddress);
    boolean existsByUsernameOrEmailAddress(String username, String emailAddress);
}
