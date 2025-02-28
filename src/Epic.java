import java.util.ArrayList;

public class Epic extends Task {
    private ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description);
        super.setStatus(checkStatus());
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtasks=" + subtasks +
                '}';
    }

    public Status checkStatus() {
        if (subtasks.isEmpty() || isSameStatus(Status.NEW)) {
            return Status.NEW;
        } else if (isSameStatus(Status.DONE)) {
            return Status.DONE;
        } else {
            return Status.IN_PROGRESS;
        }
    }


    private boolean isSameStatus(Status status) {
        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != status) {
                return false;
            }
        }
        return true;
    }






































































}
