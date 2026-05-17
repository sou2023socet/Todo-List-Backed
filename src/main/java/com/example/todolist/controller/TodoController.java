package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponse;
import com.example.todolist.dto.PageResponse;
import com.example.todolist.dto.TodoRequest;
import com.example.todolist.model.Todo;
import com.example.todolist.model.UserAccount;
import com.example.todolist.service.AuthService;
import com.example.todolist.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todo")
public class TodoController {

    private static final Logger logger = LoggerFactory.getLogger(TodoController.class);

    private final TodoService todoService;
    private final AuthService authService;

    public TodoController(TodoService todoService, AuthService authService) {
        this.todoService = todoService;
        this.authService = authService;
    }

    private String tenantIdFrom(Authentication authentication) {
        UserAccount user = authService.findByUsername(authentication.getName());
        return user.getTenantId();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTodos(Authentication authentication,
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(defaultValue = "false") boolean array) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Retrieving todos for tenant: {} - Page: {}, Size: {}, Array format: {}", tenantId, pageable.getPageNumber(), pageable.getPageSize(), array);
        var todoPage = todoService.getAllTodos(tenantId, pageable);
        logger.debug("Retrieved {} todos for tenant: {}", todoPage.getTotalElements(), tenantId);
        if (array) {
            return ResponseEntity.ok(ApiResponse.success(todoPage.getContent(), "Todos retrieved"));
        }
        var response = new PageResponse<>(todoPage.getContent(), todoPage.getNumber(), todoPage.getSize(), todoPage.getTotalElements(), todoPage.getTotalPages(), todoPage.isLast());
        return ResponseEntity.ok(ApiResponse.success(response, "Todos retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> getTodoById(@PathVariable String id, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Retrieving todo with ID: {} for tenant: {}", id, tenantId);
        Todo todo = todoService.getTodoById(id, tenantId);
        logger.debug("Retrieved todo: {} - {}", todo.getId(), todo.getTopic());
        return ResponseEntity.ok(ApiResponse.success(todo, "Todo retrieved"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Todo>> createTodo(@Valid @RequestBody TodoRequest request, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Creating new todo for tenant: {} - Topic: {}", tenantId, request.getTopic());
        Todo todo = new Todo();
        todo.setTopic(request.getTopic());
        todo.setSummaryPoints(request.getSummaryPoints());
        todo.setStatus(request.getStatus());
        todo.setPriority(request.getPriority());
        todo.setSection(request.getSection());
        Todo createdTodo = todoService.createTodo(todo, tenantId, authentication.getName());
        logger.info("Todo created successfully with ID: {} for tenant: {}", createdTodo.getId(), tenantId);
        return ResponseEntity.ok(ApiResponse.success(createdTodo, "Todo created"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> updateTodo(@PathVariable String id, @Valid @RequestBody TodoRequest request, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Updating todo with ID: {} for tenant: {} - New topic: {}", id, tenantId, request.getTopic());
        Todo todo = new Todo();
        todo.setTopic(request.getTopic());
        todo.setSummaryPoints(request.getSummaryPoints());
        todo.setStatus(request.getStatus());
        todo.setPriority(request.getPriority());
        todo.setSection(request.getSection());
        Todo updatedTodo = todoService.updateTodo(id, todo, tenantId, authentication.getName());
        logger.info("Todo updated successfully with ID: {} for tenant: {}", updatedTodo.getId(), tenantId);
        return ResponseEntity.ok(ApiResponse.success(updatedTodo, "Todo updated"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteTodo(@PathVariable String id, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Deleting todo with ID: {} for tenant: {}", id, tenantId);
        todoService.deleteTodo(id, tenantId, authentication.getName());
        logger.info("Todo deleted successfully with ID: {} for tenant: {}", id, tenantId);
        return ResponseEntity.ok(ApiResponse.success(null, "Todo deleted"));
    }

    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<String>>> getSections(Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.debug("Retrieving sections for tenant: {}", tenantId);
        List<String> sections = todoService.getSections(tenantId);
        logger.debug("Retrieved {} sections for tenant: {}", sections.size(), tenantId);
        return ResponseEntity.ok(ApiResponse.success(sections, "Sections retrieved"));
    }

    @GetMapping("/section/{section}")
    public ResponseEntity<ApiResponse<?>> getBySection(@PathVariable String section,
        Authentication authentication,
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(defaultValue = "false") boolean array) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Retrieving todos by section: {} for tenant: {} - Page: {}, Size: {}", section, tenantId, pageable.getPageNumber(), pageable.getPageSize());
        var todoPage = todoService.getTodosBySection(tenantId, section, pageable);
        logger.debug("Retrieved {} todos in section: {} for tenant: {}", todoPage.getTotalElements(), section, tenantId);
        if (array) {
            return ResponseEntity.ok(ApiResponse.success(todoPage.getContent(), "Todos by section retrieved"));
        }
        var response = new PageResponse<>(todoPage.getContent(), todoPage.getNumber(), todoPage.getSize(), todoPage.getTotalElements(), todoPage.getTotalPages(), todoPage.isLast());
        return ResponseEntity.ok(ApiResponse.success(response, "Todos by section retrieved"));
    }
}
