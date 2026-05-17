package com.example.todolist.controller;

import com.example.todolist.dto.ApiResponse;
import com.example.todolist.dto.PageResponse;
import com.example.todolist.model.Todo;
import com.example.todolist.security.UserPrincipal;
import com.example.todolist.model.UserAccount;
import com.example.todolist.service.AuthService;
import com.example.todolist.service.TodoService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FrontendTodoController {

    private final TodoService todoService;
    private final AuthService authService;

    public FrontendTodoController(TodoService todoService, AuthService authService) {
        this.todoService = todoService;
        this.authService = authService;
    }

    private String tenantIdFrom(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getTenantId();
        }

        UserAccount user = authService.findByUsername(authentication.getName());
        return user.getTenantId();
    }

    @GetMapping("/angular/todo")
    public ResponseEntity<ApiResponse<?>> listAngularTodos(Authentication authentication,
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(defaultValue = "false") boolean array) {
        String tenantId = tenantIdFrom(authentication);
        var todoPage = todoService.getAllTodos(tenantId, pageable);
        if (array) {
            return ResponseEntity.ok(ApiResponse.success(todoPage.getContent(), "Angular todo payload"));
        }
        var response = new PageResponse<>(todoPage.getContent(), todoPage.getNumber(), todoPage.getSize(), todoPage.getTotalElements(), todoPage.getTotalPages(), todoPage.isLast());
        return ResponseEntity.ok(ApiResponse.success(response, "Angular todo payload"));
    }

    @GetMapping("/react/todo")
    public ResponseEntity<ApiResponse<?>> listReactTodos(Authentication authentication,
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(defaultValue = "false") boolean array) {
        String tenantId = tenantIdFrom(authentication);
        var todoPage = todoService.getAllTodos(tenantId, pageable);
        if (array) {
            return ResponseEntity.ok(ApiResponse.success(todoPage.getContent(), "React todo payload"));
        }
        var response = new PageResponse<>(todoPage.getContent(), todoPage.getNumber(), todoPage.getSize(), todoPage.getTotalElements(), todoPage.getTotalPages(), todoPage.isLast());
        return ResponseEntity.ok(ApiResponse.success(response, "React todo payload"));
    }

    @GetMapping(value = "/todo", headers = "X-Frontend-Client")
    public ResponseEntity<ApiResponse<?>> listTodosByHeader(Authentication authentication,
        @RequestHeader(value = "X-Frontend-Client", required = true) String frontend,
        @PageableDefault(page = 0, size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
        @RequestParam(defaultValue = "false") boolean array) {
        String tenantId = tenantIdFrom(authentication);
        var todoPage = todoService.getAllTodos(tenantId, pageable);
        if (array) {
            return ResponseEntity.ok(ApiResponse.success(todoPage.getContent(), String.format("Todos retrieved for %s client", frontend)));
        }
        var response = new PageResponse<>(todoPage.getContent(), todoPage.getNumber(), todoPage.getSize(), todoPage.getTotalElements(), todoPage.getTotalPages(), todoPage.isLast());
        String message = String.format("Todos retrieved for %s client", frontend);
        return ResponseEntity.ok(ApiResponse.success(response, message));
    }
}
