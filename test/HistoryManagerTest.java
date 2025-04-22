import history.HistoryManager;
import history.InMemoryHistoryManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {
    private final HistoryManager historyManager = new InMemoryHistoryManager();

    @Test
    void add() {
        Task task = new Task("Call mommy", "give a call", Status.NEW);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    public void shouldKeepInHistoryNotEditedVersionOfTask() {
        Task task = new Task("Call mommy", "give a call", Status.NEW);
        task.setId(1);
        historyManager.add(task);

        task.setName("call");
        task.setDescription("give");
        task.setStatus(Status.IN_PROGRESS);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size(), "История должна содержать одну задачу");

        Task savedTask = history.get(0);
        assertEquals(task, savedTask, "Задача должна оставать той же при сравнении через equals");
        //сравнение по id должно показать что задача одна и та же


        assertEquals("Call mommy", savedTask.getName(), "Название задачи в истории не должно измениться");
        assertEquals("give a call", savedTask.getDescription(), "Описание задачи в истории не должно измениться");
        assertEquals(Status.NEW, savedTask.getStatus(), "Статус задачи в истории не должен измениться");
    }
}