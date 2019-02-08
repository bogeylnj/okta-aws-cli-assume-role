package com.okta.tools.helpers;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArgumentHelperTest {
    final String sourceProfile = "identity";

    @Test
    void parseFullCommand() {
        final String[] fullCommand = new String[] {"-u", "ttester", "-p", "testpass", "-pr", "default", "s3", "--profile", "test", "ls"};
        final String[] expectedArgs = new String[] {"s3", "--profile", "test", "ls"};
        CommandLine commandLine = ArgumentHelper.parse(fullCommand);
        assertEquals(3, commandLine.getOptions().length);
        assertEquals("ttester", commandLine.getOptionValue("username"));
        assertEquals("testpass", commandLine.getOptionValue("password"));
        assertEquals("default", commandLine.getOptionValue("profile"));
        assertEquals(4, commandLine.getArgs().length);
        assertArrayEquals(expectedArgs, commandLine.getArgs());
    }

    @Test
    void parseFullCommandLong() {
        final String[] fullCommandLong = new String[] {"--username", "ttester", "--password", "testpass", "--profile", "default", "s3", "--profile", "test", "ls"};
        final String[] expectedArgs = new String[] {"s3", "--profile", "test", "ls"};
        CommandLine commandLine = ArgumentHelper.parse(fullCommandLong);
        assertEquals(3, commandLine.getOptions().length);
        assertEquals("ttester", commandLine.getOptionValue("username"));
        assertEquals("testpass", commandLine.getOptionValue("password"));
        assertEquals("default", commandLine.getOptionValue("profile"));
        assertEquals(4, commandLine.getArgs().length);
        assertArrayEquals(expectedArgs, commandLine.getArgs());
    }

    @Test
    void parseNoProfile() {
        final String[] noProfile = new String[] {"s3", "ls"};
        CommandLine commandLine = ArgumentHelper.parse(noProfile);
        assertEquals(0, commandLine.getOptions().length);
        assertEquals(2, commandLine.getArgs().length);
        assertArrayEquals(noProfile, commandLine.getArgs());
    }

    @Test
    void parseProfileOptionOnly() {
        final String[] profileOptionOnly = new String[] {"--profile", "test", "s3", "ls"};
        final String[] expectedArgs = new String[]{"s3", "ls", "--profile", "test" };
        CommandLine commandLine = ArgumentHelper.parse(profileOptionOnly);
        assertEquals(1, commandLine.getOptions().length);
        assertEquals("test", commandLine.getOptionValue("profile"));
        assertEquals(4, commandLine.getArgs().length);
        assertArrayEquals(expectedArgs, commandLine.getArgs());
    }

    @Test
    void parseProfileArgOnly() {
        final String[] profileArgOnly = new String[] {"s3", "--profile", "test", "ls"};
        CommandLine commandLine = ArgumentHelper.parse(profileArgOnly);
        assertEquals(1, commandLine.getOptions().length);
        assertEquals("test", commandLine.getOptionValue("profile"));
        assertEquals(4, commandLine.getArgs().length);
        assertArrayEquals(profileArgOnly, commandLine.getArgs());
    }

    @Test
    void parseProfileOptionAndArg() {
        final String[] profileOptionAndArg = new String[] {"--profile", "default", "s3", "--profile", "test", "ls"};
        final String[] expectedArgs = new String[] { "s3", "--profile", "test", "ls" };
        CommandLine commandLine = ArgumentHelper.parse(profileOptionAndArg);
        System.out.println(commandLine.getArgList());
        assertEquals(1, commandLine.getOptions().length);
        assertEquals("default", commandLine.getOptionValue("profile"));
        assertEquals(4, commandLine.getArgs().length);
        assertArrayEquals(expectedArgs, commandLine.getArgs());
    }

    // WIP - not sure worth the time to blend Artisan Usage with Okta-cli default usage pattern for PR submission
//    @Test
//    void parseSourceProfileAndNoProfile() {;
//        CommandLine commandLine = ArgumentHelper.parse(noProfile, sourceProfile);
//        assertEquals(1, commandLine.getOptions().length);
//        assertEquals(sourceProfile, commandLine.getOptionValue("profile"));
//        assertEquals(2, commandLine.getArgs().length);
//        assertArrayEquals(noProfile, commandLine.getArgs());
//    }
//
//    @Test
//    void parseSourceProfileAndProfileOptionOnly() {;
//        String[] expectedArgs = new String[] { "s3", "ls", "--profile", "test" };
//        CommandLine commandLine = ArgumentHelper.parse(profileOptionOnly, sourceProfile);
//        System.out.println(commandLine.getArgList());
//        assertEquals(1, commandLine.getOptions().length);
//        assertEquals(sourceProfile, commandLine.getOptionValue("profile"));
//        assertEquals(4, commandLine.getArgs().length);
//        assertArrayEquals(expectedArgs, commandLine.getArgs());
//    }
}