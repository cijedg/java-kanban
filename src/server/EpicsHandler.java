package server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;
import model.Epic;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        switch (method) {
            case "GET" -> handleGetEpics(httpExchange);
            case "POST" -> handlePostEpics(httpExchange);
            case "DELETE" -> handleDeleteEpics(httpExchange);
            default ->
                    sendMethodNotAllowed(httpExchange, "Метод не поддерживается. Допустимые методы: GET, POST или DELETE");
        }
    }

    private void handleGetEpics(HttpExchange httpExchange) throws IOException {
        String acceptHeader = httpExchange.getRequestHeaders().getFirst("Accept");
        if (acceptHeader == null || !acceptHeader.contains("application/json")) {
            sendNotAcceptable(httpExchange, "Требуется заголовок Accept: application/json");
            return;
        }
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {//получение общего списка эпиков
            if (pathParts.length == 2) {
                String response = gson.toJson(taskManager.getEpics().values());
                sendText(httpExchange, response, 200);
            } else if (pathParts.length == 3) {
                int epicId = Integer.parseInt(pathParts[2]);
                Epic epic = taskManager.getEpicById(epicId);
                sendText(httpExchange, gson.toJson(epic), 200);
            } else if (pathParts.length == 4 && pathParts[3].equals("subtasks")) { //получение списка подзадач эпика по айди
                int epicId = Integer.parseInt(pathParts[2]);
                String response = gson.toJson(taskManager.getEpicById(epicId).getSubtasks());
                sendText(httpExchange, response, 200);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, "Неверный формат id эпика");
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }

    private void handlePostEpics(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            //считываем входные данные и проверяем
            InputStream inputStream = httpExchange.getRequestBody();
            String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
            Epic epic = gson.fromJson(body, Epic.class);
            //создание нового эпика
            if (pathParts.length == 2) {
                if (epic.getName().isBlank() || epic.getDescription().isBlank()) {
                    sendBadRequest(httpExchange, "Заполните необходимые поля для создания эпика");
                    return;
                }
                taskManager.addNewEpic(epic);
                sendText(httpExchange, "Добавлен эпик с id = " + epic.getId(), 201);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalStateException e) {
            sendNotAcceptable(httpExchange, e.getMessage());
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }

    private void handleDeleteEpics(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            if (pathParts.length == 3) {
                int epicId = Integer.parseInt(pathParts[2]);
                taskManager.deleteEpicById(epicId);
                sendText(httpExchange, "Эпик с id = " + epicId + " удален", 200);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(httpExchange, "Неверный формат json");
        } catch (IllegalArgumentException e) {
            sendBadRequest(httpExchange, "Неверный формат id эпика");
        } catch (NoSuchElementException e) {
            sendNotFound(httpExchange, e.getMessage());
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }
}
