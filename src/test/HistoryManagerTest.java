package test;

import history.HistoryManager;
import history.InMemoryHistoryManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Status;
import model.Task;
import model.TaskType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class HistoryManagerTest {
    private final HistoryManager historyManager = new InMemoryHistoryManager();
    private final TaskManager taskManager = new InMemoryTaskManager();

    @Test
    void add() {
        Task task = new Task("Call mommy", "give a call", Status.NEW, TaskType.TASK);
        historyManager.add(task);
        final List<Task> history = historyManager.getHistory();
        Assertions.assertNotNull(history, "История не пустая.");
        Assertions.assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    public void shouldKeepInHistoryNotEditedVersionOfTask() {
        Task task = new Task("Call mommy", "give a call", Status.NEW, TaskType.TASK);
        task.setId(1);
        historyManager.add(task);

        task.setName("call");
        task.setDescription("give");
        task.setStatus(Status.IN_PROGRESS);


        List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(1, history.size(), "История должна содержать одну задачу");

        Task savedTask = history.getFirst();
        Assertions.assertEquals(task, savedTask, "Задача должна оставать той же при сравнении через equals");
        //сравнение по id должно показать что задача одна и та же


        Assertions.assertEquals("Call mommy", savedTask.getName(), "Название задачи в истории не должно измениться");
        Assertions.assertEquals("give a call", savedTask.getDescription(), "Описание задачи в истории не должно измениться");
        Assertions.assertEquals(Status.NEW, savedTask.getStatus(), "Статус задачи в истории не должен измениться");
    }

    @Test
    void shouldKeepOrderOfTasks() {
        Task task1 = new Task("Task 1", "Desc 1", Status.NEW, TaskType.TASK);
        Task task2 = new Task("Task 2", "Desc 2", Status.IN_PROGRESS, TaskType.TASK);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> history = new ArrayList<>(historyManager.getHistory());
        Assertions.assertEquals(2, history.size(), "Размер списка должен быть равен количеству задач в истории");
        Assertions.assertEquals(task1, history.get(1), "Задачи должны храниться в истории в порядке добавления");
        Assertions.assertEquals(task2, history.get(0), "Задачи должны храниться в истории в порядке добавления");
    }

    @Test
    void shouldNotContainDuplicates() {
        Task task = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        historyManager.add(task);
        historyManager.add(task);

        Assertions.assertEquals(1, historyManager.getHistory().size());
    }

    @Test
    void shouldDeleteTaskFromMiddle() {
        Task task1 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        Task task2 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        Task task3 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewTask(task3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(2);

        List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(2, history.size());
        Assertions.assertEquals(task1, history.get(0));
        Assertions.assertEquals(task3, history.get(1));
    }

    @Test
    void shouldDeleteTaskFromTail() {
        Task task1 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        Task task2 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        Task task3 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewTask(task3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(3);

        List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(2, history.size());
        Assertions.assertEquals(task1, history.get(0));
        Assertions.assertEquals(task2, history.get(1));
    }

    @Test
    void shouldDeleteTaskFromHead() {
        Task task1 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        Task task2 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        Task task3 = new Task("task", "Task 1", Status.NEW, TaskType.TASK);
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);
        taskManager.addNewTask(task3);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        Assertions.assertEquals(2, history.size());
        Assertions.assertEquals(task2, history.get(0));
        Assertions.assertEquals(task3, history.get(1));
    }
}