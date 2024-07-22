# spi-value-analysis

## Setup 

### Java Runtime 
- Install Java JDK 17
- [Setup JDK](https://www.jetbrains.com/help/idea/sdk.html#jdk).

### Setup Gradle properties
- Request CAM profile BPI [Signavio JFrog Reader](https://spc.ondemand.com/sap/bc/webdynpro/a1sspc/cam_wd_central?item=request&profile=BPI%20Signavio%20JFrog%20Reader) 
- Go to [JFrog Artifactory](https://common.repositories.cloud.sap/) and sign in SAML SSO.
- Click in the upper right corner on your user name
  (or click [here](https://common.repositories.cloud.sap/ui/user_profile/)),
  generate an API Key (if not present) and copy your API Key.
- Replace in file `gradle.properties` the property `artifactory_user` with your user name (`i-number`).
- Replace `artifactory_password` with the API Key you generated and copied before.

### Database

- [Install Docker](https://docs.docker.com/install/)
- [Install AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-install.html)
- Create an AWS user account in the new infrastructure by following the instructions on [Getting Started](https://wiki.one.int.sap/wiki/x/7KCutw) (note this has many steps and takes a while)
- Make sure you have executed `aws-mfa-login` in your shell and your AWS_PROFILE environment variable is set to the corresponding AWS profile. For further information see [AWS-MFA-Login](https://wiki.one.int.sap/wiki/x/g6Outw)
- The ECR Docker repositories are in the account `global_res`. For more information see [AWS Account Structure](https://wiki.one.int.sap/wiki/x/j6Kutw)
- Setup an AWS profile for assuming the `DeveloperAccessRole` in the `global_res` AWS account, using your MFA profile as a source profile:

```sh
[suite-global_res]
role_arn       = arn:aws:iam::893963170360:role/DeveloperAccessRole
source_profile = mfa
```

- Use AWS CLI with the created profile to perform the login for AWS ECR (choose based on your version of AWS CLI):

```sh
# AWS CLI v1
aws ecr get-login --profile suite-global_res --region eu-central-1 --no-include-email | bash

# AWS CLI v2
aws ecr get-login-password --profile suite-global_res --region eu-central-1 | docker login --username AWS --password-stdin 893963170360.dkr.ecr.eu-central-1.amazonaws.com
```

- Pull the latest PostgreSQL Docker image from AWS ECR:

```sh
docker pull 893963170360.dkr.ecr.eu-central-1.amazonaws.com/spi-postgres:15.4
```

- Run the Docker image locally and make PostgreSQL listen on port 5432:

```sh
docker run -p 5432:5432 893963170360.dkr.ecr.eu-central-1.amazonaws.com/spi-postgres:15.4
```

### Running the application

The application can be run in different profile configuration:
- **_deployment (default)_** `[application-deployed.conf]` - default profile used for production and staging
- **_development_** `[application-development.conf]` - used for local development

To run the application in specific profile you need to add name of the configuration file as program parameter `-config=application-development.conf` like:
```sh
./gradlew run --args='-config=application-development.conf'