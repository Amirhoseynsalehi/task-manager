import model.Task;
import model.TaskPriority;
import model.TaskStatus;
import repository.FileTaskRepository;
import repository.TaskRepository;
import service.TaskService;
import exception.InvalidTaskDataException;
import exception.TaskNotFoundException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.UUID;

public class Main {

    private static TaskService service;
    private static Scanner scanner;

    public static void main(String[] args) {

        TaskRepository repository = new FileTaskRepository();
        service = new TaskService(repository);
        scanner = new Scanner(System.in);

        while (true) {
            printMenu();
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createTaskUI();
                    break;
                case "2":
                    listTasksUI();
                    break;
                case "3":
                    searchByTitleUI();
                    break;
                case "4":
                    updateStatusUI();
                    break;
                case "5":
                    deleteTaskUI();
                    break;
                case "6":
                    sortByPriorityUI();
                    break;
                case "7":
                    getTasksByStatusUI();
                    break;
                case "8":
                    advancedFilterUI();
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
        System.out.println("2. List Tasks");
        System.out.println("3. Search by Title");
        System.out.println("4. Update Status");
        System.out.println("5. Delete Task");
        System.out.println("6. Sort by Priority");
        System.out.println("7. Search by Status");
        System.out.println("8. Search advance");
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

            Task task = service.createTask(title, description, priority);

            System.out.println("\nTask created successfully.");
            System.out.println(task);

        } catch (InvalidTaskDataException e) {
            System.out.println("Validation Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
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
}