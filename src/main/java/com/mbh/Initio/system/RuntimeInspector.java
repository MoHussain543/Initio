package com.mbh.initio.system;

import com.mbh.initio.model.InstalledRuntime;

public interface RuntimeInspector {

	InstalledRuntime inspectJava();

	InstalledRuntime inspectNode();
}
