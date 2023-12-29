<template>
  <div id="OEE" :class="className" class="card card-customize">
    <div class="top-container title-container">
      <div class="line-title"></div>
      <div style="display: flex; justify-content: space-between; align-items: center;">
      <div style="margin-left: 35px;" class="title-top" v-text="resources['overall_equipment_effectiveness']"></div>
          <a-tooltip placement="bottom">
        <template slot="title">
          <div style="padding: 6px;font-size: 13px;" v-text="resources['overall_equipment_effectiveness_description']">

          </div>
        </template>
        <div style="margin-left: 10px">
          <img src="/images/icon/tooltip_info.svg" width="15" height="15"/>
        </div>
      </a-tooltip>
      </div>
      <span
        class="close-button"
        v-on:click="dimiss('OEE', 'Overall Equipment Effectiveness')"
        style="font-size: 25px;"
        aria-hidden="true"
      >&times;</span>
    </div>
    <div
      style="display: flex;
      align-items: center;
      padding: 0px;
      flex-direction: column;"
      class="card-body"
    >
      <div class="dropdown-container">
        <a-popover v-model="visible" placement="bottom" trigger="click">
          <a-button
            style="width: 108px; margin-right: 10px; padding:8px 8px;"
            class="dropdown_button"
          >
            {{titleShow()}}
            <a-icon type="caret-down" />
          </a-button>
          <div
            slot="content"
            style="padding: 8px 6px; max-height: 300px; overflow-y: scroll;"
            class="dropdown-scroll"
          >
            <template v-for="item in filtered">
              <a-col style="padding: 7px;" :key="item.key">
                <p-check :checked="checked(item)" @change="onChange($event, item)">{{item.title}}</p-check>
              </a-col>
            </template>
          </div>
        </a-popover>
      </div>
      <div
        style=" margin-bottom: 20px; padding: 0px 2px; right: 0px; width: 100%; max-width: 500px; display: flex;align-items: center;justify-content: center;"
        class="canvas-container"
      >
        <canvas style="min-height: 185px; max-width: 100%;" id="chart_effectiveness"></canvas>
        <div  style="cursor: pointer;" class="percent">
          <div>{{dataShow}}%</div>
          <div style="line-height: 20px; margin-right: 15px;" v-text="resources['oee']"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
