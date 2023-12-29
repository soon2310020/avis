const ConfigTabType = {
  PART_QUANTITY: "PART_QUANTITY",
  SHOT: "SHOT",
  CYCLE_TIME: "CYCLE_TIME",
  UPTIME: "UPTIME",
  TEMPERATURE: "TEMPERATURE",
  ACCELERATION: "ACCELERATION",
};
const GraphOptionsDefault = {};
const contextGraph = {
  shotImg: "",
  cycleTimeImg: "",
  uptimeImg: "",
  optimalCycleTime: "Approved CT",
};
var CommonChart = {};

// Shot Graph Options
CommonChart.initGraphOptionsDefault = function (
  optimalCycleTime,
  chartTypeConfig
) {
  if (chartTypeConfig) {
    contextGraph.shotImg =
      chartTypeConfig.filter((item) => item.chartDataType === "SHOT")[0]
        .chartType === "LINE"
        ? "/images/graph/shot-icon.svg"
        : "/images/graph/graph-setting-rec.svg";
    contextGraph.cycleTimeImg =
      chartTypeConfig.filter((item) => item.chartDataType === "CYCLE_TIME")[0]
        .chartType === "LINE"
        ? "/images/graph/graph-setting-line.svg"
        : "/images/graph/three-bar.svg";
    contextGraph.uptimeImg =
      chartTypeConfig.filter((item) => item.chartDataType === "UPTIME")[0]
        .chartType === "LINE"
        ? "/images/graph/graph-setting-line.svg"
        : "/images/graph/three-bar.svg";
  }
  GraphOptionsDefault[ConfigTabType.SHOT] = [
    {
      id: 0,
      name: optimalCycleTime || contextGraph.optimalCycleTime,
      code: "APPROVED_CYCLE_TIME",
      icon: "/images/graph/approve-icon.svg",
      selected: false,
    },
    {
      id: 1,
      name: "Cycle Time (Within, L1, L2)",
      code: "CYCLE_TIME",
      icon: contextGraph.cycleTimeImg,
      selected: false,
    },
    {
      id: 2,
      name: "Max CT",
      code: "MAX_CYCLE_TIME",
      icon: "/images/graph/max-icon.svg",
      selected: false,
    },
    {
      id: 3,
      name: "Min CT",
      code: "MIN_CYCLE_TIME",
      icon: "/images/graph/min-icon.svg",
      selected: false,
    },
    {
      id: 4,
      name: "Shot",
      code: "QUANTITY",
      default: true,
      icon: contextGraph.shotImg,
      selected: true,
    },
  ];

  // Cycle Time Graph Options
  GraphOptionsDefault[ConfigTabType.CYCLE_TIME] = [
    {
      id: 0,
      name: optimalCycleTime || contextGraph.optimalCycleTime,
      code: "APPROVED_CYCLE_TIME",
      // default: true,
      icon: "/images/graph/approve-icon.svg",
      selected: false,
    },
    {
      id: 1,
      name: "Cycle Time (Within, L1, L2)",
      code: "CYCLE_TIME",
      default: true,
      icon: contextGraph.cycleTimeImg,
      selected: true,
    },
    {
      id: 2,
      name: "Max CT",
      code: "MAX_CYCLE_TIME",
      // default: true,
      icon: "/images/graph/max-icon.svg",
      selected: false,
    },
    {
      id: 3,
      name: "Min CT",
      code: "MIN_CYCLE_TIME",
      // default: true,
      icon: "/images/graph/min-icon.svg",
      selected: false,
    },
    {
      id: 4,
      name: "Shot",
      code: "QUANTITY",
      icon: contextGraph.shotImg,
      selected: false,
    },
  ];

  // Uptime Graph Options
  GraphOptionsDefault[ConfigTabType.UPTIME] = [
    {
      id: 5,
      name: "Uptime (Within, L1, L2)",
      code: "UPTIME",
      default: true,
      icon: contextGraph.uptimeImg,
      selected: true,
    },
    {
      id: 6,
      name: "Uptime Target",
      code: "UPTIME_TARGET",
      // default: true,
      icon: "/images/graph/approve-icon.svg",
      selected: false,
    },
    {
      id: 4,
      // name: "Shot",
      name: "Shot",
      code: "QUANTITY",
      icon: contextGraph.shotImg,
      selected: false,
    },
  ];
};

CommonChart.convertChartType = function (chartType) {
  let type = null;
  switch (chartType) {
    case "QUANTITY":
      type = "SHOT";
      break;
    case "PART_QUANTITY":
      type = "PART_QUANTITY";
      break;
    case "CYCLE_TIME":
      type = "CYCLE_TIME";
      break;
    case "CYCLE_TIME_ANALYSIS":
      type = "CYCLE_TIME";
      break;
    case "UPTIME":
      type = "UPTIME";
      break;
    case "TEMPERATURE_ANALYSIS":
      type = "TEMPERATURE";
      break;
  }
  return type;
};
CommonChart.getCurrentChartSettings = async function (chartType) {
  const type = CommonChart.convertChartType(chartType);
  let res = await axios.get(
    `/api/config/graph-settings-config?chartDataType=${type}`
  );
  return res.data?.data;
};
CommonChart.getGraphOptionsDefault = function (
  chartType,
  optimalCycleTime,
  chartTypeConfig
) {
  const type = CommonChart.convertChartType(chartType);
  if (Object.keys(GraphOptionsDefault).length == 0) {
    CommonChart.initGraphOptionsDefault(optimalCycleTime, chartTypeConfig);
  }
  return GraphOptionsDefault[type];
};

CommonChart.getChartTypeConfig = async function (moldId) {
  let res = await axios.get(`/api/common/dsp-stp?moldId=${moldId || ""}`);
  return res.data?.chartTypeConfig;
};
CommonChart.getOptimalCycleTime = async function () {
  try {
    // let response = await axios.get('/api/common/cfg-stp?configCategory=OPTIMAL_CYCLE_TIME');
    // let val=response.data?.options?.OPTIMAL_CYCLE_TIME;

    const options = await Common.getSystem("options");
    const listConfigs = JSON.parse(options);
    const val = listConfigs.OPTIMAL_CYCLE_TIME;

    return val;
  } catch (e) {
    console.log(e);
  }
};
