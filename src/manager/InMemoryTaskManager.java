package manager;

import history.HistoryManager;
import model.Epic;
import model.Subtask;
import model.Task;
import model.TaskType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InMemoryTaskManager implements TaskManager {
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private int nextId;
    private HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //методы для задач
    @Override
    public Map<Integer, Task> getTasks() {
        return tasks;
    }

    @Override
    public int addNewTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
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
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Такой задачи нет. Сначала добавьте её.");
        } else {
            tasks.put(task.getId(), task);
            System.out.println("Задача " + task.getName() + " успешно обновлена.");
        }
    }

    @Override
    public void deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            System.out.println("Такой задачи нет. Сначала добавьте её.");
        } else {
            tasks.remove(id);
        }
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    //методы для подзадач
    @Override
    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    @Override
    public void deleteSubtasksByEpicId(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Такого эпика нет.");
        }
        List<Subtask> subtasksInEpic = getSubtasksByEpicId(id);
        for (Subtask subtask : subtasksInEpic) {
            subtasks.remove(subtask.getId());
        }
        subtasksInEpic.clear();
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
        if (subtask.getId() == 0) {
            subtask.setId(generateId());
        }
        if (subtask.getId() == subtask.getEpicId()) {
            return -1;
        }
        subtasks.put(subtask.getId(), subtask);
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
        if (!subtasks.containsKey(subtask.getId())) {
            System.out.println("Такой подзадачи нет. Сначала добавьте её.");
        } else {
            subtasks.put(subtask.getId(), subtask);
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
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Такого эпика нет.");
            return null;
        }
        Epic epic = epics.get(id);
        return epic.getSubtasks();
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            System.out.println("Такого эпика нет.");
        } else {
            epic.setStatus(epic.checkStatus());
            epics.put(epic.getId(), epic);
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
}
