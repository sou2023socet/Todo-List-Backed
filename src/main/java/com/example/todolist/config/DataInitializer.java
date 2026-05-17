package com.example.todolist.config;

import com.example.todolist.model.Priority;
import com.example.todolist.model.Status;
import com.example.todolist.model.Todo;
import com.example.todolist.model.UserAccount;
import com.example.todolist.repository.TodoRepository;
import com.example.todolist.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TodoRepository todoRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, TodoRepository todoRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.todoRepository = todoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create sample users if they don't exist
        if (userRepository.count() == 0) {
            createSampleUsers();
        }

        // Create sample todos if they don't exist
        if (todoRepository.count() == 0) {
            createSampleTodos();
        }
    }

    private void createSampleUsers() {
        List<UserAccount> sampleUsers = Arrays.asList(
            new UserAccount("admin", passwordEncoder.encode("admin123"), "default-tenant", "admin@example.com"),
            new UserAccount("john_doe", passwordEncoder.encode("password123"), "default-tenant", "john.doe@example.com"),
            new UserAccount("jane_smith", passwordEncoder.encode("password123"), "default-tenant", "jane.smith@example.com")
        );

        userRepository.saveAll(sampleUsers);
        System.out.println("Sample users created successfully!");
    }

    private void createSampleTodos() {
        Todo todo1 = new Todo();
        todo1.setTenantId("default-tenant");
        todo1.setTopic("Complete Project Documentation");
        todo1.setSummaryPoints("Write API documentation, Create user guide, Update README files");
        todo1.setStatus(Status.IN_PROGRESS);
        todo1.setPriority(Priority.HIGH);
        todo1.setSection("Work");

        Todo todo2 = new Todo();
        todo2.setTenantId("default-tenant");
        todo2.setTopic("Review Code Changes");
        todo2.setSummaryPoints("Check pull requests, Run tests, Approve merges");
        todo2.setStatus(Status.PENDING);
        todo2.setPriority(Priority.MEDIUM);
        todo2.setSection("Work");

        Todo todo3 = new Todo();
        todo3.setTenantId("default-tenant");
        todo3.setTopic("Grocery Shopping");
        todo3.setSummaryPoints("Buy vegetables, Get milk, Pick up bread");
        todo3.setStatus(Status.PENDING);
        todo3.setPriority(Priority.LOW);
        todo3.setSection("Personal");

        Todo todo4 = new Todo();
        todo4.setTenantId("default-tenant");
        todo4.setTopic("Database Optimization");
        todo4.setSummaryPoints("Analyze query performance, Add indexes, Clean up old data");
        todo4.setStatus(Status.COMPLETED);
        todo4.setPriority(Priority.HIGH);
        todo4.setSection("Work");

        Todo todo5 = new Todo();
        todo5.setTenantId("default-tenant");
        todo5.setTopic("Exercise Routine");
        todo5.setSummaryPoints("Morning run, Weight training, Yoga session");
        todo5.setStatus(Status.IN_PROGRESS);
        todo5.setPriority(Priority.MEDIUM);
        todo5.setSection("Personal");

        List<Todo> sampleTodos = Arrays.asList(todo1, todo2, todo3, todo4, todo5);

        todoRepository.saveAll(sampleTodos);
        System.out.println("Sample todos created successfully!");
    }
}