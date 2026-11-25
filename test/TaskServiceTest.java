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

        Task createdTask = taskService.createTask(title, description, priority, dueDate);

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
    void updateTask_nullPriority_shouldThrowInvalidTaskDataException() {

        Task task = taskService.createTask("Test Task", "Desc", TaskPriority.MEDIUM, LocalDate.now().plusDays(1));

        assertThrows(InvalidTaskDataException.class, () -> {
            taskService.updateTask(task.getId(), "Updated Title", "Desc", null, LocalDate.now().plusDays(1), TaskStatus.IN_PROGRESS);
        });
    }

    @Test
    void updateTask_nullStatus_shouldThrowInvalidTaskDataException() {

        Task task = taskService.createTask("Test Task", "Desc", TaskPriority.MEDIUM, LocalDate.now().plusDays(1));

        assertThrows(InvalidTaskDataException.class, () -> {
            taskService.updateTask(task.getId(), "Updated Title", "Desc", TaskPriority.HIGH, LocalDate.now().plusDays(1), null);
        });
    }

    @Test
    void updateTask_nonExistingId_shouldThrowTaskNotFoundException() {

        UUID nonExistingId = UUID.randomUUID();

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.updateTask(
                    nonExistingId,
                    "Updated Title",
                    "Updated Description",
                    TaskPriority.MEDIUM,
                    LocalDate.now().plusDays(2),
                    TaskStatus.IN_PROGRESS
            );
        });
    }



    @Test
    void deleteTask_existingTask_shouldRemoveTask() {
        Task task = taskService.createTask("Task to remove", "Will be deleted", TaskPriority.LOW, LocalDate.now().plusDays(1));
        UUID taskId = task.getId();
        taskService.deleteTask(taskId);
        assertThrows(TaskNotFoundException.class, () -> taskService.findById(taskId));
    }

    @Test
    void deleteTask_nonExistingId_shouldThrowTaskNotFoundException() {
        UUID nonExistingId = UUID.randomUUID();

        assertThrows(TaskNotFoundException.class, () -> {
            taskService.deleteTask(nonExistingId);
        });
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
    void searchByTitle_existingKeyword_shouldReturnMatchingTasks() {
        taskService.createTask("Learn Java Spring", "Backend path", TaskPriority.HIGH, LocalDate.now().plusDays(5));
        taskService.createTask("Learn JUnit", "Unit testing basics", TaskPriority.MEDIUM, LocalDate.now().plusDays(2));
        taskService.createTask("Read Book", "Non matching", TaskPriority.LOW, LocalDate.now().plusDays(1));

        List<Task> results = taskService.searchByTitle("Learn");

        assertEquals(2, results.size());
    }

    @Test
    void searchByTitle_nullOrBlankKeyword_shouldReturnEmptyList() {
        taskService.createTask("Learn Java", "Desc", TaskPriority.HIGH, LocalDate.now().plusDays(2));

        List<Task> resultNull = taskService.searchByTitle(null);
        List<Task> resultBlank = taskService.searchByTitle("   ");

        assertTrue(resultNull.isEmpty());
        assertTrue(resultBlank.isEmpty());
    }

    @Test
    void searchByTitle_caseInsensitive_shouldMatchTasks() {
        taskService.createTask("LEARN DOCKER", "Desc", TaskPriority.MEDIUM, LocalDate.now().plusDays(2));

        List<Task> results = taskService.searchByTitle("learn");

        assertEquals(1, results.size());
        assertEquals("LEARN DOCKER", results.get(0).getTitle());
    }



    @Test
    void getOverdueTasks_shouldReturnOnlyNotDoneTasksWithPastDueDate() {
        LocalDate today = LocalDate.now();

        Task overdueTodo = new Task("Overdue", "Desc", TaskPriority.MEDIUM, today.minusDays(1));
        overdueTodo.setStatus(TaskStatus.TODO);

        Task overdueDone = new Task("Done Past", "Desc", TaskPriority.MEDIUM, today.minusDays(2));
        overdueDone.setStatus(TaskStatus.DONE);

        Task future = new Task("Future", "Desc", TaskPriority.MEDIUM, today.plusDays(3));
        future.setStatus(TaskStatus.TODO);

        Task todayTask = new Task("Today", "Desc", TaskPriority.MEDIUM, today);
        todayTask.setStatus(TaskStatus.TODO);

        fakeRepository.save(overdueTodo);
        fakeRepository.save(overdueDone);
        fakeRepository.save(future);
        fakeRepository.save(todayTask);

        List<Task> result = taskService.getOverdueTasks();

        assertEquals(1, result.size());
        assertTrue(result.contains(overdueTodo));
        assertFalse(result.contains(overdueDone));
        assertFalse(result.contains(future));
        assertFalse(result.contains(todayTask));
    }

    @Test
    void sortByPriority_shouldOrderTasksFromHighToLow() {
        Task low = taskService.createTask("Task Low", "Desc", TaskPriority.LOW, LocalDate.now().plusDays(3));
        Task high = taskService.createTask("Task High", "Desc", TaskPriority.HIGH, LocalDate.now().plusDays(2));
        Task medium = taskService.createTask("Task Medium", "Desc", TaskPriority.MEDIUM, LocalDate.now().plusDays(1));

        List<Task> sorted = taskService.sortByPriority();

        assertEquals(3, sorted.size());
        assertEquals(high.getId(), sorted.get(0).getId());
        assertEquals(medium.getId(), sorted.get(1).getId());
        assertEquals(low.getId(), sorted.get(2).getId());
    }

    @Test
    void filterTasks_matchingStatusAndPriority_shouldReturnOnlyMatchedTasks() {

        Task t1 = taskService.createTask("Task 1", "Desc", TaskPriority.HIGH, LocalDate.now().plusDays(1));
        taskService.updateTask(t1.getId(), t1.getTitle(), t1.getDescription(), t1.getPriority(), t1.getDueDate(), TaskStatus.IN_PROGRESS);

        taskService.createTask("Task 2", "Desc", TaskPriority.HIGH, LocalDate.now().plusDays(1)); // Status: TODO

        Task t3 = taskService.createTask("Task 3", "Desc", TaskPriority.LOW, LocalDate.now().plusDays(1));
        taskService.updateTask(t3.getId(), t3.getTitle(), t3.getDescription(), t3.getPriority(), t3.getDueDate(), TaskStatus.IN_PROGRESS);

        List<Task> filtered = taskService.filterTasks(TaskStatus.IN_PROGRESS, TaskPriority.HIGH);

        assertEquals(1, filtered.size());
        assertEquals(t1.getId(), filtered.get(0).getId());
    }

    @Test
    void filterTasks_nullFilters_shouldReturnAllTasks() {
        taskService.createTask("Task 1", "Desc", TaskPriority.HIGH, LocalDate.now().plusDays(1));
        taskService.createTask("Task 2", "Desc", TaskPriority.LOW, LocalDate.now().plusDays(2));

        List<Task> result = taskService.filterTasks(null, null);

        assertEquals(2, result.size());
    }

    @Test
    void filterTasks_onlyStatusFilter_shouldReturnTasksWithGivenStatus() {
        Task t1 = taskService.createTask("T1", "Desc", TaskPriority.HIGH, LocalDate.now().plusDays(1));
        taskService.updateTask(t1.getId(), t1.getTitle(), t1.getDescription(), t1.getPriority(), t1.getDueDate(), TaskStatus.DONE);

        taskService.createTask("T2", "Desc", TaskPriority.LOW, LocalDate.now().plusDays(1)); // Status: TODO

        List<Task> result = taskService.filterTasks(TaskStatus.DONE, null);

        assertEquals(1, result.size());
        assertEquals(t1.getId(), result.get(0).getId());
    }

    @Test
    void filterTasks_onlyPriorityFilter_shouldReturnTasksWithGivenPriority() {
        Task t1 = taskService.createTask("T1", "Desc", TaskPriority.HIGH, LocalDate.now().plusDays(1));
        taskService.createTask("T2", "Desc", TaskPriority.LOW, LocalDate.now().plusDays(1));

        List<Task> result = taskService.filterTasks(null, TaskPriority.HIGH);

        assertEquals(1, result.size());
        assertEquals(t1.getId(), result.get(0).getId());
    }



}