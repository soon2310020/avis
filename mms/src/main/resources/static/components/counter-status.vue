<template>
  <a-tooltip placement="bottomLeft">
    <template slot="title">
      <div class="tooltip-label" v-text="getTooltipCounterStatus(mold.counterStatus)"></div>
    </template>
    <div v-if="mold.counterStatus === 'INSTALLED'" class="counter-status-label counter-installed-label">
      Installed
    </div>
    <div v-if="mold.counterStatus === 'NOT_INSTALLED'" class="counter-status-label counter-not-installed-label">
      Not Installed
    </div>
    <div v-if="mold.counterStatus === 'DETACHED'" class="counter-status-label counter-detached-label">
      Detached
    </div>
  </a-tooltip>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    mold: Object,
  },
  data() {
    return {
    };
  },
  watch: {

  },
  methods: {
    getTooltipCounterStatus(status) {
      switch (status){
        case 'INSTALLED': return 'The sensor has been matched with a tooling.';
        case 'NOT_INSTALLED': return 'The sensor has no matched tooling.';
        case 'DETACHED': return 'The sensor has become physically detached from the tooling.';
        default: return '';
      }
    },
  },
  mounted() {
  }
};
</script>

<style scoped>
.counter-status-label {
  font-size: 11px;
  /*background-color: #D6DADE;*/
  padding: 5px 0px;
  border-radius: 25px;
  text-align: center;
  width: 100px;
}

.counter-installed-label {
  background-color: #D2F8E2;
}

.counter-not-installed-label {
  background-color: #D6DADE;
}
.counter-detached-label {
  background-color:  #FF8489;
}
.tooltip-label {
  padding: 6px 8px;
}
</style>
