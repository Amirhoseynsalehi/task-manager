TaskManager
Java JUnit 5 Architecture License

📌 Project Overview
TaskManager is a complete Core Java CLI application that applies Clean Architecture, SOLID principles, and modern Java features — including the Stream API, Lambdas, Optional, and the java.time API — with a robust JUnit 5 unit-testing suite.

It is a real-world portfolio project demonstrating how to structure a non-framework Java application in well-separated layers with a fully testable service layer and persistent storage.

🏛️ Architecture & Design Principles
Layered Architecture
| Layer | Package(s) | Responsibility |
| :--- | :--- | :--- |
| **Presentation** | `Main.java` | CLI menu, user input/output, error messaging |
| **Service (Business Logic)** | `service.TaskService` | Business rules, validation, filtering, sorting, overdue tracking |
| **Repository (Abstraction & Persistence)** | `repository.TaskRepository`, `repository.FileTaskRepository`, `repository.TaskPersistence` | Storage abstraction and Java-Serialization-based persistence |
| **Domain / Model (Rich Model)** | `model.Task`, `model.TaskStatus`, `model.TaskPriority` | Rich domain model (Serializable), Enums, identity via UUID |
| **Exception** | `exception.InvalidTaskDataException`, `exception.TaskNotFoundException` | Domain-specific, self-documenting exceptions |

### SOLID Principles Mapping
- **S — Single Responsibility:** `Main` handles only the CLI, `TaskService` only business logic, `TaskPersistence` only file I/O, and `FileTaskRepository` only in-memory + delegated persistence.
- **O — Open/Closed:** New repositories (e.g., database-backed) can be added without modifying `TaskService`; new filtering/sorting strategies extend via `Comparator` and Stream pipelines.
- **L — Liskov Substitution:** `FileTaskRepository` and `FakeTaskRepository` are fully substitutable for `TaskRepository`. `FileTaskRepository.findAll()` returns defensive copies (`new Task(task)`) to protect stored state.
- **I — Interface Segregation:** `TaskRepository` is a small, focused interface (`save`, `findById`, `deleteById`, `findAll`, `saveTasks`) — no bloated contracts.
- **D — Dependency Inversion ⭐:** `TaskService` depends only on the `TaskRepository` interface, never on a concrete implementation. This allows seamless switching between `FileTaskRepository` (production) and `FakeTaskRepository` (in-memory Test Double) with a single constructor argument — enabling fast, isolated unit tests.

🧩 Domain Model & Features
Task Entity
| Field | Type | Notes |
| :--- | :--- | :--- |
| `id` | `UUID` | Auto-generated identity (`UUID.randomUUID()`), used for `equals`/`hashCode` |
| `title` | `String` | Required (non-blank) |
| `description` | `String` | Optional (defaults to `""` if null on update) |
| `status` | `TaskStatus` enum | `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED` |
| `priority` | `TaskPriority` enum | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| `createdAt` | `LocalDateTime` | Set automatically on creation |
| `dueDate` | `LocalDate` | Optional (can be cleared by passing `null` on update) |

The entity implements `Serializable` (`serialVersionUID = 1L`) for robust binary persistence.

### Validation Rules
- **Title:** Must be non-blank on both creation and update (`InvalidTaskDataException`).
- **Priority & Status:** Must not be `null` when creating/updating.
- **Due Date:** When provided on creation, must not be in the past (`dueDate >= today`). On update, a past date is rejected only if it changes the stored value (passing `null` clears the due date).
- **IDs:** `findById` or `deleteById` with a non-existent or null ID throws `TaskNotFoundException` or `InvalidTaskDataException`.

### Operations
- ✅ **Full CRUD:** Create, Read (find all / by ID), Update (full-replace fields), Delete.
- 🔍 **Advanced Filtering:** By status, priority, combined (status + priority), and keyword search in titles (case-insensitive, null/safe).
- ↕️ **Sorting:** By priority and by due date with a null-safe, nulls-last comparator.
- ⏰ **Overdue Task Tracking:** Tasks that are not `DONE` and whose `dueDate` is strictly before today.

