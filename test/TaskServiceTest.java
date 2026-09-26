import exception.InvalidTaskDataException;
import exception.TaskNotFoundException;
import model.Task;
import model.TaskPriority;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskServiceTest {

    private FakeTaskRepository fakeRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        // قبل از هر تست یک مخزن درون‌حافظه‌ای تمیز و سرویس تازه می‌سازیم
        fakeRepository = new FakeTaskRepository();
        taskService = new TaskService(fakeRepository);
    }

    @Test
    void createTask_validData_shouldCreateTaskSuccessfully() {
        // Arrange
        String title = "Complete Unit Tests";
        String description = "Writing JUnit tests for TaskService";
        TaskPriority priority = TaskPriority.HIGH;

        // Act
        Task createdTask = taskService.createTask(title, description, priority);

        // Assert
        assertNotNull(createdTask);
        assertNotNull(createdTask.getId());
        assertEquals("Complete Unit Tests", createdTask.getTitle());
        assertEquals("Writing JUnit tests for TaskService", createdTask.getDescription());
        assertEquals(TaskPriority.HIGH, createdTask.getPriority());
        assertEquals(TaskStatus.TODO, createdTask.getStatus());
        assertNotNull(createdTask.getCreatedAt());
    }

    @Test
    void createTask_blankTitle_shouldThrowInvalidTaskDataException() {
        // Assert & Act
        assertThrows(InvalidTaskDataException.class, () -> {
            taskService.createTask("   ", "Some description", TaskPriority.LOW);
        });
    }

    @Test
    void createTask_nullTitle_shouldThrowInvalidTaskDataException() {
        // Assert & Act
        assertThrows(InvalidTaskDataException.class, () -> {
            taskService.createTask(null, "Some description", TaskPriority.LOW);
        });
    }

    @Test
    void findById_existingId_shouldReturnTask() {
        // Arrange
        Task task = taskService.createTask("Learn Spring", "Dependency Injection basics", TaskPriority.MEDIUM);

        // Act
        Task found = taskService.findById(task.getId());

        // Assert
        assertEquals(task.getId(), found.getId());
        assertEquals("Learn Spring", found.getTitle());
    }

    @Test
    void findById_nonExistingId_shouldThrowTaskNotFoundException() {
        // Arrange
        UUID randomId = UUID.randomUUID();

        // Assert & Act
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.findById(randomId);
        });
    }

    @Test
    void updateStatus_existingTask_shouldUpdateSuccessfully() {
        // Arrange
        Task task = taskService.createTask("Write Documentation", "Docs for API", TaskPriority.LOW);

        // Act
        taskService.updateStatus(task.getId(), TaskStatus.DONE);
        Task updatedTask = taskService.findById(task.getId());

        // Assert
        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
    }

    @Test
    void deleteTask_existingTask_shouldRemoveTask() {
        // Arrange
        Task task = taskService.createTask("Task to remove", "Will be deleted", TaskPriority.LOW);
        UUID taskId = task.getId();

        // Act
        taskService.deleteTask(taskId);

        // Assert
        assertThrows(TaskNotFoundException.class, () -> {
            taskService.findById(taskId);
        });
    }
}
