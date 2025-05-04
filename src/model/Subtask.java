package model;

public class Subtask extends Task {
    private int epicId;

    public Subtask(String name, String description, Status status, int epicId) {
        super(name, description, status, TaskType.SUBTASK);
        this.epicId = epicId;
    }

    Subtask(int id, String name, Status status, String description, int epicId) {
        super(id, name, status, description, TaskType.SUBTASK);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        if (epicId != this.getId()) {
            this.epicId = epicId;
        }
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public String toString(Task task) {
        if (task == null) {
            return "";
        }
        return String.join(",",
                String.valueOf(getId()),
                String.valueOf(TaskType.SUBTASK),
                getName(),
                String.valueOf(getStatus()),
                getDescription(),
                String.valueOf(getEpicId())
        );
    }
}
