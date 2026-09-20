package com.mbh.initio.system;

import com.mbh.initio.model.PortObservation;

import java.util.List;

public interface PortInspector {

	List<PortObservation> inspectPorts(List<Integer> ports);
}
