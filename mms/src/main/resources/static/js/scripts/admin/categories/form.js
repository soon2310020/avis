window.onload = function () {
  document.title = "Categories" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave_sidebar");
    $("div").removeClass("wave_category");
    $("div").removeClass("profile_wave");
    $("div").removeClass("wave_save");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
    document.getElementById("remove_profile").remove();
    $("div").removeClass("hide_account");
  }, 500);
};

var Page = Common.getPage("categories");

axios.defaults.headers.post["Content-Type"] =
  "application/x-www-form-urlencoded";
var vm = new Vue({
  el: "#app",
  data: {
    mode: Page.MODE,
    parentCategories: [],
    category: {
      name: "",
      description: "",
      parentId: "",
      enabled: true,
      children: [],
    },
  },
  methods: {
    submit: function () {
      if (Page.IS_NEW) {
        this.create();
      } else {
        this.update();
      }
    },

    create: function () {
      axios
        .post(Page.API_POST, vm.category)
        .then(function (response) {
          console.log(response.data);

          if (response.data.success) {
            Common.alertCallback = function () {
              location.href = newPageUrl.CATEGORY;
            };
            Common.confirm("success", "다른 카테고리도 등록하시겠습니까?");
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
    update: function () {
      vm.children = null;
      axios
        .put(Page.API_PUT, vm.category)
        .then(function (response) {
          console.log(response.data);

          if (response.data.success) {
            Common.alertCallback = function () {
              location.href = newPageUrl.CATEGORY;
            };
            Common.alert("success");
          } else {
            Common.alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
  },
  computed: {
    New: function () {
      return Page.IS_NEW ? "New" : "Edit";
    },
  },
  mounted() {
    this.$nextTick(function () {
      // 부모 카테고리 조회
      axios.get(Page.API_BASE + "?size=1000").then(function (response) {
        vm.parentCategories = response.data.content;

        if (Page.IS_EDIT) {
          // 데이터 조회
          axios.get(Page.API_GET).then(function (response) {
            vm.category = response.data;
          });
        }
      });
    });
  },
});

$(function () {
  $(".check-all").on("click", function () {
    var isChecked = $(this).prop("checked");
    var $children = $(this)
      .closest("div")
      .parent()
      .find("input[type=checkbox]");
    $children.prop("checked", isChecked);

    vm.checkAll();
  });
  $(".check-all-children").on("click", function () {
    var isChecked = $(this).prop("checked");
    var $children = $(this).closest("li").find("input[type=checkbox]");
    $children.prop("checked", isChecked);

    vm.checkAll();
  });
});
