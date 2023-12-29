function getTimezoneOffsetString() {
  const offset = new Date().getTimezoneOffset();
  const sign = offset < 0 ? "+" : "-";
  const pad = (num) => {
    const norm = Math.floor(Math.abs(num));
    return String(norm).padStart(2, "0");
  };
  return `UTC${sign}${pad(offset / 60)}:${pad(offset % 60)}`;
}

/**
 * This code sets the default headers for Axios requests
 * to include the CSRF token and timezone, which are required by Spring Security.
 */
function updateAxiosHeaders() {
  window.axios.defaults.headers.common = {
    ...window.axios.defaults.headers.common,
    "X-Requested-With": "XMLHttpRequest",
    timezone:
      Intl.DateTimeFormat().resolvedOptions().timeZone ??
      getTimezoneOffsetString(),
  };

  const csrfToken = document
    .querySelector("meta[name='_csrf']")
    .getAttribute("content");
  const csrfTokenKey = document
    .querySelector("meta[name='_csrf_header']")
    .getAttribute("content");

  if (csrfToken && csrfTokenKey) {
    window.axios.defaults.headers.common[csrfTokenKey] = csrfToken;
  }
}

updateAxiosHeaders();
