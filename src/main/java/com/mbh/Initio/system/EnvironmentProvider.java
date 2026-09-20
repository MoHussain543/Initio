package com.mbh.initio.system;

import com.mbh.initio.model.EnvironmentVariableStatus;

public interface EnvironmentProvider {

	EnvironmentVariableStatus inspect(String variableName);
}
