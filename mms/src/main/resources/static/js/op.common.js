(function () {
  "use strict";
  window.addEventListener(
    "DOMContentLoaded",
    function () {
      // add hash to url after user login
      Common.addHashToUrlAfterLogin();
    },
    false
  );
  window.addEventListener(
    "load",
    function () {
      Common.initFooter();

      // validation
      Common.initValidator();

      // Event Handler
      Common.initEventHandler();

      // Navigation
      Common.initNavigation();

      // AlertCount
      Common.initAlertCount();

      // active for header tab
      Common.initHeaderTab();

      // Require Badge
      // Common.initRequireBadge();

      Common.initUserCount();

      // getCurrentConfig();
      // Common.checkSystemFunc();
      // handleUser();
      // getResourcesFake();
      hotjarLoad();
    },
    false
  );
})();

function hotjarLoad() {
  (function (h, o, t, j, a, r) {
    h.hj =
      h.hj ||
      function () {
        (h.hj.q = h.hj.q || []).push(arguments);
      };
    h._hjSettings = { hjid: 2784473, hjsv: 6 };
    a = o.getElementsByTagName("head")[0];
    r = o.createElement("script");
    r.async = 1;
    r.src = t + h._hjSettings.hjid + j + h._hjSettings.hjsv;
    a.appendChild(r);
  })(window, document, "https://static.hotjar.com/c/hotjar-", ".js?sv=");
}

var Common = {};
var showNotification = false;

Common.addHashToUrlAfterLogin = function () {
  if (location.href.includes("login")) return;
  const hashFromStorage = localStorage.getItem("REDIRECT_LOGIN_HASH");
  if (hashFromStorage) {
    window.location.hash = hashFromStorage;
    localStorage.removeItem("REDIRECT_LOGIN_HASH");
  }
};
Common.initFooterNumber = function () {
  const param = {
    enabled: true,
  };
  axios
    .get("/api/common/app-ver-stp?" + Common.param(param))
    .then((response) => {
      let myVersionNumber = "0";
      if (response.data.content.length > 0) {
        myVersionNumber = response.data.content[0].version;
      }
      let myVersionNumberElement = document.getElementById(
        "footerVersionNumber"
      );
      myVersionNumberElement.innerHTML =
        "Ver. " + myVersionNumber.replace(/\.$/, "");
    })
    .catch(function (error) {
      console.error(error);
    });
};

Common.onOffNumAlertOnSidebar = function (alertOn, numAlert) {
  if ($(".badge-alert").length == 0) {
    return;
  }
  var uri = URI.parse(location.href);
  var isAdminPage = uri.path.indexOf("admin") > -1 ? true : false;
  // const coll = document.getElementsByClassName("Alerts");
  var $selector = $(".nav-item.open .badge.badge-primary.badge-alert");
  console.log("onOffNumAlertOnSidebar", alertOn);
  if (isAdminPage || $selector.length == 0) {
    return;
  }
  if (numAlert) {
    $selector[0].innerText = numAlert;
  }
  if (alertOn == false) {
    $selector.hide();
  } else if (alertOn == true) {
    if ($selector[0].innerText && $selector[0].innerText != 0) {
      $selector.show();
    }
  }
};
function alertHandle(callback) {
  if ($(".badge-alert").length == 0) {
    return;
  }
  var uri = URI.parse(location.href);
  var isAdminPage = uri.path.indexOf("admin") > -1 ? true : false;
  const coll = document.getElementsByClassName("Alerts");
  let alertTotal = 0;
  let adminAlertCount = 0;
  let frontAlertCount = 0;
  console.log("function alertHandle alert-count");
  axios
    .get("/api/molds/alert-count?adminPage=" + isAdminPage)
    .then(function (response) {
      for (var i = 0; i < response.data.length; i++) {
        var alertCount = response.data[i];
        if (isAdminPage && alertCount.key === "badge-alert-total-admin") {
          if (isAdminPage) {
            alertTotal = alertCount.count;
          }
          adminAlertCount = alertCount.count;
        }
        if (alertCount.key === "badge-alert-total" && !isAdminPage) {
          alertTotal = alertCount.count;
          frontAlertCount = alertCount.count;
        }
        var $selector = $("." + alertCount.key);
        if ($selector.length == 0) {
          continue;
        }
        if (alertCount.count == 0) {
          $selector.text(alertCount.count);
          $selector.hide();
        } else {
          if (showNotification) {
            $selector.text(alertCount.count).css("display", "inline-block");
          } else {
            $selector.text(alertCount.count).css("display", "none");
          }
        }
      }
      if (alertTotal > 0) {
        coll[0].innerHTML = alertTotal;
        if (showNotification) {
          coll[0].style.display = "flex";
        } else {
          coll[0].style.display = "none";
        }
      }
      // alert count for header tab
      // if (adminAlertCount > 0) {
      //     const headerAdminAlert = document.getElementById('tab-admin-notification');
      //     if (headerAdminAlert) {
      //         headerAdminAlert.innerHTML = adminAlertCount + '';
      //         headerAdminAlert.style.display = 'flex';
      //     }
      // }

      if (frontAlertCount > 0) {
        const headerUserAlert = document.getElementById(
          "tab-user-notification"
        );
        headerUserAlert.innerHTML = frontAlertCount + "";
        headerUserAlert.style.display = "flex";
      }

      callback();
    })
    .catch(function (error) {
      console.log(error.response);
    });

  axios
    .get("/api/topics/totalNewRecode")
    .then(function (response) {
      const communicationAlertCount = response.data;
      if (communicationAlertCount > 0) {
        const headerCommunicationAlert = document.getElementById(
          "tab-communication-notification"
        );
        headerCommunicationAlert.innerHTML = communicationAlertCount + "";
        headerCommunicationAlert.style.display = "flex";
      }
    })
    .catch(function (error) {
      console.log(error.response);
    });
}

Common.handleHeaderNoti = function () {
  //fa fa-bell-slash-o
  ///api/users/notify/status
  const iconNotification = document.getElementById("icon-notification");
  const alertNotification = document.getElementById("alert-notification");
  const notiHeader = document.getElementById("notification-container");
  const notiButton = document.getElementById("header-notification");

  notiButton.addEventListener("click", function () {
    //notification-dropdown
    showNotification = !showNotification;
    if (showNotification) {
      alertHandle(() => {
        // iconNotification.className = "icon-bell";
        alertNotification.style.display = "block";
        const notiDropdown = document.getElementById("notification-dropdown");
        notiDropdown.style.display = "none";
      });
    } else {
      alertHandle(() => {
        // iconNotification.className = "fa fa-bell-slash-o";
        alertNotification.style.display = "none";
        const notiDropdown = document.getElementById("notification-dropdown");
        notiDropdown.style.display = "none";
      });
    }
    axios
      .put("/api/users/notify/update")
      .then(function (response) {
        showNotification = response.data;
      })
      .catch(function (error) {
        console.log(error.response);
      });
  });
  notiHeader.addEventListener("mouseover", function () {
    //notification-dropdown
    if (showNotification) {
      const notiDropdown = document.getElementById("notification-dropdown");
      notiDropdown.style.display = "block";
    }
  });

  notiHeader.addEventListener("mouseout", function () {
    //notification-dropdown
    const notiDropdown = document.getElementById("notification-dropdown");
    notiDropdown.style.display = "none";
  });
};

Common.initFooter = function () {
  console.log("Common.$uri.pathname: ", Common.$uri.pathname);
  const checkDoashBoardSite = Common.$uri.pathname.trim() === "/";
  const qrFooter = document.getElementById("qr-footer");
  if (!qrFooter) {
    return;
  }
  if (Common.checkMobile() && checkDoashBoardSite) {
    qrFooter.style.display = "flex";
  }
};

