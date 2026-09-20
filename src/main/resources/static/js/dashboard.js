(function () {
  const banner = document.getElementById("status-banner");
  const projectNameEl = document.getElementById("project-name");
  const projectPathEl = document.getElementById("project-path");
  const readinessBody = document.getElementById("readiness-body");
  const reanalyzeBtn = document.getElementById("reanalyze-btn");

  const sectionBodies = document.querySelectorAll("[data-section]");

  reanalyzeBtn.addEventListener("click", () => {
    refreshAnalysis();
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
      renderShell(data);
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

  function renderShell(data) {
    const project = data.project || {};
    const readiness = data.readiness || {};

    projectNameEl.textContent = project.name || "Unknown project";
    projectPathEl.textContent = project.path || "";

    const score = typeof readiness.score === "number" ? readiness.score : 0;
    const summary = readiness.summary || "";
    const issueCount = typeof readiness.issueCount === "number" ? readiness.issueCount : 0;

    readinessBody.innerHTML =
      '<div class="readiness-score" aria-label="Readiness score">' +
      escapeHtml(String(score)) +
      "%</div>" +
      '<div class="progress-track" aria-hidden="true">' +
      '<div class="progress-fill" style="width:' +
      Math.min(100, Math.max(0, score)) +
      '%"></div></div>' +
      (summary
        ? '<p class="readiness-summary">' + escapeHtml(summary) + "</p>"
        : "") +
      '<p class="readiness-meta">' +
      escapeHtml(formatIssueMeta(issueCount)) +
      "</p>";

    setSectionSummary("stack", countLabel(project.technologies, "technology", "technologies"));
    setSectionSummary("runtime", countLabel(data.runtime, "runtime requirement", "runtime requirements"));
    setSectionSummary("environment", countLabel(data.environment, "variable", "variables"));
    setSectionSummary("services", countLabel(data.services, "service", "services"));
    setSectionSummary("ports", countLabel(data.ports, "port", "ports"));
    setSectionSummary("issues", formatIssuesTeaser(data.issues));
    setSectionSummary("commands", countLabel(data.commands, "command", "commands"));
    setSectionSummary("ci", countLabel(data.ci, "workflow", "workflows"));
  }

  function setSectionsWaiting() {
    projectNameEl.textContent = "…";
    projectPathEl.textContent = "";
    readinessBody.innerHTML = '<p class="placeholder">Loading…</p>';
    sectionBodies.forEach((el) => {
      el.innerHTML = '<p class="placeholder">Loading…</p>';
    });
  }

  function renderLoadFailure() {
    projectNameEl.textContent = "—";
    projectPathEl.textContent = "";
    readinessBody.innerHTML =
      '<p class="placeholder">Analysis unavailable. Fix the error above and click Re-analyze.</p>';
    sectionBodies.forEach((el) => {
      el.innerHTML = '<p class="placeholder">—</p>';
    });
  }

  function setSectionSummary(sectionKey, text) {
    const el = document.querySelector('[data-section="' + sectionKey + '"]');
    if (!el) {
      return;
    }
    el.innerHTML =
      '<p class="summary-line">' +
      escapeHtml(text) +
      '</p><p class="summary-line">Detailed view in the next dashboard slice.</p>';
  }

  function countLabel(list, singular, plural) {
    const n = Array.isArray(list) ? list.length : 0;
    if (n === 0) {
      return "None detected";
    }
    return n + " " + (n === 1 ? singular : plural) + " detected";
  }

  function formatIssuesTeaser(issues) {
    const n = Array.isArray(issues) ? issues.length : 0;
    if (n === 0) {
      return "No issues reported";
    }
    return n + " issue" + (n === 1 ? "" : "s") + " need attention";
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

  window.refreshAnalysis = refreshAnalysis;
})();
