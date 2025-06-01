package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private final List<Subtask> subtasks = new ArrayList<>();
    LocalDateTime endTime;

    public Epic(String name, String description, Status status) {
        super(name, description, status, TaskType.EPIC);
        updateTimeFields();
    }

    Epic(int id, String name, Status status, String description, LocalDateTime startTime, Duration duration, LocalDateTime endTime) {
        super(id, name, status, description, TaskType.EPIC, startTime, duration);
        this.endTime = endTime;
        updateTimeFields();
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

    public void updateTimeFields() {
        if (subtasks.isEmpty()) {
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
        updateTimeFields();
        return endTime;
    }
}
