package managers;

import history.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

class ManagersTest {

    @Test
    void shouldCreateCorrectTaskManagerReadyForWork() {
        TaskManager taskManager = Managers.getDefault();
        Assertions.assertNotNull(taskManager, "Экземпляр manager.TaskManager не должен быть null");
        Task otherTask = new Task("na", "desc", Status.NEW, LocalDateTime.MIN, Duration.ZERO);

        int taskId = taskManager.addNewTask(otherTask);
        Task savedTask = taskManager.getTaskById(taskId);
        Assertions.assertNotNull(savedTask, "Задача должна быть добавлена");
        Assertions.assertEquals(otherTask, savedTask, "Добавленнная задача должна совпадать с сохранённой");
    }

    @Test
    void shouldCreateCorrectHistoryManagerReadyForWork() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Assertions.assertNotNull(historyManager, "Экземпляр history.HistoryManager не должен быть null");
        Task otherTask = new Task("na", "desc", Status.NEW, LocalDateTime.MIN, Duration.ZERO);
        historyManager.add(otherTask);
        List<Task> history = historyManager.getHistory();
        Assertions.assertNotNull(history, "История не должна быть пустой");
        Assertions.assertEquals(otherTask, history.getFirst(), "Добавленная задача должна совпадать с сохранённой в истории");
    }
}