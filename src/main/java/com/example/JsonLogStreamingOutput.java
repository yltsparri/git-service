package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class JsonLogStreamingOutput implements StreamingOutput {
    private static final String EMAIL_END_SYMBOL = ">";
    private static final String EMAIL_START_SYMBOL = "<";
    private static final String DATE_FIELD_NAME = "date";
    private static final String EMAIL_FIELD_NAME = "email";
    private static final String NAME_FIELD_NAME = "name";
    private static final int LINE_SPACE_OFFSET = 1;
    private static final String PARENTS_FIELD_NAME = "parents";
    private static final String PARENT_LINE_START = "parent";
    private static final String COMMITTER_LINE_START = "committer";
    private static final String COMMITTER_FIELD_NAME = COMMITTER_LINE_START;
    private static final String AUTHOR_LINE_START = "author";
    private static final String AUTHOR_FIELD_NAME = AUTHOR_LINE_START;
    private static final String TREE_LINE_START = "tree";
    private static final String TREE_FIELD_NAME = TREE_LINE_START;
    private static final String COMMIT_LINE_START = "commit ";
    private static final String COMMIT_FIELD_NAME = "commit";
    private final InputStream stream;

    public JsonLogStreamingOutput(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Stream cannot be null");
        }
        this.stream = stream;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {

        JsonFactory factory = new JsonFactory();
        try (Writer writer = new OutputStreamWriter(output)) {
            write(factory, writer);
        }
    }

    private void write(JsonFactory factory, Writer writer) throws IOException {
        try (JsonGenerator jsonGenerator = factory.createGenerator(writer)) {
            write(jsonGenerator);
        }
    }

    private void write(JsonGenerator jsonGenerator) throws IOException {
        try (InputStreamReader streamReader = new InputStreamReader(stream)) {
            write(jsonGenerator, streamReader);
        }
    }

    private void write(JsonGenerator jsonGenerator, InputStreamReader streamReader) throws IOException {
        try (BufferedReader reader = new BufferedReader(streamReader)) {
            write(jsonGenerator, reader);
        }
    }

    private void write(JsonGenerator jsonGenerator, BufferedReader reader) throws IOException {
        String line = reader.readLine();
        jsonGenerator.writeStartArray();

        ArrayList<String> parents = new ArrayList<String>();
        while (line != null) {
            line = processLine(jsonGenerator, reader, line, parents);
        }
        jsonGenerator.writeEndArray();
        jsonGenerator.flush();
    }

    private String processLine(JsonGenerator jsonGenerator, BufferedReader in, String line, ArrayList<String> parents)
            throws IOException {
        if (line.startsWith(COMMIT_LINE_START)) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField(COMMIT_FIELD_NAME,
                    line.substring(COMMIT_LINE_START.length()));
        } else if (line.startsWith(TREE_LINE_START)) {
            jsonGenerator.writeStringField(TREE_FIELD_NAME,
                    line.substring(TREE_LINE_START.length() + LINE_SPACE_OFFSET));
        } else if (line.startsWith(AUTHOR_LINE_START)) {
            jsonGenerator.writeFieldName(AUTHOR_FIELD_NAME);
            writePersonStamp(jsonGenerator, line.substring(AUTHOR_LINE_START.length() + LINE_SPACE_OFFSET));

        } else if (line.startsWith(COMMITTER_LINE_START)) {
            jsonGenerator.writeFieldName(COMMITTER_FIELD_NAME);
            writePersonStamp(jsonGenerator, line.substring(COMMITTER_LINE_START.length() + LINE_SPACE_OFFSET));
        } else if (line.startsWith(PARENT_LINE_START)) {
            parents.add(line.substring(PARENT_LINE_START.length() + LINE_SPACE_OFFSET));
        } else if (StringUtils.isEmpty(line)) {
            StringBuilder sb = new StringBuilder();
            while (StringUtils.isNotEmpty(line = in.readLine()) && line.startsWith("    ")) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(line.substring(4));
            }
            jsonGenerator.writeStringField("message", sb.toString());
        }
        line = in.readLine();
        if (line == null || line.startsWith(COMMIT_LINE_START)) {
            jsonGenerator.writeArrayFieldStart(PARENTS_FIELD_NAME);
            for (String parent : parents) {
                jsonGenerator.writeString(parent);
            }
            jsonGenerator.writeEndArray();
            parents.clear();
            jsonGenerator.writeEndObject();
        }
        return line;
    }

    private void writePersonStamp(JsonGenerator generator, String line) throws IOException {

        generator.writeStartObject();

        int nameEnd = line.indexOf(EMAIL_START_SYMBOL);
        String name = line.substring(0, nameEnd).trim();
        generator.writeStringField(NAME_FIELD_NAME, name);

        line = line.substring(nameEnd).trim();
        int emailEnd = line.indexOf(EMAIL_END_SYMBOL);
        // skip < symbol
        String email = line.substring(1, emailEnd).trim();
        generator.writeStringField(EMAIL_FIELD_NAME, email);

        // skip over > symbol
        line = line.substring(emailEnd + 1).trim();
        int dateEnd = line.indexOf(" ");
        String stamp = line.substring(0, dateEnd).trim();
        Date date = new Date(Long.parseLong(stamp) * 1000);
        generator.writeStringField(DATE_FIELD_NAME, date.toInstant().toString());

        generator.writeEndObject();
    }
}