Common.initValidator = function () {
  // Fetch all the forms we want to apply custom Bootstrap validation styles to
  var forms = document.getElementsByClassName("needs-validation");
  // Loop over them and prevent submission
  var validation = Array.prototype.filter.call(forms, function (form) {
    form.addEventListener(
      "submit",
      function (event) {
        if (form.checkValidity() === false) {
          event.preventDefault();
          event.stopPropagation();
        }
        form.classList.add("was-validated");
      },
      false
    );
  });
};

Common.initEventHandler = function () {
  // 로그아웃
  $("#op-logout").on("click", function (e) {
    // e.preventDefault();
    // $('#op-logout-form').submit();
    Common.logout();
  });
};

Common.initNavigation = function () {
  var isAdminPage = Common.$uri.pathname.indexOf("/admin") > -1;
  var isInsight = Common.$uri.pathname.indexOf("/insight") > -1;
  var isSupport =
    Common.$uri.pathname.indexOf("/support/customer-support") > -1;
  var isReportPage = Common.$uri.pathname.indexOf("/report") > -1;
  const coll = document.getElementsByClassName("nav-title");
  //plus-icon
  const plus = document.getElementsByClassName("plus-icon");

  const storage = sessionStorage.getItem("side_bar_open");
  let sideBarOpen;
  if (isAdminPage) {
    sideBarOpen = [1, 1, 1, 1, 1];
  } else {
    sideBarOpen = [1, 1, 1, 0];
  }
  if (isReportPage) {
    sideBarOpen = [0, 1, 1, 0, 0];
  } else {
    sideBarOpen = [1, 1, 1, 1];
  }

  if (isInsight) {
    sideBarOpen = [0, 1, 1, 0, 0];
  } else {
    sideBarOpen = [1, 1, 1, 1];
  }

  if (isSupport) {
    sideBarOpen = [0, 1, 1, 0, 0];
  } else {
    sideBarOpen = [1, 1, 1, 1];
  }

  console.log("storage: ", storage);
  if (storage === null || storage === "null") {
    sessionStorage.setItem("side_bar_open", JSON.stringify(sideBarOpen));
  } else {
    sideBarOpen = JSON.parse(storage);
    sideBarOpen[1] = 1;
    // sideBarOpen[2] = 0;
  }
  for (let i = 0; i < coll.length; i++) {
    coll[i].classList.toggle("active");
    const content = coll[i].nextElementSibling;
    if (sideBarOpen[i] === 1) {
      sideBarOpen[i] = 1;
      content.classList.add("side-content-active");
      // plus[i].src = '/images/icon/angle-arrow-up.svg';
    }
  }
  for (let i = 0; i < coll.length; i++) {
    coll[i].addEventListener("click", function () {
      //if (i === 2 && !isAdminPage) return;
      this.classList.toggle("active");
      const content = this.nextElementSibling;
      console.log("content.style.display: ", content);
      if (content.classList.contains("side-content-active")) {
        sideBarOpen[i] = 0;
        content.classList.add("side-content-deactive");
        content.classList.remove("side-content-active");
        // plus[i].src = '/images/icon/angle-arrow-down.svg';
      } else {
        sideBarOpen[i] = 1;
        content.classList.add("side-content-active");
        content.classList.remove("side-content-deactive");
        // plus[i].src = '/images/icon/angle-arrow-up.svg';
        ///images/icon/plus-icon.svg
      }
      console.log("sideBarOpen: ", sideBarOpen);
      sessionStorage.setItem("side_bar_open", JSON.stringify(sideBarOpen));
    });
  }
  // add event listener for click open level 3
  var navLinkToggle = document.getElementsByClassName("nav-dropdown-toggle");
  $(".nav-dropdown-toggle").on("click", function () {
    $(this).toggleClass("active");
    $(this).parent().find("ul").toggleClass("active");
    let subImgIcon = $(this).find(".sub-plus-icon");
    if ($(this).hasClass("active")) {
      subImgIcon.attr("src", "/images/icon/angle-arrow-up.svg");
    } else {
      subImgIcon.attr("src", "/images/icon/angle-arrow-down.svg");
    }
  });
  var uri = Common.$uri.pathname;
  var navLinks = $(".sidebar  ");
  var firstMenu = '<a style="color: #5F57FB" href="/">Home</a>';
  if (isAdminPage) {
    firstMenu = "Administration";
  }
  if (isInsight) {
    firstMenu = "Insights";
  }
  if (isSupport) {
    firstMenu = "Communication";
  }
  if (isReportPage) {
    firstMenu = "Report";
  }
  // $('.op-nav-0').html(firstMenu);

  navLinks.removeClass("active");

  /*
    if (uri == '/') {
        $('.sidebar .nav-link').eq(0).addClass('active');
        return;
    }
*/

  var nav1 = "";
  var nav2 = "";
  // $('.sidebar .nav-link').each(function (index) {
  //     var link = $(this).attr('href');

  //     if (link && uri.indexOf(link) > -1 && link != '/') {

  //         $(this).addClass('active');
  //         nav1 = $(this).closest('li').prevAll('.nav-title').first().text();
  //         nav2 = $(this).find('span:eq(0)').text();
  //     }
  // });

  $(".nav-level-3 .nav-link-level-3").each(function (index) {
    var link = $(this).attr("href");
    if (uri.indexOf(link) > -1 && link != "/") {
      $(this).addClass("active");
      nav1 = $(this).closest("li").prevAll(".nav-title").first().text();
      nav2 = $(this).find("span:eq(0)").text();
    }
  });

  $(".nav-level-3 .nav-link-level-3").each(function () {
    var link = $(this).attr("href");

    if (uri.indexOf(link) > -1 && link != "/") {
      $(this).addClass("active");
      $(this).closest("ul").addClass("active");
      $(this)
        .closest(".nav-item")
        .find(".sub-plus-icon")
        .attr("src", "/images/icon/angle-arrow-up.svg");
    }
  });
  $(".nav-level-3").each(function () {
    if (!$(this).hasClass("active")) {
      $(this).parent().find(".nav-link").removeClass("active");
    }
  });

  /*
    if (nav1 != '') {
        $('.op-nav-1').text(nav1).show();
    }

    if (nav2 != '') {
        $('.op-nav-2').text(nav2).show();
    }
    */
};

Common.initHeaderTab = function () {
  var uri = Common.$uri.pathname;
  if (uri === "/") {
    $(".tab-link").eq(0).parent().addClass("active");
    return;
  }
  if (uri.startsWith("/profile")) {
    return;
  }
  /*
        if (uri.startsWith('/admin')) {
            $('.tab-admin').addClass('active');
            console.log('tab-admin');
        } else if (uri.startsWith('/report')) {
            console.log('tab-reports');
            $('.tab-reports').addClass('active');
        } else if (uri.startsWith('/insight')) {
            console.log('tab-insight');
            $('.tab-insight').addClass('active');
        } else if (uri.startsWith('/support')) {
            console.log('tab-support');
            $('.tab-support').addClass('active');
        } else {
            console.log('tab-user');
            $('.tab-user').addClass('active');
        }
    */
};

