package model;

import java.util.Objects;

import static model.TaskType.TASK;

public class Task {
    private String name;
    private String description;
    private int id;
    private Status status;
    private final TaskType type;

    public Task(String name, String description, Status status, TaskType type) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.type = type;
    }

    public TaskType getType() {
        return type;
    }

    Task(int id, String name, Status status, String description, TaskType type) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Task{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", id=" + id + ", status=" + status + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String toString(Task task) {
        if (task == null) {
            return "";
        }
        return String.join(",",
                String.valueOf(getId()),
                String.valueOf(TASK),
                getName(),
                String.valueOf(getStatus()),
                getDescription(),
                "");
    }

    public static Task fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String[] splitted = value.split(",");
        int id = Integer.parseInt(splitted[0]);
        TaskType type = TaskType.valueOf(splitted[1]);
        String name = splitted[2];
        Status status = Status.valueOf(splitted[3]);
        String description = splitted[4];
        return switch (type) {
            case TASK -> new Task(id, name, status, description, TASK);
            case EPIC -> new Epic(id, name, status, description);
            case SUBTASK -> {
                int epicId = Integer.parseInt(splitted[5]);
                yield new Subtask(id, name, status, description, epicId);
            }
        };
    }
}
