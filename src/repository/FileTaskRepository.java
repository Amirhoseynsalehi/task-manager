package repository;

import model.Task;
import java.util.*;

public class FileTaskRepository implements TaskRepository {

    private final Map<UUID, Task> tasks = TaskPersistence.loadTasks();

    @Override
    public void save(Task task) {
        tasks.put(task.getId(), task);
        saveTasks();
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(tasks.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        tasks.remove(id);
        saveTasks();
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void saveTasks() {
        TaskPersistence.saveTasks(this.tasks);
    }
}
