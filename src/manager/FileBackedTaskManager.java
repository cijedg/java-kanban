package manager;

import exceptions.ManagerLoadException;
import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    Path filename;

    public FileBackedTaskManager(Path filename) {
        this.filename = filename;
    }

    //получаю мапы и записываю
    private void save() {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(
                filename,
                StandardCharsets.UTF_8)
        ) {
            bufferedWriter.write("id,type,name,status,description,epic,startTime,duration" + "\n");
            for (Task task : getTasks().values()) {
                bufferedWriter.write(toString(task) + "\n");
            }
            for (Subtask subtask : getSubtasks().values()) {
                bufferedWriter.write(toString(subtask) + "\n");
            }

            for (Epic epic : getEpics().values()) {
                bufferedWriter.write(toString(epic) + "\n");
            }
        } catch (IOException exp) {
            throw new ManagerSaveException(exp.getMessage());
        }
    }

    @Override
    public int addNewTask(Task task) {
        int taskId = super.addNewTask(task);
        save();
        return taskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteSubtasksByEpicId(int id) {
        super.deleteSubtasksByEpicId(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        int subtaskId = super.addNewSubtask(subtask);
        save();
        return subtaskId;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public int addNewEpic(Epic epic) {
        int epicId = super.addNewEpic(epic);
        save();
        return epicId;
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    //строками заполняю менеджер
    public static FileBackedTaskManager loadFromFile(File file) {
        Path path = file.toPath();
        FileBackedTaskManager manager = new FileBackedTaskManager(path);
        List<Subtask> subtasks = new ArrayList<>();
        try {
            String content = Files.readString(path, StandardCharsets.UTF_8);
            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) {
                    continue;
                }
                Task task = Task.fromString(line);
                switch (task.getType()) {
                    case TASK -> manager.addNewTask(task);
                    case SUBTASK -> subtasks.add((Subtask) task);
                    case EPIC -> manager.addNewEpic((Epic) task);
                }
            }
            subtasks.stream()
                    .forEach(manager::addNewSubtask);
        } catch (IOException e) {
            throw new ManagerLoadException(e.getMessage());
        }
        return manager;
    }

    String toString(Task task) {
        if (task == null) {
            return "";
        }
        return String.join(",",
                String.valueOf(task.getId()),
                String.valueOf(task.getType()),
                task.getName(),
                String.valueOf(task.getStatus()),
                task.getDescription(),
                task instanceof Subtask ? String.valueOf(((Subtask) task).getEpicId()) : "",
                task.getStartTime() != null ? task.getStartTime().toString() : "",
                !task.getDuration().isZero() ? String.valueOf(task.getDuration().toMinutes()) : ""
        );
    }
}
