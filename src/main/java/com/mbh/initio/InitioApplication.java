package com.mbh.initio;

import com.mbh.initio.cli.InitioCliRuntimeHints;
import com.mbh.initio.cli.InitioCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportRuntimeHints;
import picocli.CommandLine;

/**
 * Boots a minimal Spring context before dispatching to picocli. This is required for GraalVM
 * native-image builds: Spring's AOT processing (spring-boot:process-aot) always traces whichever
 * class main() actually is (there is no way to point native-image's real entry point at one class
 * while AOT-processing a different one — the AOT-traced class becomes the native image's actual
 * main class), and that single ahead-of-time trace bakes in one fixed bean graph for the whole
 * native image. Since the dashboard subcommand's own separate context (see DashboardLauncher)
 * needs the servlet/MVC beans, this bootstrap must use WebApplicationType.SERVLET too — a
 * different WebApplicationType requested at runtime does not change which beans exist under
 * native-image. server.port=-1 and logging turned off keep plain CLI commands from actually
 * opening a listening socket or printing this otherwise-unused context's startup noise.
 */
@SpringBootApplication
@ImportRuntimeHints(InitioCliRuntimeHints.class)
public class InitioApplication {

	public static void main(String[] args) {
		System.exit(execute(args));
	}

	static int execute(String[] args) {
		SpringApplication application = new SpringApplication(InitioApplication.class);
		application.setWebApplicationType(WebApplicationType.SERVLET);
		application.setLogStartupInfo(false);
		try (ConfigurableApplicationContext context = application.run(
				"--server.port=-1",
				"--logging.level.root=OFF"
		)) {
			return new CommandLine(new InitioCommand()).execute(args);
		}
	}
}
