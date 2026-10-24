import exception.InvalidTaskDataException;
import exception.TaskNotFoundException;
import model.Task;
import model.TaskPriority;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.TaskService;

import java.time.LocalDate;
import java.util.List;
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
        LocalDate dueDate = LocalDate.now().plusDays(5);

        // Act
        Task createdTask = taskService.createTask(title, description, priority, dueDate);

        // Assert
        assertNotNull(createdTask);
        assertNotNull(createdTask.getId());
        assertEquals(title, createdTask.getTitle());
        assertEquals(description, createdTask.getDescription());
        assertEquals(priority, createdTask.getPriority());
        assertEquals(TaskStatus.TODO, createdTask.getStatus());
        assertEquals(dueDate, createdTask.getDueDate());
        assertNotNull(createdTask.getCreatedAt());
    }

    @Test
    void createTask_blankTitle_shouldThrowInvalidTaskDataException() {
        assertThrows(InvalidTaskDataException.class, () -> {
            taskService.createTask("   ", "Some description", TaskPriority.LOW, LocalDate.now().plusDays(1));
        });
    }

    @Test
    void createTask_nullTitle_shouldThrowInvalidTaskDataException() {
        assertThrows(InvalidTaskDataException.class, () -> {
            taskService.createTask(null, "Some description", TaskPriority.LOW, LocalDate.now().plusDays(1));
        });
    }

    @Test
    void createTask_pastDueDate_shouldThrowInvalidTaskDataException() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertThrows(InvalidTaskDataException.class, () -> {
            taskService.createTask("Old Task", "Description", TaskPriority.MEDIUM, pastDate);
        });
    }

    @Test
    void createTask_todayDueDate_shouldBeAccepted() {
        LocalDate today = LocalDate.now();
        Task task = taskService.createTask("Today Due", "Edge case", TaskPriority.MEDIUM, today);
        assertEquals(today, task.getDueDate());
    }

    @Test
    void findById_existingId_shouldReturnTask() {
        Task task = taskService.createTask("Learn Spring", "DI basics", TaskPriority.MEDIUM, LocalDate.now().plusDays(3));
        Task found = taskService.findById(task.getId());
        assertEquals(task.getId(), found.getId());
        assertEquals("Learn Spring", found.getTitle());
    }

    @Test
    void findById_nonExistingId_shouldThrowTaskNotFoundException() {
        UUID randomId = UUID.randomUUID();
        assertThrows(TaskNotFoundException.class, () -> taskService.findById(randomId));
    }

    @Test
    void updateStatus_existingTask_shouldUpdateSuccessfully() {
        Task task = taskService.createTask("Write Docs", "Docs for API", TaskPriority.LOW, LocalDate.now().plusDays(2));
        taskService.updateStatus(task.getId(), TaskStatus.DONE);
        Task updatedTask = taskService.findById(task.getId());
        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
    }

    @Test
    void deleteTask_existingTask_shouldRemoveTask() {
        Task task = taskService.createTask("Task to remove", "Will be deleted", TaskPriority.LOW, LocalDate.now().plusDays(1));
        UUID taskId = task.getId();
        taskService.deleteTask(taskId);
        assertThrows(TaskNotFoundException.class, () -> taskService.findById(taskId));
    }

    @Test
    void sortByDueDate_shouldPlaceNullsLast_andAscendingByDate() {
        Task t1 = taskService.createTask("A", "desc", TaskPriority.LOW, LocalDate.now().plusDays(5));
        Task t2 = taskService.createTask("B", "desc", TaskPriority.LOW, LocalDate.now().plusDays(2));
        Task t3 = taskService.createTask("C", "desc", TaskPriority.LOW, null); // null dueDate

        List<Task> sorted = taskService.sortByDueDate();

        assertEquals(t2.getId(), sorted.get(0).getId()); // 2 days first
        assertEquals(t1.getId(), sorted.get(1).getId()); // 5 days second
        assertEquals(t3.getId(), sorted.get(2).getId()); // null dueDate last
    }

    @Test
    void getOverdueTasks_shouldReturnNotDoneTasksWithPastDueDate() {
        LocalDate today = LocalDate.now();

        Task overdueTodo = taskService.createTask(
                "Overdue TODO", "desc", TaskPriority.MEDIUM, today
        );
        overdueTodo.setDueDate(today.minusDays(1));

        Task overdueDone = taskService.createTask(
                "Overdue DONE", "desc", TaskPriority.MEDIUM, today
        );
        overdueDone.setDueDate(today.minusDays(2));
        taskService.updateStatus(overdueDone.getId(), TaskStatus.DONE);

        Task futureTodo = taskService.createTask(
                "Future", "desc", TaskPriority.MEDIUM, today.plusDays(3)
        );

        Task todayTodo = taskService.createTask(
                "Today", "desc", TaskPriority.MEDIUM, today
        );

        List<Task> overdue = taskService.getOverdueTasks();

        assertTrue(overdue.stream()
                .anyMatch(task -> task.getId().equals(overdueTodo.getId())));
        assertFalse(overdue.stream()
                .anyMatch(task -> task.getId().equals(overdueDone.getId())));
        assertFalse(overdue.stream()
                .anyMatch(task -> task.getId().equals(futureTodo.getId())));
        assertFalse(overdue.stream()
                .anyMatch(task -> task.getId().equals(todayTodo.getId())));
    }
}