package com.mbh.initio.system;

public record CommandResult(int exitCode, String stdout, String stderr) {
}
