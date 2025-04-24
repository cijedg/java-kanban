package history;

import model.Task;

import java.util.ArrayList;


import java.util.List;

import java.util.Map;
import java.util.HashMap;

public class InMemoryHistoryManager implements HistoryManager {

    private Map<Integer, Node> viewesHistory = new HashMap<>();
    private Node head;
    private Node tail;

    @Override
    public void add(Task task) {
        if (task == null) {
            System.out.println("Такой задачи нет.");
            return;
        }
        if (viewesHistory.containsKey(task.getId())) {
            remove(task.getId());
        }
        Node newNode = new Node(task);

        linkLast(newNode);
        viewesHistory.put(task.getId(), newNode);
    }

    @Override
    public List<Task> getHistory() {
        return List.copyOf(getTasks());
    }

    @Override
    public void remove(int id) {
        removeNode(viewesHistory.get(id));
        viewesHistory.remove(id);
    }

    private void linkLast(Node node) {
        if (head == null) {
            head = node;
        } else {
            tail.next = node;
            node.prev = tail;
        }
        tail = node;
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node task = head;
        while (task != null) {
            tasks.add(task.task);
            task = task.next;
        }
        return tasks;
    }

    private void removeNode(Node node) {

        if (node.prev == null) { //значит node это head
            head = node.next;
        } else {
            node.prev.next = node.next;
        }
        if (node.next == null) {
            tail = node.prev;
        } else {
            node.next.prev = node.prev;
        }
    }
}
