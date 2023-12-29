/**
 * The value will be true, if the user's time is UTC+8. UTC+8 is the time zone of China.
 */
const isChinaTimezone = new Date().getTimezoneOffset() === -480;

/**
 * Injects the Google Map script into the head of the document
 * TODO(emoldino-woojin): We plan to remove the part below and change it to handle it as a hook where Google Map API is used.
 */
function injectGoogleMap() {
  const head = document.getElementsByTagName("head")[0];
  const script = document.createElement("script");
  script.async = false;
  script.type = "text/javascript";
  script.src =
    "https://maps.googleapis.com/maps/api/js?key=AIzaSyBUovdhr95iIrtFrQ3nMIaXPLstuIdrmcM&language=en&callback=Function.prototype";
  head.appendChild(script);
}

if (!isChinaTimezone) injectGoogleMap();
