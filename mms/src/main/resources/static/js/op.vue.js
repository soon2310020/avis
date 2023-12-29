// Vue 전역 Mixin
var Const = {
  defaultCountry: "KR",
  defaultDialingCode: "82",
};

// Note: using in case expose to template in type module script
Vue.prototype.moment = window.moment;
Vue.prototype.$formatter = window.Common.formatter;

Vue.mixin({
  data() {
    return {
      CommonConst: Common.Const,
    };
  },
  methods: {
    replaceLongtext: function (text, size) {
      text = text && text.trim();
      if (text && text.length > size) {
        text = text.slice(0, size) + "...";
      }
      return text;
    },

    formatNumber: function (number) {
      return Common.formatNumber(number);
    },

    partName: function (part) {
      if (part.enabled) {
        return part.name;
      } else {
        return '<span class="disabled">' + part.name + "</span>";
      }
    },

    companyName: function (companyName, enabled) {
      if (enabled) {
        return companyName;
      } else {
        return '<span class="disabled">' + companyName + "</span>";
      }
    },

    locationName: function (locationName, enabled) {
      if (enabled) {
        return locationName;
      } else {
        return '<span class="disabled">' + locationName + "</span>";
      }
    },

    categoryName: function (category) {
      if (category.enabled) {
        return category.name;
      } else {
        return '<span class="disabled">' + category.name + "</span>";
      }
    },

    alertUnchangeableStatus: function () {
      alert("You cannot change the status.");
    },

    condition: function (mold, status) {
      if (mold.equipmentStatus == "DISCARDED" && status == "discarded") {
        return false;
        //return true;
      } else if (mold.equipmentStatus == "FAILURE" && status == "failure") {
        return false;
        //return true;
      } else {
        if (mold.toolingCondition == "GOOD" && status == "good") {
          return true;
        }

        if (mold.toolingCondition == "FAIR" && status == "fair") {
          return true;
        }

        if (mold.toolingCondition == "POOR" && status == "poor") {
          return true;
        }
      }
      return false;
    },

    multipartHeader: function () {
      return {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      };
    },

    lower: function (code) {
      return code == undefined ? code : code.toLowerCase();
    },

    initNotifyAlert: function (self, alertType) {
      console.log("before: " + new Date());
      setTimeout(function () {
        console.log("after: " + new Date());
        var component = Common.vue.getChild(self.$children, "notify-alert");
        if (component != null) {
          component.init(alertType);
        } else {
          console.log("component is null: " + alertType);
        }
      }, 1000);
    },
    displayYears: function () {
      let now = new Date();
      let currentYear = now.getFullYear();

      let years = [];
      for (let i = currentYear; i > currentYear - 3; i--) {
        years.push(i);
      }
      return years;
    },
    convertTimestampToDateWithFormat: function (value, format) {
      // console.log("convertTimestampToDateWithFormat value: ",value);
      // console.log("convertTimestampToDateWithFormat old: ",(value ? moment.unix(value).format(format): ''));
      let display = "";
      if (!Common.localTimeZone && Common.timeZoneSever != null) {
        display = value
          ? moment
              .unix(
                value +
                  (Common.timeZoneSever / 1000 +
                    new Date().getTimezoneOffset() * 60)
              )
              .format(format)
          : "";
      } else {
        display = value ? moment.unix(value).format(format) : "";
      }
      // console.log("convertTimestampToDateWithFormat new : ",display);

      return display;
    },
    formatToDateTime: function (value) {
      return this.convertTimestampToDateWithFormat(
        value,
        "YYYY-MM-DD HH:mm:ss"
      );
    },
    formatToDateTimeMinute: function (value) {
      return this.convertTimestampToDateWithFormat(value, "YYYY-MM-DD HH:mm");
    },
    formatToDate: function (value) {
      return this.convertTimestampToDateWithFormat(value, "YYYY-MM-DD");
    },
    firstFewLettersWithMaxlength: function (text, maxlength) {
      if (text != null && text.length > maxlength) {
        let newText = text.substr(0, maxlength + 1);
        let newTextArr = newText.split(" ");
        if (newTextArr.length == 1) return text.split(" ")[0];

        return newTextArr.slice(0, newTextArr.length - 1).join(" ");
      }
      return text;
    },
    valueWithKeyMultiPath(object, key, isCustomField) {
      try {
        if (object != null && key != null) {
          let arrKey = key.split(".");
          if (isCustomField && arrKey == 1) {
            return this.customFieldValue(object, key);
          }
          let val = object[arrKey[0]];
          let level = 1;
          while (level < arrKey.length) {
            if (isCustomField && level >= arrKey.length - 1) {
              return this.customFieldValue(val, arrKey[level]);
            }
            val = val[arrKey[level]];
            level++;
          }
          return val;
        }
        return object;
      } catch (e) {
        console.log(e);
      }
      return null;
    },
    customFieldValue(object, customFieldId) {
      try {
        if (object != null && customFieldId != null) {
          if (
            !isNaN(customFieldId) &&
            object.customFieldValueMap &&
            object.customFieldValueMap[Number(customFieldId)] &&
            object.customFieldValueMap[Number(customFieldId)].length > 0
          ) {
            return object.customFieldValueMap[Number(customFieldId)][0].value;
          }
        }
      } catch (e) {
        console.log(e);
      }
      return null;
    },
    eqTextKey(s1, s2) {
      return String(s1).trim().toLowerCase() == String(s2).trim().toLowerCase();
    },
    getRootId(noSpace) {
      let sp = noSpace ? "" : " ";
      return this.$root.$el.id ? "#" + this.$root.$el.id + sp : "";
    },
    getRootToCurrentId(noSpace) {
      return this.getRootToScopeId(this, noSpace);
    },
    getRootToScopeId(scope, noSpace) {
      let sp = noSpace ? "" : " ";
      let pathId = "";
      while (scope) {
        pathId =
          (scope.$el && scope.$el.id ? "#" + scope.$el.id + sp : "") + pathId;
        scope = scope.$parent;
      }
      console.log("getRootToScopeId", pathId);
      return pathId;
    },
    getRootToParentId(noSpace) {
      return this.getRootToScopeId(this.$parent, noSpace);
    },
    appendThousandSeperator(val, separator = ",") {
      return Common.formatter.appendThousandSeperator(val, separator);
    },

    setToastAlertGlobal({ title, content, show, status }) {
      // refer to script.html
      if (show) headerVm.showToast(title, content, status);
      else headerVm.handleCloseNotification();
    },
    shorten(val, charNumber = 40) {
      if (typeof val === "string" && val.length < charNumber) return val;
      if (typeof val === "string" && val.length >= charNumber)
        return val.slice(0, charNumber) + "...";
      return;
    },

    capitalize(val) {
      if (val) {
        const txt = val.toString();
        return txt.charAt(0).toUpperCase() + txt.slice(1);
      }
    },
    capitalizeAll(val) {
      if (typeof val === "string")
        return val
          .split(" ")
          .map((w) => w.capitalize())
          .join(" ");
    },
  },
});

