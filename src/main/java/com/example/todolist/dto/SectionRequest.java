package com.example.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SectionRequest {

    @NotBlank(message = "Section name is required")
    @Size(min = 1, max = 100, message = "Section name must be between 1 and 100 characters")
    private String name;

    public SectionRequest() {}

    public SectionRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
