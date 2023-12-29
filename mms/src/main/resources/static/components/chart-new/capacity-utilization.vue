<template>
  <div id="CAPACITY" :class="className" class="card card-customize capacity">
    <div class="top-container title-container">
      <div class="line-title"></div>
      <div style="display: flex; justify-content: space-between; align-items: center;">
      <div style="margin-left: 35px;" class="title-top" v-text="resources['capacity_utilization']"></div>
      <a-tooltip placement="bottom">
        <template slot="title">
          <div style="padding: 6px;font-size: 13px;" v-text="resources['capacity_utilization_description']">

          </div>
        </template>
        <div style="margin-left: 10px">
          <img src="/images/icon/tooltip_info.svg" width="15" height="15"/>
        </div>
      </a-tooltip>
      </div>
      <span
        class="close-button"
        v-on:click="dimiss('CAPACITY', 'Capacity Utilization')"
        style="font-size: 25px;"
        aria-hidden="true"
      >&times;</span>
    </div>
      <div class="button-wapper">
        <div
          v-for="item in timeData"
          :key="item.id"
          :style="{ background: timeId == item.id ? '#fec44e' : '#dfe4ec'}"
          v-on:click="searchData(item.startTime, item.endTime, item.id)"
          class="gray-button"
        >{{ item.title }}</div>
      </div>
    <div style="max-width: 550px; width: 90%;" class="card-body body capacity-wapper">
      <div  style="cursor: pointer;" class="row-wapper">
        <template v-for="item in chartData.filter((value, index)=> index <2)">
          <a-tooltip :key="item.id">
            <template slot="title">
              <div style="display: flex; align-items: center; flex-wrap: wrap;">
                <div
                  style="margin-right: 5px;"
                  :style="{backgroundColor: item.style.backgroundColor, height: '25px', width: '25px', border: '1px solid #fff'}"
                ></div>
                <div>{{item.title}}: {{item.value}}({{Math.round(Number(item.percent))}}%)</div>
              </div>
            </template>
            <div :style="item.style" class="pie-wapper">
              <div id="element">
                <div class="title">{{item.title}}</div>
                <div class="percent">{{Math.round(Number(item.percent))}}%</div>
              </div>
            </div>
          </a-tooltip>
        </template>
      </div>
      <div style="cursor: pointer;" class="row-wapper">
        <template v-for="item in chartData.filter((value, index)=> index >=2)">
          <a-tooltip :key="item.id">
            <template slot="title">
              <div style="display: flex; align-items: center; flex-wrap: wrap;">
                <div
                  style="margin-right: 5px;"
                  :style="{backgroundColor: item.style.backgroundColor, height: '25px', width: '25px', border: '1px solid #fff'}"
                ></div>
                <div>{{item.title}}: {{item.value}}({{Math.round(Number(item.percent))}}%)</div>
              </div>
            </template>
            <div :style="item.style" class="pie-wapper">
              <div id="element">
                <div class="title">{{item.title}}</div>
                <div class="percent">{{Math.round(Number(item.percent))}}%</div>
              </div>
            </div>
          </a-tooltip>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    type: String,
    data: Array,
    dimiss: Function,
    filterQuery: Object,
    className: String
  },
  data() {
    return {
      resources: this.data.resources,
      chartData: [
        {
          id: 1,
          value: 0,
          percent: 0,
          title: this.data.resources['frequently'],
          style: {
            backgroundColor: "#6fb97d",
            borderTopLeftRadius: "12px"
          }
        },
        {
          id: 2,
          value: 0,
          percent: 0,
          style: {
            backgroundColor: "#f7cc53",
            borderTopRightRadius: "12px"
          },
          title: this.data.resources['occasionally']
        },
        {
          id: 3,
          value: 0,
          percent: 0,
          style: {
            backgroundColor: "#f67576",
            borderBottomLeftRadius: "12px"
          },
          title: this.data.resources['rarely']
        },
        {
          id: 4,
          value: 0,
          percent: 0,
          style: {
            backgroundColor: "#808080",
            borderBottomRightRadius: "12px"
          },
          title: this.data.resources['never']
        }
      ],
      timeData: [
        {
          id: 1,
          startTime: "",
          endTime: "",
          title: this.data.resources['all_time']
        },
        {
          id: 2,
          startTime: moment()
            .subtract(3, "months")
            .unix(),
          endTime: moment().unix(),
          title: "3 " + this.data.resources['months']
        },
        {
          id: 3,
          startTime: moment()
            .subtract(6, "months")
            .unix(),
          endTime: moment().unix(),
          title: "6 " + this.data.resources['months']
        },
        {
          id: 4,
          startTime: moment()
            .subtract(9, "months")
            .unix(),
          endTime: moment().unix(),
          title: "9 " + this.data.resources['months']
        },
        {
          id: 5,
          startTime: moment()
            .subtract(12, "months")
            .unix(),
          endTime: moment().unix(),
          title: "12 " + this.data.resources['months']
        }
      ],
      timeId: 1
    };
  },
  methods: {
    goToLandingPage: function() {
      location.href = `/reports/tooling-benchmarking#CU`;
    },
    handleChartData: function(data) {
      console.log("capacity: data: ", data);
      let sum = 0;
      let active = 0,
        idle = 0,
        inactive = 0,
        disconnected = 0;

      data.forEach(item => {
        sum += item.data;
      });

      if (sum == 0) {
        return;
      }

      this.chartData = this.chartData.map((item, index) => {
        item.value = data[index] ? data[index].data : 0;
        item.percent = data[index] ? Number(data[index].data / sum) * 100 : 0;
        return item;
      });
    },

    searchData: function(startTime, endTime, timeId) {
      const { locationIds, supplierIds, toolMakerIds } = this.filterQuery;
      this.startTime = startTime;
      this.endTime = endTime;
      this.timeId = timeId;
      let url = ` /api/chart/graph-capacity-utilization`;
      if (this.startTime) {
        url = url +  "?startTime=" + startTime;
      }
      if (this.endTime) {
        url = url + (url.includes("?") ? "&" : "?") + "endTime=" + this.endTime;
      }

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
        .then(response => {
          if (response.data) {
            console.log("this.response.datacapacity: ", response.data);
            this.handleChartData(response.data);
          }
        })
        .catch(function(error) {
          console.log(error.response);
        });
    },
    async loadChange(){
      this.searchData(this.startTime, this.endTime, this.timeId);
    }
  },

  watch: {
    data: function(newVal) {
      this.handleChartData(newVal);
    },
    //locationIds, supplierIds, toolMakerIds
    "filterQuery.locationIds": function(newValue) {
      // this.searchData(this.startTime, this.endTime, this.timeId);
    },

    "filterQuery.supplierIds": function(newValue) {
      // this.searchData(this.startTime, this.endTime, this.timeId);
    },

    "filterQuery.toolMakerIds": function(newValue) {
      // this.searchData(this.startTime, this.endTime, this.timeId);
    }
  },

  computed: {},
  mounted() {
    this.handleChartData(this.data);
  }
};
</script>

