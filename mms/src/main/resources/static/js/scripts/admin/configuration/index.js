axios.defaults.headers.put["Content-Type"] = "application/json";
axios.defaults.headers.delete["Content-Type"] = "application/json";

window.onload = function () {
  document.title = "Configuration" + " | eMoldino";
  setTimeout(() => {
    $("div").removeClass("wave");
    $("div").removeClass("wave_sidebar");
    $("li").removeClass("wave_header");
    $("img").removeClass("wave_img");
  }, 500);
};
var Page = Common.getPage("molds");
var vm = new Vue({
  el: "#app",
  components: {
    configuration: httpVueLoader("/components/configuration/index.vue"),
    "tooling-configuration": httpVueLoader(
      "/components/configuration/ToolingConfiguration.vue"
    ),
    "op-configuration": httpVueLoader(
      "/components/configuration/OpConfiguration.vue"
    ),
    "alert-configuration": httpVueLoader(
      "/components/configuration/AlertConfiguration.vue"
    ),
    "data-filter": httpVueLoader("/components/configuration/DataFilter.vue"),
    "refurbishment-configuration": httpVueLoader(
      "/components/configuration/RefurbishmentConfiguration.vue"
    ),
    "optimal-cycle-time": httpVueLoader(
      "/components/configuration/OptimalCycleTime.vue"
    ),
    "overall-utilizaion-configuration": httpVueLoader(
      "/components/configuration/OverallUtilizationConfiguration.vue"
    ),
    "inactive-tooling-configuration": httpVueLoader(
      "/components/configuration/InactiveToolingConfiguration.vue"
    ),
    "chart-type": httpVueLoader("/components/configuration/ChartType.vue"),
    "save-alert": httpVueLoader("/components/configuration/SaveAlert.vue"),

    "risk-default": httpVueLoader("/components/chart-new/oee/risk-default.vue"),
    "oee-score-configuration": httpVueLoader(
      "/components/configuration/oee/oee-score-configuration.vue"
    ),
    "oee-metric-color-configuration": httpVueLoader(
      "/components/configuration/oee/oee-metric-color-configuration.vue"
    ),
    "reject-rate-config": httpVueLoader(
      "/components/configuration/reject-rate-configuration/reject-rate-input-config.vue"
    ),
    "warning-modal": httpVueLoader(
      "/components/configuration/reject-rate-configuration/warning-modal.vue"
    ),
  },
  data: {
    isChanged: false,
    newTab: "",
    displayButton: false,
    productionButton: true,
    dataFormButton: false,
    alertConfigButton: false,
    statusControlButton: false,
    resources: {},
    isOk: false,
    initConfigValue: [],
    opConfigFields: [],
    toolingList: [],
    alertConfig: {},
    refurbishmentConfigPayload: {},
    isCreating: "part",
    currentCategoryStatus: {
      TOOLING: {
        enabled: false,
      },
      PART: {
        enabled: false,
      },
      OP: {
        enabled: false,
      },
      ALERT: {
        enabled: false,
      },
      DATA_FILTER: {
        enabled: false,
      },
      REFURBISHMENT: {
        enabled: false,
        configOption: "UTILIZATION_RATE",
      },
      DASHBOARD_GROUP: {
        enabled: false,
      },
      OPTIMAL_CYCLE_TIME: {
        strategy: "WACT",
        periodMonths: 3,
      },
    },
    againstTimestamp: 0,
    riskLevelData: [],
    rejectRateConfig: {},
    prevRejectRateConfig: null,
    rejectRateFrequency: "",
    currentFrequency: null,
    prevFrequency: null,
    isRejectRateConfirmed: false,
    codes: [],
    clickMenuUrl: "",
    isGoBack: false,
    productionOptions: [
      { key: "SHOT_FILTER", title: "Shot Filter" },
      { key: "OPTIMAL_CYCLE_TIME", title: "Optimal Cycle Time" },
      { key: "REFURBISHMENT", title: "Refurbishment" },
      { key: "REJECT_RATE", title: "Reject Rate" },
      { key: "OEE_CONFIGURATION", title: "OEE Configuration" },
    ],
    selectedProductionTab: {
      key: "OEE_CONFIGURATION",
      title: "OEE Configuration",
    },
    metricColorConfig: [
      {
        key: "AVAILABILITY",
        title: "Availability",
        colorLevel: {
          low: 100,
          medium: 70,
          high: 50,
        },
        updateColor: () => {},
      },
      {
        key: "PERFORMANCE",
        title: "Performance",
        colorLevel: {
          low: 100,
          medium: 70,
          high: 50,
        },
        updateColor: () => {},
      },
      {
        key: "QUALITY",
        title: "Quality",
        colorLevel: {
          low: 100,
          medium: 70,
          high: 50,
        },
        updateColor: () => {},
      },
    ],
  },
  mounted() {
    const self = this;
    this.getAllRiskLevel();
    window.onpopstate = function () {
      self.prevButtonHandler();
    };

    const uri = URI.parse(location.href);
    const params = Common.parseParams(uri.query);
    if (params.tab) this.newTab = params.tab;
    this.changeTab();
  },
  beforeDestroy() {
    window.removeEventListener("click", this.clickOutsideHandler);
  },
  computed: {
    getRejectRateDescription() {
      let result = `Changing the frequency of reject rate input will result in deletion of reject rate input for the current`;
      if (this.rejectRateFrequency == "DAILY") {
        result += " day";
      } else {
        result += ` ${this.rejectRateFrequency
          .replaceAll("LY", "")
          .toLowerCase()}`;
      }
      return result;
    },
    getMetricColorConfig() {
      const configs = [...this.metricColorConfig].map((item) => {
        const newItem = { ...item };
        newItem.riskLevel = [
          newItem.colorLevel.high,
          newItem.colorLevel.medium,
        ];
        if (newItem.key === "AVAILABILITY") {
          newItem.updateColor = this.updateOeeMetricAvailability;
        } else if (newItem.key === "PERFORMANCE") {
          newItem.updateColor = this.updateOeeMetricPerformance;
        } else if (newItem.key === "QUALITY") {
          newItem.updateColor = this.updateOeeMetricQuality;
        }
        return newItem;
      });
      console.log("getMetricColorConfig", configs, this.metricColorConfig);
      return configs;
    },
  },
  methods: {
    async getAllRiskLevel() {
      try {
        const res = await axios.get(`/api/production/oee-stp`);
        console.log("getAllRiskLevel", res);
        this.riskLevelData = [res.data.oeeScore.high, res.data.oeeScore.medium];
        this.metricColorConfig = [...this.metricColorConfig].map((item) => {
          const newItem = { ...item };
          if (newItem.key === "AVAILABILITY") {
            newItem.colorLevel = res.data.oeeMetricAvailability;
          } else if (newItem.key === "PERFORMANCE") {
            newItem.colorLevel = res.data.oeeMetricPerformance;
          } else if (newItem.key === "QUALITY") {
            newItem.colorLevel = res.data.oeeMetricQuality;
          }
          return newItem;
        });
      } catch (error) {
        console.log(error);
      }
    },
    async updateRiskLevel(data) {
      try {
        const res = await axios.post(`/api/production/oee-stp`, data);
        console.log("updateRiskLevel", res);
      } catch (error) {
        console.log(error);
      }
    },
    updateOeeScore(data) {
      console.log("updateOeeScore", data);
      const payload = {
        oeeScore: {
          low: 100,
          medium: data[1],
          high: data[0],
        },
      };
      this.updateRiskLevel(payload);
    },
    updateOeeMetricAvailability(data) {
      console.log("updateOeeMetricAvailability", data);
      const payload = {
        oeeMetricAvailability: {
          low: 100,
          medium: data[1],
          high: data[0],
        },
      };
      this.updateRiskLevel(payload);
    },
    updateOeeMetricPerformance(data) {
      console.log("updateOeeMetricPerformance", data);
      const payload = {
        oeeMetricPerformance: {
          low: 100,
          medium: data[1],
          high: data[0],
        },
      };
      this.updateRiskLevel(payload);
    },
    updateOeeMetricQuality(data) {
      console.log("oeeMetricQuality", data);
      const payload = {
        oeeMetricQuality: {
          low: 100,
          medium: data[1],
          high: data[0],
        },
      };
      this.updateRiskLevel(payload);
    },
    handleChangeProductionTab(tab) {
      this.selectedProductionTab = tab;
    },
    prevButtonHandler() {
      if (vm.isChanged) {
        history.go(1);
        var saveAlert = Common.vue.getChild(vm.$children, "save-alert");
        if (saveAlert != null) {
          vm.isGoBack = true;
          saveAlert.showSaveAlert();
        }
      }
    },
    clickOutsideHandler(e) {
      let appContainer = this.$refs.appContainer;
      if (!appContainer.contains(e.target)) {
        let node = e.target.parentNode;
        if (node.nodeName === "A") {
          if (vm.isChanged) {
            e.preventDefault();
            var saveAlert = Common.vue.getChild(vm.$children, "save-alert");
            if (saveAlert != null) {
              vm.clickMenuUrl = node.href;
              saveAlert.showSaveAlert();
            }
          }
        }
      }
    },
    saveHandler() {
      if (this.isGoBack) {
        history.go(-1);
        this.isGoBack = false;
      }

      if (this.clickMenuUrl) {
        location.href = this.clickMenuUrl;
        this.clickMenuUrl = "";
      }
      if (this.displayButton) {
        var displayChild = Common.vue.getChild(this.$children, "chart-type");
        if (displayChild != null) {
          displayChild.saveData();
        }

        this.isChanged = false;
      } else if (this.dataFormButton) {
        var partChild = Common.vue.getChild(
          this.$children,
          "part-configuration"
        );
        if (partChild != null) {
          partChild.saveData();
        }
        var toolingChild = Common.vue.getChild(
          this.$children,
          "tooling-configuration"
        );
        if (toolingChild != null) {
          toolingChild.saveData();
        }
        this.isChanged = false;
      } else if (this.productionButton) {
        var productionChild = Common.vue.getChild(
          this.$children,
          "op-configuration"
        );
        if (productionChild != null) {
          productionChild.saveData();
        }
        var octChild = Common.vue.getChild(
          this.$children,
          "optimal-cycle-time"
        );
        if (octChild != null) {
          octChild.saveData();
        }
        var dataFilterChild = Common.vue.getChild(
          this.$children,
          "data-filter"
        );
        if (dataFilterChild != null) {
          dataFilterChild.saveData();
        }
        this.isChanged = false;
      } else if (this.alertConfigButton) {
        var alertConfig = Common.vue.getChild(
          this.$children,
          "refurbishment-configuration"
        );
        if (alertConfig != null) {
          alertConfig.saveData();
        }
        this.isChanged = false;
      } else if (this.statusControlButton) {
        const productionChild = Common.vue.getChild(
          this.$children,
          "op-configuration"
        );
        if (productionChild != null) {
          productionChild.saveData();
        }
        const utilizationConfigChild = Common.vue.getChild(
          this.$children,
          "overall-utilizaion-configuration"
        );
        if (utilizationConfigChild != null) {
          utilizationConfigChild.saveData();
        }
        const inactiveToolingConfigChild = Common.vue.getChild(
          this.$children,
          "inactive-tooling-configuration"
        );
        if (inactiveToolingConfigChild != null) {
          inactiveToolingConfigChild.saveData();
        }
        this.isChanged = false;
      }
      Common.alert("Saved Successfully");
      this.changeTab();
    },
    handleIsChange() {
      this.isChanged = true;
    },
    handleDisplayTab() {
      this.newTab = "displayTab";
      if (this.isChanged) {
        var saveAlert = Common.vue.getChild(this.$children, "save-alert");
        if (saveAlert != null) {
          saveAlert.showSaveAlert();
        }
      } else {
        this.displayButton = true;
        this.productionButton = false;
        this.dataFormButton = false;
        this.alertConfigButton = false;
        this.statusControlButton = false;
      }
    },
    handleProdctionTab() {
      this.newTab = "productionTab";
      if (this.isChanged) {
        var saveAlert = Common.vue.getChild(this.$children, "save-alert");
        if (saveAlert != null) {
          let a = saveAlert.showSaveAlert();
          console.log(a);
        }
      } else {
        this.displayButton = false;
        this.productionButton = true;
        this.dataFormButton = false;
        this.alertConfigButton = false;
        this.statusControlButton = false;
      }
    },

    handleDataFormTab() {
      this.newTab = "dataFormTab";
      if (this.isChanged) {
        var saveAlert = Common.vue.getChild(this.$children, "save-alert");
        if (saveAlert != null) {
          saveAlert.showSaveAlert();
        }
      } else {
        this.displayButton = false;
        this.productionButton = false;
        this.dataFormButton = true;
        this.alertConfigButton = false;
        this.statusControlButton = false;
      }
    },

    handleAlertConfigTab() {
      this.newTab = "alertConfigTab";
      if (this.isChanged) {
        var saveAlert = Common.vue.getChild(this.$children, "save-alert");
        if (saveAlert != null) {
          saveAlert.showSaveAlert();
        }
      } else {
        this.displayButton = false;
        this.productionButton = false;
        this.dataFormButton = false;
        this.alertConfigButton = true;
        this.statusControlButton = false;
      }
    },
    handleStatusControlTab() {
      this.newTab = "statusControl";
      if (this.isChanged) {
        var saveAlert = Common.vue.getChild(this.$children, "save-alert");
        if (saveAlert != null) {
          saveAlert.showSaveAlert();
        }
      } else {
        this.displayButton = false;
        this.productionButton = false;
        this.dataFormButton = false;
        this.alertConfigButton = false;
        this.statusControlButton = true;
      }
    },
    changeTab() {
      if (this.newTab === "displayTab") {
        this.displayButton = true;
        this.productionButton = false;
        this.dataFormButton = false;
        this.alertConfigButton = false;
        this.statusControlButton = false;
      } else if (this.newTab === "productionTab") {
        this.displayButton = false;
        this.productionButton = true;
        this.dataFormButton = false;
        this.alertConfigButton = false;
        this.statusControlButton = false;
      } else if (this.newTab === "dataFormTab") {
        this.displayButton = false;
        this.productionButton = false;
        this.dataFormButton = true;
        this.alertConfigButton = false;
        this.statusControlButton = false;
      } else if (this.newTab === "alertConfigTab") {
        this.displayButton = false;
        this.productionButton = false;
        this.dataFormButton = false;
        this.alertConfigButton = true;
        this.statusControlButton = false;
      } else if (this.newTab === "statusControl") {
        this.displayButton = false;
        this.productionButton = false;
        this.dataFormButton = false;
        this.alertConfigButton = false;
        this.statusControlButton = true;
      }
      this.newTab = "";
    },
    updateCreating(data) {
      this.isCreating = data;
    },
    cancel: function () {
      if (this.isGoBack) {
        history.go(-2);
        this.isGoBack = false;
      }

      if (this.clickMenuUrl) {
        location.href = this.clickMenuUrl;
        this.clickMenuUrl = "";
      }

      if (this.displayButton) {
        var displayChild = Common.vue.getChild(this.$children, "chart-type");
        if (displayChild != null) {
          displayChild.getChartTypeConfig();
        }

        this.isChanged = false;
      } else if (this.dataFormButton) {
        var partChild = Common.vue.getChild(
          this.$children,
          "part-configuration"
        );
        if (partChild != null) {
          partChild.cancelData();
        }
        var toolingChild = Common.vue.getChild(
          this.$children,
          "tooling-configuration"
        );
        if (toolingChild != null) {
          toolingChild.cancelData();
        }
        this.isChanged = false;
      } else if (this.productionButton) {
        var productionChild = Common.vue.getChild(
          this.$children,
          "op-configuration"
        );
        if (productionChild != null) {
          productionChild.cancelData();
        }
        var octChild = Common.vue.getChild(
          this.$children,
          "optimal-cycle-time"
        );
        if (octChild != null) {
          octChild.cancelData();
        }
        var dataFilterChild = Common.vue.getChild(
          this.$children,
          "data-filter"
        );
        if (dataFilterChild != null) {
          dataFilterChild.getConfig();
        }
        this.isChanged = false;
      } else if (this.alertConfigButton) {
        var alertConfig = Common.vue.getChild(
          this.$children,
          "refurbishment-configuration"
        );
        if (alertConfig != null) {
          alertConfig.cancelData();
        }
        this.isChanged = false;
      } else if (this.statusControlButton) {
        var productionChild = Common.vue.getChild(
          this.$children,
          "op-configuration"
        );
        if (productionChild != null) {
          productionChild.cancelData();
        }
      }
      // window.location.href = '/admin/configuration';
      this.getAllDataConfig();
      this.changeTab();
    },
    updateConfigList() {
      // checking timestamp
      let currentTimestamp = new Date().getTime();
      if (currentTimestamp - this.againstTimestamp < 100) {
        return;
      }
      this.againstTimestamp = currentTimestamp;
      let payload = {
        generalConfigPayloads: this.toolingList,
        opConfigPayloads: this.opConfigFields,
        alertConfigPayloads: this.alertConfig,
        refurbishmentConfigPayload: this.refurbishmentConfigPayload,
      };
      axios
        .post("/api/config/update/all", payload)
        .then((response) => {
          if (response.data.op_config && this.opConfigFields.length > 0) {
            response.data.op_config.forEach((responseConfig) => {
              let opConfigField = this.opConfigFields.filter(
                (config) =>
                  config.operatingStatus === responseConfig.operatingStatus
              )[0];
              if (opConfigField) {
                opConfigField.id = responseConfig.id;
              }
            });
          }
          if (response.data.general_config && this.toolingList.length > 0) {
            response.data.general_config.forEach((responseConfig) => {
              let configField = this.toolingList.filter(
                (config) =>
                  config.configCategory === responseConfig.configCategory &&
                  config.fieldName === responseConfig.fieldName
              )[0];
              if (configField) {
                configField.id = responseConfig.id;
              }
            });
          }
          // Common.alert("success");
          this.alertConfig = response.data.alert_config;
        })
        .catch((error) => {
          console.log("error", error);
        });
    },
    updateConfig(data, field) {
      axios
        .post("/api/config/update", data)
        .then(function (response) {
          if (response.data && response.data.id) {
            field.id = response.data.id;
          }
        })
        .catch(function (error) {
          console.log(error.response);
        });
    },

    updateAlertConfig(config) {
      this.alertConfig = config;
      this.updateConfigList();
    },
    updateRefurbishmentConfig(config) {
      this.refurbishmentConfigPayload = config;
      this.updateConfigList();
    },
    //save material ------------------
    collectOpConfig(config) {
      let existedField = this.opConfigFields.filter(
        (configField) =>
          configField.operatingStatus === config.operatingStatus &&
          configField.equipmentType === config.equipmentType
      )[0];
      if (!existedField) {
        this.opConfigFields.push(config);
      } else {
        Object.keys(config).forEach((field) => {
          existedField[field] = config[field];
        });
      }
      this.updateConfigList();
    },
    collectConfigs(params) {
      let existedField = this.toolingList.filter(
        (config) =>
          config.configCategory === params.configCategory &&
          config.fieldName === params.fieldName
      )[0];
      if (!existedField) {
        this.toolingList.push(params);
      } else {
        Object.keys(params).forEach((field) => {
          existedField[field] = params[field];
        });
      }
      this.updateConfigList();
    },
    collectOpConfig(config) {
      let existedField = this.opConfigFields.filter(
        (configField) =>
          configField.operatingStatus === config.operatingStatus &&
          configField.equipmentType === config.equipmentType
      )[0];
      if (!existedField) {
        this.opConfigFields.push(config);
      } else {
        Object.keys(config).forEach((field) => {
          existedField[field] = config[field];
        });
      }
      this.updateConfigList();
    },
    updateAlertConfig(config) {
      this.alertConfig = config;
      this.updateConfigList();
    },
    updateRefurbishmentConfig(config) {
      this.refurbishmentConfigPayload = config;
      this.updateConfigList();
    },
    updateCategoryConfigStatus(category, status, configOption = null) {
      let payload = {
        configCategory: category,
        enabled: status,
      };
      if (configOption) {
        payload.configOption = configOption;
      }
      if (
        this.currentCategoryStatus[category] &&
        this.currentCategoryStatus[category].id
      ) {
        payload.id = this.currentCategoryStatus[category].id;
      }
      axios
        .post("/api/config/update-config-enable", payload)
        .then(async (response) => {
          if (this.currentCategoryStatus[category] && response.data) {
            this.currentCategoryStatus[category].id = response.data.id;
            // update new config to session storage
            const optionsStringified = await Common.getSystem("options");
            const options = JSON.parse(optionsStringified);
            console.log({
              category,
              status,
              configOption,
              currentCategoryStatus: this.currentCategoryStatus,
              response,
            });
            const newConfiguration = {
              ...options,
              [category]: { ...response.data },
            };
            window.sessionStorage.setItem(
              "options",
              JSON.stringify(newConfiguration)
            );
            console.log(newConfiguration);
          }
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    async updateOCTConfig() {
      const payload = {
        options: {
          OPTIMAL_CYCLE_TIME: this.currentCategoryStatus.OPTIMAL_CYCLE_TIME,
        },
      };
      console.log("OptimalCycleTime>updateOCTConfig", payload);
      try {
        await axios.post("/api/common/cfg-stp", payload);
        sessionStorage.clear();
      } catch (error) {
        console.log(error);
      }
    },
    //----------------------
    getAllConfig() {
      console.log("@getAllConfig");
      axios
        .get("/api/config")
        .then((response) => {
          this.initConfigValue = response.data;
        })
        .catch((error) => {
          console.log(error.response);
        });
    },
    // async getOCTConfig() {
    //   try {
    //     const options = await Common.getSystem("options");
    //     const listConfigs = JSON.parse(options);
    //     if (listConfigs && listConfigs.OPTIMAL_CYCLE_TIME) {
    //       this.currentCategoryStatus.OPTIMAL_CYCLE_TIME =
    //         listConfigs.OPTIMAL_CYCLE_TIME;
    //       if (listConfigs.OPTIMAL_CYCLE_TIME !== "WACT")
    //         this.currentCategoryStatus.OPTIMAL_CYCLE_TIME.periodMonths = 3;
    //     }
    //   } catch (error) {
    //     console.log(error);
    //   }
    // },
    updateRisk(data) {
      console.log(data, "risk data");
      this.riskLevelData = [data[0], data[1]];
      const param = [
        {
          name: "LOW",
          percent: 100,
        },
        {
          name: "MEDIUM",
          percent: data[1],
        },
        {
          name: "HIGH",
          percent: data[0],
        },
      ];
      axios
        .post(`/api/machines/statistics/update-risk-level`, param)
        .then((res) => {
          console.log(res, "updateRisk");
        })
        .catch((e) => {
          console.log(e, "updateRisk");
        })
        .finally(() => {});
    },
    getRiskLevel() {
      return axios
        .get(`/api/machines/statistics/get-risk-level`)
        .then((res) => {
          this.riskLevelData = [res.data[2].percent, res.data[1].percent];
          console.log("riskLevelData", this.riskLevelData);
          this.$forceUpdate();
        })
        .catch((e) => {
          console.log(e, "riskLevelData");
        })
        .finally(() => {});
    },
    async getRejectRateFrequency() {
      try {
        const res = await axios.get("/api/rejected-part/get-configuration");
        console.log("getRejectRateFrequency", res);
        return res.data.data.frequent;
      } catch (error) {
        console.log(error);
        return null;
      }
    },
    showConfirmModal(item, prevItem) {
      this.rejectRateConfig = { ...item };
      this.prevRejectRateConfig = { ...prevItem };
      console.log("prevRejectRateConfig", this.prevRejectRateConfig);
      const child = this.$refs["warning-modal"];
      if (child) {
        child.showDeletePopup();
      }
    },
    updateRejectRateConfig() {
      // if (this.isRejectRateConfirmed) {
      this.rejectRateFrequency = this.rejectRateConfig.frequency;
      const frequency = this.rejectRateFrequency;
      axios
        .get(`/api/rejected-part/set-configuration?frequent=${frequency}`)
        .then((response) => {
          const frequency = response.data.data.frequent;
          console.log("updateRejectRateConfig", frequency, response);
          this.rejectRateConfig = { frequency: frequency };
        })
        .catch((error) => {
          console.log(error);
        });
      // }
    },
    handleCancel() {
      console.log("cancel");
      this.isRejectRateConfirmed = false;
      this.rejectRateConfig = { ...this.prevRejectRateConfig };
      this.rejectRateFrequency = this.prevRejectRateConfig.frequency;

      console.log("this.rejectRateConfig", this.rejectRateConfig);
    },
    async getAllDataConfig() {
      const data = await Common.getCategoryConfigStatus();
      Object.keys(data).forEach((key) => {
        if (this.currentCategoryStatus[key]) {
          this.currentCategoryStatus[key].id = data[key]?.id;
          this.currentCategoryStatus[key].enabled = data[key]?.enabled;
          if (data[key]?.configOption) {
            this.currentCategoryStatus[key].configOption =
              data[key].configOption;
          }
        }
      });
      console.log(
        "OptimalCycleTime>currentCategoryStatus",
        this.currentCategoryStatus
      );
      this.getAllConfig();
      // this.getOCTConfig();
    },
  },
  async created() {
    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.codes = Object.assign({}, this.codes, newVal);
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.resources = Object.assign({}, this.resources, newVal);
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.options,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.getAllDataConfig();
          this.currentCategoryStatus.OPTIMAL_CYCLE_TIME =
            newVal.OPTIMAL_CYCLE_TIME;
        }
      },
      { immediate: true }
    );

    const frequency = await this.getRejectRateFrequency();
    if (frequency) {
      this.rejectRateConfig = { frequency: frequency };
    }

    window.onbeforeunload = (e) => {
      e.preventDefault();
      e.returnValue =
        "You have unsaved changes. Are you sure you wish to leave?";
      if (vm.isChanged) {
        var saveAlert = Common.vue.getChild(vm.$children, "save-alert");
        if (saveAlert != null) {
          return saveAlert.showSaveAlert()
            ? "You have unsaved changes. Are you sure you wish to leave?"
            : "You have unsaved changes. Are you sure you wish to leave?";
        }
      }
      return "You have unsaved changes. Are you sure you wish to leave?";
    };
  },
  watch: {
    alertConfig(newVal) {
      console.log("OptimalCycleTime:alertConfig", newVal);
    },
    currentCategoryStatus(newVal) {
      console.log("OptimalCycleTime:currentCategoryStatus", newVal);
    },
    initConfigValue(newVal) {
      console.log("OptimalCycleTime:initConfigValue", newVal);
    },
  },
});
