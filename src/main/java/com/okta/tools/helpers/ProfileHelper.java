package com.okta.tools.helpers;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithSAMLResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okta.tools.AwsAccount;
import com.okta.tools.OktaAwsCliEnvironment;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileHelper {

    private final CredentialsHelper credentialsHelper;
    private OktaAwsCliEnvironment environment;
    private final Pattern assumedRoleUserPattern = Pattern.compile(
            "^arn:aws:sts::(?<account>\\d{12}):assumed-role/(?<roleName>[^/]*)/(?<userName>.*$)");

    public ProfileHelper(CredentialsHelper credentialsHelper, OktaAwsCliEnvironment environment) {
        this.credentialsHelper = credentialsHelper;
        this.environment = environment;
    }

    public void createAwsProfile(AssumeRoleWithSAMLResult assumeResult, String credentialsProfileName) throws IOException {
        BasicSessionCredentials temporaryCredentials =
                new BasicSessionCredentials(
                        assumeResult.getCredentials().getAccessKeyId(),
                        assumeResult.getCredentials().getSecretAccessKey(),
                        assumeResult.getCredentials().getSessionToken());

        String awsAccessKey = temporaryCredentials.getAWSAccessKeyId();
        String awsSecretKey = temporaryCredentials.getAWSSecretKey();
        String awsSessionToken = temporaryCredentials.getSessionToken();

        String awsRegion = environment.awsRegion;
        
        credentialsHelper.updateCredentialsFile(credentialsProfileName, awsAccessKey, awsSecretKey, awsRegion,
                awsSessionToken);
    }

    public String getProfileName(AssumeRoleWithSAMLResult assumeResult) {
        if (StringUtils.isNotBlank(environment.oktaProfile)) {
            return environment.oktaProfile;
        }

        String credentialsProfileName = assumeResult.getAssumedRoleUser().getArn();
        Matcher matcher = assumedRoleUserPattern.matcher(credentialsProfileName);
        if (matcher.matches()) {
            return matcher.group("roleName") + "_" + matcher.group("account");
        }

        return "temp";
    }

    public void updateNamedProfileReferences() throws IOException {
        final String DEFAULT_PROFILE_KEY = "default";
        ObjectMapper mapper = new ObjectMapper();
        List<AwsAccount> accounts = mapper.readValue(new File("./accounts.json"), new TypeReference<List<AwsAccount>>(){});
        accounts.forEach(a -> {
            try {
                String roleArn = environment.awsRoleToAssume.replaceAll("arn:aws:iam::\\d+", "arn:aws:iam::"+a.number);
                credentialsHelper.updateNamedProfileReference("credentials", a.name, roleArn, DEFAULT_PROFILE_KEY, null);
                credentialsHelper.updateNamedProfileReference("credentials", a.alias, roleArn, DEFAULT_PROFILE_KEY, null);
                credentialsHelper.updateNamedProfileReference("config", a.name, roleArn, DEFAULT_PROFILE_KEY, a.region);
                credentialsHelper.updateNamedProfileReference("config", a.alias, roleArn, DEFAULT_PROFILE_KEY, a.region);
            } catch (IOException e) {
                throw new RuntimeException("Failed to update profile:" + a.name, e);
            }
        });
    }
}
