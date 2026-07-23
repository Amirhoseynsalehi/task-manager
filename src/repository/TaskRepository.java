package repository;

import model.Task;
import model.TaskPersistence;
import java.util.*;

public class TaskRepository {

    private Map<UUID, Task> tasks = TaskPersistence.loadTasks();

    public void addTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(tasks.get(id));
    }

    public void removeTask(UUID id) {
        tasks.remove(id);
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public Map<UUID, Task> getTaskMap() {
        return tasks; // لازم برای save در Main
    }
}
