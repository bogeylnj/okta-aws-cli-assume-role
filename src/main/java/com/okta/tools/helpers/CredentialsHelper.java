package com.okta.tools.helpers;

import com.okta.tools.OktaAwsCliEnvironment;
import com.okta.tools.aws.settings.Credentials;

import java.io.IOException;

public class CredentialsHelper {

    private final OktaAwsCliEnvironment environment;

    public CredentialsHelper(OktaAwsCliEnvironment environment) {
        this.environment = environment;
    }

    /**
     * Updates the credentials file
     *
     * @param profileName     The profile to use
     * @param awsAccessKey    The access key to use
     * @param awsSecretKey    The secret key to use
     * @param awsRegion       The region to use
     * @param awsSessionToken The session token to use
     * @throws IOException    if a file system or permissions error occurs
     */
    void updateCredentialsFile(String profileName, String awsAccessKey, String awsSecretKey, String awsRegion, String awsSessionToken)
            throws IOException {
        FileHelper.usingPath(FileHelper.getAwsDirectory().resolve("credentials"), reader -> {
            Credentials credentials = new Credentials(reader);
            credentials.addOrUpdateProfile(profileName, awsAccessKey, awsSecretKey, awsRegion, awsSessionToken);
            return credentials;
        }, Credentials::save);
    }

    /**
     * Updates AWS named reference profile (named profile references a `source_profile` for keys)
     *
     * @param fileName        The file to update (config || credentials)
     * @param profileName     The profile to use
     * @param roleToAssume    The session token to use
     * @param sourceProfile   The session token to use
     * @param awsRegion       The region to use
     * @throws IOException    if a file system or permissions error occurs
     */
    void updateNamedProfileReference(String fileName, String profileName, String roleToAssume, String sourceProfile, String awsRegion)
        throws IOException {
        FileHelper.usingPath(FileHelper.getAwsDirectory().resolve(fileName), reader -> {
            Credentials credentials = new Credentials(reader);
            credentials.addOrUpdateReferenceProfile(profileName, roleToAssume, sourceProfile, awsRegion);
            return credentials;
        }, Credentials::save);
    }

}
