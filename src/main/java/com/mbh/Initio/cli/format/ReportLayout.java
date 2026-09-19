package com.mbh.Initio.cli.format;

import java.io.PrintWriter;

public final class ReportLayout {

	static final String DIVIDER = "────────────────────────";

	private ReportLayout() {
	}

	public static void section(PrintWriter out, String title) {
		out.println(title);
		out.println(DIVIDER);
	}

	public static void blank(PrintWriter out) {
		out.println();
	}
}
