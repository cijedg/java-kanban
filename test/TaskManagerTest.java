import manager.TaskManager;
import model.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @Test
    public void addNewTask() {

        Task task = new Task("Test addNewTask", "Test addNewTask description", Status.NEW, TaskType.TASK);
        final int taskId = manager.addNewTask(task);

        final Task savedTask = manager.getTaskById(taskId);

        Assertions.assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");

        final Map<Integer, Task> tasks = manager.getTasks();

        Assertions.assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.get(1), "Задачи не совпадают.");
    }

    @Test
    public void shouldAddAllTypesOfTasksAndFindThemById() {
        Task task = new Task("Call mommy", "give a call", Status.NEW, TaskType.TASK);
        int taskId = manager.addNewTask(task);
        Map<Integer, Task> tasks = manager.getTasks();
        Assertions.assertNotNull(tasks, "Список задач не должен быть пустым");
        assertEquals(task, tasks.get(taskId), "Найденная по id задача должна совпадать с добавленной");

        Epic epic = new Epic("paper", "hmm", Status.NEW);
        int epicId = manager.addNewEpic(epic);
        Map<Integer, Epic> epics = manager.getEpics();
        Assertions.assertNotNull(epics, "Список эпиков не должен быть пустым");
        assertEquals(epic, epics.get(epicId), "Найденный по id эпик должен совпадать с добавленным");

        Subtask subtask1 = new Subtask("annotation", "write paper", Status.NEW, epic.getId());
        int subtaskId = manager.addNewSubtask(subtask1);
        Map<Integer, Subtask> subtasks = manager.getSubtasks();
        Assertions.assertNotNull(subtasks, "Список подзадач не должен быть пустым");
        assertEquals(subtask1, subtasks.get(subtaskId), "Найденная по id подзадача должна совпадать с добавленной");
    }

    @Test
    public void shouldHaveStatusNewIfAllSubtasksHaveStatusNew() {
        Epic epic = new Epic("Эпик", "Описание", Status.NEW);
        manager.addNewEpic(epic);

        Subtask sub1 = new Subtask("name", "desc", Status.NEW, epic.getId());
        Subtask sub2 = new Subtask("name", "desc", Status.NEW, epic.getId());
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    public void shouldHaveStatusNewIfHasNoSubtasks() {
        Epic epic = new Epic("Эпик", "Описание", Status.IN_PROGRESS);
        manager.addNewEpic(epic);

        assertEquals(Status.NEW, epic.getStatus());
    }

    @Test
    public void shouldHaveStatusDoneIfAllSubtasksHaveStatusDone() {
        Epic epic = new Epic("Эпик", "Описание", Status.IN_PROGRESS);
        manager.addNewEpic(epic);

        Subtask sub1 = new Subtask("name", "desc", Status.DONE, epic.getId());
        Subtask sub2 = new Subtask("name", "desc", Status.DONE, epic.getId());
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    public void shouldHaveStatusInProgressIfAllSubtasksHaveDifferentStatus() {
        Epic epic = new Epic("Эпик", "Описание", Status.DONE);
        manager.addNewEpic(epic);

        Subtask sub1 = new Subtask("name", "desc", Status.NEW, epic.getId());
        Subtask sub2 = new Subtask("name", "desc", Status.IN_PROGRESS, epic.getId());
        Subtask sub3 = new Subtask("name", "desc", Status.DONE, epic.getId());
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);
        manager.addNewSubtask(sub3);

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void shouldSaveAndReturnDurationAndStartTime() {
        Epic epic = new Epic("Epic", "Desc", Status.NEW);
        epic.setStartTime(LocalDateTime.of(2025, 4, 6, 22, 0));
        epic.setDuration(Duration.ofMinutes(45));

        int id = manager.addNewEpic(epic);
        Epic loadedEpic = manager.getEpicById(id);

        assertEquals(LocalDateTime.of(2025, 4, 6, 22, 0), loadedEpic.getStartTime());
        assertEquals(Duration.ofMinutes(45), loadedEpic.getDuration());
    }

    @Test
    public void deleteSubtaskShouldRemoveItFromEpic() {
        Epic epic = new Epic("Test Epic", "Test Description", Status.NEW);
        int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.IN_PROGRESS, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId);

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        List<Subtask> subtasks = manager.getSubtasksByEpicId(epic.getId());
        assertEquals(2, subtasks.size());

        manager.deleteSubtaskById(subtask1.getId());
        List<Subtask> updatedSubtasks = manager.getSubtasksByEpicId(epic.getId());
        assertEquals(1, updatedSubtasks.size());
        Assertions.assertFalse(updatedSubtasks.contains(subtask1));
        Assertions.assertTrue(updatedSubtasks.contains(subtask2));
    }

    @Test
    public void deleteEpicShouldRemoveAllItsSubtasks() {
        Epic epic = new Epic("Test Epic", "Test Description", Status.NEW);
        int epicId = manager.addNewEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.NEW, epicId);

        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.deleteEpicById(epicId);

        Assertions.assertNull(manager.getSubtaskById(subtask1.getId()));
        Assertions.assertNull(manager.getSubtaskById(subtask2.getId()));
        Assertions.assertTrue(manager.getSubtasks().isEmpty());
    }

    @Test
    public void epicShouldNotContainDeletedSubtaskIds() {
        Epic epic = new Epic("Epic", "Desc", Status.NEW);
        int epicId = manager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "Desc", Status.NEW, epicId);
        Subtask sub2 = new Subtask("Sub2", "Desc", Status.NEW, epicId);
        int sub1Id = manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        manager.deleteSubtaskById(sub1Id);

        Epic updatedEpic = manager.getEpicById(epicId);
        Assertions.assertFalse(updatedEpic.getSubtasks().contains(sub1), "Подзадача 1 должна быть удалена");
        Assertions.assertTrue(updatedEpic.getSubtasks().contains(sub2), "Подзадача 2 должна остаться");
        assertEquals(1, updatedEpic.getSubtasks().size(), "Должна остаться 1 подзадача");
    }

    @Test
    public void shouldNotAddSubtaskAsItsOwnEpic() {
        Epic task = new Epic("name", "description", Status.NEW);
        manager.addNewEpic(task);
        Subtask subtask = new Subtask("na", "desc", Status.IN_PROGRESS, task.getId());
        manager.addNewSubtask(subtask);
        subtask.setEpicId(subtask.getId());
        Assertions.assertNotEquals(subtask.getId(), subtask.getEpicId(), "Subtask нельзя сделать своим же эпиком");
        assertEquals(task.getId(), subtask.getEpicId(), "Подзадача должна ссылаться на исходный эпик");
    }

    @Test
    public void shouldNotAddSubtaskWithoutExistingEpic() {
        Subtask subtask = new Subtask("na", "desc", Status.IN_PROGRESS, -1);
        manager.addNewSubtask(subtask);

        assertEquals(0, manager.getSubtasks().size());

        subtask = new Subtask("na", "desc", Status.IN_PROGRESS, 999);
        manager.addNewSubtask(subtask);

        assertEquals(0, manager.getSubtasks().size());
    }

    @Test
    public void shouldNotAddTasksWithOverlappingTime() {
        Task task = new Task("Call mommy", "give a call", Status.NEW, TaskType.TASK);
        task.setStartTime(LocalDateTime.of(2025, Month.APRIL, 25, 10, 15));
        task.setDuration(Duration.ofMinutes(45));

        Task task1 = new Task("Call mommy", "give a call", Status.NEW, TaskType.TASK);
        task1.setStartTime(LocalDateTime.of(2025, Month.APRIL, 25, 10, 35));
        task1.setDuration(Duration.ofMinutes(5));

        manager.addNewTask(task);

        assertThrows(IllegalStateException.class, () -> manager.addNewTask(task1), "Задача пересекается по времени с существующей");
    }
}
