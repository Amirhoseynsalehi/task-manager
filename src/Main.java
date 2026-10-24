import model.Task;
import model.TaskPriority;
import model.TaskStatus;
import repository.FileTaskRepository;
import repository.TaskRepository;
import service.TaskService;
import exception.InvalidTaskDataException;
import exception.TaskNotFoundException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static TaskService service;
    private static Scanner scanner;

    public static void main(String[] args) {

        TaskRepository repository = new FileTaskRepository();
        service = new TaskService(repository);
        scanner = new Scanner(System.in);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            service.saveTasks();
        }));

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createTaskUI();
                    break;
                case "2":
                    updateTaskUI();
                    break;
                case "3":
                    listTasksUI();
                    break;
                case "4":
                    searchByTitleUI();
                    break;
                case "5":
                    updateStatusUI();
                    break;
                case "6":
                    deleteTaskUI();
                    break;
                case "7":
                    sortByPriorityUI();
                    break;
                case "8":
                    getTasksByStatusUI();
                    break;
                case "9":
                    advancedFilterUI();
                    break;
                case "10":
                    showOverdueTasksUI();
                    break;
                case "11":
                    sortByDueDateUI();
                    break;
                case "0":
                    service.saveTasks();
                    System.out.println("Tasks saved. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid input! Please try again");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== Task Manager ===");
        System.out.println("1. Create Task");
        System.out.println("2. Update Task");
        System.out.println("3. List Tasks");
        System.out.println("4. Search by Title");
        System.out.println("5. Update Status");
        System.out.println("6. Delete Task");
        System.out.println("7. Sort by Priority");
        System.out.println("8. Search by Status");
        System.out.println("9. Search advance");
        System.out.println("10. Show overdue tasks");
        System.out.println("11. Sort by due date");
        System.out.println("0. Exit");
        System.out.print("\nEnter your choice:");
    }

    // UI Methods

    private static void createTaskUI() {
        try {
            System.out.print("Enter task title: ");
            String title = scanner.nextLine();

            System.out.print("Enter task description: ");
            String description = scanner.nextLine();

            System.out.println("Select task priority: HIGH / MEDIUM / LOW / URGENT");
            String priorityInput = scanner.nextLine().trim().toUpperCase();

            TaskPriority priority;
            try {
                priority = TaskPriority.valueOf(priorityInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid priority! Valid options: HIGH / MEDIUM / LOW / URGENT");
                return;
            }

        System.out.print("Enter due date (YYYY-MM-DD) [Optional, press Enter to skip]: ");
        String dateInput = scanner.nextLine().trim();

        LocalDate dueDate = null;
        if (!dateInput.isEmpty()) {
            try {
                dueDate = LocalDate.parse(dateInput);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format! Please use YYYY-MM-DD (e.g., 2026-11-01).");
                return;
            }
        }

        Task task = service.createTask(title, description, priority, dueDate);

            System.out.println("\nTask created successfully.");
            System.out.println(task);

        } catch (InvalidTaskDataException e) {
            System.out.println("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private static void updateTaskUI() {
        System.out.print("Enter Task ID: ");
        String idInput = scanner.nextLine().trim();

        try {
            UUID id = UUID.fromString(idInput);
            Task current = service.findById(id);

            System.out.println("\n--- Current Task Details ---");
            System.out.println("Title: " + current.getTitle());
            System.out.println("Description: " + current.getDescription());
            System.out.println("Priority: " + current.getPriority());
            System.out.println("Due Date: " + (current.getDueDate() != null ? current.getDueDate() : "None"));
            System.out.println("Status: " + current.getStatus());
            System.out.println("-----------------------------");
            System.out.println("(Press Enter to keep current value)\n");

            // 1. Title
            System.out.printf("New Title [%s]: ", current.getTitle());
            String titleInput = scanner.nextLine().trim();
            String title = titleInput.isEmpty() ? current.getTitle() : titleInput;

            // 2. Description
            System.out.printf("New Description [%s]: ", current.getDescription());
            String descInput = scanner.nextLine().trim();
            String description = descInput.isEmpty() ? current.getDescription() : descInput;

            // 3. Priority
            System.out.printf("New Priority (LOW, MEDIUM, HIGH, URGENT) [%s]: ", current.getPriority());
            String priorityInput = scanner.nextLine().trim().toUpperCase();
            TaskPriority priority = priorityInput.isEmpty()
                    ? current.getPriority()
                    : TaskPriority.valueOf(priorityInput);

            // 4. Due Date
            System.out.printf("New Due Date (YYYY-MM-DD) [%s]: ", (current.getDueDate() != null ? current.getDueDate() : "None"));
            String dueDateInput = scanner.nextLine().trim();
            LocalDate dueDate = dueDateInput.isEmpty()
                    ? current.getDueDate()
                    : LocalDate.parse(dueDateInput);

            // 5. Status
            System.out.printf("New Status (TODO, IN_PROGRESS, DONE) [%s]: ", current.getStatus());
            String statusInput = scanner.nextLine().trim().toUpperCase();
            TaskStatus status = statusInput.isEmpty()
                    ? current.getStatus()
                    : TaskStatus.valueOf(statusInput);

            service.updateTask(id, title, description, priority, dueDate, status);
            System.out.println("✔ Task updated successfully!");

        } catch (IllegalArgumentException | DateTimeParseException e) {
            System.out.println("❌ Invalid input format: " + e.getMessage());
        } catch (InvalidTaskDataException | TaskNotFoundException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }




    private static void listTasksUI() {
        try {
            List<Task> tasks = service.getAllTasks();

            if (tasks.isEmpty()) {
                System.out.println("No tasks currently available.");
            } else {
                System.out.println("\n=== List of all tasks ===");

                for (Task task : tasks) {
                    System.out.println(task);
                }

                System.out.println("------------------------");
            }
        } catch (Exception e) {
            System.out.println("Error displaying task list: " + e.getMessage());
        }
    }

    private static void searchByTitleUI() {
        try {
            System.out.print("Enter a keyword to search in the title:");
            String keyword = scanner.nextLine();

            List<Task> results = service.searchByTitle(keyword);

            if (results.isEmpty()) {
                System.out.println("No tasks found with this title.");
                return;
            }

            System.out.println("\nSearch results:");

            for (Task task : results) {
                System.out.println(task);
            }

        } catch (Exception e) {
            System.out.println("Error during search:" + e.getMessage());
        }
    }

        private static void updateStatusUI() {
        try {
            System.out.print("Enter task ID: ");
            String idInput = scanner.nextLine();

            UUID id = UUID.fromString(idInput);

            System.out.println("Enter new status: TODO / IN_PROGRESS / DONE / CANCELLED");
            String statusInput = scanner.nextLine().trim().toUpperCase();

            TaskStatus status;
            try {
                status = TaskStatus.valueOf(statusInput);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid status! Valid options: TODO / IN_PROGRESS / DONE / CANCELLED");
                return;
            }

            service.updateStatus(id, status);
            System.out.println("Task status updated successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ID format! Please enter a valid UUID.");
        } catch (TaskNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }


    private static void deleteTaskUI() {
        try {
            System.out.print("Enter task ID: ");
            String idInput = scanner.nextLine();

            UUID id = UUID.fromString(idInput);

            service.deleteTask(id);
            System.out.println("Task deleted successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ID format! Please enter a valid UUID.");
        } catch (TaskNotFoundException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }


    private static void sortByPriorityUI() {
        try {
            List<Task> tasks = service.sortByPriority();

            if (tasks.isEmpty()) {
                System.out.println("No tasks found.");
                return;
            }

            System.out.println("\n=== Tasks sorted by priority ===");
            tasks.forEach(System.out::println);
            System.out.println("--------------------------------");
        } catch (Exception e) {
            System.out.println("Error while sorting tasks: " + e.getMessage());
        }
    }


    private static void getTasksByStatusUI() {

        try {
            System.out.println("Enter status: TODO / IN_PROGRESS / DONE / CANCELLED");

            String input = scanner.nextLine().trim().toUpperCase();

            TaskStatus status = TaskStatus.valueOf(input);

            List<Task> tasks = service.getTasksByStatus(status);

            if (tasks.isEmpty()) {
                System.out.println("No tasks found with this status.");
                return;
            }

            System.out.println("Tasks with status " + status + ":");

            tasks.forEach(System.out::println);

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status. Valid options: TODO / IN_PROGRESS / DONE / CANCELLED");
        } catch (Exception e) {
            System.out.println("Error while filtering tasks: " + e.getMessage());
        }
    }

    private static void advancedFilterUI() {

        try {
            System.out.println("Enter status: TODO / IN_PROGRESS / DONE / CANCELLED");
            TaskStatus status = TaskStatus.valueOf(scanner.nextLine().trim().toUpperCase());

            System.out.println("Enter priority: HIGH / MEDIUM / LOW /  URGENT");
            TaskPriority priority = TaskPriority.valueOf(scanner.nextLine().trim().toUpperCase());

            List<Task> tasks = service.filterTasks(status, priority);

            if (tasks.isEmpty()) {
                System.out.println("No tasks found with given filters.");
                return;
            }

            System.out.println("Filtered tasks:");

            tasks.forEach(System.out::println);

        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status or priority.");
        } catch (Exception e) {
            System.out.println("Error while filtering tasks: " + e.getMessage());
        }
    }

    private static void showOverdueTasksUI() {
        System.out.println("=== Overdue Tasks (NOT DONE, dueDate before today) ===");
        var tasks = service.getOverdueTasks();
        if (tasks.isEmpty()) {
            System.out.println("No overdue tasks found.");
            return;
        }
        tasks.forEach(System.out::println);
    }

    private static void sortByDueDateUI() {
        System.out.println("=== Tasks Sorted by Due Date (nulls last) ===");
        var tasks = service.sortByDueDate();
        if (tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }
        tasks.forEach(System.out::println);
    }

}