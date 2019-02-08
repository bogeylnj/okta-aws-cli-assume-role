/*
 * Copyright 2017 Okta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.okta.tools;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class awscli {
    public static void main(String[] args) throws Exception {
        if (LogoutHandler.handleLogout(args)) return;

        OktaAwsCliEnvironment environment = OktaAwsConfig.loadEnvironment(args);
        OktaAwsCliAssumeRole.RunResult runResult = OktaAwsCliAssumeRole.withEnvironment(environment).run(Instant.now());

        ProcessBuilder awsProcessBuilder = new ProcessBuilder().inheritIO();
        List<String> awsCommand = new ArrayList<>();
        awsCommand.add(getAwsCommandName());
        if (environment.oktaEnvMode) {
            Map<String, String> awsEnvironment = awsProcessBuilder.environment();
            awsEnvironment.put("AWS_ACCESS_KEY_ID", runResult.accessKeyId);
            awsEnvironment.put("AWS_SECRET_ACCESS_KEY", runResult.secretAccessKey);
            awsEnvironment.put("AWS_SESSION_TOKEN", runResult.sessionToken);
        }

        awsCommand.addAll(environment.awsCommandArgs);
        awsProcessBuilder.command(awsCommand);
        Process awsSubProcess = awsProcessBuilder.start();
        int exitCode = awsSubProcess.waitFor();
        System.exit(exitCode);
    }

    private static String getAwsCommandName() {
        try {
            new ProcessBuilder().inheritIO().command("aws.cmd").start().waitFor();
            return "aws.cmd";
        }
        catch(Exception e) {
            return "aws";
        }
    }
}
