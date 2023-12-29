<template>
  <common-card card-style="style-2" title="OEE">
    <div class="progress-wrap">
      <div
        class="progress-box"
        v-for="(item, index) of progressList"
        :key="index"
      >
        <div>{{ item.title }}</div>
        <div>
          <div class="oee-progress-wrapper">
            <div
              class="oee-progress"
              :class="{ 'low-value': item.percent < 10 }"
              :style="{
                paddingLeft: item.percent < 10 ? item.percent + '%' : 0,
                width: item.percent + '%',
                backgroundColor: percentColor(item.percent, item.key),
              }"
            >
              <b>{{ item.percent + "%" }}</b>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="legend-oee">
      <div class="legend-custom-item">
        <div class="legend-box high"></div>
        <span><span v-text="resources['high_risk']"></span></span>
      </div>
      <div class="legend-custom-item">
        <div class="legend-box medium"></div>
        <span><span v-text="resources['medium_risk']"></span></span>
      </div>
      <div class="legend-custom-item">
        <div class="legend-box low"></div>
        <span><span v-text="resources['low_risk']"></span></span>
      </div>
    </div>
  </common-card>
</template>

<script>
module.exports = {
  name: "OeeProgressCard",
  props: {
    resources: Object,
    progressList: {
      default: () => [
        { percent: 0 },
        { percent: 0 },
        { percent: 0 },
        { percent: 0 },
      ],
    },
    percentColor: Function,
  },
  setup(props) {
    return {};
  },
};
</script>
