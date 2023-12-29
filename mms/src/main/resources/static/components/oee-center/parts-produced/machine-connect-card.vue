<template>
  <div class="machine-connect-container">
    <div class="d-flex" style="font-size: 12px;">
      <!-- <div style="margin-right: 15px">2022-10-15</div>
      <div>09:09</div> -->
      <div>{{connectionInfor.timeStr}}</div>
    </div>
    <div class="d-flex justify-space-between align-center">
      <div class="text-left">
        <div>
          <strong>Machine ID</strong>
        </div>
        <div>{{connectionInfor.machineCode}}</div>
      </div>
      <div class="machine-connect-plug-icon" :class="{'connected' : connectionInfor.status === 'MATCHED', 'dis-connected' : connectionInfor.status === 'UNMATCHED'}">
      </div>
      <div class="text-left">
        <div>
          <strong>Tooling ID</strong>
        </div>
        <div>{{connectionInfor.moldCode}}</div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    connectionInfor: {
      type: Object,
      default: () => ({})
    },
  },
  setup(props) {
    const getTimeDisplay  = computed(() => {
      if (props.connectionInfor.time) {
        const time = Common.convertTimestampToDate(props.connectionInfor.time)
        const day = moment(time).format('YYYY-MM-DD     hh:mm')
        return day
      }
    })

    return {
      getTimeDisplay
    }
  }
}
</script>

<style scoped>
.machine-connect-container{
  width: 295px;
  padding-bottom: 15px;
  font-size: 13px;
}
.machine-connect-container:not(:last-child){
  border-bottom: 1px solid #c8ced3;
  margin-bottom: 10px;
}
.machine-connect-plug-icon.connected{
  height: 17px;
  width: 32px;
  background-image: url("/images/icon/oee/plug-connected-icon.svg");
}
.machine-connect-plug-icon.dis-connected{
  height: 17px;
  width: 52px;
  background-image: url("/images/icon/oee/plug-disconnected-icon.svg");
}
</style>