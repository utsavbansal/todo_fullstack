package com.example.todoapp.service;

import com.example.todoapp.dto.AiResponse;
import com.example.todoapp.dto.TaskAnalysis;
import com.example.todoapp.model.Todo;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiService {

    private final WebClient webClient;
    private final Gson gson;

    @Value("${ollama.model}")
    private String model;

    public AiService(@Value("${ollama.base.url}") String ollamaBaseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
        this.gson = new Gson();
    }

    public AiResponse summarizeTasks(List<Todo> todos) {
        String tasksText = buildTasksContext(todos);
        String prompt = "Analyze these tasks and provide a concise summary including: " +
                "total tasks, completed vs incomplete, main themes, and overall progress:\n\n" + tasksText;

        String response = callOllama(prompt);
        return new AiResponse(response, "summary");
    }

    public AiResponse answerQuestion(List<Todo> todos, String question) {
        String tasksText = buildTasksContext(todos);
        String prompt = "Based on these tasks:\n\n" + tasksText +
                "\n\nAnswer this question: " + question;

        String response = callOllama(prompt);
        return new AiResponse(response, "qna");
    }

    public List<TaskAnalysis> categorizeTasks(List<Todo> todos) {
        return todos.stream().map(todo -> {
            String prompt = "Categorize this task into ONE category (Work, Personal, Shopping, Health, Education, or Other): " +
                    "Title: " + todo.getTitle() +
                    (todo.getDescription() != null ? ", Description: " + todo.getDescription() : "") +
                    "\nRespond with ONLY the category name, nothing else.";

            String category = callOllama(prompt).trim();
            return new TaskAnalysis(todo.getId(), category, null, null);
        }).collect(Collectors.toList());
    }

    public List<TaskAnalysis> prioritizeTasks(List<Todo> todos) {
        return todos.stream().map(todo -> {
            String prompt = "Analyze this task and assign a priority (HIGH, MEDIUM, or LOW): " +
                    "Title: " + todo.getTitle() +
                    (todo.getDescription() != null ? ", Description: " + todo.getDescription() : "") +
                    "\nRespond with ONLY: HIGH, MEDIUM, or LOW";

            String priority = callOllama(prompt).trim().toUpperCase();
            if (!priority.matches("HIGH|MEDIUM|LOW")) {
                priority = "MEDIUM";
            }
            return new TaskAnalysis(todo.getId(), null, priority, null);
        }).collect(Collectors.toList());
    }

    public List<TaskAnalysis> getRecommendations(List<Todo> todos) {
        return todos.stream().map(todo -> {
            String prompt = "Provide a brief actionable recommendation (1-2 sentences) for this task: " +
                    "Title: " + todo.getTitle() +
                    (todo.getDescription() != null ? ", Description: " + todo.getDescription() : "");

            String recommendation = callOllama(prompt);
            return new TaskAnalysis(todo.getId(), null, null, recommendation);
        }).collect(Collectors.toList());
    }

    private String buildTasksContext(List<Todo> todos) {
        StringBuilder sb = new StringBuilder();
        for (Todo todo : todos) {
            sb.append("- Task: ").append(todo.getTitle())
                    .append(todo.getDescription() != null ? " (" + todo.getDescription() + ")" : "")
                    .append(" [").append(todo.getCompleted() ? "Completed" : "Pending").append("]\n");
        }
        return sb.toString();
    }

    private String callOllama(String prompt) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);

            String response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);

            if (jsonResponse.has("response")) {
                return jsonResponse.get("response").getAsString();
            } else if (jsonResponse.has("error")) {
                return "Ollama error: " + jsonResponse.get("error").getAsString();
            } else {
                return "Unexpected response from AI service: " + response;
            }

        } catch (Exception e) {
            return "AI service unavailable. Error: " + e.getMessage();
        }
    }

    public String healthCheck() {
        try {
            String r = webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return "Ollama OK: " + r;
        } catch (Exception e) {
            return "Ollama unreachable: " + e.getMessage();
        }
    }



}