var OPTIONS = [
  {
    key: "WORKING",
    //title: this.data.resources['active']
    title: 'Active'
  },
  {
    key: "IDLE",
    //title: this.data.resources['idle']
    title: 'Idle'
  },
  {
    key: "NOT_WORKING",
    //title: this.data.resources['inactive']
    title: 'Inactive'
  },
  {
    key: "DISCONNECTED",
    //title: this.data.resources['disconnected']
    title: 'Disconnected'
  }
];
module.exports = {
  props: {
    type: String,
    dimiss: Function,
    filterQuery: Object,
    data: Array,
    className: String,
  },
  data() {
    return {
      resources: this.data.resources,
      op: "",
      frequent: "",
      chart: null,
      dataShow: 0,
      visible: false,
      checkedList: OPTIONS,
      oldCheckedList: OPTIONS,
      filtered: OPTIONS
    };
  },

  methods: {
    titleShow: function() {
      if (this.checkedList.length == this.filtered.length) {
        return this.resources['dashboard_all_statuses'];
      }
      if(this.checkedList.length > 1){
        return `${this.checkedList.length}` + ' ' + this.resources['statuses'];;
      }else{
        return `${this.checkedList.length}` + ' ' + this.resources['dashboard_status'];;
      }
    },

    checked: function(item) {
      console.log("this.checkedList: ", this.checkedList);
      const findIndex = this.checkedList.findIndex(
        value => value.key == item.key
      );
      return findIndex !== -1;
    },

    onMouseleave: function() {
      if (this.checkedList.toString() == this.oldCheckedList.toString()) {
        return;
      }
      this.oldCheckedList = this.checkedList;
      this.getOEEData(this.checkedList);
    },

    onChange: function(isChecked, item) {
      if (isChecked) {
        this.checkedList = this.checkedList.concat(item);
      } else {
        this.checkedList = this.checkedList.filter(
          value => value.key !== item.key
        );
      }
    },

    goToLandingPage: function() {
      location.href = `/reports/tooling-benchmarking#OEE`;
    },

    drawNeedle: function(radius, radianAngle) {
      var canvas = document.getElementById("chart_effectiveness");
      if(!canvas)
        return;
      var ctx = canvas.getContext("2d");
      var cw = canvas.offsetWidth;
      var ch = canvas.offsetHeight;
      var cx = cw / 2;
      var cy = ch - ch / 3.8;
      ctx.translate(cx, cy);
      ctx.rotate(radianAngle);
      ctx.beginPath();
      ctx.moveTo(0, -5);
      ctx.lineTo(radius, 0);
      ctx.lineTo(0, 5);
      ctx.fillStyle = "#000";
      ctx.fill();
      ctx.rotate(-radianAngle);
      ctx.translate(-cx, -cy);
      ctx.beginPath();
      ctx.arc(cx, cy, 10, 0, Math.PI * 2);
      ctx.fill();
    },

    getOEEData: function(checkedList) {
      const { locationIds, supplierIds, toolMakerIds } = this.filterQuery;
      this.checkedList = checkedList;
      let url = `/api/chart/graph-oee?ops=${checkedList
        .map(value => value.key)
        .toString()}`;
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
            console.log("this.response.data:OEE ", response.data);
            if (response.data.length > 0) {
              this.dataShow = response.data[0].data;
              this.updateChart(0.012 * response.data[0].data - 1.2 + 0.1);
            }
          }
        })
        .catch(function(error) {
          console.log(error.response);
        });
    },

    updateChart: function(radius) {
      var sef = this;
      this.chart = new Chart($("#chart_effectiveness"), {
        type: "doughnut",
        data: {
          labels: ["0", "", "30", "", "70", "", "100"],
          datasets: [
            {
              data: [0.1, 30, 0.1, 40, 0.1, 30, 0.1],
              fill: false,
              label: "Uptime",
              borderWidth: 0,
              borderColor: [
                "#f67576",
                "#f67576",
                "#f7cc53",
                "#f7cc53",
                "#6fb97d",
                "#6fb97d",
                "#fff"
              ],
              backgroundColor: [
                "#f67576",
                "#f67576",
                "#f7cc53",
                "#f7cc53",
                "#6fb97d",
                "#6fb97d",
                "#fff"
              ]
            }
          ]
        },
        plugins: [
          {
            afterDraw: function(chart) {
              sef.drawNeedle(120, radius * Math.PI);
            }
          }
        ],
        options: {
          plugins: {
            labels: {
              render: "label",
              arc: false,
              position: "outside",
              fontSize: 13,
              outsidePadding: 4,
              textMargin: 4,
            }
          },
          title: {
            display: true
          },
          legend: {
            display: false,
            position: "bottom",
            align: "center"
          },
          elements: {
            arc: {
              borderWidth: 0
            }
          },
          tooltips: {
            enabled: false
          },
          responsive: true,
          maintainAspectRatio: false,
          segmentShowStroke: false,
          circumference: 1.3 * Math.PI,
          rotation: 0.85 * Math.PI
        }
      });
    }
  },

  watch: {
    //locationIds, supplierIds, toolMakerIds
    "filterQuery.locationIds": function(newValue) {
      this.getOEEData(this.checkedList);
    },

    "filterQuery.supplierIds": function(newValue) {
      this.getOEEData(this.checkedList);
    },

    "filterQuery.toolMakerIds": function(newValue) {
      this.getOEEData(this.checkedList);
    },

    visible: function(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.onMouseleave();
      }
    }
  },

  computed: {},
  mounted() {
    this.getOEEData(this.checkedList);
    window.addEventListener("resize", e => {
      if(this.chart){
        this.chart.resize();
        this.chart.update();
      }
      
    });
  }
};
</script>

<style scoped>
.dropdown-container {
  display: flex;
  flex-wrap: wrap;
  width: 100%;
  margin-top: 20px;
  justify-content: flex-end;
}

.dropdown_button {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 90px;
  height: 27px;
  margin-bottom: 2px;
  padding: 0px 2px;
  background: #f2f5fa;
}
.canvas-container {
  display: flex;
  position: relative;
  justify-content: center;
  align-items: center;
}

.percent {
  color: #000;
  position: absolute;
  bottom: -10px;
  font-size: 25px;
  left: calc(50% - 25px);
  font-weight: 700;
  text-align: center;
}

@media (max-width: 1280px) {
  #chart_effectiveness {
    min-height: 200px;
  }
}

/* @media (max-width: 900px) {
  #chart_effectiveness {
    max-width: 150px;
  }
} */

@media (min-width: 576px) {
  #chart_effectiveness {
    min-height: 200px;
  }
}

@media (min-width: 1800px) {
  #chart_effectiveness {
    min-height: 300px;
  }
}

@media (max-width: 576px) {
  #chart_effectiveness {
    min-height: 230px;
  }
}
</style>
