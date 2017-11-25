package com.example;

import java.io.InputStream;

import javax.ws.rs.core.StreamingOutput;

public interface StreamingOutputFactory {
    StreamingOutput getStreamingOutput(InputStream stream);

    StreamingOutput getErrorStreamingOutput(InputStream stream);
}
