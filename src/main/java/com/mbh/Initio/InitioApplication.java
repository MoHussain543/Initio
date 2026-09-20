package com.mbh.initio;

import com.mbh.initio.cli.InitioCommand;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication
public class InitioApplication {

	public static void main(String[] args) {
		int code = new CommandLine(new InitioCommand()).execute(args);
		System.exit(code);
	}
}
