package test;

import exceptions.ManagerSaveException;
import manager.FileBackedTaskManager;
import manager.Managers;
import manager.TaskManager;
import model.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileBackedTaskManagerTest {
    private static Path tempFile;
    private TaskManager manager;

    @BeforeAll
    static void setUp() throws IOException {
        tempFile = Files.createTempFile("tasks", ".csv");
    }

    @BeforeEach
    void initManager() {
        manager = Managers.getDefaultSaving(tempFile);
    }

    @AfterEach
    void clearFile() throws IOException {
        Files.writeString(tempFile, "");
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldSaveEmptyFile() {
        manager.deleteAllTasks();

        String[] lines;
        try {
            String content = Files.readString(tempFile, StandardCharsets.UTF_8);
            lines = content.split("\n");
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }

        assertEquals(1, lines.length, "Файл должен содержать только заголовок");
        assertEquals("id,type,name,status,description,epic", lines[0], "Неверный формат заголовка");
    }

    @Test
    void shouldLoadFromEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(FileBackedTaskManagerTest.tempFile.toFile());
        assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
    }

    @Test
    void shouldSaveAllTypesOfTasks() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", Status.NEW, TaskType.TASK);
        manager.addNewTask(task);
        Epic epic = new Epic("name", "description", Status.NEW);
        manager.addNewEpic(epic);
        Subtask otherTask = new Subtask("na", "desc", Status.IN_PROGRESS, epic.getId());
        manager.addNewSubtask(otherTask);

        String[] lines;
        try {
            String content = Files.readString(tempFile, StandardCharsets.UTF_8);
            lines = content.split("\n");
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }

        assertEquals(4, lines.length, "Файл должен содержать 4 строки: заголовок, задачу, эпик и подзадачу");
        assertEquals("id,type,name,status,description,epic", lines[0], "Неверный формат заголовка");
        assertEquals(task.toString(task), lines[1], "Неверный формат сохранения задачи");
        assertEquals(epic.toString(epic), lines[3], "Неверный формат сохранения эпика");
        assertEquals(otherTask.toString(otherTask), lines[2], "Неверный формат сохранения подзадачи");
    }

    @Test
    void shouldLoadAllTypesOfTasks() {
        Task task = new Task("Test addNewTask", "Test addNewTask description", Status.NEW, TaskType.TASK);
        manager.addNewTask(task);
        Epic epic = new Epic("name", "description", Status.NEW);
        manager.addNewEpic(epic);
        Subtask otherTask = new Subtask("na", "desc", Status.IN_PROGRESS, epic.getId());
        manager.addNewSubtask(otherTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(FileBackedTaskManagerTest.tempFile.toFile());

        assertEquals(1, loadedManager.getTasks().size(), "Должна загрузиться одна задача");
        assertEquals(1, loadedManager.getSubtasks().size(), "Должна загрузиться одна подзадача");
        assertEquals(1, loadedManager.getEpics().size(), "Должен загрузиться один эпик");
        assertEquals(task, loadedManager.getTaskById(task.getId()), "Загруженная задача не совпадает с добавленной");
        assertEquals(otherTask, loadedManager.getSubtaskById(otherTask.getId()), "Загруженная подзадача не совпадает с добавленной");
        assertEquals(epic, loadedManager.getEpicById(epic.getId()), "Загруженный эпик не совпадает с добавленным");
    }
}

