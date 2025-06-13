package server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    public TasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        switch (method) {
            case "GET" -> handleGetTasks(httpExchange);
            case "POST" -> handlePostTasks(httpExchange);
            case "DELETE" -> handleDeleteTasks(httpExchange);
            default ->
                    sendMethodNotAllowed(httpExchange, "Метод не поддерживается. Допустимые методы: GET, POST или DELETE");
        }
    }

    private void handleGetTasks(HttpExchange httpExchange) throws IOException {
        String acceptHeader = httpExchange.getRequestHeaders().getFirst("Accept");
        if (acceptHeader == null || !acceptHeader.contains("application/json")) {
            sendNotAcceptable(httpExchange, "Требуется заголовок Accept: application/json");
            return;
        }
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            //получение общего списка задач
            if (pathParts.length == 2) {
                String response = gson.toJson(taskManager.getTasks().values());
                sendText(httpExchange, response, 200);
            } //получение задачи по айди
            else if (pathParts.length == 3) {
                int taskId = Integer.parseInt(pathParts[2]);
                Task task = taskManager.getTaskById(taskId);
                sendText(httpExchange, gson.toJson(task), 200);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, "Неверный формат id задачи");
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }


    private void handlePostTasks(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            InputStream inputStream = httpExchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Task task = gson.fromJson(body, Task.class);
            if (pathParts.length == 2) {
                if (task.getName().isBlank() || task.getDescription().isBlank() ||
                        task.getName() == null || task.getDescription() == null) {
                    sendBadRequest(httpExchange, "Заполните необходимые поля для создания задачи");
                    return;
                }
                taskManager.addNewTask(task);
                sendText(httpExchange, "Добавлена задача с id = " + task.getId(), 201);
            } else if (pathParts.length == 3) {
                int taskId = Integer.parseInt(pathParts[2]);
                if (task.getId() != taskId) {
                    sendBadRequest(httpExchange, "Id в URL и теле запроса не совпадают");
                    return;
                }
                Task existing = taskManager.getTaskById(taskId);
                existing.updateFrom(task);
                taskManager.updateTask(existing);
                sendText(httpExchange, "Задача успешно обновлена", 201);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalStateException e) {
            sendNotAcceptable(httpExchange, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, "Неверный формат id задачи");
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }

    private void handleDeleteTasks(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            if (pathParts.length == 3) {
                int taskId = Integer.parseInt(pathParts[2]);
                taskManager.deleteTaskById(taskId);
                sendText(httpExchange, "Задача с id = " + taskId + " удалена", 200);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, "Неверный формат id задачи");
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }
}