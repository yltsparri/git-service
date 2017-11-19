package com.example;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

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
    public Response catFile(@PathParam("resourceId")
    String resourceId) throws IOException, InterruptedException {
        return GitRunner.catFile(resourceId);
    }
}
