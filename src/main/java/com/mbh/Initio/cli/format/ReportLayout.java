package com.mbh.Initio.cli.format;

import java.io.PrintStream;

public final class ReportLayout {

	static final String DIVIDER = "────────────────────────";

	private ReportLayout() {
	}

	public static void section(PrintStream out, String title) {
		out.println(title);
		out.println(DIVIDER);
	}

	public static void blank(PrintStream out) {
		out.println();
	}
}
