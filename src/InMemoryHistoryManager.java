import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private ArrayList<Task> viewesHistory = new ArrayList<>(10);

    @Override
    public void add(Task task) {
        if (viewesHistory.size() == 10) {
            viewesHistory.removeFirst();
        }
        Task taskCopy = new Task(task.getName(), task.getDescription(), task.getStatus());
        taskCopy.setId(task.getId());
        viewesHistory.add(taskCopy);
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(viewesHistory);
    }
}
