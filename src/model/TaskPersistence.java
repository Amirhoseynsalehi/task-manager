package model;

import java.io.*;
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

        try (ObjectInputStream in =
                     new ObjectInputStream(new FileInputStream(file))) {

            return (Map<UUID, Task>) in.readObject();

        } catch (Exception e) {
            System.out.println("Error loading tasks: " + e.getMessage());
            return new HashMap<>();
        }
    }

    public static void saveTasks(Map<UUID, Task> tasks) {
        try (ObjectOutputStream out =
                     new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {

            out.writeObject(tasks);

        } catch (Exception e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }
}
