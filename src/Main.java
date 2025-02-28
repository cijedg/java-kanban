import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        TaskManager tm = new TaskManager();

        Task task = new Task("Call mommy", "give a call", Status.NEW);
        tm.addNewTask(task);
        System.out.println(tm.getTasks());

        task.setName("Call daddy");
        tm.updateTask(task);
        tm.deleteTaskById(task.getId());
        System.out.println(tm.getTasks());

        Epic epic = new Epic("paper", "hmm");
        tm.addNewEpic(epic);
        System.out.println(tm.getEpicById(epic.getId()));


        Subtask subtask1 = new Subtask("annotation", "write paper", Status.NEW, epic.getId());
        Subtask subtask2 = new Subtask("annotation", "write paper", Status.IN_PROGRESS, epic.getId());

        tm.addNewSubtask(subtask1);
        tm.addNewSubtask(subtask2);
        Epic epica = new Epic("epica", "eat yoghurt");
        tm.addNewEpic(epica);
        Subtask yoghurt = new Subtask("shopping", "buy yoghurt", Status.NEW, epica.getId());
        System.out.println(tm.getEpics());
        System.out.println(tm.getSubtasksByEpicId(epic.getId()));
        tm.addNewSubtask(yoghurt);
        yoghurt.setStatus(Status.DONE);
        tm.updateSubtask(yoghurt);
        System.out.println(tm.getEpicById(epica.getId()));

        epic.setDescription("really write smth");
        tm.updateEpic(epic);
        System.out.println(tm.getEpics());
        tm.deleteAllEpics();

    }
}

