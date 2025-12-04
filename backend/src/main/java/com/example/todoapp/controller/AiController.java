package com.example.todoapp.controller;

import com.example.todoapp.dto.AiRequest;
import com.example.todoapp.dto.AiResponse;
import com.example.todoapp.dto.TaskAnalysis;
import com.example.todoapp.model.Todo;
import com.example.todoapp.service.AiService;
import com.example.todoapp.service.TodoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
//@CrossOrigin(origins = "*")
public class AiController {

    private final AiService aiService;
    private final TodoService todoService;

    public AiController(AiService aiService, TodoService todoService) {
        this.aiService = aiService;
        this.todoService = todoService;
    }

    @GetMapping("/summarize")
    public ResponseEntity<AiResponse> summarizeTasks() {
        List<Todo> todos = todoService.getAllTodos();
        AiResponse response = aiService.summarizeTasks(todos);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/question")
    public ResponseEntity<AiResponse> askQuestion(@RequestBody AiRequest request) {
        List<Todo> todos = todoService.getAllTodos();
        AiResponse response = aiService.answerQuestion(todos, request.getQuestion());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categorize")
    public ResponseEntity<List<TaskAnalysis>> categorizeTasks() {
        List<Todo> todos = todoService.getAllTodos();
        List<TaskAnalysis> analysis = aiService.categorizeTasks(todos);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/prioritize")
    public ResponseEntity<List<TaskAnalysis>> prioritizeTasks() {
        List<Todo> todos = todoService.getAllTodos();
        List<TaskAnalysis> analysis = aiService.prioritizeTasks(todos);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/recommend")
    public ResponseEntity<List<TaskAnalysis>> getRecommendations() {
        List<Todo> todos = todoService.getAllTodos();
        List<TaskAnalysis> analysis = aiService.getRecommendations(todos);
        return ResponseEntity.ok(analysis);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        String status = aiService.healthCheck();
        if (status.startsWith("Ollama OK")) {
            return ResponseEntity.ok(status);
        } else {
            return ResponseEntity.status(500).body(status);
        }
    }


}