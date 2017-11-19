package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public final class InputStreamStreamingOutput implements StreamingOutput {

    private final InputStream stream;

    public InputStreamStreamingOutput(InputStream stream) {
        if (stream == null) {
            throw new IllegalArgumentException("Stream cannot be null");
        }
        this.stream = stream;
    }

    @Override
    public void write(OutputStream output)
            throws IOException, WebApplicationException {
        byte[] buffer = new byte[1024];
        int len = stream.read(buffer);
        while (len != -1) {
            output.write(buffer, 0, len);
            len = stream.read(buffer);
        }
    }

}
