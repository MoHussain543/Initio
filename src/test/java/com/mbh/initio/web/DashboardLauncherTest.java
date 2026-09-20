package com.mbh.initio.web;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;

import java.io.IOException;
import java.net.ServerSocket;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class DashboardLauncherTest {

	@Test
	void awaitShutdownBlocksUntilContextCloses() throws Exception {
		GenericApplicationContext context = new GenericApplicationContext();
		context.refresh();

		Thread waiter = new Thread(() -> {
			try {
				DashboardLauncher.awaitShutdown(context);
			} catch (InterruptedException exception) {
				Thread.currentThread().interrupt();
			}
		});
		waiter.start();

		Thread.sleep(200);
		assertTrue(waiter.isAlive(), "launcher should block until the Spring context closes");

		context.close();
		waiter.join(3_000);
		assertFalse(waiter.isAlive(), "waiter should finish after context shutdown");
	}

	@Test
	void verifyPortAvailableSuggests7332When7331IsTaken() throws IOException {
		try (ServerSocket occupied = new ServerSocket(7331)) {
			DashboardStartupFailure failure = assertThrows(
					DashboardStartupFailure.class,
					() -> DashboardLauncher.verifyPortAvailable("127.0.0.1", 7331)
			);
			assertTrue(failure.getMessage().contains("--port 7332"));
		} catch (IOException exception) {
			assumeTrue(false, "Could not bind port 7331 for test");
		}
	}
}
