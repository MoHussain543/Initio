package com.mbh.initio.cli.format;

import com.mbh.initio.projectconfig.InitioConfigLoadResult;
import com.mbh.initio.projectconfig.InitioProjectConfig;

import java.io.PrintWriter;
import java.util.Objects;

public final class ConfigValidateReportFormatter {

	public void write(InitioConfigLoadResult result, PrintWriter out) {
		Objects.requireNonNull(result, "result");
		Objects.requireNonNull(out, "out");
		out.println("INITIO CONFIG");
		ReportLayout.blank(out);
		if (result.isMissing()) {
			out.println("No initio.yml found.");
			out.println("Project configuration is optional.");
			return;
		}
		if (result.isValid()) {
			InitioProjectConfig config = result.config();
			out.println("Configuration valid.");
			out.println("File: " + result.file().getFileName());
			ReportLayout.blank(out);
			out.println("environment.required: " + config.requiredEnvironmentVariables().size());
			out.println("runtimes: " + config.runtimes().size());
			out.println("commands: " + config.commands().size());
			out.println("services: " + config.services().size());
			out.println("ignore: " + config.suppressions().size());
			return;
		}
		out.println(result.validation().formattedMessage());
	}
}
