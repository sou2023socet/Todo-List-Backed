package com.example.todolist.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SectionResponse {

    private String name;
    private long todoCount;
    private String sectionId;

    public SectionResponse() {}

    public SectionResponse(String name) {
        this.name = name;
        this.todoCount = 0;
    }

    public SectionResponse(String name, long todoCount) {
        this.name = name;
        this.todoCount = todoCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getsectionId() {
        return sectionId;
    }

    public void setsectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public long getTodoCount() {
        return todoCount;
    }

    public void setTodoCount(long todoCount) {
        this.todoCount = todoCount;
    }
}
