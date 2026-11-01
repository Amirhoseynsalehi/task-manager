package repository;

import model.Task;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaskPersistence {
    private static final String FILE_NAME = "tasks.dat";

    @SuppressWarnings("unchecked")
    public static Map<UUID, Task> loadTasks() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return new HashMap<>();
        }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
        return (Map<UUID, Task>) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
        System.err.println("Warning: Could not read " + FILE_NAME + " properly: " + e.getMessage());

        Path sourcePath = file.toPath();
        Path backupPath = new File(FILE_NAME + ".bak").toPath();

        try {
            Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.err.println("Corrupted file successfully backed up to " + FILE_NAME + ".bak");
        } catch (IOException ex) {
            System.err.println("Failed to create backup file: " + ex.getMessage());
        }

        return new HashMap<>();
    }
}


    public static void saveTasks(Map<UUID, Task> tasks) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(tasks);
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
        }
    }
}
