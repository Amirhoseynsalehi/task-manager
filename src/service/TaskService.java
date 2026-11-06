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
    if (repository == null) {
        throw new IllegalArgumentException("TaskRepository cannot be null");
    }
    this.taskRepository = repository;
}


    public Task createTask(String title, String description, TaskPriority priority, LocalDate dueDate) {
        if (title == null || title.isBlank()) {
            throw new InvalidTaskDataException("Title cannot be empty");
        }
        if (priority == null) {
            throw new InvalidTaskDataException("Priority cannot be null");
        }
        if (dueDate != null && dueDate.isBefore(LocalDate.now())) {
            throw new InvalidTaskDataException("Due date cannot be before current date");
        }

        Task task = new Task(title, description, priority,  dueDate);
        taskRepository.save(task);

        return task;
    }

    public Task findById(UUID id) {
        if (id == null) {
            throw new InvalidTaskDataException("Task ID cannot be null");
        }
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with id: " + id));
    }

    public Task updateTask(UUID id, String newTitle, String newDescription,
                           TaskPriority newPriority, LocalDate newDueDate, TaskStatus newStatus) {
        Task task = findById(id);

        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new InvalidTaskDataException("Title cannot be empty");
        }
        if (newPriority == null) {
            throw new InvalidTaskDataException("Priority cannot be null");
        }
        if (newStatus == null) {
            throw new InvalidTaskDataException("Status cannot be null");
        }

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
                .toList();
    }

    public List<Task> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        String lowerKeyword = keyword.toLowerCase().trim();
        return taskRepository.findAll()
                .stream()
                .filter(t -> t.getTitle() != null && t.getTitle().toLowerCase().contains(lowerKeyword))
                .toList();
    }

  public List<Task> sortByPriority() {
      return taskRepository.findAll()
              .stream()
              .sorted(Comparator.comparing(
                      Task::getPriority,
                      Comparator.nullsLast(Comparator.naturalOrder())
              ))
              .toList();
  }


    public List<Task> filterTasks(TaskStatus status, TaskPriority priority) {
        return taskRepository.findAll()
                .stream()
                .filter(task -> (status == null || task.getStatus() == status))
                .filter(task -> (priority == null || task.getPriority() == priority))
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
                .toList();
    }

    public List<Task> getOverdueTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findAll()
                .stream()
                .filter(task -> task.getStatus() != TaskStatus.DONE)
                .filter(task -> task.getDueDate() != null && task.getDueDate().isBefore(today))
                .toList();
    }
}
