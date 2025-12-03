package com.example.todoapp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskAnalysis {
    private Long taskId;
    private String category;
    private String priority; // HIGH, MEDIUM, LOW
    private String recommendation;
}