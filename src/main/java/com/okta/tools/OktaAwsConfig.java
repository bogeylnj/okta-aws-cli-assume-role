package com.okta.tools;

import com.okta.tools.helpers.ArgumentHelper;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/** Configuration Value Precedence: argument -> environment variable -> config property -> default value */
final class OktaAwsConfig {

    private static final Logger logger = LogManager.getLogger(OktaAwsConfig.class);
    private static final String CONFIG_FILENAME = "config.properties";

    static OktaAwsCliEnvironment loadEnvironment() {
        return loadEnvironment((String[]) null);
    }

    static OktaAwsCliEnvironment loadEnvironment(String... args) {
        Properties properties = loadProperties();
        CommandLine commandLine = ArgumentHelper.parse(args);

        // String profile = commandLine.getOptionValue("profile");
        String profile = "default"; // just artisan use-case for now, blend with okta use case (write and read same profile) as some point for PR?
        String passwordCmd = commandLine.hasOption("password") ? "echo " + commandLine.getOptionValue("password") : null;
        return new OktaAwsCliEnvironment(
                Boolean.parseBoolean(getPropertyByPrecedence(properties, "OKTA_BROWSER_AUTH")),
                getPropertyByPrecedence(properties, "OKTA_ORG"),
                getPropertyByPrecedence(commandLine.getOptionValue("username"), properties, "OKTA_USERNAME", System.getProperty("user.name")),
                deferProgram(getPropertyByPrecedence(passwordCmd, properties, "OKTA_PASSWORD_CMD")),
                getPropertyByPrecedence(properties, "OKTA_COOKIES_PATH"),
                getPropertyByPrecedence(profile, properties, "OKTA_PROFILE"),
                getPropertyByPrecedence(properties, "OKTA_AWS_APP_URL"),
                getPropertyByPrecedence(properties, "OKTA_AWS_ROLE_TO_ASSUME"),
                Integer.parseInt(getPropertyByPrecedence(commandLine.getOptionValue("timeout"), properties, "OKTA_STS_DURATION", "3600")),
                getPropertyByPrecedence(properties, "OKTA_AWS_REGION", "us-east-1"),
                getPropertyByPrecedence(properties, "OKTA_MFA_CHOICE"),
                Boolean.parseBoolean(getPropertyByPrecedence(properties, "OKTA_ENV_MODE")),
                getArgsOrDefault(commandLine)
        );
    }

    private static Supplier<String> deferProgram(String oktaPasswordCommand) {
        if (oktaPasswordCommand == null) return null;
        return () -> runProgram(oktaPasswordCommand);
    }

    private static String runProgram(String oktaPasswordCommand) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (SystemUtils.IS_OS_WINDOWS) {
            processBuilder.command("cmd", "/C", oktaPasswordCommand);
        } else if (SystemUtils.IS_OS_UNIX) {
            processBuilder.command("sh", "-c", oktaPasswordCommand);
        }
        try {
            Process passwordCommandProcess = processBuilder.start();
            String password = getOutput(passwordCommandProcess);
            int exitCode = passwordCommandProcess.waitFor();
            if (exitCode == 0) return password;
            throw new IllegalStateException("password command failed with exit code " + exitCode);
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException("password command failed", e);
        }
    }

    private static String getOutput(Process process) throws IOException {
        try (InputStream inputStream = process.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        }
    }

    private static Properties loadProperties() {
        Properties properties = new Properties();
        Optional<Path> path = getConfigFile();
        if (path.isPresent()) {
            try (InputStream config = new FileInputStream(path.get().toFile())) {
                logger.debug("Reading config settings from file: " + path.get().toAbsolutePath().toString());
                properties.load(new InputStreamReader(config));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try (InputStream config = properties.getClass().getResourceAsStream("/config.properties")) {
                if (config != null) {
                    properties.load(new InputStreamReader(config));
                } else {
                    // Don't fail if no config.properties found in classpath as we will fallback to env variables
                    logger.debug("No config.properties file found in working directory, ~/.okta, or the class loader");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return properties;
    }

    private static Optional<Path> getConfigFile() {
        Path configInWorkingDir = Paths.get(CONFIG_FILENAME);
        if (Files.isRegularFile(configInWorkingDir)) {
            return Optional.of(configInWorkingDir);
        }
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path oktaDir = userHome.resolve(".okta");
        Path configInOktaDir = oktaDir.resolve(CONFIG_FILENAME);
        if (Files.isRegularFile(configInOktaDir)) {
            return Optional.of(configInOktaDir);
        }
        return Optional.empty();
    }

    private static List<String> getArgsOrDefault(CommandLine commandLine) {
        String[] defaultArgs = new String[] {"sts", "get-caller-identity"};
        return !commandLine.getArgList().isEmpty() ? commandLine.getArgList() : Arrays.asList(defaultArgs);
    }

    private static String getPropertyByPrecedence(Properties properties, String propertyName) {
        return getPropertyByPrecedence(null, properties, propertyName, null);
    }

    private static String getPropertyByPrecedence(Properties properties, String propertyName, String defaultValue) {
        return getPropertyByPrecedence(null, properties, propertyName, defaultValue);
    }

    private static String getPropertyByPrecedence(String commandLineOption, Properties properties, String propertyName) {
        return getPropertyByPrecedence(commandLineOption, properties, propertyName, null);
    }

    private static String getPropertyByPrecedence(String commandLineOption, Properties properties, String propertyName, String defaultValue) {
        String envValue = System.getenv(propertyName);
        if (commandLineOption != null) {
            logger.debug("Using " + propertyName + "  value from the command line.");
            return commandLineOption;
        } else if (envValue != null) {
            logger.debug("Using " + propertyName + "  value from the environment.");
            return envValue;
        } else if (properties.getProperty(propertyName) != null) {
            logger.debug("Using " + propertyName + "  value from the config file." );
            return properties.getProperty(propertyName);
        } else {
            return defaultValue;
        }
    }
}
