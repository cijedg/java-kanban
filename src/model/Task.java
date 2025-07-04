package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

import static model.TaskType.TASK;

public class Task {
    private String name;
    private String description;
    private int id;
    private Status status;
    private TaskType type;
    private Duration duration;
    private LocalDateTime startTime;

    public Task(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.status = status == null ? Status.NEW : status;
        this.startTime = startTime;
        this.duration = duration;
    }


    public Task(int id, String name, Status status, String description, TaskType type, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.name = name;
        this.status = status == null ? Status.NEW : status;
        this.description = description;
        this.type = type;
        this.startTime = startTime;
        this.duration = duration;
    }

    public static Task fromString(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String[] splitted = value.split(",", -1);
        int id = Integer.parseInt(splitted[0]);
        TaskType type = TaskType.valueOf(splitted[1]);
        String name = splitted[2];
        Status status = Status.valueOf(splitted[3]);
        String description = splitted[4];
        LocalDateTime startTime = splitted[6].isBlank() ? null : LocalDateTime.parse(splitted[6]);
        Duration duration = splitted[7].isBlank() ? Duration.ZERO : Duration.ofMinutes(Long.parseLong(splitted[7]));
        return switch (type) {
            case TASK -> new Task(id, name, status, description, TASK, startTime, duration);
            case EPIC -> new Epic(id, name, status, description, startTime, duration, null);
            case SUBTASK -> {
                int epicId = Integer.parseInt(splitted[5]);
                yield new Subtask(id, name, status, description, epicId, startTime, duration);
            }
        };
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration == null ? Duration.ZERO : duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", startTime=" + getStartTime() + '\'' +
                ", duration=" + getDuration() + '\'' +
                '}';
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

    public LocalDateTime getEndTime() {
        return duration == null || duration.isZero() ? startTime : startTime.plus(duration);
    }

    public void updateFrom(Task other) {
        if (other.getName() != null) {
            this.name = other.getName();
        }
        if (other.getDescription() != null) {
            this.description = other.getDescription();
        }
        if (other.getStatus() != null) {
            this.status = other.getStatus();
        }
        if (other.getStartTime() != null) {
            this.startTime = other.getStartTime();
        }
        if (other.getDuration() != Duration.ZERO) {
            this.duration = other.getDuration();
        }
    }
}
