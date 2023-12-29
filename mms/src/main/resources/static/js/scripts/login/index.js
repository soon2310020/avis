{
  /* <script type="text/javascript" th:inline="javascript"> */
}
window.onload = function () {
  document.title = "Login" + " | eMoldino";
  localStorage.setItem("REDIRECT_LOGIN_HASH", window.location.hash);
};

$(function () {
  let params = Common.parseParams(window.location.search);
  var error = params.error;
  if (error != null) {
    if (error == "1") {
      Common.alert(
        "Your account has been locked. Please check your email to reset your password."
      );
    } else if (error == "sso") {
      Common.alert("SSO not working. ");
    } else if (error == "2") {
      Common.alert(
        "Your password has been expired . Please check your email to update with a new password."
      );
    } else if (error == "3") {
      Common.alert(
        "Your SSO account access has been requested. Please wait until this will be approved."
      );
    } else {
      Common.alert("Invalid username and password.");
    }
  }
});

$(document).ready(() => {
  Common.removeWave(800);
  $("#show-icon").click(() => {
    console.log("Click Here");
    const currentElement = $("#show-icon");
    const inputElement = $("#password");
    if (inputElement.attr("type") == "password") {
      inputElement.attr("type", "text");
      currentElement.removeClass("fa-eye-slash");
      currentElement.addClass("fa-eye");
    } else {
      inputElement.attr("type", "password");
      currentElement.removeClass("fa-eye");
      currentElement.addClass("fa-eye-slash");
    }
  });
});
// </script>
