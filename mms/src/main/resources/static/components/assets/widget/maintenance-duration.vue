<template>
  <emdn-widget :header-text="label" :info-text="label">
    <template #tooltip>
      <emdn-tooltip position="bottom" style-props="width: 300px;">
        <template #context>
          <div class="icon-info"><span class="icon"></span></div>
        </template>
        <template #body>
          <div style="text-align: left; white-space: pre-line">
            {{ tooltipInfo }}
          </div>
        </template>
      </emdn-tooltip>
    </template>
    <template #body>
      <emdn-xy-chart
        category="title"
        :data="data"
        :line-data-binder="lineDataBinder"
        :style-props="{ width: '100%', height: '224px' }"
      ></emdn-xy-chart>
    </template>
  </emdn-widget>
</template>

<script>
module.exports = {
  name: "MaintenanceDurationWidget",
  props: {
    label: String,
  },
  data() {
    return {
      category: "title",
      data: [
        [
          { title: "09-10", ctL1: 20, ctL2: 30 },
          { title: "09-11", ctL1: 25, ctL2: 60 },
          { title: "09-12", ctL1: 40, ctL2: 60 },
        ],
      ],
      lineDataBinder: [
        { key: "ctL1", displayName: "Cycle Time L1", color: "#FFC107" },
        { key: "ctL2", displayName: "Cycle Time L2", color: "#F86C6B" },
      ],
      axisDataBinder: {
        xAxis: {
          name: "",
        },
        yAxis: {
          name: "Total Part Produced",
        },
      },
      tooltipInfo: `This widget shows the average maintenance duration of the toolings accessible to you on the system, based on the current master filter setting. 
        The maintenance duration is calculated from Corrective and Preventive maintenance reports, and includes a capacity loss due to maintenance.
        `,
    };
  },
};
</script>
