import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubtaskTest {

    private TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoSubtasksWithSameIdAreEqual() {

        Epic epic = new Epic("epic", "epic");
        taskManager.addNewEpic(epic);
        Subtask task = new Subtask("name", "description", Status.DONE, epic.getId());
        Subtask otherTask = new Subtask("na", "desc", Status.IN_PROGRESS, epic.getId());
        final int taskId = taskManager.addNewSubtask(task);
        taskManager.addNewSubtask(otherTask);
        otherTask.setId(taskId);
        assertEquals(task, otherTask, "Задачи не совпадают.");
    }

}