package history;

import model.Task;

public class Node {
    public Task task;
    public Node prev;
    public Node next;

    Node(Task task) {
        this.task = new Task(task.getName(), task.getDescription(), task.getStatus(), task.getType());
        this.task.setId(task.getId());
        this.task.setStartTime(task.getStartTime());
        this.task.setDuration(task.getDuration());
        this.next = null;
        this.prev = null;
    }

}
