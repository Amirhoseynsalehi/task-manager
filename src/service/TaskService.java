package service;

import exception.InvalidTaskDataException;
import exception.TaskNotFoundException;
import model.Task;
import model.TaskPriority;
import model.TaskStatus;
import repository.TaskRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository repository) {
        this.taskRepository = repository;
    }

    public Task createTask(String title, String description, TaskPriority priority, LocalDate dueDate) {
        if (title == null || title.isBlank()) {
            throw new InvalidTaskDataException("Title cannot be empty");
        }

        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new InvalidTaskDataException("Due date cannot be before current date");
        }

        Task task = new Task(title, description, priority,  dueDate);
        taskRepository.save(task);

        return task;
    }

    public Task findById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    public Task updateTask(UUID id, String newTitle, String newDescription,
                           TaskPriority newPriority, LocalDate newDueDate, TaskStatus newStatus) {
        Task task = findById(id);

        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new InvalidTaskDataException("Title cannot be empty");
        }

        // اگر تاریخ تغییر کرده و تاریخ جدید به گذشته اشاره دارد، ارور می‌دهد
        if (newDueDate != null && !newDueDate.equals(task.getDueDate()) && newDueDate.isBefore(LocalDate.now())) {
            throw new InvalidTaskDataException("Due date cannot be before current date");
        }

        task.setTitle(newTitle.trim());
        task.setDescription(newDescription != null ? newDescription.trim() : "");
        task.setPriority(newPriority);
        task.setDueDate(newDueDate);
        task.setStatus(newStatus);

        taskRepository.save(task);
        return task;
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
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));

        task.setStatus(status);
        taskRepository.save(task);
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

    public List<Task> sortByDueDate() {
        return taskRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(
                        Task::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ))
                .collect(Collectors.toList());
    }

    public List<Task> getOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findAll()
                .stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(today))
                .collect(Collectors.toList());
    }
}
