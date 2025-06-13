package server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.TaskManager;

import java.io.IOException;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String acceptHeader = httpExchange.getRequestHeaders().getFirst("Accept");
        if (acceptHeader == null || !acceptHeader.contains("application/json")) {
            sendNotAcceptable(httpExchange, "Требуется заголовок Accept: application/json");
            return;
        }
        String method = httpExchange.getRequestMethod();
        if (method.equals("GET")) {
            handleGetHistory(httpExchange);
        } else {
            sendMethodNotAllowed(httpExchange, "Метод не поддерживается. Допустимый метод: GET");
        }
    }

    private void handleGetHistory(HttpExchange httpExchange) throws IOException {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            if (pathParts.length == 2) {
                String response = gson.toJson(taskManager.getHistory());
                sendText(httpExchange, response, 200);
            } else {
                sendBadRequest(httpExchange, "Запрос составлен некорректно");
            }
        } catch (Exception e) {
            sendInternalError(httpExchange);
        }
    }
}