<style scoped>
.text {
  color: #000;
  font-weight: 600;
  font-size: 12px;
  margin-top: 20px;
  padding-left: 10px;
  margin-bottom: 0px;
  width: 100%;
}

.capacity-wapper {
  flex-direction: column;
  /* min-height: 450px; */
  align-items: center;
  justify-content: center;
  padding: 15px 5px;
}

.chart-child {
  display: flex;
  width: 100%;
  align-items: center;
  margin-bottom: 20px;
}
.button-wapper {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 10px !important;
}
.gray-button {
  min-width: 50px;
  width: calc(20% - 5px);
  padding: 5px 0px;
  background: #f2f5fa;
  margin: 0px 5px 10px 0px;
  display: flex;
  font-size: 10px;
  cursor: pointer;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.row-wapper {
  display: flex;
  width: 100%;
  justify-content: space-between;
  align-items: center;
}
.pie-wapper {
  width: calc(50% - 1px);
  margin: 0px 1px 1px 0px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #fff;
}
.title {
  position: absolute;
  top: 7px;
  font-size: 12px;
}
.percent {
  position: absolute;
  font-weight: 600;
  font-size: 30px;
  position: absolute;
  top: calc(50% - 17px);
}

#element {
  display: 0;
  width: 99%;
  padding-bottom: 100%;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-item {
  height: 30px;
  width: 30px;
  border: 2px solid #fff;
}

#CAPACITY {
  display: flex;
  align-items: center;
}
</style>