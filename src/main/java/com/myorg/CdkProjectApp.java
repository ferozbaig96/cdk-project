package com.myorg;

import software.amazon.awscdk.core.App;

public final class CdkProjectApp {
	public static void main(final String[] args) {
		App app = new App();

		new CdkProjectStack(app, "CdkProjectStack");

		app.synth();
	}
}

