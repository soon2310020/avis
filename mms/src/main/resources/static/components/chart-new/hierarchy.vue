<template>
  <div id="HIERARCHY" :class="className" class="card">
    <div class="top-container title-container">
      <div class="line-title"></div>
      <div
        style="
          display: flex;
          justify-content: space-between;
          align-items: center;
        "
      >
        <div style="margin-left: 35px" class="title-top">Project Hierarchy</div>
        <a-tooltip placement="bottom">
          <template slot="title">
            <div
              style="padding: 6px; font-size: 13px"
              v-text="resources['project_hierarchy_description']"
            ></div>
          </template>
          <div style="margin-left: 10px">
            <img src="/images/icon/tooltip_info.svg" width="15" height="15" />
          </div>
        </a-tooltip>
      </div>
      <span
        class="close-button"
        v-on:click="dimiss('HIERARCHY', 'Project Hierachy')"
        style="font-size: 25px"
        aria-hidden="true"
        >&times;</span
      >
    </div>
    <div style="overflow: auto" class="card-body row">
      <div
        style="
          cursor: pointer;
          align-items: center;
          display: flex;
          flex-direction: column;
        "
        class="col-sm-4 chart-wrapper"
      >
        <diamond-card
          :func="clickDiamond"
          image="/images/dashboard/diamond-1.svg"
          :content="`${categoryTitle}`"
          :index="1"
          :active="diamonEnable['1']"
        ></diamond-card>
        <!-- <midder></midder> -->
        <diamond-card
          :func="clickDiamond"
          image="/images/dashboard/diamond-2.svg"
          :content="`${projectTitle}`"
          :index="2"
          :active="diamonEnable['2']"
        ></diamond-card>
        <!-- <midder></midder> -->
        <diamond-card
          :func="clickDiamond"
          image="/images/dashboard/diamond-3.svg"
          :content="`${partTitle}`"
          :index="3"
          :active="diamonEnable['3']"
        ></diamond-card>
        <!-- <midder></midder> -->
        <diamond-card
          :func="clickDiamond"
          image="/images/dashboard/diamond-4.svg"
          :content="`${toolingTitle}`"
          :index="4"
          :active="diamonEnable['4']"
        ></diamond-card>
      </div>

      <div class="col-sm-8">
        <div class="row">
          <div style="padding-right: 0px" class="col-6 col-md-3">
            <div
              style="
                border-left-color: #ebbb72;
                padding: 0px;
                padding-left: 0.5em;
              "
              class="callout callout-info"
            >
              <div style="color: #ebbb72; font-size: 0.7em" class="text-muted">
                Category
              </div>

              <strong
                style="font-size: 0.8em; margin-bottom: 0px"
                class="h4"
                v-text="categoryTitle"
              ></strong>
            </div>
          </div>
          <div
            style="padding-right: 0px; padding-left: 0px"
            class="col-6 col-md-3"
          >
            <div
              style="
                border-left-color: #e28468;
                padding: 0px;
                padding-left: 0.5em;
              "
              class="callout callout-success"
            >
              <div style="color: #e28468; font-size: 0.7em" class="text-muted">
                Project
              </div>

              <strong
                style="font-size: 0.8em; margin-bottom: 0px"
                class="h4"
                v-text="projectTitle"
              ></strong>
            </div>
          </div>
          <div
            style="padding-right: 0px; padding-left: 0px"
            class="col-6 col-md-3"
          >
            <div
              style="
                border-left-color: #ca4e75;
                padding: 0px;
                padding-left: 0.5em;
              "
              class="callout callout-warning"
            >
              <div style="color: #ca4e75; font-size: 0.7em" class="text-muted">
                Part
              </div>

              <strong
                style="font-size: 0.8em; margin-bottom: 0px"
                class="h4"
                v-text="partTitle"
              ></strong>
            </div>
          </div>
          <div
            style="padding-right: 0px; padding-left: 0px"
            class="col-6 col-md-3"
          >
            <div
              style="
                border-left-color: #6c26d1;
                padding: 0px;
                padding-left: 0.5em;
              "
              class="callout callout-danger"
            >
              <div style="color: #6c26d1; font-size: 0.7em" class="text-muted">
                Tooling
              </div>

              <strong
                style="font-size: 0.8em; margin-bottom: 0px"
                class="h4"
                v-text="toolingTitle"
              ></strong>
            </div>
          </div>
        </div>

        <table
          class="table table-striped table-heirarchy"
          style="border-top: 1px solid #ccc"
        >
          <thead>
            <tr style="background: #f2f5fa; font-size: 0.8em">
              <th>Category</th>
              <th>Project</th>
              <th>Part</th>
              <th>Tooling</th>
            </tr>
          </thead>
          <tbody style="background: #fff">
            <tr v-for="(result, index) in data ? categorySummaries : []">
              <template v-if="checkTableShow() == 'ALL'">
                <td
                  style="cursor: pointer; font-size: 0.7em"
                  v-on:click="getData(result.categoryId, result.categoryName)"
                  v-html="categoryNameHandle(result.categoryName, index)"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="
                    getData(result.categoryId, result.categoryName, () =>
                      clickDiamond(2)
                    )
                  "
                  v-text="result.projectCount"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="
                    getData(result.categoryId, result.categoryName, () =>
                      clickDiamond(3)
                    )
                  "
                  v-text="result.partCount"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="
                    getData(result.categoryId, result.categoryName, () =>
                      clickDiamond(4)
                    )
                  "
                  v-text="result.moldCount"
                ></td>
              </template>

              <template v-if="checkTableShow() == '1'">
                <td
                  style="cursor: pointer"
                  v-on:click="getData(categoryId, categoryTitle)"
                  v-html="categoryNameHandle(result.categoryName, index)"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="showProject()"
                  v-text="result.projectCount"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="clickDiamond(3)"
                  v-text="result.partCount"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="clickDiamond(4)"
                  v-text="result.moldCount"
                ></td>
              </template>

              <template v-if="checkTableShow() == '2' && step == 1">
                <td
                  v-if="index == 0"
                  style="cursor: pointer"
                  v-on:click="getData(categoryId, categoryTitle)"
                  :rowspan="categorySummaries.length"
                  v-text="categoryTitle"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="showProjectDetail(result.project)"
                  v-text="result.projectName"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="showProjectDetail(result.project)"
                  v-text="result.partCount"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="showToolingForProject(result.project)"
                  v-text="result.moldCount"
                ></td>
              </template>

              <template v-if="checkTableShow() == '2' && step == 2">
                <td
                  style="cursor: pointer"
                  v-on:click="getData(categoryId, categoryTitle)"
                  v-if="index == 0"
                  :rowspan="categorySummaries.length"
                  v-text="categoryTitle"
                ></td>
                <td
                  style="cursor: pointer"
                  v-if="index == 0"
                  :rowspan="categorySummaries.length"
                  v-text="projectTitle"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="showPart(result.part)"
                  v-text="result.partCode"
                ></td>
                <td
                  style="cursor: pointer"
                  v-on:click="showPart(result.part)"
                  v-text="result.moldCount"
                ></td>
              </template>

              <template v-if="!isDiamondClick">
                <template v-if="checkTableShow() == '3'">
                  <td
                    v-on:click="getData(categoryId, categoryTitle)"
                    v-if="index == 0"
                    :rowspan="categorySummaries.length"
                    v-text="categoryTitle"
                  ></td>
                  <td
                    v-on:click="showProjectDetail(projectData)"
                    v-if="index == 0"
                    :rowspan="categorySummaries.length"
                    v-text="projectTitle"
                  ></td>
                  <td
                    v-on:click="showProjectDetail(projectData)"
                    v-if="index == 0"
                    :rowspan="categorySummaries.length"
                    v-text="partTitle"
                  ></td>
                  <td
                    style="cursor: pointer"
                    v-on:click="showTooling(result)"
                    v-text="result"
                  ></td>
                </template>

                <template v-if="checkTableShow() == '4'">
                  <td v-text="categoryTitle"></td>
                  <td v-text="projectTitle"></td>
                  <td v-text="partTitle"></td>
                  <td v-text="result"></td>
                </template>
              </template>
              <template v-else>
                <template v-if="checkTableShow() == '3'">
                  <td
                    v-if="index == 0"
                    :rowspan="categorySummaries.length"
                    v-text="categoryTitle"
                  ></td>
                  <td
                    v-if="index == 0"
                    :rowspan="categorySummaries.length"
                    v-text="projectTitle"
                  ></td>
                  <td
                    style="cursor: pointer"
                    v-on:click="showPart(result.part)"
                    v-text="result.partCode"
                  ></td>
                  <td v-text="result.moldCount"></td>
                </template>
                <template v-if="checkTableShow() == '4'">
                  <td
                    v-if="index == 0"
                    :rowspan="categorySummaries.length"
                    v-text="categoryTitle"
                  ></td>
                  <td
                    v-if="index == 0"
                    :rowspan="categorySummaries.length"
                    v-text="projectTitle"
                  ></td>
                  <td
                    v-if="index == 0"
                    :rowspan="categorySummaries.length"
                    v-text="partTitle"
                  ></td>
                  <td
                    style="cursor: pointer"
                    v-on:click="showTooling(result)"
                    v-text="result"
                  ></td>
                </template>
              </template>
            </tr>
          </tbody>
        </table>
        <div v-on:click="goToMain()" class="main-button">Main</div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    type: String,
    dimiss: Function,
    filterQuery: Object,
    data: Object,
    className: String,
  },
  components: {
    "diamond-card": httpVueLoader("/components/chart-new/diamond-card.vue"),
    midder: httpVueLoader("/components/chart-new/diamond/midder.vue"),
    diamond: httpVueLoader("/components/chart-new/diamond/diamond.vue"),
  },
  data() {
    return {
      diamonEnable: {
        1: true,
        2: true,
        3: true,
        4: true,
      },
      categorySummaries: this.data.categorySummaries,
      categoryTitle: this.getCount().category,
      categoryId: "",
      categoryData: null,
      step: 1,
      //project
      projectData: null,
      projectTitle: this.getCount().project,
      //part
      partData: null,
      partTitle: this.getCount().part,
      //tooling
      toolingTitle: this.getCount().tooling,

      //handle for click in diamond
      isDiamondClick: false,
    };
  },
  methods: {
    clickDiamond: function (index) {
      console.log("vao 4", this.categoryData);
      if (!this.categoryData) {
        return;
      }
      const { projectDetailsData } = this.categoryData;
      let partCount = 0;
      let moldCount = 0;
      projectDetailsData.forEach((project) => {
        partCount += project.partDetailsData.length;
        project.partDetailsData.forEach((part) => {
          moldCount += part.moldCode.length;
        });
      });
      this.projectTitle = projectDetailsData.length;
      this.partTitle = partCount;
      this.toolingTitle = moldCount;
      this.step = 1;
      if (index == 2) {
        this.showProject();
      } else if (index == 3) {
        this.isDiamondClick = true;
        this.partDiamond();
      } else if (index == 4) {
        console.log("vao 4");
        this.isDiamondClick = true;
        this.toolingDiamond();
      }
    },

    showToolingForProject: function (project) {
      console.log("project: ", project);
      const { partDetailsData } = project;
      this.diamonEnable = {
        1: false,
        2: false,
        3: true,
        4: false,
      };
      this.isDiamondClick = false;
      this.projectData = project;
      this.projectTitle = project.projectName;
      this.partTitle = partDetailsData.length;
      let result = [];
      partDetailsData.forEach((part) => {
        const { moldCode } = part;
        moldCode.forEach((tooling) => {
          result.push(tooling);
        });
      });
      this.categorySummaries = result;
    },

    toolingDiamond: function () {
      const { projectDetailsData } = this.categoryData;
      this.diamonEnable = {
        1: false,
        2: false,
        3: false,
        4: true,
      };
      let result = [];
      projectDetailsData.forEach((project) => {
        project.partDetailsData.forEach((part) => {
          const { moldCode } = part;
          moldCode.forEach((tooling) => {
            result.push(tooling);
          });
        });
      });
      this.categorySummaries = result;
    },

    partDiamond: function () {
      const { projectDetailsData } = this.categoryData;
      this.diamonEnable = {
        1: false,
        2: false,
        3: true,
        4: false,
      };
      let result = [];
      projectDetailsData.forEach((project) => {
        project.partDetailsData.forEach((part) => {
          let item = {};
          item.moldCount = part.moldCode.length;
          item.partCode = part.partCode;
          item.part = part;
          result.push(item);
        });
      });
      this.categorySummaries = result;
    },

    goToMain: function () {
      this.diamonEnable = {
        1: true,
        2: true,
        3: true,
        4: true,
      };
      this.categorySummaries = this.data.categorySummaries;
      this.categoryTitle = this.getCount().category;
      this.categoryId = "";
      this.categoryData = null;
      this.step = 1;
      //project
      this.projectData = null;
      this.projectTitle = this.getCount().project;
      //part
      this.partData = null;
      this.partTitle = this.getCount().part;
      //tooling
      this.toolingTitle = this.getCount().tooling;
    },

    checkTableShow: function () {
      let count = 0;
      const keys = Object.keys(this.diamonEnable);
      let result = "ALL";
      keys.forEach((key) => {
        if (this.diamonEnable[key] == true) {
          count++;
          result = key;
        }
      });
      if (count == 4) {
        return "ALL";
      } else {
        return result;
      }
    },

    showTooling: function (data) {
      this.isDiamondClick = false;
      this.diamonEnable = {
        1: false,
        2: false,
        3: false,
        4: true,
      };
      this.toolingTitle = data;
      this.categorySummaries = [data];
    },

    showPart: function (data) {
      this.isDiamondClick = false;
      this.diamonEnable = {
        1: false,
        2: false,
        3: true,
        4: false,
      };
      this.partData = data;
      this.partTitle = data.partCode;
      this.categorySummaries = data.moldCode;
    },

    showProjectDetail: function (data) {
      this.diamonEnable = {
        1: false,
        2: true,
        3: false,
        4: false,
      };
      this.step = 2;
      this.projectData = data;
      this.projectTitle = data.projectName;
      this.categorySummaries = data.partDetailsData.map((part) => {
        return {
          part,
          partCode: part.partCode,
          moldCount: part.moldCode.length,
        };
      });
    },

    showProject: function () {
      this.step == 1;
      const { projectDetailsData } = this.categoryData;
      this.diamonEnable = {
        1: false,
        2: true,
        3: false,
        4: false,
      };
      let totalPartCount = 0;
      let totalMoldCount = 0;
      this.categorySummaries = projectDetailsData.map((project) => {
        let partCount = 0;
        let moldCount = 0;
        partCount += project.partDetailsData.length;
        project.partDetailsData.forEach((part) => {
          moldCount += part.moldCode.length;
        });
        totalMoldCount += moldCount;
        totalPartCount += partCount;
        return {
          projectName: project.projectName,
          partCount,
          moldCount,
          project,
        };
      });
      console.log("this.categorySummaries: ", this.categorySummaries);
      console.log("this.step: ", this.step);
      this.partTitle = totalPartCount;
      this.toolingTitle = totalMoldCount;
    },

    getCount: function () {
      const category = this.data.categorySummaries.length;
      let project = 0;
      let part = 0;
      let tooling = 0;
      this.data.categorySummaries.forEach((element) => {
        project += element.projectCount;
        part += element.partCount;
        tooling += element.moldCount;
      });

      return {
        category,
        project,
        part,
        tooling,
      };
    },

    categoryNameHandle: function (categoryName, index) {
      return (
        '<strong style="color: ' +
        vm.summary.colors[index] +
        '">' +
        categoryName +
        "</strong>"
      );
    },
    goToPage: function (type) {
      if (type == "category") {
        location.href = `${Common.PAGE_URL.CATEGORY}`;
      }
    },
    getRandomColor: function () {
      var letters = "0123456789ABCDEF".split("");
      var color = "#";
      for (var i = 0; i < 6; i++) {
        color += letters[Math.floor(Math.random() * 16)];
      }
      return color;
    },

    getData: function (categoryId, categoryName, func) {
      ///api/chart/category-summary/details?categoryIds=&payload
      const { locationIds, supplierIds, toolMakerIds } = this.filterQuery;
      let url = `/api/chart/category-summary/details?categoryId=${categoryId}`;
      if (toolMakerIds.length > 0) {
        url =
          url +
          (url.includes("?") ? "&" : "?") +
          "toolMakerIds=" +
          toolMakerIds.toString();
      }
      if (supplierIds.length > 0) {
        url =
          url +
          (url.includes("?") ? "&" : "?") +
          "supplierIds=" +
          supplierIds.toString();
      }
      if (locationIds.length > 0) {
        const locationString = locationIds.toString();
        url =
          url +
          (url.includes("?") ? "&" : "?") +
          "locationIds=" +
          locationString;
      }

      axios
        .get(url)
        .then((response) => {
          if (response.data) {
            this.categoryData = response.data;
            const { projectDetailsData } = response.data;
            let partCount = 0;
            let moldCount = 0;
            projectDetailsData.forEach((project) => {
              partCount += project.partDetailsData.length;
              project.partDetailsData.forEach((part) => {
                moldCount += part.moldCode.length;
              });
            });
            this.categorySummaries = [
              {
                categoryId: response.data.categoryId,
                categoryName: response.data.categoryName,
                projectCount: projectDetailsData.length,
                partCount,
                moldCount,
              },
            ];
            this.projectTitle = projectDetailsData.length;
            this.partTitle = partCount;
            this.toolingTitle = moldCount;

            this.categoryTitle = categoryName;
            this.categoryId = categoryId;
            this.diamonEnable = {
              1: true,
              2: false,
              3: false,
              4: false,
            };
            console.log("func: ", func);
            func();
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },
  },

  computed: {},
  watch: {
    data: function (newVal) {
      console.log("hrirarchy: ", newVal);
    },
  },

  mounted() {
    console.log("hrirarchy: ", this.data);
  },
};
</script>

<style scoped>
.table-heirarchy td {
  border: none !important;
  background: #fff;
  font-size: 0.8em !important;
}

.main-button {
  position: absolute;
  cursor: pointer;
  bottom: 3px;
  right: 20px;
}

.main-button.active,
.main-button:active,
.main-button:focus {
  color: #3ec1d4;
}
.main-button:hover {
  color: #3ec1d4;
}
</style>
