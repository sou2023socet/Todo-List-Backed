package com.example.todolist.dto;

import com.example.todolist.model.Priority;
import com.example.todolist.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TodoRequest {

    @NotBlank
    @Size(max = 200)
    private String topic;

    @NotBlank
    @Size(max = 1000)
    private String summaryPoints;

    @NotNull
    private Status status;

    @NotNull
    private Priority priority;

    @NotBlank
    @Size(max = 100)
    private String section;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSummaryPoints() {
        return summaryPoints;
    }

    public void setSummaryPoints(String summaryPoints) {
        this.summaryPoints = summaryPoints;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }
}
