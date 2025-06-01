import manager.InMemoryTaskManager;
import manager.Managers;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createManager() {
        return (InMemoryTaskManager) Managers.getDefault();
    }

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    public void shouldReturnMinus1IfAddEpicInSameEpicAsSubtask() {
        Epic task = new Epic("name", "description", Status.NEW);
        int epicId = manager.addNewEpic(task);
        Subtask otherTask = new Subtask("na", "desc", Status.IN_PROGRESS, task.getId());
        otherTask.setId(epicId);
        int result = manager.addNewSubtask(otherTask);
        assertEquals(-1, result, "Epic нельзя добавить в самого себя в виде подзадачи");
        Epic savedEpic = manager.getEpicById(epicId);
        Assertions.assertTrue(savedEpic.getSubtasks().isEmpty(), "Список подзадач эпика должен остаться пустым");
    }

    @Test
    public void shouldIgnoreAssignedIdAndGenerateNew() {
        Task taskWithAssignedId = new Task("Call mommy", "give a call", Status.NEW, TaskType.TASK);
        taskWithAssignedId.setId(100);
        int assignedId = manager.addNewTask(taskWithAssignedId);

        Task taskWithGeneratedId = new Task("name", "desc", Status.NEW, TaskType.TASK);
        int generatedId = manager.addNewTask(taskWithGeneratedId);

        Assertions.assertNotEquals(assignedId, generatedId, "Id задач не должны совпадать");

        Task savedTaskWithAssignedId = manager.getTaskById(assignedId);
        Task savedTaskWithGeneratedId = manager.getTaskById(generatedId);

        assertEquals(taskWithAssignedId, savedTaskWithAssignedId, "Задача с заданным id должна совпадать с сохранённой");
        assertEquals(taskWithGeneratedId, savedTaskWithGeneratedId, "Задача со сгенерированным id должна совпадать с сохранённой");
    }

    @Test
    public void shouldNotChangeTaskFieldsAfterAdding() {
        Task task = new Task("Call mommy", "give a call", Status.NEW, TaskType.TASK);

        Task taskCopy = new Task(task.getName(), task.getDescription(), task.getStatus(), TaskType.TASK);
        taskCopy.setId(task.getId());

        int taskId = manager.addNewTask(task);

        Task savedTask = manager.getTaskById(taskId);
        assertEquals(taskCopy.getName(), savedTask.getName(), "Название задачи не должно измениться");
        assertEquals(taskCopy.getDescription(), savedTask.getDescription(), "Описание задачи не должно измениться");
        assertEquals(taskCopy.getStatus(), savedTask.getStatus(), "Статус задачи не должен измениться");
    }
}