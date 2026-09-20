package com.mbh.initio.web;

import com.mbh.initio.InitioApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;

public final class DashboardLauncher {

	public int start(Path projectPath, String host, int port, PrintWriter out, PrintWriter err) {
		try {
			validateNetwork(host, port);
			verifyPortAvailable(host, port);
		} catch (DashboardStartupFailure failure) {
			err.println(failure.getMessage());
			return failure.exitCode();
		}

		SpringApplication application = new SpringApplication(InitioApplication.class);
		application.setWebApplicationType(WebApplicationType.SERVLET);

		String projectPathProperty = projectPath.toAbsolutePath().normalize().toString();
		ConfigurableApplicationContext context = application.run(
				"--server.port=" + port,
				"--server.address=" + host,
				"--initio.dashboard.project-path=" + projectPathProperty
		);

		out.println();
		out.println("Initio dashboard running at http://" + host + ":" + port + "/");
		out.println("Project: " + projectPathProperty);
		out.println("Press Ctrl+C to stop.");
		out.println();

		try {
			awaitShutdown(context);
			return 0;
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
			context.close();
			return 130;
		}
	}

	static void awaitShutdown(ConfigurableApplicationContext context) throws InterruptedException {
		CountDownLatch shutdown = new CountDownLatch(1);
		context.addApplicationListener((ContextClosedEvent event) -> shutdown.countDown());
		shutdown.await();
	}

	static void validateNetwork(String host, int port) throws DashboardStartupFailure {
		if (port < 1 || port > 65535) {
			throw new DashboardStartupFailure(
					"""
					Initio could not start the dashboard.

					Port %d is not valid. Use a value between 1 and 65535.
					""".formatted(port),
					1
			);
		}
		try {
			InetAddress.getByName(host);
		} catch (UnknownHostException exception) {
			throw new DashboardStartupFailure(
					"""
					Initio could not start the dashboard.

					Host "%s" is not valid.
					""".formatted(host),
					1
			);
		}
	}

	static void verifyPortAvailable(String host, int port) throws DashboardStartupFailure {
		try (ServerSocket socket = new ServerSocket()) {
			socket.setReuseAddress(false);
			socket.bind(new InetSocketAddress(host, port));
		} catch (IOException exception) {
			throw new DashboardStartupFailure(
					"""
					Initio could not start the dashboard.

					Port %d is already in use.

					Try:
					  initio dashboard --port %d
					""".formatted(port, suggestAlternatePort(port)),
					1
			);
		}
	}

	private static int suggestAlternatePort(int port) {
		return port == 7331 ? 7332 : port + 1;
	}
}
