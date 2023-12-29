$(function() {
    var error = /*[[${param.error}]]*/;
    //alert('test22')

    if (error != null) {
        if (error == "1") {
            Common.alert('User account is locked.');
        } else if (error == "sso") {
            Common.alert('SSO not working. ');
        } else {
            Common.alert('Invalid username and password.');
        }

    }
});