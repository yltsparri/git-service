package com.example;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public final class InputStreamStreamingOutput implements StreamingOutput, Closeable {

    private final InputStream stream;

    public InputStreamStreamingOutput(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Stream cannot be null");
        }
        this.stream = stream;
    }

    @Override
    public void write(OutputStream output) throws IOException, WebApplicationException {
        stream.transferTo(output);
        close();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
