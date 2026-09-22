package com.mbh.initio.cli;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * Picocli builds its command tree (and version providers) by reflectively instantiating each
 * {@code @Command}-annotated class and setting its {@code @Spec}/{@code @Parameters}/{@code @Option}
 * fields directly. Native-image only keeps reflective access working for what it can prove is
 * reachable at build time, and it cannot see into picocli's own runtime class lookups for our
 * app-specific command classes, so each one must be registered explicitly here.
 */
public class InitioCliRuntimeHints implements RuntimeHintsRegistrar {

	private static final Class<?>[] REFLECTIVELY_CONSTRUCTED_TYPES = {
			InitioCommand.class,
			CheckCommand.class,
			ConfigCommand.class,
			ConfigValidateCommand.class,
			DashboardCommand.class,
			DoctorCommand.class,
			InfoCommand.class,
			TasksCommand.class,
			InitioVersionProvider.class
	};

	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		for (Class<?> type : REFLECTIVELY_CONSTRUCTED_TYPES) {
			hints.reflection().registerType(
					type,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
					MemberCategory.DECLARED_FIELDS
			);
		}
	}
}
