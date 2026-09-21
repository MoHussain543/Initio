(function () {
  const COMMAND_CATEGORY_ORDER = ["RUN", "DEV", "TEST", "BUILD", "OTHER"];

  const banner = document.getElementById("status-banner");
  const projectNameEl = document.getElementById("project-name");
  const projectPathEl = document.getElementById("project-path");
  const readinessBody = document.getElementById("readiness-body");
  const reanalyzeBtn = document.getElementById("reanalyze-btn");
  const app = document.getElementById("app");

  reanalyzeBtn.addEventListener("click", () => refreshAnalysis());

  app.addEventListener("click", (event) => {
    const button = event.target.closest("[data-copy]");
    if (!button) {
      return;
    }
    const text = button.getAttribute("data-copy");
    if (text) {
      copyToClipboard(text, button);
    }
  });

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", refreshAnalysis);
  } else {
    refreshAnalysis();
  }

  let hasLoadedOnce = false;

  async function refreshAnalysis() {
    setLoading(true, hasLoadedOnce);
    clearError();
    setSectionsWaiting();

    try {
      const data = await getAnalysis();
      renderDashboard(data);
      hasLoadedOnce = true;
      setLoading(false);
      reanalyzeBtn.disabled = false;
    } catch (error) {
      setLoading(false);
      showError(error);
      reanalyzeBtn.disabled = false;
      renderLoadFailure();
    }
  }

  function setLoading(isLoading, reanalyzing) {
    if (isLoading) {
      banner.classList.remove("hidden", "status-banner--error");
      banner.classList.add("status-banner--loading");
      banner.textContent = reanalyzing ? "Re-analyzing…" : "Loading analysis…";
      reanalyzeBtn.disabled = true;
    } else if (!banner.classList.contains("status-banner--error")) {
      banner.classList.add("hidden");
      banner.classList.remove("status-banner--loading");
    }
  }

  function clearError() {
    banner.classList.remove("status-banner--error");
  }

  function showError(error) {
    const code = error && error.code ? error.code : "UNKNOWN";
    const message = error && error.message ? error.message : "Something went wrong.";
    banner.classList.remove("hidden", "status-banner--loading");
    banner.classList.add("status-banner--error");
    banner.innerHTML =
      "<strong>Could not load analysis</strong><br>" +
      escapeHtml(message) +
      ' <code>(' + escapeHtml(code) + ")</code>";
  }

  function renderDashboard(data) {
    const project = data.project || {};
    const readiness = data.readiness || {};

    projectNameEl.textContent = project.name || "Unknown project";
    projectPathEl.textContent = project.path || "";

    renderReadiness(readiness);
    renderConfiguration(data.configuration);
    renderStack(project.technologies);
    renderRuntime(data.runtime);
    renderEnvironment(data.environment);
    renderServices(data.services);
    renderPorts(data.ports);
    renderIssues(data.issues);
    renderCommands(data.commands);
    renderCi(data.ci);
  }

  function renderReadiness(readiness) {
    const scored = typeof readiness.score === "number";
    const score = scored ? readiness.score : null;
    const summary = readiness.summary || "";
    const issueCount = typeof readiness.issueCount === "number" ? readiness.issueCount : 0;
    const scoreLabel = scored ? escapeHtml(String(score)) + "%" : "Unknown";
    const fill = scored ? Math.min(100, Math.max(0, score)) : 0;
    const bucket = readinessBucket(scored ? score : null);
    const bucketSuffix = bucket ? " readiness-score--" + bucket : "";
    const fillSuffix = bucket ? " progress-fill--" + bucket : "";

    readinessBody.innerHTML =
      '<div class="readiness-score' + bucketSuffix + '" aria-label="Readiness score">' +
      scoreLabel +
      "</div>" +
      '<div class="progress-track" aria-hidden="true">' +
      '<div class="progress-fill' + fillSuffix + '" style="width:' +
      fill +
      '%"></div></div>' +
      (summary ? '<p class="readiness-summary">' + escapeHtml(summary) + "</p>" : "") +
      '<p class="readiness-meta">' + escapeHtml(formatIssueMeta(issueCount)) + "</p>";
  }

  function readinessBucket(score) {
    if (score === null) {
      return "";
    }
    if (score >= 80) {
      return "good";
    }
    if (score >= 50) {
      return "warning";
    }
    return "bad";
  }

  function renderConfiguration(configuration) {
    const el = section("config");
    if (!el) {
      return;
    }
    if (!configuration || !configuration.present) {
      el.innerHTML = emptyState("No initio.yml. Project configuration is optional.");
      return;
    }
    el.innerHTML =
      '<p class="config-file mono">' +
      escapeHtml(configuration.file || "initio.yml") +
      "</p>" +
      '<ul class="config-counts">' +
      configCount("environment.required", configuration.environmentRequired) +
      configCount("runtimes", configuration.runtimes) +
      configCount("commands", configuration.commands) +
      configCount("services", configuration.services) +
      configCount("ignore", configuration.ignore) +
      "</ul>" +
      '<p class="config-note">Configured facts keep initio.yml as their source. There is no in-browser editor.</p>';
  }

  function configCount(label, value) {
    const count = typeof value === "number" ? value : 0;
    return (
      "<li><strong>" +
      escapeHtml(String(count)) +
      "</strong> " +
      escapeHtml(label) +
      "</li>"
    );
  }

  function renderStack(technologies) {
    const el = section("stack");
    if (!el) {
      return;
    }
    if (!technologies || technologies.length === 0) {
      el.innerHTML = emptyState("No technologies detected");
      return;
    }
    el.innerHTML =
      '<ul class="chip-list">' +
      technologies
        .map(
          (tech) =>
            '<li class="chip"><span class="chip-name">' +
            escapeHtml(tech.name) +
            '</span><span class="chip-meta">' +
            escapeHtml(formatCategory(tech.category)) +
            "</span></li>"
        )
        .join("") +
      "</ul>";
  }

  function renderRuntime(rows) {
    renderStatusRows(
      "runtime",
      rows,
      (row) => {
        const installed = row.installedVersion
          ? escapeHtml(row.installedVersion)
          : '<span class="muted">—</span>';
        return (
          '<span class="row-primary">' +
          escapeHtml(row.label || row.runtime) +
          '</span><span class="row-secondary">' +
          installed +
          "</span>"
        );
      },
      "No runtime requirements declared"
    );
  }

  function renderEnvironment(rows) {
    renderStatusRows(
      "environment",
      rows,
      (row) =>
        '<span class="row-primary mono">' + escapeHtml(row.name) + "</span>",
      "No environment variables declared"
    );
  }

  function renderServices(rows) {
    renderStatusRows(
      "services",
      rows,
      (row) => {
        let label = escapeHtml(row.name);
        if (row.image) {
          label += ' <span class="muted">(' + escapeHtml(row.image) + ")</span>";
        }
        return '<span class="row-primary">' + label + "</span>";
      },
      "No compose services declared"
    );
  }

  function renderPorts(rows) {
    renderStatusRows(
      "ports",
      rows,
      (row) => {
        let secondary = row.label ? escapeHtml(row.label) : "";
        if (row.occupantHint) {
          secondary += (secondary ? " · " : "") + escapeHtml(row.occupantHint);
        }
        return (
          '<span class="row-primary mono">' +
          escapeHtml(String(row.port)) +
          '</span><span class="row-secondary">' +
          (secondary || "—") +
          "</span>"
        );
      },
      "No ports declared"
    );
  }

  function renderStatusRows(sectionKey, rows, labelHtml, emptyMessage) {
    const el = section(sectionKey);
    if (!el) {
      return;
    }
    if (!rows || rows.length === 0) {
      el.innerHTML = emptyState(emptyMessage);
      return;
    }
    el.innerHTML =
      '<ul class="status-rows">' +
      rows
        .map((row) => {
          const status = row.status || "UNVERIFIED";
          return (
            '<li class="status-row">' +
            '<span class="status-icon ' +
            statusIconClass(status) +
            '" aria-hidden="true">' +
            statusIconChar(status) +
            "</span>" +
            '<div class="status-row-body">' +
            labelHtml(row) +
            '<span class="status-label">' +
            escapeHtml(formatStatusLabel(status)) +
            "</span>" +
            sourceHtml(row.source) +
            "</div></li>"
          );
        })
        .join("") +
      "</ul>";
  }

  function renderIssues(issues) {
    const el = section("issues");
    if (!el) {
      return;
    }
    if (!issues || issues.length === 0) {
      el.innerHTML = emptyState("No issues found. Your environment matches declared requirements.");
      return;
    }
    el.innerHTML =
      '<ul class="issue-list">' +
      issues
        .map((issue) => {
          const severity = (issue.severity || "INFO").toLowerCase();
          const copyText = issue.copyText || issue.suggestedAction || "";
          const sources = Array.isArray(issue.sources) ? issue.sources : [];
          const copyButton = copyText
            ? copyButtonHtml(copyText, "Copy")
            : "";
          const sourceHtml = sources.length
            ? '<p class="issue-source">' +
              sources.map((source) => escapeHtml(String(source))).join(" · ") +
              "</p>"
            : "";
          return (
            '<li class="issue-card issue-card--' +
            escapeHtml(severity) +
            '">' +
            '<div class="issue-head">' +
            '<span class="badge badge--' +
            escapeHtml(severity) +
            '">' +
            escapeHtml(issue.severity || "INFO") +
            "</span>" +
            (issue.ruleId
              ? '<span class="issue-rule">' + escapeHtml(issue.ruleId) + "</span>"
              : "") +
            '<h3 class="issue-title">' +
            escapeHtml(issue.title) +
            "</h3></div>" +
            '<p class="issue-detail">' +
            escapeHtml(issue.detail) +
            "</p>" +
            sourceHtml +
            (copyButton ? '<div class="issue-actions">' + copyButton + "</div>" : "") +
            "</li>"
          );
        })
        .join("") +
      "</ul>";
  }

  function renderCommands(commands) {
    const el = section("commands");
    if (!el) {
      return;
    }
    if (!commands || commands.length === 0) {
      el.innerHTML = emptyState("No commands detected");
      return;
    }

    const grouped = groupCommands(commands);
    let html = "";
    for (const category of COMMAND_CATEGORY_ORDER) {
      const list = grouped[category];
      if (!list || list.length === 0) {
        continue;
      }
      html +=
        '<div class="command-group"><h3 class="command-group-title">' +
        escapeHtml(category) +
        '</h3><ul class="command-list">';
      for (const cmd of list) {
        html +=
          '<li class="command-item">' +
          '<code class="command-text">' +
          escapeHtml(cmd.command) +
          "</code>" +
          '<div class="command-meta">' +
          '<span class="badge badge--origin">' +
          escapeHtml(formatOrigin(cmd.origin)) +
          "</span>" +
          '<span class="command-source">' +
          escapeHtml(cmd.sourceDescription || "") +
          (cmd.sourceFile ? " · " + escapeHtml(shortPath(cmd.sourceFile)) : "") +
          "</span></div>" +
          copyButtonHtml(cmd.command, "Copy") +
          "</li>";
      }
      html += "</ul></div>";
    }
    el.innerHTML = html;
  }

  function renderCi(entries) {
    const el = section("ci");
    if (!el) {
      return;
    }
    if (!entries || entries.length === 0) {
      el.innerHTML = emptyState("No GitHub Actions workflows detected");
      return;
    }
    el.innerHTML =
      '<ul class="ci-list">' +
      entries
        .map((entry) => {
          let body = '<li class="ci-card"><p class="ci-workflow mono">' + escapeHtml(shortPath(entry.workflowFile)) + "</p>";
          if (entry.javaVersions && entry.javaVersions.length) {
            body +=
              '<p class="ci-line">' +
              entry.javaVersions.map((v) => "Java " + escapeHtml(v)).join(", ") +
              "</p>";
          }
          if (entry.nodeVersions && entry.nodeVersions.length) {
            body +=
              '<p class="ci-line">' +
              entry.nodeVersions.map((v) => "Node " + escapeHtml(v)).join(", ") +
              "</p>";
          }
          if (entry.runCommands && entry.runCommands.length) {
            body += '<ul class="ci-runs">';
            for (const run of entry.runCommands.slice(0, 5)) {
              body +=
                '<li><code>' +
                escapeHtml(run) +
                "</code>" +
                copyButtonHtml(run, "Copy") +
                "</li>";
            }
            body += "</ul>";
          }
          return body + "</li>";
        })
        .join("") +
      "</ul>";
  }

  function groupCommands(commands) {
    const grouped = {};
    const sorted = commands.slice().sort((a, b) => {
      const ca = COMMAND_CATEGORY_ORDER.indexOf(a.category);
      const cb = COMMAND_CATEGORY_ORDER.indexOf(b.category);
      const ra = ca === -1 ? 99 : ca;
      const rb = cb === -1 ? 99 : cb;
      if (ra !== rb) {
        return ra - rb;
      }
      return (a.command || "").localeCompare(b.command || "");
    });
    for (const cmd of sorted) {
      const key = cmd.category || "OTHER";
      if (!grouped[key]) {
        grouped[key] = [];
      }
      grouped[key].push(cmd);
    }
    return grouped;
  }

  function setSectionsWaiting() {
    projectNameEl.textContent = "…";
    projectPathEl.textContent = "";
    readinessBody.innerHTML = '<p class="placeholder">Loading…</p>';
    document.querySelectorAll("[data-section]").forEach((el) => {
      el.innerHTML = '<p class="placeholder">Loading…</p>';
    });
  }

  function renderLoadFailure() {
    projectNameEl.textContent = "—";
    projectPathEl.textContent = "";
    readinessBody.innerHTML =
      '<p class="placeholder">Analysis unavailable. Fix the error above and click Re-analyze.</p>';
    document.querySelectorAll("[data-section]").forEach((el) => {
      el.innerHTML = '<p class="placeholder">—</p>';
    });
  }

  function section(key) {
    return document.querySelector('[data-section="' + key + '"]');
  }

  function sourceHtml(source) {
    if (!source) {
      return "";
    }
    return '<span class="row-source">' + escapeHtml(shortPath(source)) + "</span>";
  }

  function emptyState(message) {
    return '<p class="placeholder">' + escapeHtml(message) + "</p>";
  }

  function copyButtonHtml(text, label) {
    return (
      '<button type="button" class="btn btn-copy" data-copy="' +
      escapeHtmlAttr(text) +
      '">' +
      escapeHtml(label) +
      "</button>"
    );
  }

  async function copyToClipboard(text, button) {
    try {
      await navigator.clipboard.writeText(text);
      const original = button.textContent;
      button.textContent = "Copied";
      button.classList.add("btn-copy--done");
      setTimeout(() => {
        button.textContent = original;
        button.classList.remove("btn-copy--done");
      }, 1500);
    } catch (clipboardError) {
      button.textContent = "Failed";
      setTimeout(() => {
        button.textContent = "Copy";
      }, 1500);
    }
  }

  function statusIconClass(status) {
    if (isGoodStatus(status)) {
      return "status-icon--ok";
    }
    if (isBadStatus(status)) {
      return "status-icon--bad";
    }
    return "status-icon--unknown";
  }

  function statusIconChar(status) {
    if (isGoodStatus(status)) {
      return "✓";
    }
    if (isBadStatus(status)) {
      return "✗";
    }
    return "?";
  }

  function isGoodStatus(status) {
    return (
      status === "SATISFIED" ||
      status === "RUNNING" ||
      status === "AVAILABLE" ||
      status === "IN_USE_BY_EXPECTED_SERVICE"
    );
  }

  function isBadStatus(status) {
    return (
      status === "MISSING" ||
      status === "EMPTY" ||
      status === "INCOMPATIBLE" ||
      status === "STOPPED" ||
      status === "CONFLICT"
    );
  }

  function formatStatusLabel(status) {
    return status.replace(/_/g, " ").toLowerCase();
  }

  function formatOrigin(origin) {
    switch (origin) {
      case "DECLARED":
        return "Declared";
      case "CONVENTIONAL":
        return "Conventional";
      case "INFERRED":
        return "Inferred";
      case "CONFIGURED":
        return "Configured";
      default:
        return origin || "Unknown";
    }
  }

  function formatCategory(category) {
    if (!category) {
      return "";
    }
    return category.charAt(0) + category.slice(1).toLowerCase().replace(/_/g, " ");
  }

  function shortPath(path) {
    if (!path) {
      return "";
    }
    const parts = path.split(/[/\\]/);
    if (parts.length <= 3) {
      return path;
    }
    return "…/" + parts.slice(-2).join("/");
  }

  function formatIssueMeta(issueCount) {
    if (issueCount === 0) {
      return "No open issues in this snapshot.";
    }
    return issueCount + " issue" + (issueCount === 1 ? "" : "s") + " in this snapshot.";
  }

  function escapeHtml(value) {
    return String(value)
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function escapeHtmlAttr(value) {
    return escapeHtml(value).replace(/'/g, "&#39;");
  }

  window.refreshAnalysis = refreshAnalysis;
})();
