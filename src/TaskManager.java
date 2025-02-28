import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private int nextId;


    //методы для задач
    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public int addNewTask(Task task) {
        nextId++;
        tasks.put(nextId, task);
        task.setId(nextId);
        System.out.println("Задача " + task.getName() + " успешно добавлена.");
        return nextId;
    }

    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        }
        System.out.println("Такой задачи нет.");
        return null;
    }

    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Такой задачи нет. Сначала добавьте её.");
        } else {
            tasks.put(task.getId(), task);
            System.out.println("Задача " + task.getName() + " успешно обновлена.");
        }
    }

    public void deleteTaskById(int id) {
        if (!tasks.containsKey(id)) {
            System.out.println("Такой задачи нет. Сначала добавьте её.");
        } else {
            tasks.remove(id);
            System.out.println("Задача успешно удалена.");
        }
    }

    public void deleteAllTasks() {
        tasks.clear();
        System.out.println("Все задачи удалены.");
    }

    //методы для подзадач
    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public void deleteSubtasksByEpicId(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Такого эпика нет.");
        }
        ArrayList<Subtask> subtasksInEpic = getSubtasksByEpicId(id);
        subtasksInEpic.clear();
        updateEpic(epics.get(id));
        System.out.println("Подзадачи эпика успешно удалены.");
    }

    public Subtask getSubtaskById(int id) {
        if (subtasks.containsKey(id)) {
            return subtasks.get(id);
        }
        System.out.println("Такой подзадачи нет.");
        return null;
    }

    public int addNewSubtask(Subtask subtask) {
        nextId++;
        subtasks.put(nextId, subtask);
        subtask.setId(nextId);
        Epic epic = epics.get(subtask.getEpicId());
        ArrayList<Subtask> list = epic.getSubtasks();
        list.add(subtask);
        updateEpic(epic);
        System.out.println("Подзадача " + subtask.getName() + " успешно добавлена.");
        return nextId;
    }

    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            System.out.println("Такой подзадачи нет. Сначала добавьте её.");
        } else {
            subtasks.put(subtask.getId(), subtask);
            Epic epic = epics.get(subtask.getEpicId());
            updateEpic(epic);
            System.out.println("Подадача " + subtask.getName() + " успешно обновлена.");
        }
    }

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
            System.out.println("Подзадача успешно удалена.");
        }
    }

    //методы для эпиков
    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public void deleteAllEpics() {
        deleteAllSubtasks();
        epics.clear();
        System.out.println("Все эпики удалены.");
    }

    public Epic getEpicById(int id) {
        if (epics.containsKey(id)) {
            return epics.get(id);
        }
        System.out.println("Такого эпика нет.");
        return null;
    }

    public int addNewEpic(Epic epic) {
        nextId++;
        epics.put(nextId, epic);
        epic.setId(nextId);
        System.out.println("Эпик " + epic.getName() + " успешно добавлен.");
        return nextId;
    }

    public ArrayList<Subtask> getSubtasksByEpicId(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Такого эпика нет.");
            return null;
        }
        Epic epic = epics.get(id);
        return epic.getSubtasks();
    }

    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            System.out.println("Такого эпика нет.");
        } else {
            epic.setStatus(epic.checkStatus());
            epics.put(epic.getId(), epic);
            System.out.println("Эпик " + epic.getName() + " успешно обновлен.");
        }
    }

    public void deleteEpicById(int id) {
        if (!epics.containsKey(id)) {
            System.out.println("Такого эпика нет.");
        } else {
            deleteSubtasksByEpicId(id);
            epics.remove(id);
            System.out.println("Эпик успешно удалён.");
        }
    }

}
