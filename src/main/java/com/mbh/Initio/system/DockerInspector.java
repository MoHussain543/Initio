package com.mbh.initio.system;

import com.mbh.initio.model.ProjectAnalysis;
import com.mbh.initio.model.ServiceStatus;

import java.util.List;

public interface DockerInspector {

	List<ServiceStatus> inspectServices(ProjectAnalysis project);
}
