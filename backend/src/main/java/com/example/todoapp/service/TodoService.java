package com.example.todoapp.service;

import com.example.todoapp.model.Todo;
import com.example.todoapp.repository.TodoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

    private final TodoRepository todoRepository;

    public TodoService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    public List<Todo> getAllTodos() {
        return todoRepository.findAll();
    }

    public Optional<Todo> getTodoById(Long id) {
        return todoRepository.findById(id);
    }

    public Todo createTodo(Todo todo) {
        return todoRepository.save(todo);
    }

    public Todo updateTodo(Long id, Todo todoDetails) {
        return todoRepository.findById(id)
                .map(todo -> {
                    todo.setTitle(todoDetails.getTitle());
                    todo.setDescription(todoDetails.getDescription());
                    todo.setCompleted(todoDetails.getCompleted());
                    return todoRepository.save(todo);
                })
                .orElseThrow(() -> new RuntimeException("Todo not found: " + id));
    }

    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
    }

    public List<Todo> getTodosByStatus(Boolean completed) {
        return todoRepository.findByCompletedOrderByCreatedAtDesc(completed);
    }
}
