package com.example.todolist.service;

import com.example.todolist.dto.TodoRequest;
import com.example.todolist.model.Todo;
import com.example.todolist.repository.TodoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final AuditService auditService;

    public TodoService(TodoRepository todoRepository, AuditService auditService) {
        this.todoRepository = todoRepository;
        this.auditService = auditService;
    }

    // ========== TODO CRUD Operations ==========

    public Page<Todo> getAllTodos(String tenantId, Pageable pageable) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        return todoRepository.findByTenantId(tenantId, pageable);
    }

    public Todo getTodoById(String id, String tenantId) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Todo ID cannot be null or empty");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        return todoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found for tenant"));
    }

    public Todo createTodo(TodoRequest request, String tenantId, String username) {
        validateTodoRequest(request);
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        
        Todo todo = new Todo();
        applyRequestToTodo(todo, request);
        todo.setTenantId(tenantId);
        Todo saved = todoRepository.save(todo);
        auditService.log(username, tenantId, "CREATE", "Todo", saved.getId(), "Created new todo: " + request.getTopic());
        return saved;
    }

    public Todo updateTodo(String id, TodoRequest request, String tenantId, String username) {
        validateTodoRequest(request);
        Todo existing = getTodoById(id, tenantId);
        applyRequestToTodo(existing, request);
        Todo saved = todoRepository.save(existing);
        auditService.log(username, tenantId, "UPDATE", "Todo", saved.getId(), "Updated todo: " + request.getTopic());
        return saved;
    }

    private void applyRequestToTodo(Todo todo, TodoRequest request) {
        todo.setTopic(request.getTopic());
        todo.setSummaryPoints(request.getSummaryPoints());
        todo.setStatus(request.getStatus());
        todo.setPriority(request.getPriority());
        todo.setSection(request.getSection());
    }

    private void validateTodoRequest(TodoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Todo request cannot be null");
        }
        if (request.getTopic() == null || request.getTopic().isBlank()) {
            throw new IllegalArgumentException("Todo topic cannot be null or empty");
        }
        if (request.getSection() == null || request.getSection().isBlank()) {
            throw new IllegalArgumentException("Section cannot be null or empty");
        }
    }

    public void deleteTodo(String id, String tenantId, String username) {
        Todo todo = getTodoById(id, tenantId);
        todoRepository.delete(todo);
        auditService.log(username, tenantId, "DELETE", "Todo", todo.getId(), "Deleted todo: " + todo.getTopic());
    }

    // ========== SECTION Operations ==========

    public List<String> getSections(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        List<String> sections = todoRepository.findDistinctSectionByTenantId(tenantId);
        if (sections == null || sections.isEmpty()) {
            throw new IllegalArgumentException("No sections found for tenant");
        }
        return sections;
    }

    public Page<Todo> getTodosBySection(String tenantId, String section, Pageable pageable) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        if (section == null || section.isBlank()) {
            throw new IllegalArgumentException("Section cannot be null or empty");
        }
        return todoRepository.findByTenantIdAndSection(tenantId, section, pageable);
    }

    public String createSection(String sectionName, String tenantId, String username) {
        if (sectionName == null || sectionName.isBlank()) {
            throw new IllegalArgumentException("Section name cannot be null or empty");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        
        // Check if section already exists
        List<String> existingSections = todoRepository.findDistinctSectionByTenantId(tenantId);
        if (existingSections.contains(sectionName)) {
            throw new IllegalArgumentException("Section already exists: " + sectionName);
        }
        
        auditService.log(username, tenantId, "CREATE", "Section", sectionName, "Created new section");
        return sectionName;
    }

    public String updateSection(String oldSectionName, String newSectionName, String tenantId, String username) {
        if (oldSectionName == null || oldSectionName.isBlank()) {
            throw new IllegalArgumentException("Old section name cannot be null or empty");
        }
        if (newSectionName == null || newSectionName.isBlank()) {
            throw new IllegalArgumentException("New section name cannot be null or empty");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        
        // Verify old section exists
        List<String> existingSections = todoRepository.findDistinctSectionByTenantId(tenantId);
        if (!existingSections.contains(oldSectionName)) {
            throw new IllegalArgumentException("Section not found: " + oldSectionName);
        }
        
        // Check if new section name already exists
        if (!oldSectionName.equals(newSectionName) && existingSections.contains(newSectionName)) {
            throw new IllegalArgumentException("Section already exists: " + newSectionName);
        }
        
        // Update all todos in this section
        Page<Todo> todosInSection = todoRepository.findByTenantIdAndSection(tenantId, oldSectionName, org.springframework.data.domain.Pageable.unpaged());
        for (Todo todo : todosInSection.getContent()) {
            todo.setSection(newSectionName);
            todoRepository.save(todo);
        }
        
        auditService.log(username, tenantId, "UPDATE", "Section", newSectionName, "Renamed section from " + oldSectionName + " to " + newSectionName);
        return newSectionName;
    }

    public void deleteSection(String sectionName, String tenantId, String username) {
        if (sectionName == null || sectionName.isBlank()) {
            throw new IllegalArgumentException("Section name cannot be null or empty");
        }
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        
        // Verify section exists
        List<String> existingSections = todoRepository.findDistinctSectionByTenantId(tenantId);
        if (!existingSections.contains(sectionName)) {
            throw new IllegalArgumentException("Section not found: " + sectionName);
        }
        
        // Delete all todos in this section
        long deletedCount = todoRepository.deleteByTenantIdAndSection(tenantId, sectionName);
        
        auditService.log(username, tenantId, "DELETE", "Section", sectionName, "Deleted section and " + deletedCount + " associated todos");
    }

    public long getSectionTodoCount(String tenantId, String section) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("Tenant ID cannot be null or empty");
        }
        if (section == null || section.isBlank()) {
            throw new IllegalArgumentException("Section cannot be null or empty");
        }
        return todoRepository.countByTenantIdAndSection(tenantId, section);
    }
}
