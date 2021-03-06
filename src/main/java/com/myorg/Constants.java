package com.myorg;

public class Constants {
	public static final String PROJECT_NAME = "CdkProject";
	public static final String VCS_REPOSITORY_BRANCH = "main";
	public static final String CODE_DEPLOY_APP_NAME = Constants.PROJECT_NAME + "CodeDeployApplication";
	public static final String CODE_DEPLOY_DEPLOYMENT_GROUP_NAME = Constants.PROJECT_NAME + "DeploymentGroup";

	// User Input parameters
	public static final String LAMBDA_FUNCTION_NAME = "CdkSampleLambda";
	public static final String LAMBDA_FUNCTION_ARN = "arn:aws:lambda:us-east-1:784439035548:function:CdkSampleLambda";
	public static final String LAMBDA_FUNCTION_ALIAS = "prod";

	public static final String IAM_MANAGED_POLICY_CODEDEPLOY_FULL_ACCESS = "AWSCodeDeployFullAccess";
	public static final String IAM_MANAGED_POLICY_LAMBDA_FULL_ACCESS = "AWSLambda_FullAccess";
}
