package http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubtasksHandlerTest extends BaseApiTest {
    private static final String BASE_ENDPOINT = "/subtasks";
    private static final String ENDPOINT_WITH_ID = "/subtasks/";
    private int epicId; // эпик для подзадач

    @BeforeEach
    void setUpEpic() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Test Epic\",\"description\":\"Epic for subtasks\"}";
        HttpResponse<String> response = sendRequest("POST", "/epics", epicJson);
        epicId = Integer.parseInt(response.body().substring(response.body().indexOf("=") + 1).trim());
    }

    @Test
    void shouldGetEmptyAllSubtasks() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", BASE_ENDPOINT, "");
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldGetAllSubtasks() throws IOException, InterruptedException {
        String subtaskJson = String.format("{\"name\":\"Subtask 1\",\"description\":\"Desc 1\",\"epicId\":%d}", epicId);
        String subtaskJson2 = String.format("{\"name\":\"Subtask 2\",\"description\":\"Desc 2\"," +
                "\"epicId\":%d,\"startTime\":\"2023-01-01T10:00:00\",\"duration\":\"PT1H\"}", epicId);

        sendRequest("POST", BASE_ENDPOINT, subtaskJson);
        sendRequest("POST", BASE_ENDPOINT, subtaskJson2);

        HttpResponse<String> response = sendRequest("GET", BASE_ENDPOINT, "");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask 1"));
        assertTrue(response.body().contains("2023-01-01T10:00:00"));
    }

    @Test
    void shouldGetSubtaskById() throws IOException, InterruptedException {
        String subtaskJson = String.format("{\"name\":\"Test Subtask\",\"description\":\"Test Desc\",\"epicId\":%d}", epicId);
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, subtaskJson);
        int subtaskId = Integer.parseInt(postResponse.body().substring(postResponse.body().indexOf("=") + 1).trim());

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + subtaskId, "");
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Test Subtask"));
    }

    @Test
    void shouldReturnNotFoundForNotExistingId() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", ENDPOINT_WITH_ID + 999, "");
        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Not Found"));
    }

    @Test
    void shouldReturnNotFoundWhenEpicIdDoesNotExist() throws IOException, InterruptedException {
        String subtaskJson = "{\"name\":\"Test Subtask\",\"description\":\"Test Desc\",\"epicId\":85}";

        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, subtaskJson);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Bad Request. Передан неверный id эпика"));
    }

    @Test
    void shouldReturnBadRequestForInvalidId() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", ENDPOINT_WITH_ID + "invalid", "");
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Bad Request"));
    }

    @Test
    void shouldCreateSubtask() throws IOException, InterruptedException {
        String subtaskJson = String.format("{\"name\":\"New Subtask\",\"description\":\"New Desc\",\"epicId\":%d}", epicId);
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, subtaskJson);

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Добавлена подзадача с id = "));
    }

    @Test
    void shouldReturnBadRequestWhenCreatingSubtaskWithoutEpic() throws IOException, InterruptedException {
        String subtaskJson = "{\"name\":\"No Epic\",\"description\":\"No epic specified\"}";
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, subtaskJson);

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Bad Request"));
    }

    @Test
    void shouldUpdateSubtask() throws IOException, InterruptedException {
        String subtaskJson = String.format("{\"name\":\"Original\",\"description\":\"Original Desc\",\"epicId\":%d}", epicId);
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, subtaskJson);
        int subtaskId = Integer.parseInt(postResponse.body().split("=")[1].trim());

        String updatedJson = String.format("{\"id\":%d,\"name\":\"Updated\",\"description\":\"Updated Desc\",\"epicId\":%d}",
                subtaskId, epicId);
        HttpResponse<String> updateResponse = sendRequest("POST", ENDPOINT_WITH_ID + subtaskId, updatedJson);

        assertEquals(201, updateResponse.statusCode());
        assertTrue(updateResponse.body().contains("Подзадача успешно обновлена"));

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + subtaskId, "");
        assertTrue(getResponse.body().contains("Updated"));
    }

    @Test
    void shouldHandlePartialSubtaskUpdate() throws IOException, InterruptedException {
        String subtaskJson = String.format("{\"name\":\"Original\",\"description\":\"Original Desc\",\"epicId\":%d}", epicId);
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, subtaskJson);
        int subtaskId = Integer.parseInt(postResponse.body().split("=")[1].trim());

        String updateJson = "{\"id\":" + subtaskId + ",\"name\":\"Updated\"}";
        HttpResponse<String> updateResponse = sendRequest("POST", ENDPOINT_WITH_ID + subtaskId, updateJson);
        assertEquals(201, updateResponse.statusCode());

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + subtaskId, "");
        assertTrue(getResponse.body().contains("Updated"));
        assertTrue(getResponse.body().contains("Original Desc")); // Описание осталось
    }

    @Test
    void shouldUpdateOnlyTimeFields() throws IOException, InterruptedException {
        String subtaskJson = String.format("{\"name\":\"Subtask\",\"description\":\"Desc\"," +
                "\"epicId\":%d,\"startTime\":\"2023-01-01T10:00:00\",\"duration\":\"PT1H\"}", epicId);
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, subtaskJson);
        int subtaskId = Integer.parseInt(postResponse.body().substring(postResponse.body().indexOf("=") + 1).trim());

        String updateJson = "{\"id\":" + subtaskId + ",\"startTime\":\"2023-01-03T15:30:00\"}";
        HttpResponse<String> updateResponse = sendRequest("POST", ENDPOINT_WITH_ID + subtaskId, updateJson);
        assertEquals(201, updateResponse.statusCode());

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + subtaskId, "");
        String responseBody = getResponse.body();

        assertTrue(responseBody.contains("2023-01-03T15:30:00"));
        assertTrue(responseBody.contains("PT1H"));
        assertTrue(responseBody.contains("Subtask"));
    }

    @Test
    void shouldDeleteSubtask() throws IOException, InterruptedException {
        String subtaskJson = String.format("{\"name\":\"To Delete\",\"description\":\"Will be deleted\",\"epicId\":%d}", epicId);
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, subtaskJson);
        int subtaskId = Integer.parseInt(postResponse.body().split("=")[1].trim());

        HttpResponse<String> deleteResponse = sendRequest("DELETE", ENDPOINT_WITH_ID + subtaskId, "");
        assertEquals(200, deleteResponse.statusCode());
        assertTrue(deleteResponse.body().contains("Подзадача с id = " + subtaskId + " удалена"));

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + subtaskId, "");
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void shouldReturnBadRequestForEmptySubtaskNameOrDescription() throws IOException, InterruptedException {
        String invalidJson = String.format("{\"name\":\"\",\"description\":\"Desc\",\"epicId\":%d}", epicId);
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, invalidJson);
        assertEquals(400, response.statusCode());

        String invalidJson2 = String.format("{\"name\":\"Name\",\"description\":\"\",\"epicId\":%d}", epicId);
        HttpResponse<String> response2 = sendRequest("POST", BASE_ENDPOINT, invalidJson2);
        assertEquals(400, response2.statusCode());
    }

    @Test
    void shouldReturnBadRequestForInvalidJson() throws IOException, InterruptedException {
        String invalidJson = String.format("{\"name\":\"Subtask\",\"description\":\"unclosed string...,\"epicId\":%d}", epicId);
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, invalidJson);
        assertEquals(400, response.statusCode());
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
    void shouldReturnNotAcceptableIfSubtaskHasTimeOverlap() throws IOException, InterruptedException {
        String subtask1Json = String.format("{\"name\":\"Subtask 1\",\"description\":\"Desc 1\"," +
                "\"epicId\":%d,\"startTime\":\"2023-01-01T10:00:00\",\"duration\":\"PT1H\"}", epicId);
        String subtask2Json = String.format("{\"name\":\"Subtask 2\",\"description\":\"Desc 2\"," +
                "\"epicId\":%d,\"startTime\":\"2023-01-01T10:30:00\",\"duration\":\"PT1H\"}", epicId);

        sendRequest("POST", BASE_ENDPOINT, subtask1Json);
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, subtask2Json);

        assertEquals(406, response.statusCode());
        assertTrue(response.body().contains("Подзадача пересекается по времени с существующей"));
    }

    @Test
    void shouldReturnBadRequestWhenUrlIdAndBodyIdMismatch() throws IOException, InterruptedException {
        String subtaskJson = String.format("{\"id\":1,\"name\":\"Subtask\",\"description\":\"Desc\",\"epicId\":%d}", epicId);
        HttpResponse<String> response = sendRequest("POST", ENDPOINT_WITH_ID + "2", subtaskJson);
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
