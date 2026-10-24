# TaskManager

![Java](https://img.shields.io/badge/Java-17%2B-orange?logo=openjdk&logoColor=white)
![JUnit 5](https://img.shields.io/badge/Tests-JUnit%205-25A162?logo=junit5&logoColor=white)
![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20Layered-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📌 Project Overview

**TaskManager** is a complete **Core Java CLI application** that applies **Clean Architecture**, **SOLID principles**, and **modern Java features** — including the **Stream API**, **Lambdas**, **Optional**, and the **java.time** API — with a **comprehensive JUnit 5 unit-testing suite**.

It is a real-world portfolio project demonstrating how to structure a non-framework Java application in well-separated layers with a fully testable service layer and persistent storage.

## 🏛️ Architecture & Design Principles

### Layered Architecture

| Layer | Package(s) | Responsibility |
|---|---|---|
| **Presentation** | `Main.java` | CLI menu, user input/output, error messaging |
| **Service (Business Logic)** | `service.TaskService` | Business rules, validation, filtering, sorting, overdue tracking |
| **Repository (Abstraction & Persistence)** | `repository.TaskRepository`, `repository.FileTaskRepository`, `repository.TaskPersistence` | Storage abstraction and Java-Serialization-based persistence |
| **Domain / Model (Rich Model)** | `model.Task`, `model.TaskStatus`, `model.TaskPriority` | Rich domain model (`Serializable`), Enums, identity via UUID |
| **Exception** | `exception.InvalidTaskDataException`, `exception.TaskNotFoundException` | Domain-specific, self-documenting exceptions |

### SOLID Principles Mapping

- **S — Single Responsibility**: `Main` handles only the CLI, `TaskService` only business logic, `TaskPersistence` only file I/O, and `FileTaskRepository` only in-memory + delegated persistence.
- **O — Open/Closed**: New repositories (e.g., database-backed) can be added without modifying `TaskService`; new filtering/sorting strategies extend via `Comparator` and Stream pipelines.
- **L — Liskov Substitution**: `FileTaskRepository` and `FakeTaskRepository` are fully substitutable for `TaskRepository` with no behavioral surprises.
- **I — Interface Segregation**: `TaskRepository` is a small, focused interface (`save`, `findById`, `deleteById`, `findAll`, `saveTasks`) — no bloated contracts.
- **D — Dependency Inversion ⭐**: `TaskService` depends **only on the `TaskRepository` interface**, never on a concrete implementation. This allows seamless switching between `FileTaskRepository` (production, file persistence) and `FakeTaskRepository` (in-memory **Test Double**) with a single constructor argument — the core enabler of the fast, isolated unit tests.

## 🧩 Domain Model & Features

### `Task` Entity

| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` | Auto-generated identity (`UUID.randomUUID()`), used for `equals`/`hashCode` |
| `title` | `String` | Required |
| `description` | `String` | Optional |
| `status` | `TaskStatus` enum | `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED` |
| `priority` | `TaskPriority` enum | `LOW`, `MEDIUM`, `HIGH`, `URGENT` |
| `createdAt` | `LocalDateTime` | Set automatically on creation |
| `dueDate` | `LocalDate` | Optional |

The entity is `Serializable` (`serialVersionUID = 1L`) for robust binary persistence.

### Validation Rules

- **Title** must be non-blank (create & update).
- **Due date** must not be in the past (`dueDate >= today`) when provided.

### Operations

- ✅ Full **CRUD**: create, read (find all / by ID), update, delete.
- 🔍 **Advanced Filtering**: by status, by priority, combined (status + priority), and **keyword search** in titles (case-insensitive).
- ↕️ **Sorting**: by **priority** and by **due date** with a **null-safe, nulls-last comparator**.
- ⏰ **Overdue Task Tracking**: tasks that are not `DONE` and whose `dueDate` is before today.

### Persistence

- **Java Serialization** to `tasks.dat` (`TaskPersistence`).
- **Graceful corrupt-file recovery**: if the file cannot be read, it is automatically **backed up to `tasks.dat.bak`** and the app starts with a clean state instead of crashing.

### Graceful Shutdown

- A **JVM Shutdown Hook** (`Runtime.getRuntime().addShutdownHook(...)`) calls `service.saveTasks()`, so data is persisted even if the app exits unexpectedly (e.g., `Ctrl+C`).

## 📁 Project Directory Structure

```text
TaskManager/
├── .gitignore
├── TaskManager.iml                # IntelliJ IDEA module (standard project)
├── lib/                           # JUnit 5 runtime dependencies
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
│   └── TaskServiceTest.java
└── tasks.dat                      # Auto-generated data file (runtime)
```

## 🧪 Testing Strategy

- **Unit tests** written with **JUnit 5** (`TaskServiceTest`), executed against the service layer.
- **Test Double pattern**: `FakeTaskRepository` is a lightweight **in-memory implementation** of `TaskRepository` — tests are **isolated, fast, and have no file I/O dependency** (thanks to Dependency Inversion).
- Fresh repository + service instance created per test via `@BeforeEach`.
- **Edge cases covered**:
  - Validation failures (blank title, past due date → `InvalidTaskDataException`).
  - Missing IDs (`TaskNotFoundException`).
  - Overdue date logic (overdue TODO included, overdue DONE excluded, future/today excluded).
  - **Null-safe sorting** (tasks without due dates sort last).

## 🚀 How to Run & Build

### Requirements

- **JDK 17+** (modern Java features used)

### Option 1 — IntelliJ IDEA (recommended)

1. Open IntelliJ IDEA → **File → Open** → select the `TaskManager` folder (standard `.iml` module project).
2. Ensure **Project SDK = 17+** under *File → Project Structure*.
3. Run `Main` (or run the `TaskServiceTest` test class for tests — JUnit 5 jars are already in `lib/`).

### Option 2 — Terminal

**Compile:**

```bash
cd TaskManager
mkdir -p out/production/TaskManager
javac -d out/production/TaskManager $(find src -name "*.java")
```

**Run:**

```bash
java -cp out/production/TaskManager Main
```

**Compile & run unit tests** (using the bundled JUnit jars in `lib/`):

```bash
CP="lib/*:out/production/TaskManager"
javac -cp "$CP" -d out/test/TaskManager $(find test -name "*.java")
java -cp "$CP:out/test/TaskManager" \
     org.junit.platform.console.ConsoleLauncher \
     --scan-classpath out/test/TaskManager
```

> If you have the JUnit **Console Launcher** jar, add it to the classpath; otherwise run the tests from IntelliJ IDEA (already configured).

## 🗺️ Roadmap & Next Steps

- [ ] **Build-tool migration**: move from the IntelliJ `.iml` project to **Maven** or **Gradle** for dependency management and reproducible builds.
- [ ] **Database integration**: replace file serialization with a relational store using **Spring Data JPA + PostgreSQL** behind the existing `TaskRepository` interface (no service-layer change needed — DIP pays off).
- [ ] **REST API**: expose the service layer as a RESTful API with **Spring Boot** and a web UI client.
- [ ] Input validation with annotations, JSON persistence option, and CI (GitHub Actions) with automated test runs.

## 📄 License

This project is licensed under the **MIT License**.

## 👤 Author

**Amirhoseyn-salehi**
- GitHub: https://github.com/Amirhoseynsalehi
- LinkedIn: www.linkedin.com/in/amirhoseyn-salehi
- Gmail: a.h.salehi2000@gmail.com
