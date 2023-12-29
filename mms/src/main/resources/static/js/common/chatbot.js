(function chatbotLoad() {
  (function (w, d, s, o, f, js, fjs) {
    w["botsonic_widget"] = o;
    w[o] =
      w[o] ||
      function () {
        (w[o].q = w[o].q || []).push(arguments);
      };
    (js = d.createElement(s)), (fjs = d.getElementsByTagName(s)[0]);
    js.id = o;
    js.src = f;
    js.async = 1;
    fjs.parentNode.insertBefore(js, fjs);
  })(
    window,
    document,
    "script",
    "Botsonic",
    "https://d2nnr6irhfmb65.cloudfront.net/CDN/botsonic.min.js"
  );
  Botsonic("init", {
    serviceBaseUrl:
      "https://vp3az9kw2c.execute-api.us-east-1.amazonaws.com/prod",
    token: "6db53723-16b9-499d-a75d-bfe5f1ce35c1",
  });
})();
