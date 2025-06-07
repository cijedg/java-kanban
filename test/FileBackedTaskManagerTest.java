import exceptions.ManagerLoadException;
import exceptions.ManagerSaveException;
import manager.FileBackedTaskManager;
import manager.Managers;
import model.*;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static Path tempFile;

    @Override
    protected FileBackedTaskManager createManager() {
        return Managers.getDefaultSaving(tempFile);
    }

    @BeforeEach
    void setUp() {
        manager = createManager();
    }

    @BeforeAll
    static void initFile() throws IOException {
        tempFile = Files.createTempFile("tasks", ".csv");
    }

    @AfterAll
    static void tearDown() throws IOException {
        Files.deleteIfExists(tempFile);
    }

    @AfterEach
    void clearFile() throws IOException {
        Files.writeString(tempFile, "");
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
        assertEquals("id,type,name,status,description,epic,startTime,duration", lines[0], "Неверный формат заголовка");
    }

    @Test
    void shouldLoadFromEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(FileBackedTaskManagerTest.tempFile.toFile());
        assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
        assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
    }

    @Test
    void shouldSaveAndLoadAllTaskFieldsCorrectly() {
        LocalDateTime taskStartTime = LocalDateTime.of(2023, 1, 1, 10, 0);
        Duration taskDuration = Duration.ofMinutes(45);

        LocalDateTime subtaskStartTime = LocalDateTime.of(2023, 1, 1, 11, 0);
        Duration subtaskDuration = Duration.ofMinutes(30);

        Task task = new Task("Task", "Task description", Status.NEW, TaskType.TASK);
        task.setStartTime(taskStartTime);
        task.setDuration(taskDuration);
        manager.addNewTask(task);

        Epic epic = new Epic("Epic", "Epic description", Status.NEW);
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Subtask description", Status.IN_PROGRESS, epic.getId());
        subtask.setStartTime(subtaskStartTime);
        subtask.setDuration(subtaskDuration);

        manager.addNewSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile.toFile());

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals(1, loadedManager.getEpics().size());
        assertEquals(1, loadedManager.getSubtasks().size());

        Task loadedTask = loadedManager.getTaskById(task.getId());
        assertNotNull(loadedTask);
        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(task.getType(), loadedTask.getType());
        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(task.getStatus(), loadedTask.getStatus());
        assertEquals(task.getDescription(), loadedTask.getDescription());
        assertEquals(task.getStartTime(), loadedTask.getStartTime());
        assertEquals(task.getDuration(), loadedTask.getDuration());

        Subtask loadedSubtask = loadedManager.getSubtaskById(subtask.getId());
        assertNotNull(loadedSubtask);
        assertEquals(subtask.getId(), loadedSubtask.getId());
        assertEquals(subtask.getType(), loadedSubtask.getType());
        assertEquals(subtask.getName(), loadedSubtask.getName());
        assertEquals(subtask.getStatus(), loadedSubtask.getStatus());
        assertEquals(subtask.getDescription(), loadedSubtask.getDescription());
        assertEquals(subtask.getStartTime(), loadedSubtask.getStartTime());
        assertEquals(subtask.getDuration(), loadedSubtask.getDuration());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());

        Epic loadedEpic = loadedManager.getEpicById(epic.getId());
        assertNotNull(loadedEpic);
        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(epic.getType(), loadedEpic.getType());
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(epic.getStatus(), loadedEpic.getStatus());
        assertEquals(epic.getDescription(), loadedEpic.getDescription());

        assertEquals(subtaskStartTime, loadedEpic.getStartTime());
        assertEquals(subtaskDuration, loadedEpic.getDuration());
        assertEquals(subtaskStartTime.plus(subtaskDuration), loadedEpic.getEndTime());
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

    @Test
    void shouldThrowManagerLoadExceptionWhenFileDoesNotExist() {
        Path nonExistingFile = Path.of("non_existing_file.csv");

        assertThrows(ManagerLoadException.class,
                () -> FileBackedTaskManager.loadFromFile(nonExistingFile.toFile()),
                "Попытка загрузки из несуществующего файла должна вызывать исключение");
    }

    @Test
    void shouldNotThrowManagerSaveExceptionWhenSavingToValidFile() {
        Task task = new Task("Valid Task", "Description", Status.NEW, TaskType.TASK);

        assertDoesNotThrow(
                () -> manager.addNewTask(task),
                "Корректная операция сохранения не должна вызывать исключений");
    }
}

