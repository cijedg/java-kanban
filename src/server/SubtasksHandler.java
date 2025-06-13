package server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {

    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        switch (method) {
            case "GET" -> handleGetSubtasks(httpExchange);
            case "POST" -> handlePostSubtasks(httpExchange);
            case "DELETE" -> handleDeleteSubtasks(httpExchange);
            default ->
                    sendMethodNotAllowed(httpExchange, "Метод не поддерживается. Допустимые методы: GET, POST или DELETE");
        }
    }

    private void handleGetSubtasks(HttpExchange httpExchange) throws IOException {
        String acceptHeader = httpExchange.getRequestHeaders().getFirst("Accept");
        if (acceptHeader == null || !acceptHeader.contains("application/json")) {
            sendNotAcceptable(httpExchange, "Требуется заголовок Accept: application/json");
            return;
        }
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            //получение общего списка подзадач
            if (pathParts.length == 2) {
                String response = gson.toJson(taskManager.getSubtasks().values());
                sendText(httpExchange, response, 200);
            } //получение подзадачи по айди
            else if (pathParts.length == 3) {
                int subtaskId = Integer.parseInt(pathParts[2]);
                Subtask subtask = taskManager.getSubtaskById(subtaskId);
                sendText(httpExchange, gson.toJson(subtask), 200);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, "Неверный формат id подзадачи");
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }

    private void handlePostSubtasks(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            //считываем входные данные и проверяем
            InputStream inputStream = httpExchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Subtask subtask = gson.fromJson(body, Subtask.class);
            //создание новой подзадачи
            if (pathParts.length == 2) {
                if (subtask.getName().isBlank() || subtask.getDescription().isBlank()) {
                    sendBadRequest(httpExchange, "Заполните необходимые поля для создания подзадачи");
                    return;
                }
                taskManager.addNewSubtask(subtask);
                sendText(httpExchange, "Добавлена подзадача с id = " + subtask.getId(), 201);
            } //обновление существующей подзадачи
            else if (pathParts.length == 3) {
                int subtaskId = Integer.parseInt(pathParts[2]);
                if (subtask.getId() != subtaskId) {
                    sendBadRequest(httpExchange, "Id в URL и теле запроса не совпадают");
                    return;
                }
                Subtask existing = taskManager.getSubtaskById(subtaskId);
                existing.updateFrom(subtask);
                taskManager.updateSubtask(existing);
                sendText(httpExchange, "Подзадача успешно обновлена", 201);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalStateException e) {
            sendNotAcceptable(httpExchange, e.getMessage());
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalError(httpExchange);
        }
    }

    private void handleDeleteSubtasks(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            if (pathParts.length == 3) {
                int subtaskId = Integer.parseInt(pathParts[2]);
                taskManager.deleteSubtaskById(subtaskId);
                sendText(httpExchange, "Подзадача с id = " + subtaskId + " удалена", 200);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, "Неверный формат id подзадачи");
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }
}
