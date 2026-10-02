import model.Task;
import repository.TaskRepository;

import java.util.*;

public class FakeTaskRepository implements TaskRepository {
    private final Map<UUID, Task> storage = new HashMap<>();

    @Override
    public void save(Task task) {
        storage.put(task.getId(), task);
    }

    @Override
    public Optional<Task> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public void deleteById(UUID id) {
        storage.remove(id);
    }

    @Override
    public List<Task> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void saveTasks() {
        // در تست نیازی به ذخیره روی دیسک نداریم، خالی می‌ماند
    }
}
