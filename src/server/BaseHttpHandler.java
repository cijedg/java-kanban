package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Status;
import server.adapters.DurationAdapter;
import server.adapters.LocalDateTimeAdapter;
import server.adapters.StatusAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class BaseHttpHandler {
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .registerTypeAdapter(Status.class, new StatusAdapter())
                .create();
    }

    protected void sendText(HttpExchange httpExchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(DEFAULT_CHARSET);
        if (response.length > 0) {
            httpExchange.getResponseHeaders().set("Content-Type", "application/json;charset=utf-8");
        }
        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(statusCode, response.length);
            os.write(response);
        }
    }

    protected void sendNotFound(HttpExchange httpExchange, String text) throws IOException {
        byte[] response = ("Not Found. " + text).getBytes(DEFAULT_CHARSET);
        httpExchange.sendResponseHeaders(404, response.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendInternalError(HttpExchange httpExchange) throws IOException {
        byte[] response = "Internal Server Error".getBytes(DEFAULT_CHARSET);
        httpExchange.sendResponseHeaders(500, response.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendBadRequest(HttpExchange httpExchange, String text) throws IOException {
        byte[] response = ("Bad Request. " + text).getBytes(DEFAULT_CHARSET);
        httpExchange.sendResponseHeaders(400, response.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendNotAcceptable(HttpExchange httpExchange, String text) throws IOException {
        byte[] response = ("Not Acceptable. " + text).getBytes(DEFAULT_CHARSET);
        httpExchange.sendResponseHeaders(406, response.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response);
        }
    }

    protected void sendMethodNotAllowed(HttpExchange httpExchange, String text) throws IOException {
        byte[] response = ("Method Not Allowed. " + text).getBytes(DEFAULT_CHARSET);
        httpExchange.sendResponseHeaders(405, response.length);
        try (OutputStream os = httpExchange.getResponseBody()) {
            os.write(response);
        }
    }
}