Common.initAlertCount = function (alertOnly) {
  if ($(".badge-alert").length == 0) {
    return;
  }
  var uri = URI.parse(location.href);
  var isAdminPage = uri.path.indexOf("admin") > -1 ? true : false;
  const coll = document.getElementsByClassName("Alerts");
  let alertTotal = 0;
  //sidebar-icon
  console.log("Common.initAlertCount");
  axios
    .get("/api/users/notify/status")
    .then(function (response) {
      showNotification = true;
      axios
        .get("/api/molds/alert-count?adminPage=" + isAdminPage)
        .then(function (response) {
          let adminAlertCount = 0;
          let frontAlertCount = 0;
          for (var i = 0; i < response.data.length; i++) {
            var alertCount = response.data[i];
            if (alertCount.key === "badge-alert-total-admin") {
              if (isAdminPage) {
                alertTotal = alertCount.count;
              }
              adminAlertCount = alertCount.count;
            }

            if (alertCount.key === "badge-alert-total") {
              if (!isAdminPage) {
                alertTotal = alertCount.count;
              }
              frontAlertCount = alertCount.count;
            }
            var $selector = $("." + alertCount.key);
            if ($selector.length == 0) {
              continue;
            }
            if (alertCount.count == 0) {
              $selector.text(alertCount.count);
              $selector.hide();
            } else {
              if (showNotification) {
                $selector.text(alertCount.count).css("display", "inline-block");
              }
              if (!showNotification) {
                const iconNotification =
                  document.getElementById("icon-notification");
                const alertNotification =
                  document.getElementById("alert-notification");
                // iconNotification.className = "fa fa-bell-slash-o";
                alertNotification.style.display = "none";
                const notiDropdown = document.getElementById(
                  "notification-dropdown"
                );
                notiDropdown.style.display = "none";
              }
            }
          }
          // // alert count for header tab
          // if (adminAlertCount > 0) {
          //     const headerAdminAlert = document.getElementById('tab-admin-notification');
          //     if (headerAdminAlert) {
          //         headerAdminAlert.innerHTML = adminAlertCount + '';
          //         headerAdminAlert.style.display = 'flex';
          //     }
          // }

          if (frontAlertCount > 0) {
            const headerUserAlert = document.getElementById(
              "tab-user-notification"
            );
            headerUserAlert.innerHTML = frontAlertCount + "";
            headerUserAlert.style.display = "flex";
          }

          Common.handleHeaderNoti();
          if (alertTotal > 0) {
            if (showNotification) {
              coll[0].innerHTML = alertTotal;
              coll[0].style.display = "flex";
            }
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });

      if (!alertOnly)
        axios
          .get("/api/topics/totalNewRecode")
          .then(function (response) {
            const communicationAlertCount = response.data;
            if (communicationAlertCount > 0) {
              const headerCommunicationAlert = document.getElementById(
                "tab-communication-notification"
              );
              headerCommunicationAlert.innerHTML = communicationAlertCount + "";
              headerCommunicationAlert.style.display = "flex";
            }
          })
          .catch(function (error) {
            console.log(error.response);
          });
    })
    .catch(function (error) {
      console.log(error);
    });
};

Common.initUserCount = function () {
  if ($(".badge-alert").length == 0) {
    return;
  }
  axios
    .get("/api/users/requested-count")
    .then(function (response) {
      //badge-alert-2110
      var $selector = $(".badge-alert-2110");
      if ($selector.length == 0) {
        return;
      }
      if (response.data > 0) {
        $selector.text(response.data).css("display", "inline-block");
      }
    })
    .catch(function (error) {
      console.log(error);
    });
};

Common.initRequireBadge = function () {
  console.log("Common.initRequireBadge");
  if ($(".needs-validation").length == 0) {
    return;
  }
  $(
    ".needs-validation input, .needs-validation select, .needs-validation textarea"
  ).each(function () {
    var required = $(this).attr("required");

    if (required == "required") {
      $(this)
        .parent()
        .parent()
        .find("label")
        .append(' <span class="badge-require"></span>');
    }
  });
};

Common.alert = function (message, title) {
  var $alert = $("#op-alert");
  $(".op-modal-cancel").hide();
  $alert.find(".modal-body p").text(message);
  $alert.find(".modal-title").text(title);
  $alert.modal("show");
};
Common.alertCallback = function () {};
Common.alertCallbackClose = function () {};

Common.confirm = function (message, title) {
  var $alert = $("#op-alert");
  $(".op-modal-cancel").show();
  $alert.find(".modal-body p").text(message);
  $alert.modal("show");
};

Common.logout = async () => {
  window.sessionStorage.clear();
  try {
    await axios.post("/logout");
    window.location.href = "/";
  } catch (error) {
    console.log(error);
  }
};

Common.getPagingData = function (page) {
  // var jsonString = '{"content":[{"createdAt":1533366112.000000000,"updatedAt":1533366112.000000000,"id":1,"name":"홍길동","loginId":"skc","email":"skc@onlinepowers.com","userType":null,"gravatar":"//www.gravatar.com/avatar/0cfe94217b10060fc58516438ae68c2a?d=mm","createdDate":"2018. 8. 4"}],"pageable":{"sort":{"sorted":false,"unsorted":true},"offset":0,"pageSize":10,"pageNumber":0,"paged":true,"unpaged":false},"last":true,"totalPages":1,"totalElements":1,"size":10,"number":0,"sort":{"sorted":false,"unsorted":true},"numberOfElements":1,"first":true}';
  //var page = JSON.parse(jsonString);

  var size = page.limitPage ? page.limitPage : 10;

  var current = page.number + 1;
  var half_size_floor = Math.floor(size / 2);

  var startPage = current < half_size_floor ? 1 : current - half_size_floor;
  startPage =
    current > page.totalPages - half_size_floor
      ? page.totalPages - size + 1
      : startPage;
  startPage = page.totalPages < size ? 1 : startPage;
  if (startPage < 1) startPage = 1;

  var endPage = startPage + size - 1;
  endPage = endPage > page.totalPages ? page.totalPages : endPage;
  endPage = page.totalPages < size ? page.totalPages : endPage;

  var pagination = [];

  if (current > 1) {
    var pageData = { isActive: false };
    pageData.pageNumber = current - 1;
    pageData.text = "Prev";
    pagination.push(pageData);
  }

  for (var pageNumber = startPage; pageNumber <= endPage; pageNumber++) {
    var pageData = { isActive: false };
    pageData.pageNumber = pageNumber;
    pageData.text = pageNumber;

    if (current == pageNumber) {
      pageData.isActive = true;
    }

    pagination.push(pageData);
  }

  if (current < endPage) {
    var pageData = { isActive: false };
    pageData.pageNumber = current + 1;
    pageData.text = "Next";
    pagination.push(pageData);
  }

  return pagination;
};

/**
 * location.href 기준으로 parameter 값을 가져옴
 * @param name
 * @returns {string}
 */
Common.getParameter = function (name) {
  name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
  var regex = new RegExp("[\\?&]" + name + "=([^&#]*)");
  var results = regex.exec(location.search);
  return results === null
    ? ""
    : decodeURIComponent(results[1].replace(/\+/g, " "));
};

/**
 * Json Data를 queryString으로 변환함..
 * @param params
 * @returns {string}
 */
Common.param = function (params) {
  if (params == undefined) {
    return "";
  }

  // ES5
  var queryString = Object.keys(params)
    .map((key) => {
      return encodeURIComponent(key) + "=" + encodeURIComponent(params[key]);
    })
    .join("&");
  return queryString;
};

Common.parseUrl = function (url) {
  var parser = document.createElement("a");
  parser.href = url;

  // IE 8 and 9 dont load the attributes "protocol" and "host" in case the source URL
  // is just a pathname, that is, "/example" and not "http://domain.com/example".
  parser.href = parser.href;

  // IE 7 and 6 wont load "protocol" and "host" even with the above workaround,
  // so we take the protocol/host from window.location and place them manually
  if (parser.host === "") {
    var newProtocolAndHost =
      window.location.protocol + "//" + window.location.host;
    if (url.charAt(1) === "/") {
      parser.href = newProtocolAndHost + url;
    } else {
      // the regex gets everything up to the last "/"
      // /path/takesEverythingUpToAndIncludingTheLastForwardSlash/thisIsIgnored
      // "/" is inserted before because IE takes it of from pathname
      var currentFolder = ("/" + parser.pathname).match(/.*\//)[0];
      parser.href = newProtocolAndHost + currentFolder + url;
    }
  }

  // copies all the properties to this object
  var properties = [
    "host",
    "hostname",
    "hash",
    "href",
    "port",
    "protocol",
    "search",
  ];
  for (var i = 0, n = properties.length; i < n; i++) {
    this[properties[i]] = parser[properties[i]];
  }

  // pathname is special because IE takes the "/" of the starting of pathname
  this.pathname =
    (parser.pathname.charAt(0) !== "/" ? "/" : "") + parser.pathname;

  return this;
};
Common.$uri = Common.parseUrl(location.href);

Common.getPage = function (pageCode) {
  var page = {
    CODE: pageCode,
  };
  page.LIST_PAGE = "/admin/" + page.CODE;
  page.ID = Common.$uri.pathname
    .replace(page.LIST_PAGE + "/", "")
    .replace("/edit", "");
  page.IS_NEW = page.ID === "new";
  page.IS_EDIT = !page.IS_NEW;
  page.MODE_EDIT = "EDIT";
  page.MODE_NEW = "NEW";
  page.MODE = page.IS_NEW ? page.MODE_NEW : page.MODE_EDIT;

  page.API_BASE = "/api/" + page.CODE;
  page.API_POST = page.API_BASE;
  page.API_GET = page.API_BASE + "/" + page.ID;
  page.API_PUT = page.API_GET;
  page.API_DELETE = page.API_GET;

  return page;
};

Common.PAGE_URL = Object.freeze({
  COMPANY: `/common/dat-fam#COMPANY`,
  CATEGORY: `/common/dat-fam#CATEGORY`,
  MACHINE: `/common/dat-fam#MACHINE`,
  TOOLING: `/common/dat-fam#TOOLING`,
  PART: `/common/dat-fam#PART`,
  LOCATION: `/common/dat-fam#LOCATION`,
  TERMINAL: `/common/dvc#TERMINAL`,
  SENSOR: `/common/dvc#SENSOR`,
  DASHBOARD: `dashboard`,
  WORK_ORDER: `/admin/work-order`,
  TABBED_DASHBOARD: `tabbedDashboard`,
  USER: `/admin/users`,
  END_OF_LIFE_CYCLE: "/asset/tol-eol",
  ASSET_ALERT_RELOCATION: "/asset/alr#RELOCATION",
  ANALYSIS_QUALITY: "/analysis/quality",
  ANALYSIS_QUALITY_CYCLE_TIME_DEVIATION: "/analysis/quality#cyc-tim-dev",
  ANALYSIS_QUALITY_CYCLE_TIME_FLUCTUATION: "/analysis/quality#cyc-tim-flu",
  // Test master filter
  MACHINE_NEW: `/common/dat-fam#MACHINE_NEW`,
  SENSOR_NEW: `/common/dvc#SENSOR_NEW`,
  LOCATION_NEW: `/common/dat-fam#LOCATION_NEW`,
  PART_NEW: `/common/dat-fam#PART_NEW`,
  TOOLING_OLD: `/common/dat-fam#TOOLING_OLD`,
  // PRODUCTION ALERT
  MACHINE_DOWNTIME: `/production/alr#MACHINE_DOWNTIME`,
  CYCLE_TIME: `/production/alr#CYCLE_TIME`,
  UPTIME: `/production/alr#UPTIME`,
  // GENERAL ALERT
  DISCONNECTED: `/common/alr#DISCONNECTED`,
  DETACHMENT: `/common/alr#DETACHMENT`,
  RESET: `/common/alr#RESET`,
  DATA_APPROVAL: `/common/alr#DATA_APPROVAL`,
  // ASSET ALERT
  RELOCATION: `/asset/alr#RELOCATION`,
  REFURBISHMENT: `/asset/alr#REFURBISHMENT`,
});

Common.getApiBase = () => ({
  COMPANY: `/api/common/com-stp`,
  CATEGORY: `/api/common/cat-stp`,
  MACHINE: `/api/common/mch-stp`,
  MOLD: `/api/common/tol-stp`,
  PART: `/api/common/par-stp`,
  LOCATION: `/api/common/plt-stp`,
  TERMINAL: `/api/common/tmn-stp`,
  COUNTER: `/api/common/ssr-stp`,
  TAB: `/api/common/tab-stp`,
});

Common.handleNoResults = function (selector, dataCount) {
  var $template = $(selector);
  $template.find(".op-list").css("display", "table-row-group");
  $template.find(".pagination").css("display", "flex");

  var $noResult = $template.find(".no-results");
  if (dataCount > 0) {
    $noResult.addClass("d-none");
  } else {
    $noResult.removeClass("d-none");
  }
};

Common.formatNumber = function (number) {
  try {
    return number.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  } catch (e) {
    return number;
  }
};

Common.maxLength = function (obj, len) {
  if (obj.value.length > len) {
    obj.value = obj.value.slice(0, len);
  }
};

Common.vue = {
  getChild($children, componentName) {
    for (let child of $children) {
      if (child.$options._componentTag == componentName) {
        return child;
      }
    }
    return null;
  },
  getChildWithAttrVal($children, componentName, attr = null) {
    for (let child of $children) {
      if (
        child.$options._componentTag == componentName &&
        (!attr || child.$options.propsData[attr.key] === attr.value)
      ) {
        return child;
      }
    }
    return null;
  },
};

Common.checkMobile = function () {
  if (
    navigator.userAgent.match(/Android/i) ||
    navigator.userAgent.match(/webOS/i) ||
    navigator.userAgent.match(/iPhone/i) ||
    navigator.userAgent.match(/iPad/i) ||
    navigator.userAgent.match(/iPod/i) ||
    navigator.userAgent.match(/BlackBerry/i) ||
    navigator.userAgent.match(/Windows Phone/i)
  ) {
    return true;
  } else {
    return false;
  }
};

Common.getLiteListUser = function () {
  return axios
    .get("/api/system-note/user-lite-list")
    .then((response) => {
      response.data.dataList.sort((firstUser, secondUser) => {
        let firstName = firstUser.name.toUpperCase();
        let secondName = secondUser.name.toUpperCase();
        return firstName > secondName ? 1 : -1;
      });
      return new Promise((resolve) => resolve(response.data.dataList));
    })
    .catch((error) => {
      console.log("error getLiteListUser", error);
    });
};
Common.getOriginalChecklist = function (moldId) {
  return axios
    .get(`/api/checklist/checklist-mold?moldId=${moldId}`)
    .then((response) => {
      return new Promise((resolve) => resolve(response.data));
    })
    .catch((error) => {
      console.log("error getOriginalChecklist", error);
    });
};
(Common.exportPageToPdf = function (exportStatus, exportFileName) {
  console.log("exportPageToPdf");
  let pageContentItems = document.getElementsByClassName("page-content");
  let offsetHeight = 0;
  for (let i = 0; i < pageContentItems.length; i++) {
    if (offsetHeight < pageContentItems[i].offsetHeight) {
      offsetHeight = pageContentItems[i].offsetHeight;
    }
  }
  for (let i = 0; i < pageContentItems.length; i++) {
    pageContentItems[i].style.height = offsetHeight + "px";
  }
  let groupItem = $(".group-item")[0];
  let pdfWidth = 565;
  let pdfHeight = Math.round(
    (pdfWidth * groupItem.offsetHeight) / groupItem.offsetWidth
  );
  let totalPages = pageContentItems.length;
  let pdf = jsPDF({
    orientation: "landscape",
    unit: "px",
    format: [pdfWidth, pdfHeight],
  });
  let element = document.getElementById("export-content");
  domtoimage.toJpeg(element, { quality: 1.0 }).then((imgData) => {
    pdf.addImage(imgData, "JPEG", 0, 0, pdfWidth, element.offsetHeight);
    for (let i = 0; i < totalPages; i++) {
      pdf.addPage(pdfWidth, pdfHeight);
      pdf.addImage(
        imgData,
        "JPEG",
        0,
        -(pdfHeight * i),
        pdfWidth,
        pdfHeight * totalPages
      );
    }
    pdf.deletePage(1);
    exportStatus.isCallingDone = true;
    setTimeout(() => {
      if (!exportStatus.isStoppedExport) {
        pdf.save(exportFileName);
      }
    }, 2000);
  });
}),
  /** @deprecated */
  (Common.getCategoryConfigStatus = async () => {
    try {
      const options = await Common.getSystem("options");
      return JSON.parse(options);
    } catch (error) {
      console.log(error);
    }
  });

/** @deprecated */
Common.getCurrentOpConfig = function (equipmentType) {
  return axios
    .get("/api/config/op-config?equipmentType=" + (equipmentType || ""))
    .then((response) => new Promise((resolve) => resolve(response.data)))
    .catch((error) => {
      console.log("error", error);
    });
};

Common.getRandomElement = function (items) {
  return items[Math.floor(Math.random() * items.length)];
};

/**@deprecated */
Common.getCurrentAlertConfig = function () {
  return axios
    .get("/api/config/alert-config")
    .then((response) => new Promise((resolve) => resolve(response.data)))
    .catch((error) => {
      console.log("error", error);
    });
};

Common.getCurrentRefurbishmentConfig = function () {
  return axios
    .get("/api/config/refurbishment-config")
    .then((response) => new Promise((resolve) => resolve(response.data)))
    .catch((error) => {
      console.log("error", error);
    });
};

Common.triggerShowActionbarFeature = function (children) {
  let child = Common.vue.getChild(children, "action-bar-feature");
  if (child) {
    child.triggerFromOutSide();
  }
};

Common.getClientNameAndCompanyId = function () {
  const defaultData = [
    {
      domain: "https://ds0124.emoldino.com/",
      id: "ds0124",
      clientName: "Dyson",
    },
    {
      domain: "https://dyson.emoldino.com/",
      id: "ds0124",
      clientName: "Dyson",
    },
    {
      domain: "https://loreal.emoldino.com/",
      id: "lf0408",
      clientName: "L'Oreal",
    },
    { domain: "https://ag0513.emoldino.com/", id: "ag0513", clientName: "ABB" },
    {
      domain: "https://sg0527.emoldino.com/",
      id: "sg0527",
      clientName: "Schaeffler",
    },
    { domain: "https://ps0611.emoldino.com/", id: "ps0611", clientName: "P&G" },
    {
      domain: "https://fc0616.emoldino.com",
      id: "fc0616",
      clientName: "Denso",
    },
    { domain: "http://47.98.59.87/", id: "abb-china", clientName: "ABB" },
    {
      domain: "https://bg0824.emoldino.com/",
      id: "bg0824",
      clientName: "Beiersdorf",
    },
    {
      domain: "https://su0901.emoldino.com/",
      id: "su0901",
      clientName: "Stanley Black & Decker",
    },
    { domain: "https://dev.emoldino.com/", id: "dev", clientName: "dev" },
    { domain: "https://feature.emoldino.com/", id: "test", clientName: "test" },
    {
      domain: "http://feature.emoldino.com:8085/",
      id: "test8085",
      icon: "test 8085",
    },
    {
      domain: "https://di0402.emoldino.com/",
      id: "di0402",
      clientName: "De'Longhi",
    },
    {
      domain: "https://ns0407.emoldino.com/",
      id: "ns0407",
      clientName: "Nestle",
    },
    {
      domain: "https://lf0408.emoldino.com/",
      id: "lf0408",
      clientName: "L'Oreal",
    },
    {
      domain: "https://cb0413.emoldino.com/",
      id: "cb0413",
      clientName: "Continental",
    },
    {
      domain: "https://ml0421.emoldino.com/",
      id: "ml0421",
      clientName: "Megatech",
    },
    {
      domain: "https://mm0427.emoldino.com/",
      id: "mm0427",
      clientName: "Mabe",
    },
    {
      domain: "https://vf0616.emoldino.com/",
      id: "vf0616",
      clientName: "Volvo",
    },
    {
      domain: "https://bb0703.emoldino.com/",
      id: "bb0703",
      clientName: "Boticario",
    },
    {
      domain: "https://eb0702.emoldino.com/",
      id: "eb0702",
      clientName: "Electrolux",
    },
    {
      domain: "https://ep1111.emoldino.com/",
      id: "eaton",
      clientName: "Eaton",
    },
    {
      domain: "http://cc0429.emoldino.com/",
      id: "cc0429",
      clientName: "Cepheid",
    },
    {
      domain: "http://su0901.emoldino.com/",
      id: "stanley",
      clientName: "Stanley BD",
    },
    {
      domain: "http://en0414.emoldino.com/",
      id: "en-usa",
      clientName: "Electrolux",
    },
    { domain: "https://ni0331.emoldino.com/", id: "nice", clientName: "Nice" },
    {
      domain: "https://ag0422.emoldino.com/",
      id: "ag0422",
      clientName: "Adient",
    },
    { domain: "https://demo.emoldino.com/", id: "demo", clientName: "demo" },
    { domain: "https://zebra.emoldino.com/", id: "zebra", clientName: "Zebra" },
    {
      domain: "https://tu0824.emoldino.com/",
      id: "tu0824",
      clientName: "Tesla",
    },
    { domain: "https://icee.emoldino.com/", id: "icee", clientName: "Icee" },
    {
      domain: "https://ju0316.emoldino.com/",
      id: "ju0316",
      clientName: "Jaguar",
    },
    {
      domain: "https://deep-dev.emoldino.com/",
      id: "deep-dev",
      clientName: "deep-dev",
    },
    {
      domain: "https://dn0525.emoldino.com/",
      id: "dn0525",
      clientName: "Daikin",
    },
    {
      domain: "https://pn0622.emoldino.com/",
      id: "pn0622",
      clientName: "Philips",
    },
    {
      domain: "https://di0711.emoldino.com/",
      id: "di0711",
      clientName: "Denso",
    },
    { domain: "localhost", id: "localhost-id", clientName: "localhost" },
    { domain: "abb.emoldino.com", id: "abb", clientName: "ABB" },
    {
      domain: "schaeffler.emoldino.com",
      id: "schaeffler",
      clientName: "Schaeffler ",
    },
    { domain: "pg.emoldino.com", id: "pg", clientName: "P&G " },
    { domain: "denso.emoldino.com", id: "denso", clientName: "Denso" },
    {
      domain: "beiersdorf.emoldino.com",
      id: "beiersdorf",
      clientName: "Beiersdorf",
    },
    { domain: "sbd.emoldino.com", id: "sbd", clientName: "Stanley BD" },
    { domain: "aam.emoldino.com", id: "aam", clientName: "AAM" },
    { domain: "tesla.emoldino.com", id: "tesla", clientName: "Tesla" },
    { domain: "mabe.emoldino.com", id: "mabe", clientName: "Mabe" },
    { domain: "eaton.emoldino.com", id: "eaton", clientName: "Eaton " },
    {
      domain: "electroluxus.emoldino.com",
      id: "electroluxus",
      clientName: "Electrolux",
    },
    {
      domain: "continental.emoldino.com",
      id: "continental",
      clientName: "Continental",
    },
    { domain: "paccar.emoldino.com", id: "paccar", clientName: "Paccar" },
    {
      domain: "delonghi.emoldino.com",
      id: "delonghi",
      clientName: "De'Longhi",
    },
    { domain: "nestle.emoldino.com", id: "nestle", clientName: "Nestle" },
    { domain: "nice.emoldino.com", id: "nice", clientName: "Nice" },
    { domain: "icee.emoldino.com", id: "icee", clientName: "Icee" },
    {
      domain: "renaulttr.emoldino.com",
      id: "renaulttr",
      clientName: "Renault",
    },
    { domain: "adient.emoldino.com", id: "adient", clientName: "Adient" },
    { domain: "loreal.emoldino.com", id: "loreal", clientName: "L'Oreal" },
    { domain: "volvo.emoldino.com", id: "volvo", clientName: "Volvo" },
    {
      domain: "electroluxsa.emoldino.com",
      id: "electroluxsa",
      clientName: "Electrolux",
    },
    {
      domain: "boticario.emoldino.com",
      id: "boticario",
      clientName: "Boticario",
    },
    { domain: "jabil.emoldino.com", id: "jabil", clientName: "Jabil" },
    { domain: "daikin.emoldino.com", id: "daikin", clientName: "Daikin" },
    {
      domain: "baylis.emoldino.com",
      id: "baylis",
      clientName: "Baylis Medical",
    },
    { domain: "cepheid.emoldino.com", id: "cepheid", clientName: "Cepheid" },
    { domain: "jaguar.emoldino.com", id: "jaguar", clientName: "Jaguar" },
    { domain: "philips.emoldino.com", id: "philips", clientName: "Philips" },
    { domain: "ford.emoldino.com", id: "ford", clientName: "Ford" },
    { domain: "densoin.emoldino.com", id: "densoin", clientName: "Denso" },
    { domain: "irobot.emoldino.com", id: "irobot", clientName: "iRobot" },
    { domain: "misumi.emoldino.com", id: "misumi", clientName: "Misumi" },
  ];
  return defaultData.find((o) => o.domain.includes(window.location.hostname));
};

function getCurrentConfig() {
  console.log("Start getCurrentConfig");
  axios
    .get("/api/config/time-language")
    .then((response) => {
      updateCommonConfig(response);
      console.log("Done getCurrentConfig");
    })
    .catch(function (error) {
      console.log(error.response);
      //show notification for role for login
      if (
        error &&
        error.response &&
        error.response.status == 403 &&
        location.pathname &&
        !location.pathname.startsWith("/login")
      ) {
        Common.alertCallback = function () {
          $("#op-logout").click();
        };
        Common.alertCallbackClose = function () {
          $("#op-logout").click();
        };
        Common.alert("The account does not have access to the system.");
      }
    });
}

Common.setHeightActionBar = function () {
  $("#action-bar").height($("#thead-actionbar").height());
};
function updateCommonConfig(response) {
  console.log("updateCommonConfig: ", response);

  if (response.data) {
    Common.localTimeZone = response.data.localTimeZone;
    Common.timeZoneSever = response.data.timeZoneSever;
    if (response.data.language) {
      let existed = this.languageOptions.filter(
        (item) => item.value === response.data.language
      );
      if (existed && existed.length > 0) {
        Common.language = response.data.language;
      }
    }
  }
}

Common.camelToSnake = function (str) {
  return str.replace(/[A-Z]/g, (c) => {
    return "_" + c.toLowerCase();
  });
};
String.prototype.capitalize = function () {
  return this.charAt(0).toUpperCase() + this.slice(1);
};

String.prototype.capitalizeAll = function () {
  return this.split(" ")
    .map((w) => w.capitalize())
    .join(" ");
};

Common.getCustomFieldConfig = function (objectType) {
  return axios
    .get("/api/custom-field?objectType=" + objectType)
    .then((response) => {
      let headerCustom = [];
      if (response.data && response.data.length > 0) {
        headerCustom = response.data.map((d) => {
          let h = {
            isCustomField: true,
            name: d.fieldName,
            code: d.id,
            rules: {
              required: d.required,
              isNumber: d.fieldType == "NUMBER",
            },
            default: d.defaultInput ? d.defaultInputValue : null,
          };
          return h;
        });
      }
      return new Promise((resolve) => resolve(headerCustom));
    });
};

Common.getCustomColumn = function (objectType, midOject) {
  return axios
    .get("/api/custom-field?objectType=" + objectType)
    .then((response) => {
      let customColumnList = [];
      let perfixObject = "";
      if (midOject && midOject.trim() != "") {
        perfixObject = midOject + ".";
      }
      if (response.data && response.data.length > 0) {
        customColumnList = response.data.map((item) => {
          let col = {
            label: item.fieldName,
            field: perfixObject + item.id,
            isCustomField: true,
            sortable: true,
            sortField: perfixObject + "customField-" + item.id,
          };
          return col;
        });
      }
      return new Promise((resolve) => resolve(customColumnList));
    })
    .catch((error) => {
      console.log("error", error);
    });
};
Common.eqTextKey = function (s1, s2) {
  return String(s1).trim().toLowerCase() == String(s2).trim().toLowerCase();
};

Common.changeDeletedColumn = function (
  vm,
  configCategory,
  allColumnList,
  preFixField,
  configDeleteField
) {
  if (allColumnList != null && allColumnList.length > 0)
    Common.getCategoryConfigStatus().then((data) => {
      if (data && data.length > 0) {
        let currentConfig = data.filter(
          (item) => item.configCategory === configCategory
        )[0];
        if (currentConfig && currentConfig.enabled) {
          // get current config value
          axios
            .get("/api/config?configCategory=" + configCategory)
            .then((response) => {
              let perfixObject = "";
              if (preFixField && preFixField.trim() != "") {
                perfixObject = preFixField + ".";
              }
              if (response.data && response.data.length > 0) {
                console.log("form response data", response.data);
                response.data.forEach((field) => {
                  if (field.deletedField) {
                    allColumnList.forEach((col) => {
                      if (
                        getFieldNameConfig(col.field, configCategory) ==
                        perfixObject + field.fieldName
                      ) {
                        col.hiddenInToggle = field.deletedField;
                      }
                    });
                  }
                  if (configDeleteField != null)
                    configDeleteField[field.fieldName] = field.deletedField;
                });
              }
              vm.$forceUpdate();

              return new Promise((resolve) => resolve(response.data));
            })
            .catch((error) => {
              console.log(error.response);
            });
        }
      }
    });
};

function getFieldNameConfig(field, objectType) {
  if (objectType == "PART") {
    switch (field) {
      // case 'partCode':
      // case 'name':
      // case 'category':
      // case 'enabled':
      // case 'projectName':
      // case 'totalMolds':
      // case 'activeMolds':
      // case 'idleMolds':
      // case 'inactiveMolds':
      // case 'disconnectedMolds':
      // case 'totalProduced':
      case "designRevision":
        return "designRevision";
      // case 'resinCode':
      case "resinGrade":
        return field;
      case "partSize":
        return "size";
      case "partWeight":
        return "weight";
    }
    return field;
  }

  switch (field) {
    // case 'toolingId':
    // case 'part':
    // case 'company':
    // case 'location':
    // case 'lastShot':
    // case 'cycleTime':
    // case 'op':
    // case 'equipmentStatus':
    // case 'cycleTimeToleranceL1':
    // case 'cycleTimeToleranceL2':
    // case 'uptimeTarget':
    case "engineerInCharge":
      return "engineers";
    case "weightOfRunnerSystem":
      return "weightRunner";
    case "contractedCycleTimeSeconds":
      return "approvedCycleTime";
    // case 'counterCode':
    // case 'designedShot':
    case "hotRunnerDrop":
      return "hotRunnerDrop";
    case "hotRunnerZone":
      return "hotRunnerZone";
    case "injectionMachineId":
      return "injectionMachineId";
    case "labour":
      return "labour";
    // case 'lastShotDate':
    case "quotedMachineTonnage":
    // case 'preventCycle':
    case "runnerMaker":
      return "runnerMaker";
    case "maxCapacityPerWeek":
      return "maxCapacityPerWeek";
    // case 'preventOverdue':
    case "shiftsPerDay":
      return "shiftsPerDay";
    case "productionDays":
      return "productionDays";
    case "shotSize":
      return "shotSize";
    // case 'supplierCompanyName':
    case "toolDescription":
      return "toolDescription";
    case "toolingSizeView":
      return "size";
    case "toolingWeightView":
      return "weight";
    case "toolingComplexity":
      return "toolingComplexity";
    case "toolingLetter":
      return "toolingLetter";
    case "toolingType":
      return "toolingType";
    case "toolMakerCompanyName":
      return "toolMakerCompanyName";
    case "runnerTypeTitle":
      return "runnerType";
    case "preventUpcoming":
      return "preventUpcoming";
    // case 'uptimeLimitL1':
    // case 'uptimeLimitL2':
    // case 'utilizationRate':
    case "madeYear":
      return "madeYear";
    // case 'mold.hotRunnerDrop':
    // case 'mold.hotRunnerZone':
    // case 'mold.quotedMachineTonnage':
    // case 'mold.preventCycle':
    // case 'mold.runnerMaker':
    // case 'mold.maxCapacityPerWeek':
    // case 'mold.preventOverdue':
    // case 'mold.productionDays':
    // case 'mold.shiftsPerDay':
    // case 'mold.shotSize':
    // case 'mold.supplierCompanyName':
    // case 'mold.toolDescription':
    case "mold.toolingSizeView":
      return "size";
    case "mold.toolingWeightView":
      return "weight";
    // case 'mold.toolingComplexity':
    // case 'mold.toolingLetter':
    // case 'mold.toolingType':
    // case 'mold.toolMakerCompanyName':
    case "mold.runnerTypeTitle":
      return "runnerType";
    // case 'mold.preventUpcoming':
    // case 'mold.uptimeTarget':
    // case 'mold.uptimeLimitL1':
    // case 'mold.uptimeLimitL2':
    // case 'mold.utilizationRate':
    case "weightOfRunnerSystem":
      return "weightRunner";
    // case 'mold.madeYear':
  }

  return field;
}

/** @deprecated */
Common.configCategoryList = null;
Common.checkCategoryConfigStatus = async function (configCategory) {
  let listConfig = [];
  if (Common.configCategoryList == null) {
    let res = await axios.get("/api/config/config-enable");
    if (res.data && res.data.length > 0) {
      listConfig = res.data;
    }
    Common.configCategoryList = listConfig;
  }

  const currentConfig = Common.configCategoryList[configCategory];

  return currentConfig && currentConfig.enabled;
};

/** @deprecated */
Common.getConfigDelete = async function (configCategory) {
  let deleteField = {};
  try {
    if (Common.checkCategoryConfigStatus(configCategory)) {
      let response = await axios.get(
        "/api/config?configCategory=" + configCategory
      );
      if (response.data && response.data.length > 0) {
        console.log("form response data", response.data);
        response.data.forEach((field) => {
          deleteField[field.fieldName] = field.deletedField;
        });
      }
    }
  } catch (e) {
    console.log(e);
  }
  return deleteField;
};
Common.clickFromPin = false;
Common.changeClickFromPin = function (boolean) {
  Common.clickFromPin = boolean;
};
Common.getClickFromPin = function () {
  return Common.clickFromPin;
};

Common.triggerShowNewFeatureNotify = function (
  children,
  component,
  data = null,
  attr = null
) {
  let child = Common.vue.getChildWithAttrVal(children, component, attr);
  if (child) {
    child.triggerFromOutSide(data);
  }
};

Common.getAvatarName = function (name) {
  if (name) {
    let nameParts = name.split(/\s+/);
    // console.log(nameParts)
    let convertName = "";
    if (nameParts) {
      if (nameParts.length == 1) {
        convertName = nameParts[0].charAt(0).toUpperCase();
      } else {
        convertName = `${nameParts[0].charAt(0).toUpperCase()}${nameParts[
          nameParts.length - 1
        ]
          .charAt(0)
          .toUpperCase()}`;
      }
    }
    return convertName;
  }
  return "";
};
Common.getRequestedColor = function (name, id) {
  if (name) {
    let colorList = [
      "#AFACFF",
      "#FF7171",
      "#DB91FC",
      "#FFACAC",
      "#FFB371",
      "#FFACF2",
      "#FFACE5",
      "#86C7FF",
      "#62B0D9",
      "#ACBFFF",
      "#FF8699",
      "#90E5AA",
      "#CCACFF",
      "#74AEFC",
      "#E0CD7E",
      "#FF8489",
      "#FFA167",
      "#64D0C3",
      "#86B0FF",
      "#D2F8E2",
      "#C69E9E",
      "#FFC68D",
      "#F8D2D2",
      "#E58383",
    ];
    let f = Common.getAvatarName(name);
    let index = 0;
    if (f.length > 0) {
      index = f.charCodeAt(0);
      if (index > 65) index -= 65;
      if (f.length > 1) {
        index += f.charCodeAt(1);
        if (index > 65) index -= 65;
      }
    }
    if (id) {
      index += id;
    }
    index = index % colorList.length;

    return colorList[index];
  }
  return "#FFFFFF";
};

Common.parseParams = (querystring) => {
  // parse query string
  const params = new URLSearchParams(querystring);

  const obj = {};

  // iterate over all keys
  for (const key of params.keys()) {
    if (params.getAll(key).length > 1) {
      obj[key] = params.getAll(key);
    } else {
      obj[key] = params.get(key);
    }
  }

  return obj;
};
Common.removeWave = function (time) {
  const classList = [
    "wave",
    "wave1",
    "wave_sidebar",
    "wave_location",
    "wave_save",
    "wave_header",
    "wave_img",
    "hide_account",
    "profile_wave",
    "wave_mold1",
    "wave_mold2",
    "wave_save",
  ];
  const classes = classList.join(" ");
  setTimeout(() => {
    $("li").removeClass(classes);
    $("div").removeClass(classes);
    $("img").removeClass(classes);
    if (document.getElementById("removee"))
      document.getElementById("removee").remove();
    const waveHeaderEl = document.getElementById("wave-header");
    const waveLogin = document.getElementById("wave-register");
    if (waveHeaderEl) waveHeaderEl.remove();
    if (waveLogin) waveLogin.remove();
  }, time || 500);
};

Common.URL = {
  PART: `/admin/parts`,
  MOLD: `/admin/molds`,
  ALERT_CENTER: `/front/alert-center`,
  WORK_ORDER: `/admin/work-order`,
};
Common.getNumMillisecondsToTime = (time) => {
  return time && time > moment().valueOf() ? time - moment().valueOf() : 0;
};

Common.octs = {
  ACT: "Approved CT",
  WACT: "Weighted Average CT",
};
Common.MAX_VIEW_POINT = 31;
Common.Const = {
  StorageType: {
    PART_PICTURE: "PART_PICTURE",
    PROJECT_IMAGE: "PROJECT_IMAGE",
  },
};

// Utils
Common.formatter = {
  appendThousandSeperator: (val, separator = ",") => {
    return val?.toString()?.replace(/\B(?=(\d{3})+(?!\d))/g, separator) || "";
  },
  /** @deprecated change it to numberOnly func */
  removeSeperator: (val, seperator = ",") => {
    const re = new RegExp(`${seperator}`);
    if (val) {
      const newText = val.toString();
      return newText.replace(re, "");
    }
    return "";
  },
  /** @deprecated pls use directly toFixed util */
  formatToDecimal: (val, digitNumber = 0) => {
    return Number(val).toFixed(digitNumber);
  },
  /** @deprecated function too specified for global formatter */
  appendPercent: (val, appendChar = "%") => {
    if (!isNaN(val)) {
      return val.toFixed(1).toString() + appendChar;
    }
    return "-";
  },
  numberOnly: (val) => {
    return val?.toString()?.replace(/\D/g, "") || "";
  },
  /** @description shorten by symbol such as 1.000.000 -> 1M */
  numberShorten: (val, decimal = 0) => {
    let number;
    if (Math.abs(val) >= 1.0e9) {
      number = (Math.abs(val) / 1.0e9).toFixed(decimal) + "B";
    } else if (Math.abs(val) >= 1.0e6) {
      number = (Math.abs(val) / 1.0e6).toFixed(decimal) + "M";
    } else if (Math.abs(val) >= 1.0e3) {
      number = (Math.abs(val) / 1.0e3).toFixed(decimal) + "K";
    } else {
      number = Math.abs(val).toString();
    }
    return number;
  },
  /** @deprecated String Object already */
  toCase: (val, formatCase) => {
    if (val) {
      if (formatCase === "capitalize") {
        const fixRaw = val.replaceAll("_", " ");
        const str = fixRaw.split(" ");
        const newStr = str.map(
          (item) => item.charAt(0).toUpperCase() + item.slice(1).toLowerCase()
        );
        return newStr.join(" ");
      }
      return val;
    }
    return "";
  },
};
Common.listener = {
  onReachEnd: (el) => {
    return el.offsetHeight + el.scrollTop >= el.scrollHeight;
  },
};

Common.needToChangeDate = function (chartType, dateViewType) {
  if (chartType == "UPTIME" && dateViewType == "HOURLY") {
    return true;
  }
  if (
    dateViewType != "HOURLY" &&
    (chartType === "TEMPERATURE_ANALYSIS" ||
      chartType === "CYCLE_TIME_ANALYSIS")
  ) {
    return true;
  }
  return false;
};

Common.dateFns = {
  months: [
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
  ],
  dateFormat: "YYYY-MM-DD",
};

Common.getCfgStp = async function (configCategory) {
  try {
    let response = await axios.get(
      "/api/common/cfg-stp?configCategory=" + configCategory
    );
    if (response.data.options) {
      return response.data.options[configCategory];
    }
    return null;
  } catch (e) {
    console.log(e);
  }
};
Common.onChangeHref = function (href) {
  setTimeout(() => {
    window.location.href = href;
  }, 700);
};

Common.addNoCacheLink = function (srcLink) {
  try {
    console.log("addNoCacheLink");
    var uri = URI.parse(srcLink);
    let params = Common.parseParams(uri.query);
    let ver = $("#noCacheVerId").attr("value")
      ? $("#noCacheVerId").attr("value")
      : "v=" + moment().format("YYYYMMDDHHmm");
    if (Object.keys(params).length == 0 && !srcLink.endsWith("?")) {
      srcLink += "?";
    }
    if (!Object.keys(params).includes("v")) {
      if (srcLink.endsWith("?")) {
        srcLink += ver;
      } else {
        srcLink += "&" + ver;
      }
    }
    return srcLink;
  } catch (e) {
    console.log(e);
    return srcLink;
  }
};
Common.getNoCacheVer = function () {
  let ver = $("#noCacheVerId").attr("value")
    ? $("#noCacheVerId").attr("value")
    : "v=" + moment().format("YYYYMMDDHHmm");
  return ver;
};

Common.anchor = (containerEl, targetEl, delay) => {
  if (window.location.hash) {
    console.log("scroll to " + window.location.hash);
    setTimeout(() => {
      containerEl.animate({ scrollTop: targetEl.offset().top + 100 }, 1000);
    }, delay || 2000);
  }
};

Common.getCurrentWeekNumber = (date) => {
  let oneJan = new Date(date.getFullYear(), 0, 1);
  let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));

  if (oneJan.getDay() < 4) {
    numberOfDays -= oneJan.getDay();
  } else {
    if (numberOfDays < 7 - oneJan.getDay()) {
      oneJan = new Date(date.getFullYear() - 1, 0, 1);
      numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
    } else {
      numberOfDays += oneJan.getDay();
    }
  }
  let result = Math.floor((numberOfDays + 1) / 7);
  return result;
};

