import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Status;
import model.Task;
import model.TaskType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TaskTest {

    private TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoTasksWithSameIdAreEqual() {
        Task task = new Task("name", "description", Status.NEW, TaskType.TASK);
        Task otherTask = new Task("na", "desc", Status.NEW, TaskType.TASK);
        final int taskId = taskManager.addNewTask(task);
        taskManager.addNewTask(otherTask);
        otherTask.setId(taskId);
        Assertions.assertEquals(task, otherTask, "Задачи не совпадают.");
    }

}