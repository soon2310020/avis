<template>
  <div class="product-statistic">
    <table>
      <tr
        v-for="(field, index) in fields"
        :key="index"
      >
        <td>
          <span>{{ field.title }}</span>
        </td>
        <td v-if="field.field === 'totalProduced'" :class="[field.isNumber ? 'text-right' : field.isCenter ? 'text-center' : 'text-left']">
          <span class="hyperlink" @click="openCustomChartBar()" style="color: #3491ff; position: relative;" @mouseover="isHover = true"
            @mouseleave="isHover = false">
            {{ renderNumberData(project[field.field]) }}
            <div v-show="!isHover" class="link-icon1">
              <img src="/images/icon/hyperlink-icon.svg" alt="icon" />
            </div>
            <div v-show="isHover" class="link-icon1">
              <img src="/images/icon/hyperlink-icon-hover.svg" alt="icon" />
            </div>
            <div
                v-if="isShowCommonBarChart"
                v-click-outside="closeCommonChartBar"
                class="modal-custom-barchart"
            >
              <div
                  class="icon-wrapper"
                  @click.stop="isShowCommonBarChart = false"
                  aria-hidden="true"
              >
                <span class="t-icon-close"></span>
              </div>
              <common-barchart :data="project"></common-barchart>
            </div>
          </span>
        </td>
        <td v-else-if="field.field === 'deliveryRiskLevel'" :class="[field.isNumber ? 'text-right' : field.isCenter ? 'text-center' : 'text-left']">
          <span>
            <span
              v-if="riskLevel"
              :class="riskLevel"
              class="risk-level__icon"
            ></span>
            {{riskLevelLabel}}
          </span>
        </td>
        <td v-else :class="[field.isNumber ? 'text-right' : field.isCenter ? 'text-center' : 'text-left']">
          <span>
            {{ renderNumberData(project[field.field]) }}
          </span>
        </td>
      </tr>
    </table>
  </div>
</template>

<script>
const riskLevelType = {
  'low': 'Low Risk',
  'medium': 'Medium Risk',
  'high': 'High Risk'
}

module.exports = {
  props: {
    resources: Object,
    project: {
      type: Object,
      default: {
        id: null,
        // productionDemand: null,
        molds: [],
        name: "",
        parts: [],
        projectImage: null,
        suppliers: [],
        totalProduced: 0,
        predictedQuantity: 0,
        totalProductionDemand: 0,
        totalMaxCapacity: 0,
        deliveryRiskLevel: ''
      },
    },
    fields: {
      type: Array,
      default: () => [],
    },
  },
  components: {
    "common-barchart": httpVueLoader("/components/common-barchart.vue"),
  },
  data() {
    return {
      isHover: false,
      isShowCommonBarChart: false
    }
  },
  computed: {
    riskLevel() {
      return this?.project?.deliveryRiskLevel?.toLowerCase() || ''
    },
    riskLevelLabel() {
      return this.riskLevel ? riskLevelType[this.riskLevel] : '-'
    }
  },
  methods: {
    openCustomChartBar() {
      setTimeout(() => {
        this.isShowCommonBarChart = true;
      }, 200)
    },
    closeCommonChartBar() {
      if (this.isShowCommonBarChart) {
        this.isShowCommonBarChart = false;
      }
    },
    renderNumberData(val) {
      return val || val === 0 ? Common.formatNumber(val) : '-'
    },
  },
  watch: {
    project(newVal) {
      console.log('@product-statistic ==== project', newVal)
    },
    fields(newVal) {
      console.log('@product-statistic ==== fields', newVal)
    }
  }
};
</script>

<style scoped>
.product-statistic {
  width: 390px;
  padding: 10px 0;
}
.product-statistic table {
  width: 100%;
}
.product-statistic td {
  height: 40px;
  font-size: 16px;
  padding: 0 10px;
  border: 1px solid #d6dade;
  font-weight: 500;
  color: #595959;
}
.product-statistic td:last-child {
  width: 25%;
  text-align: center;
}

td span {
  font-size: 14px;
  white-space: nowrap;
  position: relative;
}

td:first-child span {
    font-weight: bold;
}

td span img {
    position: absolute;
    left: calc(100% + 4px);
}
.icon-wrapper {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  height: 15px;
}
.t-icon-close {
  width: 8px;
  height: 8px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

</style>