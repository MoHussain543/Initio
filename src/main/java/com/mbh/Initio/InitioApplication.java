package com.mbh.Initio;

import com.mbh.Initio.cli.InitioCommand;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication
public class InitioApplication {

	public static void main(String[] args) {
		int code = new CommandLine(new InitioCommand()).execute(args);
		System.exit(code);
	}
}
