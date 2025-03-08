import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private int nextId = 0;
    private HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    //методы для задач
    @Override
    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = task.getId();
        if (taskId == 0 || tasks.containsKey(taskId)) {
            taskId = nextId++;
            task.setId(taskId);
        }
        tasks.put(taskId, task);
        return taskId;
    }

    @Override
    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            historyManager.add(tasks.get(id));
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
    public HashMap<Integer, Subtask> getSubtasks() {
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
        ArrayList<Subtask> subtasksInEpic = getSubtasksByEpicId(id);
        subtasksInEpic.clear();
        updateEpic(epics.get(id));
    }

    @Override
    public Subtask getSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            historyManager.add(subtasks.get(id));
            return subtasks.get(id);
        }
        return null;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (subtask.getId() == subtask.getEpicId()) {
            return -1;
        }
        int subtaskId = subtask.getId();
        if (subtaskId == 0 || subtasks.containsKey(subtaskId)) {
            subtaskId = nextId++;
            subtask.setId(subtaskId);
        }
        tasks.put(subtaskId, subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            ArrayList<Subtask> list = epic.getSubtasks();
            list.add(subtask);
            updateEpic(epic);
        }
        return subtaskId;
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
            ArrayList<Subtask> list = epic.getSubtasks();
            list.remove(subtask);
            updateEpic(epic);
            subtasks.remove(id);
        }
    }

    //методы для эпиков
    @Override
    public HashMap<Integer, Epic> getEpics() {
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
            historyManager.add(epics.get(id));
            return epics.get(id);
        }
        System.out.println("Такого эпика нет.");
        return null;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int epicId = epic.getId();
        if (epicId == 0 || epics.containsKey(epicId)) {
            epicId = nextId++;
            epic.setId(epicId);
        }
        return epicId;
    }

    @Override
    public ArrayList<Subtask> getSubtasksByEpicId(int id) {
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

}
