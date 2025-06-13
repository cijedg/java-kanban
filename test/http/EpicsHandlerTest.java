package http;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EpicsHandlerTest extends BaseApiTest {
    private static final String BASE_ENDPOINT = "/epics";
    private static final String ENDPOINT_WITH_ID = "/epics/";
    private static final String SUBTASKS_ENDPOINT = "/subtasks";

    @Test
    void shouldGetEmptyAllEpics() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", BASE_ENDPOINT, "");
        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @Test
    void shouldGetAllEpics() throws IOException, InterruptedException {
        String epicJson1 = "{\"name\":\"Epic 1\",\"description\":\"Description 1\"}";
        String epicJson2 = "{\"name\":\"Epic 2\",\"description\":\"Description 2\"}";

        sendRequest("POST", BASE_ENDPOINT, epicJson1);
        sendRequest("POST", BASE_ENDPOINT, epicJson2);

        HttpResponse<String> response = sendRequest("GET", BASE_ENDPOINT, "");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Epic 1"));
        assertTrue(response.body().contains("Epic 2"));
    }

    @Test
    void shouldGetEpicById() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Test Epic\",\"description\":\"Test Description\"}";
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, epicJson);
        int epicId = extractIdFromResponse(postResponse);

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Test Epic"));
    }

    @Test
    void shouldGetEpicSubtasks() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Test Epic\",\"description\":\"Test Description\"}";
        HttpResponse<String> epicResponse = sendRequest("POST", BASE_ENDPOINT, epicJson);
        int epicId = extractIdFromResponse(epicResponse);

        String subtaskJson1 = String.format("{\"name\":\"Subtask 1\",\"description\":\"Desc 1\",\"epicId\":%d}", epicId);
        String subtaskJson2 = String.format("{\"name\":\"Subtask 2\",\"description\":\"Desc 2\",\"epicId\":%d}", epicId);
        sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson1);
        sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson2);

        HttpResponse<String> response = sendRequest("GET", ENDPOINT_WITH_ID + epicId + "/subtasks", "");
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask 1"));
        assertTrue(response.body().contains("Subtask 2"));
    }

    @Test
    void shouldReturnNotFoundForNotExistingEpic() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", ENDPOINT_WITH_ID + 999, "");
        assertEquals(404, response.statusCode());
    }

    @Test
    void shouldCreateEpic() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"New Epic\",\"description\":\"New Description\"}";
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, epicJson);

        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains("Добавлен эпик с id = "));
    }

    @Test
    void shouldReturnBadRequestForEmptyEpicNameOrDescription() throws IOException, InterruptedException {
        String invalidJson = "{\"name\":\"\",\"description\":\"Description\"}";
        HttpResponse<String> response = sendRequest("POST", BASE_ENDPOINT, invalidJson);
        assertEquals(400, response.statusCode());

        String invalidJson2 = "{\"name\":\"Name\",\"description\":\"\"}";
        HttpResponse<String> response2 = sendRequest("POST", BASE_ENDPOINT, invalidJson2);
        assertEquals(400, response2.statusCode());
    }

    @Test
    void shouldDeleteEpic() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Epic to delete\",\"description\":\"Will be deleted\"}";
        HttpResponse<String> postResponse = sendRequest("POST", BASE_ENDPOINT, epicJson);
        int epicId = extractIdFromResponse(postResponse);

        HttpResponse<String> deleteResponse = sendRequest("DELETE", ENDPOINT_WITH_ID + epicId, "");
        assertEquals(200, deleteResponse.statusCode());
        assertTrue(deleteResponse.body().contains("Эпик с id = " + epicId + " удален"));

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        assertEquals(404, getResponse.statusCode());
    }

    @Test
    void shouldDeleteEpicWithSubtasks() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Epic with subtasks\",\"description\":\"Desc\"}";
        HttpResponse<String> epicResponse = sendRequest("POST", BASE_ENDPOINT, epicJson);
        int epicId = extractIdFromResponse(epicResponse);

        String subtaskJson = String.format("{\"name\":\"Subtask\",\"description\":\"Desc\",\"epicId\":%d}", epicId);
        sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson);

        HttpResponse<String> deleteResponse = sendRequest("DELETE", ENDPOINT_WITH_ID + epicId, "");
        assertEquals(200, deleteResponse.statusCode());

        HttpResponse<String> subtasksResponse = sendRequest("GET", SUBTASKS_ENDPOINT, "");
        assertFalse(subtasksResponse.body().contains("Subtask"));
    }

    @Test
    void shouldReturnBadRequestForInvalidPath() throws IOException, InterruptedException {
        HttpResponse<String> response = sendRequest("GET", BASE_ENDPOINT + "/invalid/path", "");
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

    @Test
    void shouldUpdateEpicTimeWhenAddingSubtasks() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Time Epic\",\"description\":\"Time Desc\"}";
        HttpResponse<String> epicResponse = sendRequest("POST", BASE_ENDPOINT, epicJson);
        int epicId = extractIdFromResponse(epicResponse);

        String subtaskJson1 = String.format("{\"name\":\"Sub 1\",\"description\":\"Desc 1\",\"epicId\":%d," +
                "\"startTime\":\"2023-01-01T10:00:00\",\"duration\":PT1H}", epicId);
        String subtaskJson2 = String.format("{\"name\":\"Sub 2\",\"description\":\"Desc 2\",\"epicId\":%d," +
                "\"startTime\":\"2023-01-01T12:00:00\",\"duration\":PT30M}", epicId);

        sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson1);
        sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson2);

        HttpResponse<String> epicGetResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        String epicBody = epicGetResponse.body();

        assertTrue(epicBody.contains("\"startTime\": \"2023-01-01T10:00:00\""));
        assertTrue(epicBody.contains("\"duration\": \"PT2H30M\""));
        assertTrue(epicBody.contains("\"endTime\": \"2023-01-01T12:30:00\""));
    }

    @Test
    void shouldUpdateEpicStatusBasedOnSubtasks() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Status Epic\",\"description\":\"Status Desc\"}";
        HttpResponse<String> epicResponse = sendRequest("POST", BASE_ENDPOINT, epicJson);
        int epicId = extractIdFromResponse(epicResponse);

        HttpResponse<String> initialStatusResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        assertTrue(initialStatusResponse.body().contains("\"status\": \"NEW\""));

        String subtaskJson1 = String.format("{\"name\":\"Sub 1\",\"description\":\"Desc 1\",\"epicId\":%d}", epicId);
        int subtaskId = extractIdFromResponse(sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson1));
        HttpResponse<String> newStatusResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        assertTrue(newStatusResponse.body().contains("\"status\": \"NEW\""));

        String updateJson = String.format("{\"id\":%d,\"name\":\"Sub 1\",\"description\":\"Desc 1\"," +
                "\"epicId\":%d,\"status\":\"IN_PROGRESS\"}", subtaskId, epicId);
        sendRequest("POST", SUBTASKS_ENDPOINT, updateJson);
        HttpResponse<String> inProgressResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        assertTrue(inProgressResponse.body().contains("\"status\": \"IN_PROGRESS\""));

        String subtaskJson2 = String.format("{\"name\":\"Sub 2\",\"description\":\"Desc 2\",\"epicId\":%d," +
                "\"status\":\"DONE\"}", epicId);
        sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson2);
        HttpResponse<String> mixedStatusResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        assertTrue(mixedStatusResponse.body().contains("\"status\": \"IN_PROGRESS\""));

        String updateAllJson = String.format("{\"id\":%d,\"name\":\"Sub 1\",\"description\":\"Desc 1\"," +
                "\"epicId\":%d,\"status\":\"DONE\"}", subtaskId, epicId);
        sendRequest("POST", SUBTASKS_ENDPOINT, updateAllJson);
        HttpResponse<String> doneStatusResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        assertTrue(doneStatusResponse.body().contains("\"status\": \"DONE\""));
    }

    @Test
    void shouldHandleEpicWithoutSubtasksTime() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Empty Epic\",\"description\":\"Empty Desc\"}";
        HttpResponse<String> epicResponse = sendRequest("POST", BASE_ENDPOINT, epicJson);
        int epicId = extractIdFromResponse(epicResponse);

        HttpResponse<String> getResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        String responseBody = getResponse.body();

        assertTrue(responseBody.contains("\"startTime\": null"));
        assertTrue(responseBody.contains("\"duration\": null"));
        assertTrue(responseBody.contains("\"endTime\": null"));
    }

    @Test
    void shouldUpdateEpicTimeWhenDeletingSubtasks() throws IOException, InterruptedException {
        String epicJson = "{\"name\":\"Delete Time Epic\",\"description\":\"Delete Time Desc\"}";
        HttpResponse<String> epicResponse = sendRequest("POST", BASE_ENDPOINT, epicJson);
        int epicId = extractIdFromResponse(epicResponse);

        String subtaskJson1 = String.format("{\"name\":\"Sub 1\",\"description\":\"Desc 1\",\"epicId\":%d," +
                "\"startTime\":\"2023-01-01T10:00:00\",\"duration\":PT1H}", epicId);
        String subtaskJson2 = String.format("{\"name\":\"Sub 2\",\"description\":\"Desc 2\",\"epicId\":%d," +
                "\"startTime\":\"2023-01-01T12:00:00\",\"duration\":PT30M}", epicId);

        HttpResponse<String> sub1Response = sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson1);
        HttpResponse<String> sub2Response = sendRequest("POST", SUBTASKS_ENDPOINT, subtaskJson2);
        int sub1Id = extractIdFromResponse(sub1Response);

        sendRequest("DELETE", SUBTASKS_ENDPOINT + "/" + sub1Id, "");

        HttpResponse<String> epicGetResponse = sendRequest("GET", ENDPOINT_WITH_ID + epicId, "");
        String epicBody = epicGetResponse.body();

        assertTrue(epicBody.contains("\"startTime\": \"2023-01-01T12:00:00\""));
        assertTrue(epicBody.contains("\"duration\": \"PT30M\""));
        assertTrue(epicBody.contains("\"endTime\": \"2023-01-01T12:30:00\""));
    }

    private int extractIdFromResponse(HttpResponse<String> response) {
        String body = response.body();
        return Integer.parseInt(body.substring(body.lastIndexOf("=") + 1).trim());
    }
}