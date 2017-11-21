package com.example;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import javax.validation.constraints.Pattern;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;

/**
 * Root resource (exposed at "git" path)
 */
@Path("git")
public class GitService {

    static final String RESOURCE_ID_PARAM = "resourceId";
    static final String TO_PARAM = "to";
    static final String FROM_PARAM = "from";
    static final String MAX_COUNT_PARAM = "max-count";
    static final String HASH_REGEX = "[0-9,a-z]+";
    static final String RESOURCE_ID_VALIDATION_MESSAGE = "The resourceId must be valid hash";
    static final String FROM_VALIDATION_MESSAGE = "The from must be valid hash";
    static final String TO_VALIDATION_MESSAGE = "The to must be valid hash";

    @Context
    UriInfo uri;

    @GET
    @Path("/hostname")
    @Produces(MediaType.TEXT_PLAIN)
    public String getHostname() {
        URI myUri = uri.getBaseUri();
        return myUri.getHost();
    }

    @GET
    @Path("/dirname")
    @Produces(MediaType.TEXT_PLAIN)
    public String getDirectoryName() {
        java.nio.file.Path path = Paths.get(".").toAbsolutePath().normalize();
        return path.getName(path.getNameCount() - 1).toString();
    }

    @GET
    @Path("/cat-file/{" + RESOURCE_ID_PARAM + "}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response catFile(
            @PathParam(RESOURCE_ID_PARAM)
            @Pattern(regexp = HASH_REGEX,
                    message = RESOURCE_ID_VALIDATION_MESSAGE)
            String resourceId)
            throws IOException, InterruptedException {
        return GitRunner.catFile(resourceId);
    }

    @GET
    @Path("/log")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getLog(
            @QueryParam(MAX_COUNT_PARAM)
            @DefaultValue("10")
            int maxCount,

            @QueryParam(FROM_PARAM)
            @Pattern(regexp = HASH_REGEX,
                    message = FROM_VALIDATION_MESSAGE)
            String fromCommit,

            @QueryParam(TO_PARAM)
            @Pattern(regexp = HASH_REGEX,
                    message = TO_VALIDATION_MESSAGE)
            String toCommit)
            throws IOException, InterruptedException {
        if (StringUtils.isNotBlank(toCommit) && StringUtils.isBlank(fromCommit)) {
            throw new IllegalArgumentException("from parameter is required if to is specified");
        }
        return GitRunner.getLog(maxCount, fromCommit, toCommit);
    }
}
