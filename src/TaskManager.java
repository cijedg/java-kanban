import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface TaskManager {
    List<Task> getHistory();

    //методы для задач
    HashMap<Integer, Task> getTasks();

    int addNewTask(Task task);

    Task getTaskById(int id);

    void updateTask(Task task);

    void deleteTaskById(int id);

    void deleteAllTasks();

    //методы для подзадач
    HashMap<Integer, Subtask> getSubtasks();

    void deleteAllSubtasks();

    void deleteSubtasksByEpicId(int id);

    Subtask getSubtaskById(int id);

    int addNewSubtask(Subtask subtask);

    void updateSubtask(Subtask subtask);

    void deleteSubtaskById(int id);

    //методы для эпиков
    HashMap<Integer, Epic> getEpics();

    void deleteAllEpics();

    Epic getEpicById(int id);

    int addNewEpic(Epic epic);

    ArrayList<Subtask> getSubtasksByEpicId(int id);

    void updateEpic(Epic epic);

    void deleteEpicById(int id);
}
