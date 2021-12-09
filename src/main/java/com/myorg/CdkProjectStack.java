package com.myorg;

import com.google.common.collect.ImmutableMap;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.codebuild.BuildEnvironment;
import software.amazon.awscdk.services.codebuild.BuildSpec;
import software.amazon.awscdk.services.codebuild.LinuxBuildImage;
import software.amazon.awscdk.services.codebuild.PipelineProject;
import software.amazon.awscdk.services.codecommit.Repository;
import software.amazon.awscdk.services.codedeploy.LambdaApplication;
import software.amazon.awscdk.services.codedeploy.LambdaDeploymentConfig;
import software.amazon.awscdk.services.codedeploy.LambdaDeploymentGroup;
import software.amazon.awscdk.services.codepipeline.Action;
import software.amazon.awscdk.services.codepipeline.Artifact;
import software.amazon.awscdk.services.codepipeline.Pipeline;
import software.amazon.awscdk.services.codepipeline.StageProps;
import software.amazon.awscdk.services.codepipeline.actions.CodeBuildAction;
import software.amazon.awscdk.services.codepipeline.actions.CodeBuildActionType;
import software.amazon.awscdk.services.codepipeline.actions.CodeCommitSourceAction;
import software.amazon.awscdk.services.lambda.Alias;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.IFunction;
import software.amazon.awscdk.services.lambda.IVersion;
import software.constructs.Construct;

import java.util.Arrays;
import java.util.Collections;

public class CdkProjectStack extends Stack {
	public CdkProjectStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public CdkProjectStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

		// CodeCommit repository
		final Repository repository = Repository.Builder.create(this, Constants.PROJECT_NAME + "Repository")
			.repositoryName(Constants.PROJECT_NAME + "Repository")
			.build();

		/*
		// CodePipeline with CodeBuild (Modern API)
		final CodePipeline codePipeline = CodePipeline.Builder.create(this, Constants.PROJECT_NAME + "Pipeline")
			.selfMutation(false)
			.pipelineName(Constants.PROJECT_NAME + "Pipeline")
			.synth(CodeBuildStep.Builder.create("DeployToLambdaVersion")
				.input(CodePipelineSource.codeCommit(repository, Constants.VCS_REPOSITORY_BRANCH))
				// .installCommands(Arrays.asList(
				// 	"npm install -g aws-cdk"
				// ))
				.commands(Arrays.asList(
					"echo ls -ali",
					"ls -ali",
					"echo pwd",
					"pwd"
				))
				.build()
			).build();
		*/

		Artifact sourceOutput = new Artifact("SourceArtifact");
		final Action codeCommitAction = CodeCommitSourceAction.Builder
			.create()
			.actionName("TrackCodeChanges")
			.repository(repository)
			.branch(Constants.VCS_REPOSITORY_BRANCH)
			.output(sourceOutput)
			.build();

		final StageProps sourceStage = StageProps.builder()
			.stageName("Source")
			.actions(Collections.singletonList(codeCommitAction))
			.build();

