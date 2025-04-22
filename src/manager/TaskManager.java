package manager;

import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;
import java.util.Map;
public interface TaskManager {

    List<Task> getHistory();

    //методы для задач
    Map<Integer, Task> getTasks();

    int addNewTask(Task task);

    Task getTaskById(int id);

    void updateTask(Task task);

    void deleteTaskById(int id);

    void deleteAllTasks();

    //методы для подзадач
    Map<Integer, Subtask> getSubtasks();

    void deleteAllSubtasks();

    void deleteSubtasksByEpicId(int id);

    Subtask getSubtaskById(int id);

    int addNewSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    //методы для эпиков
    Map<Integer, Epic> getEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    int addNewEpic(Epic epic);

    List<Subtask> getSubtasksByEpicId(int id);

    void updateEpic(Epic epic);

    void deleteEpicById(int id);
}
