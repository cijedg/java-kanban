package models;

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

    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoSubtasksWithSameIdAreEqual() {

        Epic epic = new Epic("epic", "epic", Status.NEW, LocalDateTime.MIN, Duration.ZERO);
        taskManager.addNewEpic(epic);
        Subtask task = new Subtask("name", "description", Status.DONE, epic.getId(), LocalDateTime.MIN, Duration.ZERO);
        Subtask otherTask = new Subtask("na", "desc", Status.IN_PROGRESS, epic.getId(), LocalDateTime.MIN, Duration.ZERO);
        final int taskId = taskManager.addNewSubtask(task);
        taskManager.addNewSubtask(otherTask);
        otherTask.setId(taskId);
        Assertions.assertEquals(task, otherTask, "Задачи не совпадают.");
    }

    @Test
    public void shouldCalculateEndTimeRight() {
        Subtask task = new Subtask("name", "description", Status.NEW, 1,
                LocalDateTime.of(2025, 3, 3, 22, 0), Duration.ofHours(1));

        assertEquals(LocalDateTime.of(2025, 3, 3, 23, 0), task.getEndTime());
    }

}