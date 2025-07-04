package models;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaskTest {

    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoTasksWithSameIdAreEqual() {
        Task task = new Task("name", "description", Status.NEW, null, null);
        Task otherTask = new Task("na", "desc", Status.NEW, null, null);
        final int taskId = taskManager.addNewTask(task);
        taskManager.addNewTask(otherTask);
        otherTask.setId(taskId);
        assertEquals(task, otherTask, "Задачи не совпадают.");
    }

    @Test
    public void shouldCalculateEndTimeRight() {
        Task task = new Task("name", "description", Status.NEW,
                LocalDateTime.of(2025, 3, 3, 22, 0), Duration.ofHours(1));

        assertEquals(LocalDateTime.of(2025, 3, 3, 23, 0), task.getEndTime());
    }

    @Test
    public void taskWithZeroDurationShouldHaveSameStartTimeAndEndTime() {
        Task task = new Task("name", "description", Status.NEW,
                LocalDateTime.now(), Duration.ZERO);

        assertEquals(task.getStartTime(), task.getEndTime());
    }

}