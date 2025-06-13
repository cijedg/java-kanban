package http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TasksHandlerTest extends BaseApiTest {
    private static final String BASE_ENDPOINT = "/tasks";
    private static final String ENDPOINT_WITH_ID = "/tasks/";

    @Test
    void shouldGetEmptyAllTasks() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", BASE_ENDPOINT, "");
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldGetAllTasks() throws IOException, InterruptedException {
        String taskJson = "{\"name\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"IN_PROGRESS\"}";
        String task1Json = "{\"name\":\"Task with time\",\"description\":\"Description\"," +
                "\"status\":\"DONE\",\"startTime\":\"2023-01-01T10:00:00\",\"duration\":\"PT1H\"}";
        HttpResponse<String> responseTask = sendRequest("POST", BASE_ENDPOINT, taskJson);
        HttpResponse<String> responseTask1 = sendRequest("POST", BASE_ENDPOINT, task1Json);

        HttpResponse<String> finalResponse = sendRequest("GET", BASE_ENDPOINT, "");
        assertEquals(200, finalResponse.statusCode());
        assertTrue(finalResponse.body().contains("Test Task"));
        assertTrue(finalResponse.body().contains("2023-01-01T10:00:00"));
    }

    @Test
    void shouldGetTaskById() throws IOException, InterruptedException {
        String taskJson = "{\"name\":\"Test Task\",\"description\":\"Test Description\",\"status\":\"IN_PROGRESS\"}";
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, taskJson);
        int taskId = Integer.parseInt(postResponse.body().substring(postResponse.body().indexOf("=") + 1).trim());

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + taskId, "");
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Test Task"));
    }

    @Test
    void shouldReturnNotFoundForNotExistingId() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", ENDPOINT_WITH_ID + 5, "");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturnBadRequestForInvalidId() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", ENDPOINT_WITH_ID + "five", "");
        HttpResponse<String> otherResponse = sendRequest("GET", ENDPOINT_WITH_ID + "85/", "");
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Bad Request"));
    }

    @Test
    void shouldCreateTask() throws IOException, InterruptedException {
        String taskJson = "{\"name\":\"New Task\",\"description\":\"New Description\"}";
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, taskJson);

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Добавлена задача с id = "));
    }

    @Test
    void shouldUpdateTask() throws IOException, InterruptedException {
        String taskJson = "{\"name\":\"Original Task\",\"description\":\"Original Description\"}";
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, taskJson);
        int taskId = Integer.parseInt(postResponse.body().split("=")[1].trim());

        String updatedTaskJson = "{\"id\":" + taskId + ",\"name\":\"Updated Task\",\"description\":\"Updated Description\"}";
        HttpResponse<String> updateResponse = sendRequest("POST", ENDPOINT_WITH_ID + taskId, updatedTaskJson);

        assertEquals(201, updateResponse.statusCode());
        assertTrue(updateResponse.body().contains("Задача успешно обновлена"));

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + taskId, "");
        assertTrue(getResponse.body().contains("Updated Task"));
    }

    @Test
    void shouldHandlePartialTaskUpdate() throws IOException, InterruptedException {
        String taskJson = "{\"name\":\"Original\",\"description\":\"Original\"}";
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, taskJson);
        int taskId = Integer.parseInt(postResponse.body().split("=")[1].trim());

        String updateJson = "{\"id\":" + taskId + ",\"name\":\"Updated\"}";
        HttpResponse<String> updateResponse = sendRequest("POST", ENDPOINT_WITH_ID + taskId, updateJson);
        assertEquals(201, updateResponse.statusCode());

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + taskId, "");
        assertTrue(getResponse.body().contains("Updated"));
        assertTrue(getResponse.body().contains("Original"));
    }

    @Test
    void shouldUpdateOnlyTimeFields() throws IOException, InterruptedException {
        String taskJson = "{\"name\":\"Task\",\"description\":\"Desc\"," +
                "\"startTime\":\"2023-01-01T10:00:00\",\"duration\":\"PT1H\"}";
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, taskJson);
        int taskId = Integer.parseInt(postResponse.body().split("=")[1].trim());

        String updateJson = "{\"id\":" + taskId +
                ",\"startTime\":\"2023-01-03T15:30:00\"}";
        HttpResponse<String> updateResponse = sendRequest("POST", ENDPOINT_WITH_ID + taskId, updateJson);
        assertEquals(201, updateResponse.statusCode());

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + taskId, "");
        String responseBody = getResponse.body();

        assertTrue(responseBody.contains("2023-01-03T15:30:00"));
        assertTrue(responseBody.contains("PT1H"));
        assertTrue(responseBody.contains("Task"));
    }

    @Test
    void shouldDeleteTask() throws IOException, InterruptedException {
        String taskJson = "{\"name\":\"Task to delete\",\"description\":\"Will be deleted\"}";
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, taskJson);
        int taskId = Integer.parseInt(postResponse.body().split("=")[1].trim());

        HttpResponse<String> deleteResponse = sendRequest("DELETE", ENDPOINT_WITH_ID + taskId, "");
        assertEquals(200, deleteResponse.statusCode());
        assertTrue(deleteResponse.body().contains("Задача с id = " + taskId + " удалена"));

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + taskId, "");
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void shouldReturnBadRequestForEmptyTaskNameOrDescription() throws IOException, InterruptedException {
        String invalidJson = "{\"name\":\"Task\",\"description\":\"\"}";
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, invalidJson);
        assertEquals(400, response.statusCode());

        String invalidJson1 = "{\"name\":\"\",\"description\":\"Be cool\"}";
        HttpResponse<String> response1 = sendRequest("POST", BASE_ENDPOINT, invalidJson1);
        assertEquals(400, response1.statusCode());
    }

    @Test
    void shouldReturnBadRequestForInvalidJson() throws IOException, InterruptedException {
        String invalidJson = "{\"name\":\"Task\",\"description\":\"unclosed string...}";
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, invalidJson);
        assertEquals(400, response.statusCode());

        String invalidJson1 = "{\"name\":\"Task\",\"description\":}";
        HttpResponse<String> response1 = sendRequest("POST", BASE_ENDPOINT, invalidJson1);
        assertEquals(400, response1.statusCode());
    }

    @Test
    void shouldReturnNotAcceptableForMissingAcceptHeader() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + BASE_ENDPOINT))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Требуется заголовок Accept: application/json"));
    }

    @Test
    void shouldReturnNotAcceptableIfTaskHasTimeOverlap() throws IOException, InterruptedException {
        String taskJson = "{\"name\":\"Test Task\",\"description\":\"Test Description\"," +
                "\"status\":\"IN_PROGRESS\",\"startTime\":\"2023-01-01T10:59:00\"}";
        String task1Json = "{\"name\":\"Task with time\",\"description\":\"Description\"," +
                "\"status\":\"DONE\",\"startTime\":\"2023-01-01T10:00:00\",\"duration\":\"PT1H\"}";
        HttpResponse<String> responseTask = sendRequest("POST", BASE_ENDPOINT, taskJson);
        HttpResponse<String> responseTask1 = sendRequest("POST", BASE_ENDPOINT, task1Json);

        assertEquals(406, responseTask1.statusCode());
        assertTrue(responseTask1.body().contains("Задача пересекается по времени с существующей"));
    }

    @Test
    void shouldReturnBadRequestWhenUrlIdAndBodyIdMismatch() throws IOException, InterruptedException {
        String taskJson = "{\"id\":1,\"name\":\"Task\",\"description\":\"Desc\"}";
        HttpResponse<String> response = sendRequest("POST", ENDPOINT_WITH_ID + "2", taskJson);
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Id в URL и теле запроса не совпадают"));
    }

    @Test
    void shouldReturnMethodNotAllowedForUnsupportedMethods() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + BASE_ENDPOINT))
                .method("PUT", HttpRequest.BodyPublishers.ofString("{}"))
                .header("Accept", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode());
    }
}

