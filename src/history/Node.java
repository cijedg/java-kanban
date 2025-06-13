package history;

import model.Task;

public class Node {
    public Task task;
    public Node prev;
    public Node next;

    Node(Task task) {
        this.task = new Task(task.getId(), task.getName(), task.getStatus(), task.getDescription(), task.getType(), task.getStartTime(), task.getDuration());
        this.next = null;
        this.prev = null;
    }

}
