package http;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistoryHandlerTest extends BaseApiTest {
    private static final String HISTORY_URL = "/history";

    @Test
    void shouldReturnEmptyListWhenNoTasksViewed() throws Exception {
        HttpResponse<String> response = sendRequest("GET", HISTORY_URL, "");

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldReturnListOfViewedTasks() throws Exception {
        String task1 = "{\"name\":\"Task 1\",\"description\":\"Desc 1\"}";
        String task2 = "{\"name\":\"Task 2\",\"description\":\"Desc 2\"}";

        HttpResponse<String> create1 = sendRequest("POST", "/tasks", task1);
        HttpResponse<String> create2 = sendRequest("POST", "/tasks", task2);

        int id1 = Integer.parseInt(create1.body().split("=")[1].trim());
        int id2 = Integer.parseInt(create2.body().split("=")[1].trim());

        sendRequest("GET", "/tasks/" + id1, "");
        sendRequest("GET", "/tasks/" + id2, "");

        HttpResponse<String> history = sendRequest("GET", HISTORY_URL, "");
        JsonArray jsonArray = JsonParser.parseString(history.body()).getAsJsonArray();

        List<Integer> ids = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            ids.add(element.getAsJsonObject().get("id").getAsInt());
        }
        assertEquals(2, ids.size(), "Должны быть 2 задачи в истории");

        JsonObject historyTask1 = jsonArray.get(0).getAsJsonObject();
        assertEquals("Task 1", historyTask1.get("name").getAsString());
        assertEquals("Desc 1", historyTask1.get("description").getAsString());
    }

    @Test
    void shouldReturnTasksInCorrectOrder() throws Exception {
        String task1 = "{\"name\":\"Task 1\",\"description\":\"desc\"}";
        String task2 = "{\"name\":\"Task 2\",\"description\":\"desc\"}";
        String task3 = "{\"name\":\"Task 3\",\"description\":\"desc\"}";

        HttpResponse<String> create1 = sendRequest("POST", "/tasks", task1);
        HttpResponse<String> create2 = sendRequest("POST", "/tasks", task2);
        HttpResponse<String> create3 = sendRequest("POST", "/tasks", task3);

        int id1 = Integer.parseInt(create1.body().substring(create1.body().indexOf("=") + 1).trim());
        int id2 = Integer.parseInt(create2.body().substring(create2.body().indexOf("=") + 1).trim());
        int id3 = Integer.parseInt(create3.body().substring(create3.body().indexOf("=") + 1).trim());

        sendRequest("GET", "/tasks/" + id1, "");
        sendRequest("GET", "/tasks/" + id3, "");
        sendRequest("GET", "/tasks/" + id2, "");
        sendRequest("GET", "/tasks/" + id1, "");

        HttpResponse<String> history = sendRequest("GET", HISTORY_URL, "");
        JsonArray jsonArray = JsonParser.parseString(history.body()).getAsJsonArray();

        List<Integer> ids = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            ids.add(element.getAsJsonObject().get("id").getAsInt());
        }

        assertEquals(3, ids.size(), "Должны быть 3 задачи в истории");
        assertEquals(List.of(id3, id2, id1), ids, "Порядок задач не совпадает");
    }

    @Test
    void shouldReturnBadRequestForInvalidPath() throws Exception {
        HttpResponse<String> response = sendRequest("GET", HISTORY_URL + "/extra", "");

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Запрос составлен некорректно"));
    }

    @Test
    void shouldReturnNotAcceptableWithoutAcceptHeader() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Требуется заголовок Accept: application/json"));
    }

    @Test
    void shouldReturnMethodNotAllowed() throws Exception {
        HttpResponse<String> response = sendRequest("POST", HISTORY_URL, "{}");

        assertEquals(405, response.statusCode());
        assertTrue(response.body().contains("Метод не поддерживается"));
    }
}