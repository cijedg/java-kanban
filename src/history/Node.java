package history;
import model.Task;

public class Node {
    public Task task;
    public Node prev;
    public Node next;

    Node(Task task) {
        this.task = task;
        this.next = null;
        this.prev = null;
    }

}
