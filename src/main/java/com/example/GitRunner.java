package com.example;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.util.HttpStatus;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class GitRunner {

    public static Response catFile(final String resourceId) throws IOException, InterruptedException {
        final String[] command = new String[]{"git", "cat-file", "-p", resourceId};
        return getGitResponse(command, MediaType.APPLICATION_OCTET_STREAM, new SimpleStreamingOutputFactory());
    }

    public static Response getLogJson(int maxCount, String fromCommit, String toCommit)
            throws IOException, InterruptedException {

        final String[] commandArray = createGitLogCommand(maxCount, fromCommit, toCommit);
        return getGitResponse(commandArray, MediaType.APPLICATION_JSON, new JsonLogStreamingOutputFactory());
    }

    private static String[] createGitLogCommand(int maxCount, String fromCommit, String toCommit) {
        final List<String> command = new ArrayList<String>(
                Arrays.asList(
                        "git",
                        "log",
                        "--date-order",
                        "--pretty=raw",
                        "--decorate=full",
                        "--max-count=" + maxCount));
        String rangeParam = buildRangeParameter(fromCommit, toCommit);
        if (!StringUtils.isBlank(rangeParam)) {
            command.add(rangeParam);
        }
        final String[] commandArray = command.toArray(new String[0]);
        return commandArray;
    }

    private static String buildRangeParameter(String fromCommit, String toCommit) {
        String rangeParam = StringUtils.trimToEmpty(fromCommit);
        if (StringUtils.isNotBlank(rangeParam)) {
            rangeParam += "...";
        }
        if (StringUtils.isNotBlank(toCommit)) {
            rangeParam += toCommit.trim();
        }
        return rangeParam;
    }

    public static Response getGitResponse(final String[] command, String mediaType,
                                          StreamingOutputFactory outputFactory)
            throws IOException, InterruptedException {
        final Process process = createGitProcess(command);
        int status = 0;
        if (!process.isAlive()) {
            status = process.exitValue();
        }
        if (status == 0) {
            final InputStream input = process.getInputStream();
            StreamingOutput entity = outputFactory.getStreamingOutput(input);
            return Response.ok(entity, mediaType)
                    .build();
        } else {
            final InputStream error = process.getErrorStream();
            return Response.status(HttpStatus.BAD_REQUEST_400.getStatusCode())
                    .entity(outputFactory.getErrorStreamingOutput(error))
                    .build();
        }
    }

    private static Process createGitProcess(final String[] command) throws IOException, InterruptedException {
        final ProcessBuilder builder = new ProcessBuilder(command);
        final Process process = builder.start();

        while (waitForExitOrOutput(process)) {
        }
        return process;
    }

    private static boolean waitForExitOrOutput(final Process process)
            throws InterruptedException, IOException {
        return process.isAlive() &&
                process.waitFor(20, TimeUnit.MILLISECONDS) &&
                process.getInputStream().available() == 0;
    }

    private static final class SimpleStreamingOutputFactory implements StreamingOutputFactory {

        @Override
        public StreamingOutput getStreamingOutput(InputStream stream) {
            return new InputStreamStreamingOutput(stream);
        }

        @Override
        public StreamingOutput getErrorStreamingOutput(InputStream stream) {
            return new InputStreamStreamingOutput(stream);
        }
    }

    private static final class JsonLogStreamingOutputFactory implements StreamingOutputFactory {

        @Override
        public StreamingOutput getStreamingOutput(InputStream stream) {
            return new JsonLogStreamingOutput(stream);
        }

        @Override
        public StreamingOutput getErrorStreamingOutput(InputStream stream) {
            return new InputStreamStreamingOutput(stream);
        }

    }
}
