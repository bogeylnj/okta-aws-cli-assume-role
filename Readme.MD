# AWS CLI

The Artisan `awscred` allows us to take advantage of Okta to use the AWS Command Line Interface without relying on 
permanent AWS keys.  Okta provided an open-source project on Github as an example implementation, and we have enhanced 
it to meet our needs for interacting with AWS services from the command line as well as via batch and automation.

- [Okta's Github project](https://github.com/oktadeveloper/okta-aws-cli-assume-role)
- [awscred Artisan wiki page](https://wiki/display/PROJ/AWS+F+I+-+awscred)

### Use Cases
- User at command line terminal (Windows, Mac/Linux)
- ServiceAccounts running automation (ActiveBatch, Bamboo, etc)


# Tool Details

### Execution
`awscred` will authenticate the User via username/password entry on the command line or using browser authentication 
(enabled by default, requires ArtisanCA cert). After authenticated, `awscred` will populate Artisan accounts into AWS Named Profiles in 
`%userprofile%\.aws\credentials|config`. This allows for easy RoleChaining, but restricts timeouts to 1hr on chained 
Roles.


`awscred` 2.0 will can run an `aws` cli command (if installed) after completing the authentication process. If one is not provided, it 
will default to running `sts get-user-identity`, so you can run it both ways
- `awscred` or 
- `awscred --profile dev s3 ls` (e.g.)


### Timeouts
Initial AssumeRole keys are "extended timeout" ready and you can set the timeout for the (15min-12hours), however 
RoleChaining timeouts are still limited by AWS, so `--profile dev` will do AssumeRole to a Role in the DEV account and 
this is still capped at 1hour

### Caching
- okta caches authentication
- aws cli caches AssumeRole to named profiles `/.aws/cli/cache`

### Java Security Limitations
1. SSO - `OKTA_BROWSER_AUTH=true` gives the tool the ability to open an embedded an OpenFX webkit browser and utlize 
Okta's loging dialogs to authenticate Users. Java versions >1.8.0_192->_202] have disabled the embedded browsers ability
 to pull credentials from the environment. (Could be Java/OpenFX/other causing this)
2. JS SRI - Java/OpenFX versions >1.8.0_162 can not handle JS resource `integrity` attributes (verify the hashcoe of the
 resource file). Okta introduced this with OktaServer version 2019.2+ (we observed break on 20190221). We able to 
 introduce a hack "workaround" to strip out these integrity checks before rendering. We supplied this code to the 
 okta-aws-cli guy and he worked it into v1.0.9 which we have not merged yet



# Deployment / Publishing
Buid by running `mvn package` - generates `target/okta-aws-cli-<version>.jar`

Publish: 
> The following files should be included in install package for Software Center
- `awscred.bat` (update to match `okta-aws-cli` version)
- `config.properties`
- `okta-aws-cli-<version>.jar`
- `accounts.json` (artisan account mapping for AWS Named Profile references)
- `log4j2.xml`


# Features Backlog (2.1 fix version)
- Merge latest code from `upstream` repo
- remove 'aws.cmd' check, causes Mac error messages (find root cause for needing this)
- PRD/SBX okta URL handling
    - `config.properties` location override?
    - cmd line option for all config file options?
- `-u/-p` should disable browser auth (srv accounts)
- `-u/-p` are nice fallback/usable options for some Users
- stdout session expiration
- Timeouts on destination Roles
- SSO
- SCCM / Java install with cert more reliable (remove override in `awscred.bat`)
- OKTA_ENV_MODE - don't write keys anywhere, just hold in memory and call `aws` command only through `okta-cli`


# Release Notes
### awscred-2.0 | [JIRA](https://jira.artisanpartners.com/browse/AWSAD-150) | [DecisionRecord/Notes](https://paper.dropbox.com/doc/IAM-Role-STS-Timeout-Values-ddr--AYMRLuko05xXb1kaBnSPgh10Ag-ckxz9cDNNUr5b7sWlditI)
20190225
- Pull down new code base for `okta-aws-cli-assue-role`
- Implement our current awscred workflow into this new code base
    - Add command line options
    - Generate AWS Named profiles in the AWS configuration files
- Enable Browser Authentication & App-Level MFA
- Add code to prep for a future release to add extended timeouts and SSO (AWS RoleChaining and java security limitations currently)

___
### awscred-1.0
20180613
- Use FIRST INLINE policy as default identity role policy with which to assume other account roles

20180411
- add profiles to `.aws/credentials`, as well (for SDKs)
- add all new account profiles and abbreviations (leave nonproduction|nonprd in place)
- changed property file name to `awscred.properties` (`config.properties` also accepted)
- `.aws/config` (CLI)- change sandbox to default to us-east-1, unlike all other accounts
- added "Testing" and "Deployment / Publishing" sections to Readme above
  

20170809
- Added Maven support to allow users to rebuild project without IDEA.  Type in 'mvn target' to build complete application.
- Created single jar with all dependencies bundled-in to simplify deployment and manage dependencies
- Modified code to allow for command line switches to leverage utility via batch as well as console.
- Added defaults for MFA and user login based on usage pattern
- Added feature to create profiles for all of our AWS accounts to simplify switching between AWS accounts




# Testing
Test CLI
- need to set region per profile, can't use default

Test an SDK (powershell) (defaults to identity profile)
- need to set default region (used as default for all named credentials.profiles)
- can't override default region on credentials.profiles

### Features to Test
- Args/Overrides(username, password, timeout, )
    commons-cli
        - options = configured things that are parsed
        - args = all other values on the command line
        ** set to stop parsing options at first sign of arg (e.g., -opt1 a --opt2 b arg1 --arg2 arg3)
        ** optional options are null if not passed (e.g., no --profile = null)
- Build credential, config files for all accounts 
    - use property file for account list
- Need testing User
    - ??? default FIRST INLINE POLICY
    - ??? z_account when using integratedAuthentication
    - ??? Multiple Roles vs auto-select only Role
