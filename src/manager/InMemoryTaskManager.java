package manager;

import history.HistoryManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskType;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;


public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
                    .thenComparing(Task::getId)
    );
    private int nextId;

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory().isEmpty()
                ? List.of()
                : List.copyOf(historyManager.getHistory());
    }

    //приоритизация задач по времени и получения списка
    @Override
    public void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.isEmpty()
                ? List.of()
                : List.copyOf(prioritizedTasks);
    }

    //методы для задач
    @Override
    public Map<Integer, Task> getTasks() {
        return tasks;
    }

    @Override
    public int addNewTask(Task task) {
        if (hasAnyTimeOverlap(task)) {
            throw new IllegalStateException("Задача пересекается по времени с существующей");
        }
        int id = generateId();
        task.setId(id);
        task.setType(TaskType.TASK);
        tasks.put(id, task);
        addToPrioritizedTasks(task);
        return id;
    }

    @Override
    public Task getTaskById(int id) {
        if (!tasks.containsKey(id)) {
            throw new NoSuchElementException("Задача с указанным id не найдена");
        } else {
            historyManager.add(taskCopy(tasks.get(id)));
            return tasks.get(id);
        }
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            throw new NoSuchElementException("Задача с указанным id не найдена");
        }
        if (hasAnyTimeOverlap(task)) {
            throw new IllegalStateException("Задача пересекается по времени с существующей");
        }
        prioritizedTasks.remove(task);
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
    }

    @Override
    public void deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            throw new NoSuchElementException("Задача с указанным id не найдена");
        }
        prioritizedTasks.remove(tasks.get(id));
        tasks.remove(id);
    }

    @Override
    public void deleteAllTasks() {
        prioritizedTasks.removeIf(task -> tasks.containsKey(task.getId()));
        tasks.clear();
    }

    //методы для подзадач
    @Override
    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public void deleteAllSubtasks() {
        prioritizedTasks.removeIf(subtask -> subtasks.containsKey(subtask.getId()));
        subtasks.clear();
    }

    @Override
    public void deleteSubtasksByEpicId(int id) {
        if (!epics.containsKey(id)) {
            throw new NoSuchElementException("Эпик с указанным id не найден");
        }
        getSubtasksByEpicId(id).stream()
                .peek(prioritizedTasks::remove)
                .map(Subtask::getId)
                .forEach(subtasks::remove);
        updateEpic(epics.get(id));
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (!subtasks.containsKey(id)) {
            throw new NoSuchElementException("Подзадача с указанным id не найдена");
        }
        historyManager.add(taskCopy(subtasks.get(id)));
        return subtasks.get(id);

    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (subtask.getId() == subtask.getEpicId() ||
                !epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Передан неверный id эпика");
        }
        if (hasAnyTimeOverlap(subtask)) {
            throw new IllegalStateException("Подзадача пересекается по времени с существующей");
        }
        if (subtask.getId() == 0) {
            subtask.setId(generateId());
        }
        subtask.setType(TaskType.SUBTASK);
        subtasks.put(subtask.getId(), subtask);
        addToPrioritizedTasks(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasks().add(subtask);
            updateEpic(epic);
        }
        return subtask.getId();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (hasAnyTimeOverlap(subtask)) {
            throw new IllegalStateException("Подзадача пересекается по времени с существующей");
        }
        if (!subtasks.containsKey(subtask.getId())) {
            throw new NoSuchElementException("Подзадача с указанным id не найдена");
        } else {
            prioritizedTasks.remove(subtask);
            subtasks.put(subtask.getId(), subtask);
            addToPrioritizedTasks(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            updateEpic(epic);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        if (!subtasks.containsKey(id)) {
            throw new NoSuchElementException("Подзадача с указанным id не найдена");
        } else {
            Subtask subtask = getSubtaskById(id);
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            List<Subtask> list = epic.getSubtasks();
            list.remove(subtask);
            updateEpic(epic);
            subtasks.remove(id);
        }
    }

    //методы для эпиков
    @Override
    public Map<Integer, Epic> getEpics() {
        return epics;
    }

    @Override
    public void deleteAllEpics() {
        deleteAllSubtasks();
        epics.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        if (!epics.containsKey(id)) {
            throw new NoSuchElementException("Эпик с указанным id не найден");
        }
        historyManager.add(taskCopy(epics.get(id)));
        return epics.get(id);

    }

    @Override
    public int addNewEpic(Epic epic) {
        epic.setType(TaskType.EPIC);
        epic.setId(generateId());
        epic.setStatus(epic.checkStatus());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int id) {
        if (!epics.containsKey(id)) {
            return List.of();
        }
        return epics.get(id).getSubtasks();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            throw new NoSuchElementException("Эпик с указанным id не найден");
        }
        epic.setStatus(epic.checkStatus());
        epic.updateTimeFields();
        epics.put(epic.getId(), epic);
    }

    @Override
    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) {
            throw new NoSuchElementException("Эпик с указанным id не найден");
        }
        deleteSubtasksByEpicId(id);
        epics.remove(id);

    }

    private Task taskCopy(Task task) {
        if (task instanceof Subtask subtask) {
            Subtask copy = new Subtask(subtask.getName(), subtask.getDescription(),
                    subtask.getStatus(), subtask.getEpicId(), subtask.getStartTime(), subtask.getDuration());
            copy.setId(task.getId());
            return copy;
        } else if (task instanceof Epic epic) {
            Epic copy = new Epic(epic.getName(), epic.getDescription(), epic.getStatus(), epic.getStartTime(), epic.getDuration());
            copy.setId(task.getId());
            return copy;
        } else {
            Task copy = new Task(task.getName(), task.getDescription(), task.getStatus(), task.getStartTime(), task.getDuration());
            copy.setId(task.getId());
            return copy;
        }
    }

    private int generateId() {
        return ++nextId;
    }

    private boolean hasTimeOverlap(Task task1, Task task2) {
        if (task1.getStartTime() == null || task2.getStartTime() == null) {
            return false; // задачи без времени не могут пересекаться
        }
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime end2 = task2.getEndTime();
        return task1.getStartTime().isBefore(end2) && end1.isAfter(task2.getStartTime());
    }

    private boolean hasAnyTimeOverlap(Task task) {
        return prioritizedTasks.stream()
                .filter(streamTask -> !streamTask.equals(task))
                .anyMatch(streamTask -> hasTimeOverlap(task, streamTask));
    }
}
