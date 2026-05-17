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

    public Page<Todo> getAllTodos(String tenantId, Pageable pageable) {
        return todoRepository.findByTenantId(tenantId, pageable);
    }

    public Todo getTodoById(String id, String tenantId) {
        return todoRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Todo not found for tenant"));
    }

    public Todo createTodo(TodoRequest request, String tenantId, String username) {
        Todo todo = new Todo();
        applyRequestToTodo(todo, request);
        todo.setTenantId(tenantId);
        Todo saved = todoRepository.save(todo);
        auditService.log(username, tenantId, "CREATE", "Todo", saved.getId(), "Created new todo");
        return saved;
    }

    public Todo updateTodo(String id, TodoRequest request, String tenantId, String username) {
        Todo existing = getTodoById(id, tenantId);
        applyRequestToTodo(existing, request);
        Todo saved = todoRepository.save(existing);
        auditService.log(username, tenantId, "UPDATE", "Todo", saved.getId(), "Updated todo fields");
        return saved;
    }

    private void applyRequestToTodo(Todo todo, TodoRequest request) {
        todo.setTopic(request.getTopic());
        todo.setSummaryPoints(request.getSummaryPoints());
        todo.setStatus(request.getStatus());
        todo.setPriority(request.getPriority());
        todo.setSection(request.getSection());
    }

    public void deleteTodo(String id, String tenantId, String username) {
        Todo todo = getTodoById(id, tenantId);
        todoRepository.delete(todo);
        auditService.log(username, tenantId, "DELETE", "Todo", todo.getId(), "Deleted todo");
    }

    public Page<Todo> getTodosBySection(String tenantId, String section, Pageable pageable) {
        return todoRepository.findByTenantIdAndSection(tenantId, section, pageable);
    }

    public List<String> getSections(String tenantId) {
        return todoRepository.findDistinctSectionByTenantId(tenantId);
    }
}