Common.getMonthTitle = (month, type) => {
  const MONTHS = [
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December",
  ];
  const MONTHS_SHORT = [
    "Jan",
    "Feb",
    "Mar",
    "Apr",
    "May",
    "Jun",
    "Jul",
    "Aug",
    "Sep",
    "Oct",
    "Nov",
    "Dec",
  ];
  if (type == "short") {
    return MONTHS_SHORT[+month - 1];
  }
  return MONTHS[+month - 1];
};

Common.getSystem = async (key) => {
  if (
    !key ||
    !window.sessionStorage.getItem(key) ||
    Common.getKeyData(key) === "undefined" ||
    Common.serverRestarted()
  ) {
    try {
      if (Common.getKeyData(key) === "undefined")
        console.error(Common.getKeyData());
      const response = await axios.get("/api/common/man-pag");
      Common.setKeyData(response.data);
      return Common.getKeyData(key);
    } catch (error) {
      console.log(error);
    }
  }
  return Common.getKeyData(key);
};

Common.serverRestarted = function () {
  let ver = $("#noCacheVerId").attr("value");
  let oldVer = window.localStorage.getItem("noCacheVerId");
  if (oldVer !== ver) {
    console.log("oldVer", oldVer, "ver", ver);
    window.localStorage.setItem("noCacheVerId", ver);
    return true;
  }
  return false;
};

