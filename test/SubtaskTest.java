import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    private TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoSubtasksWithSameIdAreEqual() {

        Epic epic = new Epic("epic", "epic", Status.NEW);
        taskManager.addNewEpic(epic);
        Subtask task = new Subtask("name", "description", Status.DONE, epic.getId());
        Subtask otherTask = new Subtask("na", "desc", Status.IN_PROGRESS, epic.getId());
        final int taskId = taskManager.addNewSubtask(task);
        taskManager.addNewSubtask(otherTask);
        otherTask.setId(taskId);
        Assertions.assertEquals(task, otherTask, "Задачи не совпадают.");
    }

    @Test
    public void shouldCalculateEndTimeRight() {
        Subtask task = new Subtask("name", "description", Status.NEW, 1);
        task.setStartTime(LocalDateTime.of(2025, 3, 3, 22, 0));
        task.setDuration(Duration.ofHours(1));

        assertEquals(LocalDateTime.of(2025, 3, 3, 23, 0), task.getEndTime());
    }

}