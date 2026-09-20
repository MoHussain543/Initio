package com.mbh.initio.system;

import java.time.Duration;
import java.util.List;

public interface CommandExecutor {

	CommandResult execute(List<String> command, Duration timeout);
}
