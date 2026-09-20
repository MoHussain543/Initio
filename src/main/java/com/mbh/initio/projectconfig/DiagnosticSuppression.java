package com.mbh.initio.projectconfig;

import com.mbh.initio.diagnostic.DiagnosticRuleId;

import java.util.Objects;

public sealed interface DiagnosticSuppression {

	record Environment(String name) implements DiagnosticSuppression {
		public Environment {
			Objects.requireNonNull(name, "name");
		}
	}

	record Port(int port) implements DiagnosticSuppression {
		public Port {
			if (port < 1 || port > 65535) {
				throw new IllegalArgumentException("port must be between 1 and 65535");
			}
		}
	}

	record Rule(DiagnosticRuleId ruleId) implements DiagnosticSuppression {
		public Rule {
			Objects.requireNonNull(ruleId, "ruleId");
		}
	}
}
