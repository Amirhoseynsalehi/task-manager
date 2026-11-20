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
    void updateTask_existingTask_shouldUpdateSuccessfully() {

        Task task = taskService.createTask("Write Docs", "Docs for API", TaskPriority.LOW, LocalDate.now().plusDays(2));

        taskService.updateTask(task.getId(), "Updated Title", "Updated Description", TaskPriority.HIGH, LocalDate.now().plusDays(5), TaskStatus.DONE);
        Task updatedTask = taskService.findById(task.getId());

        assertEquals("Updated Title", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(TaskPriority.HIGH, updatedTask.getPriority());
        assertEquals(LocalDate.now().plusDays(5), updatedTask.getDueDate());
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
    void getOverdueTasks_shouldReturnOnlyNotDoneTasksWithPastDueDate() {
        // 1. ARRANGE
        LocalDate today = LocalDate.now();

        Task overdueTodo = new Task("Overdue", "Desc", TaskPriority.MEDIUM, today.minusDays(1));
        overdueTodo.setStatus(TaskStatus.TODO);

        Task overdueDone = new Task("Done Past", "Desc", TaskPriority.MEDIUM, today.minusDays(2));
        overdueDone.setStatus(TaskStatus.DONE); // این باید در خروجی نباشد

        Task future = new Task("Future", "Desc", TaskPriority.MEDIUM, today.plusDays(3));
        future.setStatus(TaskStatus.TODO);

        Task todayTask = new Task("Today", "Desc", TaskPriority.MEDIUM, today);
        todayTask.setStatus(TaskStatus.TODO);

        fakeRepository.save(overdueTodo);
        fakeRepository.save(overdueDone);
        fakeRepository.save(future);
        fakeRepository.save(todayTask);

        // 2. ACT
        List<Task> result = taskService.getOverdueTasks();

        // 3. ASSERT
        assertEquals(1, result.size());
        assertTrue(result.contains(overdueTodo));
        assertFalse(result.contains(overdueDone));
        assertFalse(result.contains(future));
        assertFalse(result.contains(todayTask));
    }
}