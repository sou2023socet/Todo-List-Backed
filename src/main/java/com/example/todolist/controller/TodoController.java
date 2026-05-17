package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponse;
import com.example.todolist.dto.PageResponse;
import com.example.todolist.dto.SectionRequest;
import com.example.todolist.dto.SectionResponse;
import com.example.todolist.dto.TodoRequest;
import com.example.todolist.mapper.SectionMapper;
import com.example.todolist.model.Todo;
import com.example.todolist.model.UserAccount;
import com.example.todolist.security.UserPrincipal;
import com.example.todolist.service.AuthService;
import com.example.todolist.service.TodoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
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
    private final SectionMapper sectionMapper;

    public TodoController(TodoService todoService, AuthService authService, SectionMapper sectionMapper) {
        this.todoService = todoService;
        this.authService = authService;
        this.sectionMapper = sectionMapper;
    }

    private String tenantIdFrom(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getTenantId();
        }

        Object details = authentication.getDetails();
        if (details instanceof String tenantId && !tenantId.isBlank()) {
            return tenantId;
        }

        UserAccount user = authService.findByUsername(authentication.getName());
        return user.getTenantId();
    }

    // ========== TODO CRUD Endpoints ==========

    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllTodos(Authentication authentication,
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(defaultValue = "false") boolean array) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Retrieving todos for tenant: {} - Page: {}, Size: {}, Array format: {}", tenantId, pageable.getPageNumber(), pageable.getPageSize(), array);
        try {
            var todoPage = todoService.getAllTodos(tenantId, pageable);
            logger.debug("Retrieved {} todos for tenant: {}", todoPage.getTotalElements(), tenantId);
            if (array) {
                return ResponseEntity.ok(ApiResponse.success(todoPage.getContent(), "Todos retrieved successfully"));
            }
            var response = new PageResponse<>(todoPage.getContent(), todoPage.getNumber(), todoPage.getSize(), todoPage.getTotalElements(), todoPage.getTotalPages(), todoPage.isLast());
            return ResponseEntity.ok(ApiResponse.success(response, "Todos retrieved successfully"));
        } catch (Exception e) {
            logger.error("Error retrieving todos for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error retrieving todos: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> getTodoById(@PathVariable String id, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Retrieving todo with ID: {} for tenant: {}", id, tenantId);
        try {
            Todo todo = todoService.getTodoById(id, tenantId);
            logger.debug("Retrieved todo: {} - {}", todo.getId(), todo.getTopic());
            return ResponseEntity.ok(ApiResponse.success(todo, "Todo retrieved successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Todo not found: {} for tenant: {}", id, tenantId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error retrieving todo: {} for tenant: {}", id, tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error retrieving todo: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Todo>> createTodo(@Valid @RequestBody TodoRequest request, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Creating new todo for tenant: {} - Topic: {}", tenantId, request.getTopic());
        try {
            Todo createdTodo = todoService.createTodo(request, tenantId, authentication.getName());
            logger.info("Todo created successfully with ID: {} for tenant: {}", createdTodo.getId(), tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdTodo, "Todo created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid todo request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error creating todo for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error creating todo: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Todo>> updateTodo(@PathVariable String id, @Valid @RequestBody TodoRequest request, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Updating todo with ID: {} for tenant: {} - New topic: {}", id, tenantId, request.getTopic());
        try {
            Todo updatedTodo = todoService.updateTodo(id, request, tenantId, authentication.getName());
            logger.info("Todo updated successfully with ID: {} for tenant: {}", updatedTodo.getId(), tenantId);
            return ResponseEntity.ok(ApiResponse.success(updatedTodo, "Todo updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid todo update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error updating todo: {} for tenant: {}", id, tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error updating todo: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteTodo(@PathVariable String id, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Deleting todo with ID: {} for tenant: {}", id, tenantId);
        try {
            todoService.deleteTodo(id, tenantId, authentication.getName());
            logger.info("Todo deleted successfully with ID: {} for tenant: {}", id, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Todo deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Todo not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error deleting todo: {} for tenant: {}", id, tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error deleting todo: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    // ========== SECTION CRUD Endpoints ==========

    @GetMapping("/sections")
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getSections(Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Retrieving sections for tenant: {}", tenantId);
        try {
            List<String> sections = todoService.getSections(tenantId);
            List<SectionResponse> responses = sectionMapper.toResponseList(sections);
            logger.debug("Retrieved {} sections for tenant: {}", responses.size(), tenantId);
            return ResponseEntity.ok(ApiResponse.success(responses, "Sections retrieved successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("No sections found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error retrieving sections for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error retrieving sections: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    @PostMapping("/sections")
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@Valid @RequestBody SectionRequest request, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Creating new section for tenant: {} - Name: {}", tenantId, request.getName());
        try {
            String sectionName = todoService.createSection(request.getName(), tenantId, authentication.getName());
            SectionResponse response = sectionMapper.toResponse(sectionName);
            logger.info("Section created successfully: {} for tenant: {}", sectionName, tenantId);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Section created successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid section request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error creating section for tenant: {}", tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error creating section: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    @PutMapping("/sections/{oldName}")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(
            @PathVariable String oldName,
            @Valid @RequestBody SectionRequest request,
            Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Updating section for tenant: {} - Old name: {}, New name: {}", tenantId, oldName, request.getName());
        try {
            String updatedName = todoService.updateSection(oldName, request.getName(), tenantId, authentication.getName());
            SectionResponse response = sectionMapper.toResponse(updatedName);
            logger.info("Section updated successfully: {} -> {} for tenant: {}", oldName, updatedName, tenantId);
            return ResponseEntity.ok(ApiResponse.success(response, "Section updated successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid section update request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error updating section: {} for tenant: {}", oldName, tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error updating section: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    @DeleteMapping("/sections/{name}")
    public ResponseEntity<ApiResponse<Object>> deleteSection(@PathVariable String name, Authentication authentication) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Deleting section for tenant: {} - Name: {}", tenantId, name);
        try {
            todoService.deleteSection(name, tenantId, authentication.getName());
            logger.info("Section deleted successfully: {} for tenant: {}", name, tenantId);
            return ResponseEntity.ok(ApiResponse.success(null, "Section deleted successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Section not found for deletion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error deleting section: {} for tenant: {}", name, tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error deleting section: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }

    @GetMapping("/section/{section}")
    public ResponseEntity<ApiResponse<?>> getBySection(@PathVariable String section,
        Authentication authentication,
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(defaultValue = "false") boolean array) {
        String tenantId = tenantIdFrom(authentication);
        logger.info("Retrieving todos by section: {} for tenant: {} - Page: {}, Size: {}", section, tenantId, pageable.getPageNumber(), pageable.getPageSize());
        try {
            var todoPage = todoService.getTodosBySection(tenantId, section, pageable);
            logger.debug("Retrieved {} todos in section: {} for tenant: {}", todoPage.getTotalElements(), section, tenantId);
            if (array) {
                return ResponseEntity.ok(ApiResponse.success(todoPage.getContent(), "Todos by section retrieved successfully"));
            }
            var response = new PageResponse<>(todoPage.getContent(), todoPage.getNumber(), todoPage.getSize(), todoPage.getTotalElements(), todoPage.getTotalPages(), todoPage.isLast());
            return ResponseEntity.ok(ApiResponse.success(response, "Todos by section retrieved successfully"));
        } catch (IllegalArgumentException e) {
            logger.warn("Error retrieving todos by section: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(400, e.getMessage(), null, java.time.Instant.now().toString()));
        } catch (Exception e) {
            logger.error("Error retrieving todos by section: {} for tenant: {}", section, tenantId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Error retrieving todos by section: " + e.getMessage(), null, java.time.Instant.now().toString()));
        }
    }
}
