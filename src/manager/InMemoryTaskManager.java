package manager;

import history.HistoryManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskType;

import java.util.*;


public class InMemoryTaskManager implements TaskManager {
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();
    private int nextId;
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime)
                    .thenComparing(Task::getId)
    );

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //приоритизация задач по времени и получения списка
    private void addToPrioritizedTasks(Task task) {
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
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
        task.setId(generateId());
        tasks.put(task.getId(), task);
        addToPrioritizedTasks(task);
        return task.getId();
    }

    @Override
    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            historyManager.add(taskCopy(tasks.get(id)));
            return tasks.get(id);
        }
        System.out.println("Такой задачи нет.");
        return null;
    }

    @Override
    public void updateTask(Task task) {
        if (hasAnyTimeOverlap(task)) {
            throw new IllegalStateException("Задача пересекается по времени с существующей");
        }
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Такой задачи нет. Сначала добавьте её.");
        } else {
            prioritizedTasks.remove(task);
            tasks.put(task.getId(), task);
            addToPrioritizedTasks(task);
        }
    }

    @Override
    public void deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            System.out.println("Такой задачи нет. Сначала добавьте её.");
        } else {
            prioritizedTasks.remove(tasks.get(id));
            tasks.remove(id);
        }
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
            System.out.println("Такого эпика нет.");
        }
        getSubtasksByEpicId(id).stream()
                .peek(prioritizedTasks::remove)
                .map(Subtask::getId)
                .forEach(subtasks::remove);
        updateEpic(epics.get(id));
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            historyManager.add(taskCopy(subtasks.get(id)));
            return subtasks.get(id);
        }
        return null;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (subtask.getId() == subtask.getEpicId() || subtask.getEpicId() < 0
                || !epics.containsKey(subtask.getEpicId())) {
            return -1;
        }
        if (hasAnyTimeOverlap(subtask)) {
            throw new IllegalStateException("Подзадача пересекается по времени с существующей");
        }
        if (subtask.getId() == 0) {

            subtask.setId(generateId());
        }
        subtasks.put(subtask.getId(), subtask);
        addToPrioritizedTasks(subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            List<Subtask> list = epic.getSubtasks();
            list.add(subtask);
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
            System.out.println("Такой подзадачи нет. Сначала добавьте её.");
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
            System.out.println("Такой подзадачи нет. Сначала добавьте её.");
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
        if (epics.containsKey(id)) {
            historyManager.add(taskCopy(epics.get(id)));
            return epics.get(id);
        }
        System.out.println("Такого эпика нет.");
        return null;
    }

    @Override
    public int addNewEpic(Epic epic) {
        epic.setId(generateId());
        epic.setStatus(epic.checkStatus());
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int id) {
        if (!epics.containsKey(id)) {
            return Collections.emptyList();
        }
        return epics.get(id).getSubtasks();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            System.out.println("Такого эпика нет.");
        } else {
            epic.setStatus(epic.checkStatus());
            epics.put(epic.getId(), epic);
            epic.updateTimeFields();
        }
    }

    @Override
    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Такого эпика нет.");
        } else {
            deleteSubtasksByEpicId(id);
            epics.remove(id);
        }
    }

    private Task taskCopy(Task task) {
        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            Subtask copy = new Subtask(subtask.getName(), subtask.getDescription(),
                    subtask.getStatus(), subtask.getEpicId());
            copy.setId(task.getId());
            return copy;
        } else if (task instanceof Epic) {
            Epic epic = (Epic) task;
            Epic copy = new Epic(epic.getName(), epic.getDescription(), epic.getStatus());
            copy.setId(task.getId());
            return copy;
        } else {
            Task copy = new Task(task.getName(), task.getDescription(), task.getStatus(), TaskType.TASK);
            copy.setId(task.getId());
            return copy;
        }
    }

    private int generateId() {
        return ++nextId;
    }

    private boolean hasTimeOverlap(Task task1, Task task2) {
        return task1.getStartTime().isBefore(task2.getEndTime()) &&
                task1.getEndTime().isAfter(task2.getStartTime());
    }

    private boolean hasAnyTimeOverlap(Task task) {
        return prioritizedTasks.stream()
                .anyMatch(streamTask -> hasTimeOverlap(task, streamTask));
    }
}
