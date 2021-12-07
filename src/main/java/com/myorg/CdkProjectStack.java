package com.myorg;

import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.pipelines.CodeBuildStep;
import software.amazon.awscdk.pipelines.CodePipeline;
import software.amazon.awscdk.pipelines.CodePipelineSource;
import software.amazon.awscdk.services.codecommit.Repository;
import software.constructs.Construct;

import java.util.Arrays;

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

		// CodePipeline
		final CodePipeline codePipeline = CodePipeline.Builder.create(this, Constants.PROJECT_NAME + "Pipeline")
			.pipelineName(Constants.PROJECT_NAME + "Pipeline")
			.synth(CodeBuildStep.Builder.create("DeployToLambdaVersion")
				.input(CodePipelineSource.codeCommit(repository, Constants.VCS_REPOSITORY_BRANCH))
				.commands(Arrays.asList(
					"echo ls",
					"ls",
					"echo pwd",
					"pwd"
				))
				.build()
			).build();


	}
}
