package com.example;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.grizzly.http.server.HttpServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class GitServiceTest {

    private static final String POM_CONTENT_STRING = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
			"         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" + 
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
        // c.configuration().enable(new org.glassfish.jersey.media.json.JsonJaxbFeature());

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
        String responseMsg = target.path("git/cat-file/2944af04be2950a7d3872ab8525b564915a8f4f9")
        		.request()
        		.get(String.class);
        assertEquals(POM_CONTENT_STRING, responseMsg);
    }
}