Vue.directive("click-outside", {
  bind(el, binding, vnode) {
    el.clickOutsideEvent = function (event) {
      const isDisplayed = el.offsetParent; // prevent func invoke before mounted
      const isClickInside = el === event.target || el.contains(event.target);
      if (isDisplayed && !isClickInside) {
        vnode.context[binding.expression](event);
      }
    };
    document.addEventListener("click", el.clickOutsideEvent, true);
  },
  unbind(el) {
    document.removeEventListener("click", el.clickOutsideEvent);
  },
});

Vue.directive("click-outside-except-calendar", {
  bind: function (el, binding, vnode) {
    el.clickOutsideEvent = function (event) {
      // checking that click was outside the el and his childrens
      if (!(el === event.target || el.contains(event.target))) {
        let calendar = document.getElementsByClassName(
          "ant-calendar-picker-container"
        );
        if (!calendar || !calendar.length) {
          // and if it did, call method provided in attribute value
          vnode.context[binding.expression](event);
        }
      }
    };
    document.body.addEventListener("click", el.clickOutsideEvent);
  },
  unbind: function (el) {
    document.body.removeEventListener("click", el.clickOutsideEvent);
  },
});

Vue.directive("capitalize", {
  bind: function (el, binding, vnode) {
    if (el.innerText) {
      const renderedString =
        el.innerText.charAt(0).toUpperCase() + el.innerHTML.slice(1);
      el.innerText = renderedString;
    }
  },
});

Vue.directive("capitalize-all", {
  bind: function (el, binding, vnode) {
    // console.log({
    //     el: el,
    //     elTarget: el?.target,
    //     elCurrentTarget: el?.currentTarget,
    //     innerHTML: el?.target?.innerHTML
    // })
  },
});

// Vue.use(VueDraggable.default);
Vue.use(PrettyCheckbox);

// filter

// components
Vue.use(window["vue-tel-input"]);
Vue.component("draggable", vuedraggable);
Vue.component(
  "user-list-cell",
  httpVueLoader("/components/user-list-cell.vue")
);
Vue.component(
  "mold-parts-dropdown-view",
  httpVueLoader("/components/mold-parts-dropdown-view.vue")
);
Vue.component(
  "last-shot-filter",
  httpVueLoader("/components/last-shot-filter.vue")
);
Vue.component(
  "file-previewer",
  httpVueLoader("/components/mold-detail/file-previewer.vue")
);

