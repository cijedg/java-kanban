package http;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrioritizedHandlerTest extends BaseApiTest {
    private static final String PRIORITIZED_URL = "/prioritized";

    @Test
    void shouldReturnEmptyListWhenNoTasksWithTime() throws Exception {
        String task = "{\"name\":\"Task\",\"description\":\"Desc\"}";
        sendRequest("POST", "/tasks", task);

        HttpResponse<String> response = sendRequest("GET", PRIORITIZED_URL, "");

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body(), "Задачи без стартового времени не должны попадать в список");
    }

    @Test
    void shouldAddOnlyTasksWithTime() throws Exception {
        String taskWithTime = "{\"name\":\"WithTime\",\"description\":\"Desc\",\"startTime\":\"2023-01-01T10:00:00\",\"duration\":\"PT1H\"}";
        String taskWithoutTime = "{\"name\":\"WithoutTime\",\"description\":\"Desc\"}";

        sendRequest("POST", "/tasks", taskWithTime);
        sendRequest("POST", "/tasks", taskWithoutTime);

        HttpResponse<String> response = sendRequest("GET", PRIORITIZED_URL, "");
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

        assertEquals(1, jsonArray.size(), "Должна быть только одна задача с временем");
        assertEquals("WithTime", jsonArray.get(0).getAsJsonObject().get("name").getAsString());
    }

    @Test
    void shouldReturnTasksInTimeOrder() throws Exception {
        String task1 = "{\"name\":\"Task1\",\"description\":\"Desc\",\"startTime\":\"2023-01-01T11:00:00\",\"duration\":\"PT1H\"}";
        String task2 = "{\"name\":\"Task2\",\"description\":\"Desc\",\"startTime\":\"2023-01-01T09:00:00\",\"duration\":\"PT1H\"}";
        String task3 = "{\"name\":\"Task3\",\"description\":\"Desc\",\"startTime\":\"2023-01-01T10:00:00\",\"duration\":\"PT1H\"}";

        sendRequest("POST", "/tasks", task1);
        sendRequest("POST", "/tasks", task2);
        sendRequest("POST", "/tasks", task3);

        HttpResponse<String> response = sendRequest("GET", PRIORITIZED_URL, "");
        JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();

        assertEquals(3, jsonArray.size());
        assertEquals("Task2", jsonArray.get(0).getAsJsonObject().get("name").getAsString(), "Задачи должны храниться в отсортированном виде");
        assertEquals("Task3", jsonArray.get(1).getAsJsonObject().get("name").getAsString(), "Задачи должны храниться в отсортированном виде");
        assertEquals("Task1", jsonArray.get(2).getAsJsonObject().get("name").getAsString(), "Задачи должны храниться в отсортированном виде");
    }

    @Test
    void shouldReturnBadRequestForInvalidPath() throws Exception {
        HttpResponse<String> response = sendRequest("GET", PRIORITIZED_URL + "/invalid", "");
        assertEquals(400, response.statusCode());
    }

    @Test
    void shouldReturnNotAcceptableWithoutAcceptHeader() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + PRIORITIZED_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    void shouldReturnMethodNotAllowedForNonGetMethods() throws Exception {
        HttpResponse<String> postResponse = sendRequest("POST", PRIORITIZED_URL, "{}");
        assertEquals(405, postResponse.statusCode());
    }
}