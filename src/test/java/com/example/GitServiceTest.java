package com.example;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public final class GitServiceTest {

    private static final String AUTHOR_LINE_START = "author ";
    private static final String COMMIT_LINE_START = "commit ";
    private static final String POM_CONTENT_STRING = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            +
            "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n"
            +
            "\n" +
            "    <modelVersion>4.0.0</modelVersion>\n" +
            "\n" +
            "    <groupId>git-service</groupId>\n" +
            "    <artifactId>git-service</artifactId>\n" +
            "    <packaging>war</packaging>\n" +
            "    <version>1.0-SNAPSHOT</version>\n" +
            "    <name>git-service</name>\n" +
            "    \n" +
            "    <dependencyManagement>\n" +
            "        <dependencies>\n" +
            "            <dependency>\n" +
            "                <groupId>org.glassfish.jersey</groupId>\n" +
            "                <artifactId>jersey-bom</artifactId>\n" +
            "                <version>${jersey.version}</version>\n" +
            "                <type>pom</type>\n" +
            "                <scope>import</scope>\n" +
            "            </dependency>\n" +
            "        </dependencies>\n" +
            "    </dependencyManagement>\n" +
            "\n" +
            "    <dependencies>\n" +
            "        <dependency>\n" +
            "            <groupId>org.glassfish.jersey.containers</groupId>\n" +
            "            <artifactId>jersey-container-grizzly2-http</artifactId>\n" +
            "        </dependency>\n" +
            "        <dependency>\n" +
            "            <groupId>org.glassfish.jersey.inject</groupId>\n" +
            "            <artifactId>jersey-hk2</artifactId>\n" +
            "        </dependency>\n" +
            "\n" +
            "        <!-- uncomment this to get JSON support:\n" +
            "         <dependency>\n" +
            "            <groupId>org.glassfish.jersey.media</groupId>\n" +
            "            <artifactId>jersey-media-json-binding</artifactId>\n" +
            "        </dependency>\n" +
            "        -->\n" +
            "        <dependency>\n" +
            "            <groupId>junit</groupId>\n" +
            "            <artifactId>junit</artifactId>\n" +
            "            <version>4.9</version>\n" +
            "            <scope>test</scope>\n" +
            "        </dependency>\n" +
            "    </dependencies>\n" +
            "\n" +
            "    <build>\n" +
            "        <plugins>\n" +
            "            <plugin>\n" +
            "                <groupId>org.apache.maven.plugins</groupId>\n" +
            "                <artifactId>maven-compiler-plugin</artifactId>\n" +
            "                <version>2.5.1</version>\n" +
            "                <inherited>true</inherited>\n" +
            "                <configuration>\n" +
            "                    <source>1.7</source>\n" +
            "                    <target>1.7</target>\n" +
            "                </configuration>\n" +
            "            </plugin>\n" +
            "            <plugin>\n" +
            "                <groupId>org.codehaus.mojo</groupId>\n" +
            "                <artifactId>exec-maven-plugin</artifactId>\n" +
            "                <version>1.2.1</version>\n" +
            "                <executions>\n" +
            "                    <execution>\n" +
            "                        <goals>\n" +
            "                            <goal>java</goal>\n" +
            "                        </goals>\n" +
            "                    </execution>\n" +
            "                </executions>\n" +
            "                <configuration>\n" +
            "                    <mainClass>com.example.Main</mainClass>\n" +
            "                </configuration>\n" +
            "            </plugin>\n" +
            "        </plugins>\n" +
            "    </build>\n" +
            "\n" +
            "    <properties>\n" +
            "        <jersey.version>2.26</jersey.version>\n" +
            "        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>\n" +
            "    </properties>\n" +
            "</project>\n" +
            "";
    private HttpServer server;
    private WebTarget target;

    @Before
    public void setUp() throws Exception {
        // start the server
        server = Main.startServer();
        // create the client
        Client c = ClientBuilder.newClient();

        // uncomment the following line if you want to enable
        // support for JSON in the client (you also have to uncomment
        // dependency on jersey-media-json module in pom.xml and Main.startServer())
        // --
        // c.configuration().enable(new
        // org.glassfish.jersey.media.json.JsonJaxbFeature());

        target = c.target(Main.BASE_URI);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdownNow();
    }

    @Test
    public void testHostname() {
        String responseMsg = target.path("git/hostname")
                .request()
                .get(String.class);
        assertEquals("0.0.0.0", responseMsg);
    }

    @Test
    public void testDirName() {
        String responseMsg = target.path("git/dirname")
                .request()
                .get(String.class);
        assertEquals("git-service", responseMsg);
    }

    @Test
    public void testCatFile() {
        String responseMsg = target.path("git/cat-file/2944af04be2950a7d3872ab8525b564915a8f4f9").request()
                .get(String.class);
        assertEquals(POM_CONTENT_STRING, responseMsg);
    }

    @Test
    public void testCatFileValidation() {
        Response response = target.path("git/cat-file/dssd >> test").request().get();
        assertEquals(HttpStatus.BAD_REQUEST_400.getStatusCode(), response.getStatus());

        String responseMsg = response.readEntity(String.class);
        assertEquals(GitService.RESOURCE_ID_VALIDATION_MESSAGE, responseMsg);
    }

    @Test
    public void testLogMaxCount() {
        testLogMaxCount(1);
        testLogMaxCount(2);
        testLogMaxCount(4);
    }

    private void testLogMaxCount(int count) {
        String responseMsg = target.path("git/log")
                .queryParam(GitService.MAX_COUNT_PARAM, count)
                .request()
                .get(String.class);
        String[] lines = responseMsg.split("\n");

        Stream<String> commitLines = Arrays.stream(lines)
                .filter(line -> line.startsWith(COMMIT_LINE_START));
        assertEquals(count, commitLines.count());

        Stream<String> authorLines = Arrays.stream(lines)
                .filter(line -> line.startsWith(AUTHOR_LINE_START));
        assertEquals(count, authorLines.count());
    }

    @Test
    public void testLogFrom() {
        String fromCommit = "ad8c76551cdcb741bf5ed6fbba2e8ba5d40ad8a4";
        String responseMsg = target.path("git/log")
                .queryParam(GitService.FROM_PARAM, fromCommit)
                .request()
                .get(String.class);
        String[] lines = responseMsg.split("\n");
        Stream<String> fromCommitLines = Arrays.stream(lines)
                .filter(line -> line.equals(COMMIT_LINE_START + fromCommit));
        assertEquals(0, fromCommitLines.count());

        Stream<String> afterFromFommitLine = Arrays.stream(lines)
                .filter(line -> line.equals(COMMIT_LINE_START + "c2580ffa43cb94667fee3f2093475766b189c0e7"));
        assertEquals(1, afterFromFommitLine.count());
    }

    @Test
    public void testLogTo() {
        String fromCommit = "ad8c76551cdcb741bf5ed6fbba2e8ba5d40ad8a4";
        String toCommit = "463202be1fd5d8c1b2167a8bcf43c5d7ba3120b5";
        String responseMsg = target.path("git/log")
                .queryParam(GitService.FROM_PARAM, fromCommit)
                .queryParam(GitService.TO_PARAM, toCommit)
                .request()
                .get(String.class);
        String[] lines = responseMsg.split("\n");

        assertEquals(COMMIT_LINE_START + toCommit, lines[0]);

        Stream<String> fromCommitLines = Arrays.stream(lines)
                .filter(line -> line.equals(COMMIT_LINE_START + fromCommit));

        assertEquals(0, fromCommitLines.count());
    }
}
