package manager;

import history.HistoryManager;
import history.InMemoryHistoryManager;

import java.nio.file.Path;

public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTaskManager getDefaultSaving(Path filename) {
        return new FileBackedTaskManager(filename);
    }
}

