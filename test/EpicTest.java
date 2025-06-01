import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    private TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoEpicsWithSameIdAreEqual() {
        Epic task = new Epic("name", "description", Status.NEW);
        Epic otherTask = new Epic("na", "desc", Status.NEW);
        final int taskId = taskManager.addNewEpic(task);
        taskManager.addNewEpic(otherTask);
        otherTask.setId(taskId);
        assertEquals(task, otherTask, "Задачи не совпадают.");
    }

    @Test
    public void durationShouldBeSumOfAllSubtasksDurations() {
        Epic epic = new Epic("name", "description", Status.NEW);

        taskManager.addNewEpic(epic);
        Subtask sub1 = new Subtask("name", "desc", Status.IN_PROGRESS, epic.getId());
        Subtask sub2 = new Subtask("name", "desc", Status.NEW, epic.getId());
        sub1.setStartTime(LocalDateTime.of(2025, 5, 2, 16, 3));
        sub2.setStartTime(LocalDateTime.of(2025, 5, 2, 17, 3));


        sub1.setDuration(Duration.ofMinutes(30));
        sub2.setDuration(Duration.ofHours(1));

        taskManager.addNewSubtask(sub1);
        taskManager.addNewSubtask(sub2);

        assertEquals(Duration.ofMinutes(120), epic.getDuration());
    }

    @Test
    public void ShouldHaveTheEarliestStartTimeAndTheLatestEndTime() {
        Epic epic = new Epic("name", "description", Status.NEW);
        taskManager.addNewEpic(epic);
        Subtask sub1 = new Subtask("name", "desc", Status.IN_PROGRESS, epic.getId());
        Subtask sub2 = new Subtask("name", "desc", Status.NEW, epic.getId());

        sub1.setStartTime(LocalDateTime.of(2025, 5, 2, 16, 3));
        sub2.setStartTime(LocalDateTime.of(2026, 5, 2, 16, 3));
        sub1.setDuration(Duration.ofMinutes(30));
        sub2.setDuration(Duration.ofHours(1));
        taskManager.addNewSubtask(sub1);
        taskManager.addNewSubtask(sub2);

        assertEquals(LocalDateTime.of(2025, 5, 2, 16, 3), epic.getStartTime());
        assertEquals(LocalDateTime.of(2026, 5, 2, 17, 3), Optional.ofNullable(epic.getEndTime())
                .get());
    }

    @Test
    public void ShouldHaveNoTime() {
        Epic epic = new Epic("name", "description", Status.NEW);

        assertNull(epic.getStartTime());
        assertNull(epic.getEndTime());
        assertEquals(Duration.ZERO, epic.getDuration());
    }

    @Test
    public void shouldHandleSubtaskWithNullStartTime() {
        Epic epic = new Epic("name", "description", Status.NEW);
        taskManager.addNewEpic(epic);
        Subtask sub1 = new Subtask("name", "desc", Status.IN_PROGRESS, epic.getId());
        sub1.setStartTime(null);
        taskManager.addNewSubtask(sub1);

        int id = taskManager.addNewEpic(epic);
        Epic loadedEpic = taskManager.getEpicById(id);

        assertNull(loadedEpic.getStartTime());
        assertNull(loadedEpic.getEndTime());
    }

    @Test
    void shouldUpdateTimeWhenSubtaskChanges() {
        Epic epic = new Epic("Эпик", "Описание", Status.IN_PROGRESS);
        taskManager.addNewEpic(epic);
        Subtask subtask = new Subtask("Подзадача", "Описание", Status.NEW, epic.getId());
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));

        taskManager.addNewSubtask(subtask);
        LocalDateTime initialEndTime = epic.getEndTime();

        subtask.setDuration(Duration.ofHours(2));
        assertNotEquals(initialEndTime, epic.getEndTime());
    }

}