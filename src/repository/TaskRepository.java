package repository;

import model.Task;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskRepository {
    void save(Task task);
    Optional<Task> findById(UUID id);
    void deleteById(UUID id);
    List<Task> findAll();
    void saveTasks(); // برای ذخیره‌سازی وضعیت
}