Common.getKeyData = (key) => {
  const data = {
    server: window.sessionStorage.getItem("server"),
    type: window.sessionStorage.getItem("type"),
    localTimeZone: window.sessionStorage.getItem("localTimeZone"),
    language: window.sessionStorage.getItem("language"),
    me: window.sessionStorage.getItem("me"),
    options: window.sessionStorage.getItem("options"),
    messages: window.sessionStorage.getItem("messages"),
    versions: window.sessionStorage.getItem("versions"),
    menuPermissions: window.sessionStorage.getItem("menuPermissions"),
  };
  if (!key) return data;
  return data[key];
};

Common.setKeyData = (data) => {
  window.sessionStorage.setItem("server", data.server);
  window.sessionStorage.setItem("type", data.type);
  window.sessionStorage.setItem("localTimeZone", data.localTimeZone);
  window.sessionStorage.setItem("language", data.language);
  window.sessionStorage.setItem("me", JSON.stringify(data.me));
  window.sessionStorage.setItem("options", JSON.stringify(data.options));
  window.sessionStorage.setItem("messages", JSON.stringify(data.messages));
  window.sessionStorage.setItem("versions", JSON.stringify(data.versions));
  window.sessionStorage.setItem(
    "menuPermissions",
    JSON.stringify(data.menuPermissions)
  );
};

