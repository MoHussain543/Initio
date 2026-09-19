package com.mbh.Initio.detector;

import com.mbh.Initio.analysis.DetectionResult;
import com.mbh.Initio.analysis.ProjectContext;

public interface ProjectDetector {

	boolean supports(ProjectContext context);

	DetectionResult detect(ProjectContext context);
}
