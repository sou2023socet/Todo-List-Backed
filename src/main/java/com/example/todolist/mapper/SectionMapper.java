package com.example.todolist.mapper;

import com.example.todolist.dto.SectionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SectionMapper {

    public SectionResponse toResponse(String name) {
        return new SectionResponse(name);
    }

    public SectionResponse toResponse(String name, long todoCount) {
        return new SectionResponse(name, todoCount);
    }

    public List<SectionResponse> toResponseList(List<String> names) {
        return names.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<SectionResponse> toResponseListWithCounts(List<String> names, java.util.Map<String, Long> countMap) {
        return names.stream()
                .map(name -> new SectionResponse(name, countMap.getOrDefault(name, 0L)))
                .collect(Collectors.toList());
    }
}
