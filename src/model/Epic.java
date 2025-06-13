package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description, Status status, LocalDateTime startTime, Duration duration) {
        super(name,
                description,
                status,
                startTime,
                duration);
        updateTimeFields();
    }

    Epic(int id, String name, Status status, String description, LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        super(id, name, status, description, TaskType.EPIC, startTime, duration);
        this.endTime = endTime;
        updateTimeFields();
    }

    public List<Subtask> getSubtasks() {
        return subtasks == null
                ? subtasks = new ArrayList<>()
                : subtasks;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", id=" + getId() +
                ", status=" + getStatus() +
                ", subtasks=" + subtasks +
                ", startTime=" + getStartTime() + '\'' +
                ", duration=" + getDuration() + '\'' +
                ", endTime=" + getEndTime() + '\'' +
                '}';
    }

    public Status checkStatus() {
        if (getSubtasks().isEmpty() || isSameStatus(Status.NEW)) {
            return Status.NEW;
        } else if (isSameStatus(Status.DONE)) {
            return Status.DONE;
        } else {
            return Status.IN_PROGRESS;
        }
    }


    private boolean isSameStatus(Status status) {
        return getSubtasks().stream()
                .allMatch(subtask -> subtask.getStatus() == status);
    }

    public void updateTimeFields() {
        if (getSubtasks().isEmpty()) {
            setStartTime(null);
            endTime = null;
            setDuration(Duration.ZERO);
            return;
        }

        setStartTime(
                subtasks.stream()
                        .map(Subtask::getStartTime)
                        .filter(Objects::nonNull)
                        .min(LocalDateTime::compareTo)
                        .orElse(null)
        );

        endTime = subtasks.stream()
                .map(Subtask::getEndTime)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        setDuration(getStartTime() == null ? Duration.ZERO : Duration.between(getStartTime(), endTime));
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }
}
