import manager.Managers;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager taskManager = Managers.getDefault();

    @Test
    public void addNewTask() {

        Task task = new Task("Test addNewTask", "Test addNewTask description", Status.NEW);
        final int taskId = taskManager.addNewTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final Map<Integer, Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(1), "Задачи не совпадают.");
    }

    @Test
    public void shouldReturnMinus1IfAddEpicInSameEpicAsSubtask() {

        Epic task = new Epic("name", "description");
        int epicId = taskManager.addNewEpic(task);
        Subtask otherTask = new Subtask("na", "desc", Status.IN_PROGRESS, task.getId());
        otherTask.setId(epicId);
        int result = taskManager.addNewSubtask(otherTask);
        assertEquals(-1, result, "Epic нельзя добавить в самого себя в виде подзадачи");
        Epic savedEpic = taskManager.getEpicById(epicId);
        assertTrue(savedEpic.getSubtasks().isEmpty(), "Список подзадач эпика должен остаться пустым");
    }

    @Test
    public void shouldNotAddSubtaskAsItsOwnEpic() {
        Epic task = new Epic("name", "description");
        taskManager.addNewEpic(task);
        Subtask subtask = new Subtask("na", "desc", Status.IN_PROGRESS, task.getId());
        taskManager.addNewSubtask(subtask);
        subtask.setEpicId(subtask.getId());
        assertNotEquals(subtask.getId(), subtask.getEpicId(), "Subtask нельзя сделать своим же эпиком");
        assertEquals(task.getId(), subtask.getEpicId(), "Подзадача должна ссылаться на исходный эпик");
    }

    @Test
    public void shouldAddAllTypesOfTasksAndFindThemById() {
        Task task = new Task("Call mommy", "give a call", Status.NEW);
        int taskId = taskManager.addNewTask(task);
        Map<Integer, Task> tasks = taskManager.getTasks();
        assertNotNull(tasks, "Список задач не должен быть пустым");
        assertEquals(task, tasks.get(taskId), "Найденная по id задача должна совпадать с добавленной");

        Epic epic = new Epic("paper", "hmm");
        int epicId = taskManager.addNewEpic(epic);
        Map<Integer, Epic> epics = taskManager.getEpics();
        assertNotNull(epics, "Список эпиков не должен быть пустым");
        assertEquals(epic, epics.get(epicId), "Найденный по id эпик должен совпадать с добавленным");

        Subtask subtask1 = new Subtask("annotation", "write paper", Status.NEW, epic.getId());
        int subtaskId = taskManager.addNewSubtask(subtask1);
        Map<Integer, Subtask> subtasks = taskManager.getSubtasks();
        assertNotNull(subtasks, "Список подзадач не должен быть пустым");
        assertEquals(subtask1, subtasks.get(subtaskId), "Найденная по id подзадача должна совпадать с добавленной");
    }

    @Test
    public void shouldIgnoreAssignedIdAndGenerateNew() {
        Task taskWithAssignedId = new Task("Call mommy", "give a call", Status.NEW);
        taskWithAssignedId.setId(100);
        int assignedId = taskManager.addNewTask(taskWithAssignedId);

        Task taskWithGeneratedId = new Task("name", "desc");
        int generatedId = taskManager.addNewTask(taskWithGeneratedId);

        assertNotEquals(assignedId, generatedId, "Id задач не должны совпадать");

        Task savedTaskWithAssignedId = taskManager.getTaskById(assignedId);
        Task savedTaskWithGeneratedId = taskManager.getTaskById(generatedId);

        assertEquals(taskWithAssignedId, savedTaskWithAssignedId, "Задача с заданным id должна совпадать с сохранённой");
        assertEquals(taskWithGeneratedId, savedTaskWithGeneratedId, "Задача со сгенерированным id должна совпадать с сохранённой");
    }

    @Test
    public void shouldNotChangeTaskFieldsAfterAdding() {
        Task task = new Task("Call mommy", "give a call", Status.NEW);

        Task taskCopy = new Task(task.getName(), task.getDescription(), task.getStatus());
        taskCopy.setId(task.getId());

        int taskId = taskManager.addNewTask(task);

        Task savedTask = taskManager.getTaskById(taskId);
        assertEquals(taskCopy.getName(), savedTask.getName(), "Название задачи не должно измениться");
        assertEquals(taskCopy.getDescription(), savedTask.getDescription(), "Описание задачи не должно измениться");
        assertEquals(taskCopy.getStatus(), savedTask.getStatus(), "Статус задачи не должен измениться");
    }

    @Test
    void deleteSubtaskShouldRemoveItFromEpic() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        List<Subtask> subtasks = taskManager.getSubtasksByEpicId(epic.getId());
        assertEquals(2, subtasks.size());

        taskManager.deleteSubtaskById(subtask1.getId());
        List<Subtask> updatedSubtasks = taskManager.getSubtasksByEpicId(epic.getId());
        assertEquals(1, updatedSubtasks.size());
        assertFalse(updatedSubtasks.contains(subtask1));
        assertTrue(updatedSubtasks.contains(subtask2));
    }

    @Test
    void deleteEpicShouldRemoveAllItsSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Description");
        int epicId = taskManager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId);

        taskManager.addNewSubtask(subtask1);
        taskManager.addNewSubtask(subtask2);
        taskManager.deleteEpicById(epicId);

        assertNull(taskManager.getSubtaskById(subtask1.getId()));
        assertNull(taskManager.getSubtaskById(subtask2.getId()));
        assertTrue(taskManager.getSubtasks().isEmpty());
    }

    @Test
    void epicShouldNotContainDeletedSubtaskIds() {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = taskManager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW, epicId);
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW, epicId);
        int sub1Id = taskManager.addNewSubtask(sub1);
        taskManager.addNewSubtask(sub2);

        taskManager.deleteSubtaskById(sub1Id);

        Epic updatedEpic = taskManager.getEpicById(epicId);
        assertFalse(updatedEpic.getSubtasks().contains(sub1), "Подзадача 1 должна быть удалена");
        assertTrue(updatedEpic.getSubtasks().contains(sub2), "Подзадача 2 должна остаться");
        assertEquals(1, updatedEpic.getSubtasks().size(), "Должна остаться 1 подзадача");
    }
}