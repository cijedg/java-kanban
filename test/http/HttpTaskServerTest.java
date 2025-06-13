package http;

import manager.InMemoryTaskManager;
import manager.TaskManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.HttpTaskServer;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;


class HttpTaskServerTest {
    private TaskManager manager;
    private HttpTaskServer server;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void shouldStartServer() {
        assertDoesNotThrow(server::start, "Сервер должен запускаться без ошибок");
    }

    @Test
    void shouldStopThrow() {
        assertDoesNotThrow(server::stop, "Сервер должен завершать работу без ошибок");
    }

    @Test
    void shouldInitializeWithProvidedManager() {
        assertSame(manager, server.getTaskManager(), "Сервер должен создаваться с переданными менеджером");
    }

    @Test
    void shouldThrowIfPortIsBusy() throws InterruptedException, IOException {
        server.start();
        HttpTaskServer server2 = new HttpTaskServer(manager);
        assertThrows(IOException.class, server2::start,
                "Должно выбрасываться исключение при попытке запустить сервер на занятом порте");
    }

    @Test
    void shouldThrowWhenStartingAlreadyRunningServer() throws IOException {
        server.start();
        assertThrows(Exception.class, server::start,
                "Должно выбрасываться исключение при повторном запуске сервера");
    }
}