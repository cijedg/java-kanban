package http;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class BaseApiTest {
    protected static final String BASE_URL = "http://localhost:8080";
    protected TaskManager manager = new InMemoryTaskManager();
    protected HttpTaskServer server = new HttpTaskServer(manager);
    protected HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    protected void setUp() throws IOException {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        server.start();
    }

    @AfterEach
    protected void tearDown() {
        server.stop();
    }


    protected HttpResponse<String> sendRequest(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
        switch (method.toUpperCase()) {
            case "GET" -> builder.GET();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body));
            case "DELETE" -> builder.DELETE();
            default -> builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        }
        HttpRequest request = builder.build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
