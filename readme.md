# 📋 TaskManager - Java SE Clean Architecture Project

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20%2F%20Layered-blue?style=for-the-badge)](#architecture--design-principles)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

A robust, enterprise-ready **Task Management System** built with pure **Java SE**. This project demonstrates core and modern Java features, strict layered architecture (Separation of Concerns), domain-driven error handling, and comprehensive unit testing using JUnit 5.

---

## 🌟 Key Features & Technical Highlights

- **Layered Clean Architecture:** Complete decoupling of the UI/CLI layer, Business Logic (`Service`), Persistence (`Repository`), and Domain (`Model`).
- **Modern Java Time API (`java.time`):**
    - Immutable date-time tracking using `LocalDateTime` for creation audit (`createdAt`).
    - Business deadline tracking using `LocalDate` for task due dates (`dueDate`).
- **Functional Programming & Stream API:**
    - Declarative filtering, sorting, and transformation pipelines using Java Streams and Lambdas.
    - Efficient aggregation (e.g., filtering tasks by priority, status, or deadline).
- **Domain Exception Handling:**
    - Granular, domain-specific custom exceptions (`TaskNotFoundException`, `InvalidTaskDataException`) preventing silent failures and leaky abstractions.
- **Data Persistence:**
    - Robust file-based persistence layer (`FileTaskRepository` / serialization) abstracting I/O operations away from the domain logic.
- **Enterprise-Grade Testing:**
    - Full isolation of business logic via the **In-Memory Fake Repository Pattern** (`FakeTaskRepository`).
    - Comprehensive unit test suite built with **JUnit 5**, testing both happy paths and edge/failure cases.

---

## 🏗 Architecture & Design Principles

The application is engineered adhering to **SOLID** principles, particularly:
- **Single Responsibility Principle (SRP):** Each class owns one cohesive responsibility (e.g., `TaskService` handles business constraints; repositories handle persistence).
- **Dependency Inversion Principle (DIP):** High-level modules (`TaskService`) depend on abstractions (`TaskRepository` interface), allowing pluggable persistence (file-based or in-memory test mocks) without modifying core business rules.

┌───────────────────────────────┐

│ CLI / Main │

└───────────────┬───────────────┘

│

▼

┌───────────────────────────────┐

│ TaskService │ ◄── Business Logic & Validation

└───────┬───────────────┬───────┘

│ │

depends on interface uses domain

│ │

▼ ▼

┌──────────────────────┐ ┌─────────────┐

│ TaskRepository │ │ Task Model │

└──────────▲───────────┘ └─────────────┘

│

┌────────┴────────┐

│ │

┌──────────────┐ ┌──────────────────┐

│ FileTaskRepo │ │ FakeTaskRepo │ (Used for Unit Tests)

└──────────────┘ └──────────────────┘

---

## 📁 Project Structure
```text
TaskManager/
├── src/
│   ├── Main.java                          # CLI entry point and menu orchestration
│   ├── exception/
│   │   ├── InvalidTaskDataException.java  # Validation error handling
│   │   └── TaskNotFoundException.java     # Lookup error handling
│   ├── model/
│   │   ├── Task.java                      # Domain entity (encapsulation, validation)
│   │   ├── TaskPriority.java              # Enum: LOW, MEDIUM, HIGH, URGENT
│   │   └── TaskStatus.java                # Enum: TODO, IN_PROGRESS, DONE
│   ├── repository/
│   │   ├── TaskRepository.java            # Persistence abstraction (DIP)
│   │   ├── FileTaskRepository.java        # Binary file persistence implementation
│   │   └── TaskPersistence.java           # Serialization helper
│   └── service/
│       └── TaskService.java               # Core business rules & Stream pipelines
└── test/
├── FakeTaskRepository.java            # In-memory test double for zero-I/O testing
└── TaskServiceTest.java               # 13 comprehensive JUnit 5 unit tests
🧪 Testing Suite
The project includes an isolated unit test suite validating core business behaviors without touching disk I/O:

✅ Task creation with automatic createdAt generation and optional dueDate.
✅ Validation rules for title, priority, and date constraints.
✅ Search, update, and status transition workflows.
✅ Error scenarios verifying proper propagation of custom domain exceptions.
✅ Query pipelines powered by Java Stream API.
Running Tests
Via IntelliJ IDEA:

Navigate to test/TaskServiceTest.java.
Click the green Run arrow next to the class name or press Ctrl + Shift + F10 (Windows/Linux) / Cmd + Shift + R (macOS).
Via Terminal (JUnit Console Runner / Standard Java):
# Compile source and test files
javac -d out/production -sourcepath src src/**/*.java
javac -d out/test -cp out/production:lib/* -sourcepath test test/*.java

# Run tests
java -jar lib/junit-platform-console-standalone-*.jar --class-path out/production:out/test --scan-class-path
🚀 Getting Started
Prerequisites
JDK 17 or later installed.
Any modern Java IDE (IntelliJ IDEA recommended).
Installation & Run
Clone the repository:
   git clone https://github.com/<your-username>/TaskManager.git
   cd TaskManager
   
Run in IntelliJ IDEA:

Open the directory as an IntelliJ project.
Run src/Main.java.
Run from Terminal:
   javac -d out src/**/*.java
   java -cp out Main
   
👤 Author
Amirhossein Salehi

LinkedIn: Your Profile Link
GitHub: @your-username