### Persistence & Robustness
- **Java Serialization:** Stored in `tasks.dat` via `TaskPersistence`.
- **Corrupt-File Recovery:** If the data file cannot be read, it is automatically backed up to `tasks.dat.bak` and the application starts with a clean state instead of crashing.
- **Graceful Shutdown:** A JVM Shutdown Hook (`Runtime.getRuntime().addShutdownHook(...)`) calls `service.saveTasks()`, ensuring data is persisted even on unexpected exits (e.g., `Ctrl+C`).

📁 Project Directory Structure
```text
TaskManager/
├── .gitignore
├── TaskManager.iml                # IntelliJ IDEA module configuration
├── lib/                           # JUnit 5 runtime dependencies (v5.8.1)
│   ├── apiguardian-api-1.1.2.jar
│   ├── junit-jupiter-5.8.1.jar
│   ├── junit-jupiter-api-5.8.1.jar
│   ├── junit-jupiter-engine-5.8.1.jar
│   ├── junit-jupiter-params-5.8.1.jar
│   ├── junit-platform-commons-1.8.1.jar
│   ├── junit-platform-engine-1.8.1.jar
│   └── opentest4j-1.2.0.jar
├── src/                           # Production source code
│   ├── Main.java                  # Presentation layer (CLI)
│   ├── exception/
│   │   ├── InvalidTaskDataException.java
│   │   └── TaskNotFoundException.java
│   ├── model/
│   │   ├── Task.java              # Rich domain model (Serializable)
│   │   ├── TaskPriority.java      # LOW, MEDIUM, HIGH, URGENT
│   │   └── TaskStatus.java        # TODO, IN_PROGRESS, DONE, CANCELLED
│   ├── repository/
│   │   ├── FileTaskRepository.java # Concrete file-backed repository
│   │   ├── TaskPersistence.java    # Serialization I/O + corrupt-file recovery
│   │   └── TaskRepository.java     # Repository abstraction (DIP)
│   └── service/
│       └── TaskService.java        # Business logic & validation
├── test/                          # Unit tests (JUnit 5)
│   ├── FakeTaskRepository.java    # In-memory Test Double
│   └── TaskServiceTest.java       # Service layer test suite (11 tests)
└── tasks.dat                      # Auto-generated runtime data file
```
🧪 Testing Strategy

Unit Tests: Written with JUnit 5 (TaskServiceTest, containing 11 focused test cases covering creation validation, find/not-found behavior, successful update, deletion, null-safe due-date sorting, and overdue task filtering).
Test Double Pattern: FakeTaskRepository provides a lightweight in-memory implementation of TaskRepository — tests run completely isolated, fast, and without file I/O dependency.
Isolation: A fresh repository and service instance are initialized before each test via @BeforeEach.

🚀 How to Run & Build

Requirements
JDK 17+
Option 1 — IntelliJ IDEA (Recommended)
Open IntelliJ IDEA → File → Open → select the TaskManager folder.
Ensure Project SDK is set to Java 17+ (File → Project Structure).
Run Main.java for the CLI app, or right-click TaskServiceTest.java to run the test suite (JUnit 5 jars are pre-configured in lib/).
Option 2 — Terminal (Manual Compilation)

Compile production sources:

cd TaskManager
mkdir -p out/production/TaskManager
javac -d out/production/TaskManager $(find src -name "*.java")

Run the application:

java -cp out/production/TaskManager Main

(Note: For running tests via terminal, configure your classpath to include both production classes, test classes, and JUnit platform jars or execute directly from your IDE.)

🗺️ Roadmap & Next Steps

Build-Tool Migration: Move from the IntelliJ .iml setup to Maven or Gradle for dependency management and reproducible builds.
Database Integration: Replace file serialization with a relational store using Spring Data JPA + PostgreSQL behind the existing TaskRepository interface (zero service-layer changes required — showcasing DIP benefits).
REST API: Expose the service layer as a RESTful API with Spring Boot.
Advanced Quality Gates: Add annotation-based validation, JSON persistence options, and CI (GitHub Actions) with automated test pipelines.
📄 License

This project is licensed under the MIT License.

👤 Author

Amirhossein Salehi

GitHub: Amirhoseynsalehi
LinkedIn: amirhoseyn-salehi
Gmail: a.h.salehi2000@gmail.com