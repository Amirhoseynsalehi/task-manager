package service;

import model.Task;
import model.TaskPriority;
import model.TaskStatus;
import repository.TaskRepository;

import java.util.*;
import java.util.stream.Collectors;

public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Task createTask(String title, String description, TaskPriority priority) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        Task task = new Task(title, description, priority);
        repository.addTask(task);

        return task;
    }

    public Task findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
    }

    public List<Task> getAllTasks() {
        return repository.getAllTasks();
    }

    public void deleteTask(UUID id) {
        findById(id);
        repository.removeTask(id);
    }

    public void updateStatus(UUID id, TaskStatus status) {
        Task task = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("تسک پیدا نشد"));

        task.setStatus(status);
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return repository.getAllTasks()
                .stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Task> searchByTitle(String keyword) {
        return repository.getAllTasks()
                .stream()
                .filter(t -> t.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Task> sortByPriority() {
        return repository.getAllTasks()
                .stream()
                .sorted(Comparator.comparing(Task::getPriority))
                .collect(Collectors.toList());
    }

    public List<Task> filterTasks(TaskStatus status, TaskPriority priority) {

        return repository.getAllTasks()
                .stream()
                .filter(task -> task.getStatus() == status
                        && task.getPriority() == priority)
                .toList();
    }


}
