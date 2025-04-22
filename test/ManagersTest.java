import history.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void shouldCreateCorrectTaskManagerReadyForWork() {
        TaskManager taskManager = Managers.getDefault();
        assertNotNull(taskManager, "Экземпляр manager.TaskManager не должен быть null");
        Task otherTask = new Task("na", "desc");

        int taskId = taskManager.addNewTask(otherTask);
        Task savedTask = taskManager.getTaskById(taskId);
        assertNotNull(savedTask, "Задача должна быть добавлена");
        assertEquals(otherTask, savedTask, "Добавленнная задача должна совпадать с сохранённой");
    }

    @Test
    void shouldCreateCorrectHistoryManagerReadyForWork() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager, "Экземпляр history.HistoryManager не должен быть null");
        Task otherTask = new Task("na", "desc");
        historyManager.add(otherTask);
        List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не должна быть пустой");
        assertEquals(otherTask, history.get(0), "Добавленная задача должна совпадать с сохранённой в истории");
    }
}