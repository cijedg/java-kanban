package model;

import java.util.ArrayList;
import java.util.List;

import static model.TaskType.EPIC;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(String name, String description, Status status) {
        super(name, description, status, TaskType.EPIC);

    }

    Epic(int id, String name, Status status, String description) {
        super(id, name, status, description, TaskType.EPIC);
    }

    public List<Subtask> getSubtasks() {
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

    @Override
    public String toString(Task task) {
        if (task == null) {
            return "";
        }
        return String.join(",",
                String.valueOf(getId()),
                String.valueOf(EPIC),
                getName(),
                String.valueOf(getStatus()),
                getDescription(),
                ""
        );
    }

}
