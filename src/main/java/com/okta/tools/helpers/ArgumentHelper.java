package com.okta.tools.helpers;

import org.apache.commons.cli.*;
import org.apache.commons.lang.ArrayUtils;

public class ArgumentHelper {

    private static final String USAGE_TEXT = "awscred [-h] [-u <username>] [-p <password>] [--pr <profile>] [-t <timeout>] [<aws-cmd-args>]" +
        "\n\n       awscred logout  Force Okta re-authentication\n\n";
    private static final String PROFILE_OPTION_NAME = "--profile";

    private ArgumentHelper(){}

    public static CommandLine parse(String[] args) {
        Options options = getOptions();
        try {
            CommandLine commandLine = new DefaultParser().parse(options, args, true);
            exitIfHelp(commandLine, options);
            return handleProfileArg(commandLine, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp(USAGE_TEXT, options);
            throw new IllegalArgumentException(e);
        }
    }

    private static void exitIfHelp(CommandLine commandLine, Options options) {
        if(commandLine.hasOption("help")) {
            new HelpFormatter().printHelp(USAGE_TEXT, options);
            System.exit(0);
        }
    }

    private static CommandLine handleProfileArg(CommandLine commandLine, String[] args) throws ParseException {
        String[] fillArgs;
        if(commandLine.hasOption(PROFILE_OPTION_NAME) && !commandLine.getArgList().contains(PROFILE_OPTION_NAME)) {
            fillArgs = (String[]) ArrayUtils.addAll(args, new String[]{PROFILE_OPTION_NAME, commandLine.getOptionValue("profile")});
        }
        else if(!commandLine.hasOption(PROFILE_OPTION_NAME) && commandLine.getArgList().contains(PROFILE_OPTION_NAME)) {
            int index = commandLine.getArgList().indexOf(PROFILE_OPTION_NAME);
            fillArgs = (String[]) ArrayUtils.addAll(new String[]{PROFILE_OPTION_NAME, commandLine.getArgList().get(index+1)}, args);
        }
        else {
            return commandLine;
        }

        return new DefaultParser().parse(getOptions(), fillArgs, true);
    }

    private static Options getOptions() {
        Options options = new Options();
        options.addOption("h", "help", false, "Help and Usage message");
        options.addOption("u", "username", true, "Username for authentication - defaults to " + System.getProperty("user.name"));
        options.addOption("p", "password", true, "Password for authentication");
        options.addOption("t", "timeout", true, "AWS STS session duration in seconds (default=3600)");
        options.addOption("pr", "profile", true, "AWS Named Profile");
        return options;
    }
}
