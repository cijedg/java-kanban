import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EpicTest {

    private TaskManager taskManager = new InMemoryTaskManager();

    @Test
    public void shouldBeEqualIfTwoEpicsWithSameIdAreEqual() {
        Epic task = new Epic("name", "description");
        Epic otherTask = new Epic("na", "desc");
        final int taskId = taskManager.addNewEpic(task);
        taskManager.addNewEpic(otherTask);
        otherTask.setId(taskId);
        Assertions.assertEquals(task, otherTask, "Задачи не совпадают.");
    }


}