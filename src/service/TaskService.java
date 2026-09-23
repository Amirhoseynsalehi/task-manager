package service;

import model.Task;
import model.TaskPriority;
import model.TaskStatus;
import repository.TaskRepository;

import java.util.*;
import java.util.stream.Collectors;

public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository repository) {
        this.taskRepository = repository;
    }

    public Task createTask(String title, String description, TaskPriority priority) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        Task task = new Task(title, description, priority);
        taskRepository.save(task);

        return task;
    }

    public Task findById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Task not found"));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public void deleteTask(UUID id) {
        findById(id);
        taskRepository.deleteById(id);
    }

    public void updateStatus(UUID id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("تسک پیدا نشد"));

        task.setStatus(status);
    }

    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findAll()
                .stream()
                .filter(t -> t.getStatus() == status)
                .collect(Collectors.toList());
    }

    public List<Task> searchByTitle(String keyword) {
        return taskRepository.findAll()
                .stream()
                .filter(t -> t.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Task> sortByPriority() {
        return taskRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Task::getPriority))
                .collect(Collectors.toList());
    }

    public List<Task> filterTasks(TaskStatus status, TaskPriority priority) {

        return taskRepository.findAll()
                .stream()
                .filter(task -> task.getStatus() == status
                        && task.getPriority() == priority)
                .toList();
    }

    public void saveTasks() {
        taskRepository.saveTasks();
    }

}