/** @deprecated */
Vue.component(
  "common-select-dropdown",
  httpVueLoader("/components/common/dc-component/custom-dropdown.vue")
);
Vue.component(
  "common-dropdown",
  httpVueLoader("/components/@base/dropdown/dropdown.vue")
);
Vue.component(
  "vue-upload",
  httpVueLoader("/ui/node_modules/vue-image-crop-upload-master/upload-2.vue")
);
Vue.component(
  "upload-profile",
  httpVueLoader("/ui/node_modules/vue-image-crop-upload-master/upload-4.vue")
);
Vue.component(
  "common-popover",
  httpVueLoader("/components/@base/popover/popover.vue")
);
Vue.component(
  "common-select-popover",
  httpVueLoader("/components/common/dc-component/common-select-popover.vue")
);
Vue.component(
  "common-searchbar",
  httpVueLoader("/components/common/searchbar.vue")
);
Vue.component(
  "common-searchbox",
  httpVueLoader("/components/common/dc-component/custom-searchbox.vue")
);
Vue.component("common-dialog", httpVueLoader("/components/common/dialog.vue"));
Vue.component(
  "common-button",
  httpVueLoader("/components/common/common-dropdown-button.vue")
);
Vue.component(
  "common-select-button",
  httpVueLoader("/components/@base/button/select-button.vue")
);
Vue.component(
  "common-treeselect-dropdown",
  httpVueLoader("/components/common/treeselect-dropdown.vue")
);
Vue.component(
  "common-button",
  httpVueLoader("/components/common/dc-component/common-dropdown-button.vue")
);
Vue.component(
  "common-select",
  httpVueLoader("/components/@base/select/select.vue")
);
Vue.component(
  "common-card",
  httpVueLoader("/components/common/card-custom.vue")
);
Vue.component("cta-button", httpVueLoader("/components/common/cta-button.vue"));

Vue.component(
  "common-hour-picker",
  httpVueLoader("/components/@base/hour-picker/hour-picker.vue")
);

// base components
Vue.component(
  "base-dropdown",
  httpVueLoader("/components/@base/dropdown/base-dropdown.vue")
);
Vue.component(
  "base-button",
  httpVueLoader("/components/@base/button/base-button.vue")
);

Vue.component(
  "base-input",
  httpVueLoader("/components/@base/input/base-input.vue")
);
Vue.component("base-avatar", httpVueLoader("/components/@base/avatar.vue"));
Vue.component(
  "base-simple-steps",
  httpVueLoader("/components/@base/steps/base-simple-steps.vue")
);
Vue.component(
  "base-steps",
  httpVueLoader("/components/@base/steps/base-steps.vue")
);
Vue.component(
  "base-chip",
  httpVueLoader("/components/@base/select/selection-card.vue")
);
Vue.component(
  "base-search",
  httpVueLoader("/components/@base/input/base-search.vue")
);
Vue.component(
  "base-drop-count",
  httpVueLoader("/components/@base/popover/drop-count.vue")
);

Vue.component(
  "base-tooltip",
  httpVueLoader("/components/@base/tooltip/base-tooltip.vue")
);
Vue.component(
  "base-drop-count",
  httpVueLoader("/components/@base/popover/drop-count.vue")
);
Vue.component(
  "base-priority-card",
  httpVueLoader("/components/@base/priority-card.vue")
);
Vue.component(
  "base-dialog",
  httpVueLoader("/components/@base/dialog/base-dialog.vue")
);
Vue.component(
  "base-card-tab",
  httpVueLoader("/components/@base/tabs/base-card-tab.vue")
);
Vue.component(
  "base-card-tab-list",
  httpVueLoader("/components/@base/tabs/base-card-tab-list.vue")
);
Vue.component(
  "base-chip-tab",
  httpVueLoader("/components/@base/tabs/base-chip-tab.vue")
);
Vue.component(
  "base-chip-tab-list",
  httpVueLoader("/components/@base/tabs/base-chip-tab-list.vue")
);
Vue.component(
  "base-upload",
  httpVueLoader("/components/@base/upload/base-upload.vue")
);
Vue.component(
  "base-popup",
  httpVueLoader("/components/@base/popup/base-popup.vue")
);

Vue.component(
  "toast-alert",
  httpVueLoader("/components/@base/toast/toast-alert.vue")
);
Vue.component(
  "base-steps",
  httpVueLoader("/components/@base/steps/base-steps.vue")
);
Vue.component(
  "base-popover",
  httpVueLoader("/components/@base/popover/popover.vue")
);
Vue.component(
  "base-paging",
  httpVueLoader("/components/@base/paging/base-paging.vue")
);
Vue.component(
  "base-option-button-group",
  httpVueLoader("/components/@base/group-button/option-button-group.vue")
);
Vue.component(
  "base-filter-search-bar",
  httpVueLoader("/components/@base/input/filter-search-bar.vue")
);
Vue.component(
  "base-loader",
  httpVueLoader("/components/@base/loader/base-loader.vue")
);
// Vue.component('base-images-slider', httpVueLoader('/components/@base/images-preview/base-images-slider.vue'))

// declare global business components
Vue.component(
  "data-entry-modal",
  httpVueLoader(
    "/components/data-requests/request-modal/data-entry/data-entry-modal.vue"
  )
);
