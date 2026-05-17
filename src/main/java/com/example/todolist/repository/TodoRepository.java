package com.example.todolist.repository;

import com.example.todolist.model.Todo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface TodoRepository extends MongoRepository<Todo, String> {
    Page<Todo> findByTenantId(String tenantId, Pageable pageable);
    Optional<Todo> findByIdAndTenantId(String id, String tenantId);
    Page<Todo> findByTenantIdAndSection(String tenantId, String section, Pageable pageable);
    List<String> findDistinctSectionByTenantId(String tenantId);
}
