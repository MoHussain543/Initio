/**
 * Fetches the dashboard analysis snapshot from the Initio API.
 * @returns {Promise<object>} AnalysisResponse JSON
 * @throws {{ code: string, message: string }} ApiErrorResponse shape on failure
 */
async function getAnalysis() {
  let response;
  try {
    response = await fetch("/api/v1/analysis", {
      headers: { Accept: "application/json" },
    });
  } catch (networkError) {
    throw {
      code: "NETWORK_ERROR",
      message: "Could not reach the Initio dashboard API. Is the server still running?",
    };
  }

  if (response.ok) {
    return response.json();
  }

  let error = {
    code: "REQUEST_FAILED",
    message: "The analysis request failed.",
  };

  try {
    const body = await response.json();
    if (body && typeof body.code === "string" && typeof body.message === "string") {
      error = { code: body.code, message: body.message };
    }
  } catch (parseError) {
    error.message = response.statusText || error.message;
  }

  throw error;
}
