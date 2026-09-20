package com.mbh.initio.diagnostic;

import com.mbh.initio.analysis.AnalysisContext;
import com.mbh.initio.model.DiagnosticIssue;

import java.util.List;

public interface DiagnosticRule {

	List<DiagnosticIssue> evaluate(AnalysisContext context);
}
