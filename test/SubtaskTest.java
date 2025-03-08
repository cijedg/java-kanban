import org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {

    private TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoSubtasksWithSameIdAreEqual() {

        Epic epic = new Epic("epic", "epic");
        taskManager.addNewEpic(epic);
        Subtask task = new Subtask("name", "description", Status.DONE, epic);
        Subtask otherTask = new Subtask("na", "desc", Status.IN_PROGRESS, epic);
        final int taskId = taskManager.addNewSubtask(task);
        taskManager.addNewSubtask(otherTask);
        otherTask.setId(taskId);
        assertEquals(task, otherTask,"Задачи не совпадают.");
    }

}