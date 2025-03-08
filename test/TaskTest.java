
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TaskTest {

    private TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoTasksWithSameIdAreEqual() {
        Task task = new Task("name", "description");
        Task otherTask = new Task("na", "desc");
        final int taskId = taskManager.addNewTask(task);
        taskManager.addNewTask(otherTask);
        otherTask.setId(taskId);
        assertEquals(task, otherTask,"Задачи не совпадают.");
    }
  
}