Common.logout = async () => {
  window.sessionStorage.clear();
  window.localStorage.clear();
  try {
    await axios.post("/logout");
    window.location.href = "/login";
  } catch (error) {
    console.log(error);
  }
};

Common.isPrimitive = (val) => {
  return Object(val) !== val;
};
Common.convertDateToTimestamp = (date) => {
  const parseDate = Date.parse(date);
  return parseDate / 1000;
};

Common.convertTimestampToDate = (timestamp) => {
  return new Date(timestamp * 1000);
};

Common.download = async (fileUrl, fileName) => {
  const anchor = document.createElement("a");
  anchor.href = fileUrl;
  anchor.setAttribute("download", fileName);
  anchor.click();
};

Common.downloadMultipleFile = async (files) => {
  for (let i = 0; i < files.length; i++) {
    setTimeout(() => {
      let file = files[i];
      fetch(file.saveLocation)
        .then((resp) => resp.blob())
        .then((blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement("a");
          a.style.display = "none";
          a.href = url;
          // the filename you want
          a.download = file.fileName;
          document.body.appendChild(a);
          a.click();
          window.URL.revokeObjectURL(url);
        })
        .catch((e) => console.error(e));
    }, i * 100);
  }
};

Common.downloadDocument = (files) => {
  files.forEach((f) => {
    let fileURL = "";
    if (!f.saveLocation) {
      fileURL = URL.createObjectURL(f);
    } else {
      fileURL = f.saveLocation;
    }
    console.log("fileURL", fileURL);
    Common.download(fileURL, f.name);
  });
};

Common.getWeekStartEndDates = (year, weekNumber) => {
  // Determine the day to start counting the first week
  let startDay = 1; // By default, assume the first week starts on January 1st

  const january1st = new Date(year, 0, 1).getDay();
  if (january1st >= 4) {
    // If January 1st is on a Wednesday, Thursday, Friday, or Saturday,
    // the first week starts on the previous year
    startDay = -((7 - january1st) % 7) + 2;
  }

  // Calculate the start and end dates of the specified week
  const firstDayOfYear = new Date(year, 0, startDay);
  const firstDayOfWeek = new Date(
    firstDayOfYear.getTime() + (weekNumber - 1) * 7 * 86400000
  );
  const lastDayOfWeek = new Date(firstDayOfWeek.getTime() + 6 * 86400000);

  return { start: firstDayOfWeek, end: lastDayOfWeek };
};

Common.downloadFile = async (url, fileName) => {
  const response = await fetch(url);
  const blob = await response.blob();

  const a = document.createElement("a");
  a.href = URL.createObjectURL(blob);
  a.download = fileName || new Date().getTime();
  a.style.display = "none";
  document.body.appendChild(a);

  a.click();

  document.body.removeChild(a);
  URL.revokeObjectURL(a.href);
};
