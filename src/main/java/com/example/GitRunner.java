package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.grizzly.http.util.HttpStatus;

public final class GitRunner {

    public static Response catFile(final String resourceId) throws IOException, InterruptedException {
        final String[] command = new String[] { "git", "cat-file", "-p", resourceId };
        return getGitResponse(command);
    }

    public static Response getLog(int maxCount, String fromCommit, String toCommit)
            throws IOException, InterruptedException {

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
        return getGitResponse(command.toArray(new String[0]));
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

    public static Response getGitResponse(final String[] command)
            throws IOException, InterruptedException {
        final ProcessBuilder builder = new ProcessBuilder(command);
        final Process process = builder.start();
        int status = 0;
        while (waitForExitOrOutput(process)) {
        }
        if (!process.isAlive()) {
            status = process.exitValue();
        }
        if (status == 0) {
            final InputStream input = process.getInputStream();
            InputStreamStreamingOutput entity = new InputStreamStreamingOutput(input);
            return Response.ok(entity, MediaType.APPLICATION_OCTET_STREAM)
                    .build();
        } else {
            final InputStream error = process.getErrorStream();
            return Response.status(HttpStatus.BAD_REQUEST_400.getStatusCode())
                    .entity(new InputStreamStreamingOutput(error))
                    .build();
        }
    }

    private static boolean waitForExitOrOutput(final Process process)
            throws InterruptedException, IOException {
        return process.isAlive() &&
                process.waitFor(20, TimeUnit.MILLISECONDS) &&
                process.getInputStream().available() == 0;
    }
}
