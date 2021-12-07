# CDK Java project!

This is a project for Java development with CDK.

The `cdk.json` file tells the CDK Toolkit how to execute your app.

It is a [Maven](https://maven.apache.org/) based project, so you can open this project with any Maven compatible Java IDE to build and run tests.

# Problem statement:

* With AWS Lambda deployment, after we update our code, new requests to our function will be processed using the updated code. Newly deployed code may contain unidentified bugs which can impact the users.
* CodePipeline only supports ZIP/TAR/TGZ as the bundle type, and CodeDeploy requires the bundle to be YAML or JSON for a Lambda deployment. Hence, CodePipeline does not support CodeDeploy Lambda Deployments.


# Solution:

* Canary deployments help us in these situations. This pipeline will run on every commit to main branch of your configured Github repository and then automatically deploy the code to Lambda Function by creating a new version.
* Alias Traffic Shifting using CodeDeploy (Canary deployment) to release the app code to an initial subset of user and eventually to all users
* (optional) Rollback in case of errors via CloudWatch alarms
* To make CodePipeline support CodeDeploy Lambda Deployment, CodeBuild is used to deploy to lambda version and create an appspec.yaml file as an artifact which will be consumed by CodeDeploy in our pipeline.


Pipeline in CodePipeline will look like:

    Source (Github) → CodeBuild → CodeDeploy

# Services that we will be using:
Lambda, CodeBuild, CodeDeploy, CodePipeline (and Optional - CloudWatch Alarms)


## Useful commands

 * `mvn package`     compile and run tests
 * `cdk ls`          list all stacks in the app
 * `cdk synth`       emits the synthesized CloudFormation template
 * `cdk deploy`      deploy this stack to your default AWS account/region
 * `cdk diff`        compare deployed stack with current state
 * `cdk docs`        open CDK documentation

Enjoy!