		final PipelineProject pipelineProject = PipelineProject.Builder
			.create(this, "Project")
			.projectName(Constants.PROJECT_NAME)
			//.logging(LoggingOptions.builder().cloudWatch(CloudWatchLoggingOptions.builder().enabled(true).build()).build())
			.buildSpec(BuildSpec.fromObject(ImmutableMap.of(
				"version", "0.2",
				"phases", ImmutableMap.of(
					"build", ImmutableMap.of(
						"commands", Arrays.asList(
							"echo Zipping deployment package...",
							"zip -r9 deployment_package.zip ."
						)),
					"post_build", ImmutableMap.of(
						"commands", Arrays.asList(

							"echo aws version",
							"aws --version",

							"echo Getting current lambda version against alias " + Constants.LAMBDA_FUNCTION_ALIAS,
							"currentVersion=`aws lambda get-alias --function-name " + Constants.LAMBDA_FUNCTION_NAME + " --name " + Constants.LAMBDA_FUNCTION_ALIAS
								+ " | jq .'FunctionVersion'`",

							"echo Updating lambda Function...",
							"aws lambda update-function-code --function-name " + Constants.LAMBDA_FUNCTION_NAME
								+ " --zip-file fileb://deployment_package.zip --publish >> response.json",

							"echo Getting newly deployed version",
							"targetVersion=`cat response.json | jq .'Version'`",

							"echo Creating code-deploy.yaml file for CodeDeploy",
							"cat << EOF > code-deploy.yaml\n"
								+ "applicationName: \n"
								+ "deploymentGroupName: \n"
								+ "revision:\n"
								+ "  revisionType: AppSpecContent\n"
								+ "  appSpecContent:\n"
								+ "    content: |\n"
								+ "      version: 0.0\n"
								+ "      Resources:\n"
								+ "        - MyFunction:\n"
								+ "            Type: AWS::Lambda::Function\n"
								+ "            Properties:\n"
								+ "              Name: " + Constants.LAMBDA_FUNCTION_NAME + "\n"
								+ "              Alias: " + Constants.LAMBDA_FUNCTION_ALIAS + "\n"
								+ "              TargetVersion: $targetVersion\n"
								+ "              CurrentVersion: $currentVersion\n"
								+ "EOF",
							"cat code-deploy.yaml",

							"echo Creating deployment...",
							String.format("aws deploy create-deployment --application-name %s --deployment-group-name %s --cli-input-yaml file://code-deploy.yaml",
								Constants.CODE_DEPLOY_APP_NAME, Constants.CODE_DEPLOY_DEPLOYMENT_GROUP_NAME)
						)
					)),
				"artifacts", ImmutableMap.of(
					"name", "code-deploy.yaml",
					"files", Collections.singletonList("code-deploy.yaml")

				)
			)))
			// STANDARD_5_0 includes CLIv2. Default is STANDARD_1_0 having CLIv1
			.environment(BuildEnvironment.builder().buildImage(LinuxBuildImage.STANDARD_5_0).build())
			.build();

		Artifact buildOutput = new Artifact("DeployToLambdaOutputArtifact");

		final Action codeBuildAction = CodeBuildAction.Builder
			.create()
			.actionName("DeployToLambdaVersion")
			.type(CodeBuildActionType.BUILD)
			.project(pipelineProject)
			.input(sourceOutput)
			.outputs(Collections.singletonList(buildOutput))
			.build();

		final StageProps buildStage = StageProps.builder()
			.stageName("Build")
			.actions(Collections.singletonList(codeBuildAction))
			.build();

		IFunction function = Function.fromFunctionArn(this, Constants.PROJECT_NAME + "Function",
			Constants.LAMBDA_FUNCTION_ARN);

		IVersion version = function.getLatestVersion();

		LambdaDeploymentGroup.Builder
			.create(this, Constants.PROJECT_NAME + "LambdaDeploymentGroup")
			.application(LambdaApplication.Builder
				.create(this, Constants.PROJECT_NAME + "CodeDeployApplication")
				.applicationName(Constants.CODE_DEPLOY_APP_NAME)
				.build()
			)
			// .deploymentConfig(LambdaDeploymentConfig.CANARY_10_PERCENT_5_MINUTES) // todo
			.deploymentConfig(LambdaDeploymentConfig.ALL_AT_ONCE)
			// .alias(version.addAlias("prod"))
			.alias(Alias.Builder
				.create(this, Constants.PROJECT_NAME + "Alias")
				.aliasName(Constants.LAMBDA_FUNCTION_ALIAS)
				.version(version)
				.build())
			.deploymentGroupName(Constants.CODE_DEPLOY_DEPLOYMENT_GROUP_NAME)
			.build();

		Pipeline.Builder
			.create(this, Constants.PROJECT_NAME + "Pipeline")
			.stages(Arrays.asList(sourceStage, buildStage))
			.build();

	}
}
