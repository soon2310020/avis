<template>
  <div>
    <div v-if="type === 'CAPACITY'" id="CAPACITY" style="max-width: 50%;" class="card">
      <div class="top-container title-container">
        <div class="line-title"></div>
        <div class="title-top" v-text="resources['capacity_utilization']"></div>
        <span style="font-size: 25px;" aria-hidden="true">&times;</span>
      </div>
      <div style="display: flex;" class="card-body row">
        <div class="chart-wrapper col-sm-6 chart-container">
          <canvas id="chart-preventive" style="height: 320px;"></canvas>
        </div>
      </div>
    </div>
    <div
      v-if="type === 'CYCLE_TIME_STATUS'"
      id="CYCLE_TIME_STATUS"
      style="max-width: 50%;"
      class="card"
    >
      <div class="top-container title-container">
        <div class="line-title"></div>
        <div class="title-top" v-text="resources['cycle_time_status']"></div>
        <span style="font-size: 25px;" aria-hidden="true">&times;</span>
      </div>
      <div class="card-body row">
        <div class="chart-wrapper col-sm-6 chart-container">
          <canvas id="chart-cycle-time-status" style="height: 320px;"></canvas>
        </div>
      </div>
    </div>
    <div v-if="type === 'TOOLING'" id="TOOLING" style="max-width: 50%;" class="card">
      <div class="top-container title-container">
        <div class="line-title"></div>
        <div class="title-top" v-text="resources['tooling_status']"></div>
        <span style="font-size: 25px;" aria-hidden="true">&times;</span>
      </div>
      <div class="card-body">
        <div
          style="align-items: center; display: flex; justify-content: center;"
          class="chart-wrapper"
        >
          <canvas id="chart-overview" style="height: 320px;"></canvas>
        </div>
      </div>
    </div>
    <div
      v-if="type === 'MAINTENANCE_STATUS'"
      id="MAINTENANCE_STATUS"
      style="max-width: 50%;"
      class="card"
    >
      <div class="top-container title-container">
        <div class="line-title"></div>
        <div class="title-top" v-text="resources['preventive_maintenance_status']"></div>
        <span style="font-size: 25px;" aria-hidden="true">&times;</span>
      </div>
      <div class="card-body row">
        <div class="maintenance-item">
          <div class="big-size">
            1
            <div class="maintenance-text" v-text="resources['upcomming']"></div>
          </div>
          <img width="70" height="70" src="/images/icon/timetable.svg" />
        </div>
        <div class="maintenance-item">
          <div class="big-size">
            1
            <div class="maintenance-text" v-text="resources['overdue']"></div>
          </div>
          <img width="70" height="70" src="/images/icon/runningman.svg" />
        </div>
      </div>
    </div>
    <div v-if="type === 'UPTIME_STATUS'" id="UPTIME_STATUS" style="max-width: 50%;" class="card">
      <div class="top-container title-container">
        <div class="line-title"></div>
        <div class="title-top" v-text="resources['uptime_status']"></div>
        <span style="font-size: 25px;" aria-hidden="true">&times;</span>
      </div>
      <div class="card-body row">
        <div class="chart-wrapper col-sm-6 chart-container">
          <canvas id="chart-efficiency-status" style="height: 320px; max-height: 100% !important;"></canvas>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {

  props: {
      type: String,
        resources: Object,
    },
  data() {
    return {};
  },
  methods: {
    checkShow: function(type) {}
  },

  computed: {},
  mounted() {}
};
</script>