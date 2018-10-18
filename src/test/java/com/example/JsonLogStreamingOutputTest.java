package com.example;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonLogStreamingOutputTest {
    static class Person {
        public String name;
        public String email;
        public Date date;

        public void assertEqual(Person other) {
            assertEquals("Names differ", name, other.name);
            assertEquals("E-mails differ", email, other.email);
            assertEquals("Dates differ", date.getTime(), other.date.getTime());
        }
    }

    static class Commit {
        public String commit;
        public String tree;
        public String message;
        public String[] parents;
        public Person author;
        public Person committer;

        public void assertEqual(Commit other) {
            assertEquals("Commit hashes differ", commit, other.commit);
            assertEquals("Trees differ", tree, other.tree);
            assertEquals("Messages differ", message, other.message);
            if (author != other.author) {
                author.assertEqual(other.author);
            }
            if (committer != other.committer) {
                committer.assertEqual(other.committer);
            }
            if (parents != other.parents) {
                assertEquals("Parent counts differ", parents.length, other.parents.length);
                for (int index = 0; index < parents.length; index++) {
                    assertEquals("Parents differ at index " + index, parents[0], other.parents[0]);
                }
            }
        }
    }

    @Test
    public void parseSimpleOutput() throws WebApplicationException, IOException {
        final String secondCommitHash = "2c8686f2b644fa0b03da6817d28d6041e05f054b";
        final String secondCommitMessageFirstLine = "Use transferTo method to copy stream to output";
        final String secondCommitMessageSecondLine = "Line2";
        final Person firstAuthor = new Person() {
            {
                name = "Ülo Parri";
                email = "email@gmail.com";
                date = new GregorianCalendar(
                        2017,
                        Calendar.NOVEMBER,
                        21,
                        22,
                        55,
                        10).getTime();
            }
        };
        final Person firstCommitter = new Person() {
            {
                name = "Ülo2 Parri";
                email = "email2@gmail.com";
                date = new GregorianCalendar(
                        2017,
                        Calendar.NOVEMBER,
                        21,
                        22,
                        55,
                        10).getTime();
            }
        };
        Commit firstCommit = new Commit() {
            {
                commit = "b4a46b18800c2efb9e875c3f3e3726f83a7f51a5";
                tree = "e39755d2ddd1c0cd13dbc55aa0a3d70df8f79129";
                message = "Service to get git log with basic tests";
                parents = new String[] { secondCommitHash };
                author = firstAuthor;
                committer = firstCommitter;
            }
        };
        final Person secondAuthor = new Person() {
            {
                name = "Ülo3 Parri";
                email = "email3@gmail.com";
                date = new GregorianCalendar(
                        2017,
                        Calendar.NOVEMBER,
                        20,
                        21,
                        32,
                        05).getTime();
            }
        };
        final Person secondCommitter = new Person() {
            {
                name = "Committer Parri";
                email = "email2@gmail.com";
                date = new GregorianCalendar(
                        2017,
                        Calendar.NOVEMBER,
                        20,
                        21,
                        32,
                        05).getTime();
            }
        };
        final Commit secondCommit = new Commit() {
            {
                commit = secondCommitHash;
                tree = "62db2ae7cf15adeab64365e3799f55dc5a0cb5a2";
                message = secondCommitMessageFirstLine + "\n" + secondCommitMessageSecondLine;
                parents = new String[] { "64fd50f8a7938a358185bdbcdd9ef81b336a2001" };
                author = secondAuthor;
                committer = secondCommitter;
            }
        };
        final String commitLogFormatPattern = "commit {0}" +
                "\ntree {1}\n" +
                "parent {2}\n" +
                "author {3} <{4}> {5,number,#} +0200\n" +
                "committer {6} <{7}> {8,number,#} +0200\n" +
                "\n" +
                "    {9}\n" +
                "\n" +
                "commit {10}\n" +
                "tree {11}\n" +
                "parent {12}\n" +
                "author {13} <{14}> {15,number,#} +0200\n" +
                "committer {16} <{17}> {18,number,#} +0200\n" +
                "\n" +
                "    {19}\n" +
                "    {20}\n" +
                "";
        final String outputText = MessageFormat.format(
                commitLogFormatPattern,
                firstCommit.commit,
                firstCommit.tree,
                firstCommit.parents[0],
                firstAuthor.name,
                firstAuthor.email,
                getUnixTime(firstAuthor),
                firstCommitter.name,
                firstCommitter.email,
                getUnixTime(firstCommitter),
                firstCommit.message,
                secondCommit.commit,
                secondCommit.tree,
                secondCommit.parents[0],
                secondAuthor.name,
                secondAuthor.email,
                getUnixTime(secondAuthor),
                secondCommitter.name,
                secondCommitter.email,
                getUnixTime(secondCommitter),
                secondCommitMessageFirstLine,
                secondCommitMessageSecondLine);
        Commit[] commits;
        try (InputStream stream = new ByteArrayInputStream(outputText.getBytes(StandardCharsets.UTF_8.name()))) {
            try (JsonLogStreamingOutput streamer = new JsonLogStreamingOutput(stream)) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                streamer.write(output);
                JsonFactory factory = new JsonFactory();
                ObjectMapper mapper = new ObjectMapper(factory);
                commits = mapper.readValue(output.toByteArray(), Commit[].class);
            }
        }

        assertEquals(2, commits.length);

        firstCommit.assertEqual(commits[0]);

        secondCommit.assertEqual(commits[1]);
    }

    private static long getUnixTime(Person firstAuthor) {
        return firstAuthor.date.getTime() / 1000;
    }
}
