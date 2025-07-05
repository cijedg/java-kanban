package server.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import model.Status;

import java.io.IOException;

public class StatusAdapter extends TypeAdapter<Status> {
    @Override
    public void write(JsonWriter jsonWriter, Status value) throws IOException {
        if (value == null) {
            jsonWriter.value(String.valueOf(Status.NEW));
        } else {
            jsonWriter.value(String.valueOf(value));
        }
    }

    @Override
    public Status read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == null) {
            return Status.NEW;
        }
        return Status.valueOf(jsonReader.nextString());
    }
}
