package com.example;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

import javax.validation.constraints.Pattern;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * Root resource (exposed at "git" path)
 */
@Path("git")
public class GitService {

    public static final String HASH_VALIDATION_MESSAGE = "The resourceId must be valid hash";
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
    @Path("/cat-file/{resourceId}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response catFile(
            @PathParam("resourceId")
            @Pattern(regexp = "[0-9,a-z]+",
                    message = HASH_VALIDATION_MESSAGE)
            String resourceId)
            throws IOException, InterruptedException {
        return GitRunner.catFile(resourceId);
    }
}
