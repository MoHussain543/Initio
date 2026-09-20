package com.mbh.initio.detector;

import com.mbh.initio.analysis.DetectionResult;
import com.mbh.initio.analysis.ProjectContext;

public interface ProjectDetector {

	boolean supports(ProjectContext context);

	DetectionResult detect(ProjectContext context);
}
