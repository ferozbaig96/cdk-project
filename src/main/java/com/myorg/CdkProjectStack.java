package com.myorg;

import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.constructs.Construct;

public class CdkProjectStack extends Stack {
	public CdkProjectStack(final Construct scope, final String id) {
		this(scope, id, null);
	}

	public CdkProjectStack(final Construct scope, final String id, final StackProps props) {
		super(scope, id, props);

	}
}
