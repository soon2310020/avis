<template>
  <tr
    :class="{
      'background-level-1': result.level == 1,
      'background-level-2': result.level > 1,
    }"
  >
    <td class="custom-td">
      <template v-if="listChecked.length >= 0">
        <input
          @click="(e) => check(e, result)"
          :checked="checkSelect(result.id, result.parentId)"
          class="checkbox"
          type="checkbox"
          :id="result.id + 'checkbox'"
          :value="result.id"
        />
      </template>
    </td>
    <template v-for="(column, index) in columnList">
      <td
        v-if="
          (column.enableTemp || (!column.disableTemp && column.enabled)) &&
          !column.hiddenInToggle
        "
        :class="[column.isNumber ? 'text-right' : 'text-left']"
        :key="index"
      >
        <template v-if="column.field == 'name'">
          <div :class="'depth-' + result.level">
            <div style="display: flex; align-items: center">
              <!-- <span v-if="result.level > 1"> ㄴ </span> -->
              <div
                v-if="result.level < 3 && result.children.length > 0"
                class="blue-arrow"
                :class="{ 'blue-arrow-rotate': !isShowingChild }"
                @click="showChildren"
              ></div>
              <div v-else style="width: 9px"></div>
              <a-tooltip
                v-if="result.name.length > 20"
                placement="bottom"
                style="margin-left: 8px"
              >
                <template slot="title">
                  <div style="padding: 6px 8px">
                    <span>{{ result.name }}</span>
                  </div>
                </template>
                <a
                  href="javascript:void(0)"
                  :class="{ 'text-gray text-decoration-none cursor-none': !result.id }"
                  @click="showDetail(result)"
                  >{{ getTruncateText(result.name) }}</a
                >
              </a-tooltip>
              <a
                v-else
                href="javascript:void(0)"
                :class="{ 'text-gray text-decoration-none cursor-none': !result.id }"
                @click="showDetail(result)"
                style="margin-left: 8px"
                >{{ result.name }}</a
              >
            </div>

            <div
              class="small text-muted font-size-11-2"
              style="margin-left: 19px"
            >
              {{ getLevelName }}
            </div>
          </div>
        </template>
        <template
          v-else-if="
            result.level == 1 &&
            ['weeklyProductionDemand'].includes(column.field)
          "
        >
          <span>-</span>
        </template>
        <template
          v-else-if="
            result.level > 1 &&
            ['weeklyProductionDemand'].includes(column.field)
          "
        >
          <span>{{
            result[column.field] || result[column.field] === 0
              ? formatNumber(result[column.field])
              : "-"
          }}</span>
        </template>
        <template
          v-else-if="
            ['moldCount', 'partCount', 'supplierCount'].includes(column.field)
          "
        >
          <span
            :class="{
              inactive: result.level <= 2,
              hyperlink: result.level > 2,
            }"
            @click="column.onClick ? column.onClick(result) : () => {}"
          >
            {{
              result[column.field] || result[column.field] === 0
                ? formatNumber(result[column.field])
                : "-"
            }}
          </span>
        </template>
        <template
          v-else-if="
            [
              'totalMaxCapacity',
              'weeklyMaxCapacity',
              'totalProductionDemand',
            ].includes(column.field)
          "
        >
          <span @click="column.onClick ? column.onClick(result) : () => {}">
            {{
              result[column.field] || result[column.field] === 0
                ? formatNumber(result[column.field])
                : "-"
            }}
          </span>
        </template>
        <template v-else-if="['totalProduced'].includes(column.field)">
          <span
            @mouseover="isHover = true"
            @mouseleave="isHover = false"
            @click="openCustomChartBar()"
            class=""
            style="position: relative"
            :class="{
              hyperlink: result.level > 2,
              inactive: result.level <= 2,
            }"
          >
            {{
              result[column.field] || result[column.field] === 0
                ? formatNumber(result[column.field])
                : "-"
            }}
            <div v-show="!isHover && result.level > 2" class="link-icon">
              <img src="/images/icon/hyperlink-icon.svg" alt="icon" />
            </div>
            <div v-show="isHover && result.level > 2" class="link-icon">
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
              <common-barchart :data="result"></common-barchart>
            </div>
          </span>
        </template>
        <template v-else-if="column.field === 'predictedQuantity'">
          <span>
            {{
              result[column.field] || result[column.field] === 0
                ? formatNumber(result[column.field])
                : "-"
            }}
          </span>
        </template>
        <template v-else-if="column.field === 'deliveryRiskLevel'">
          <span style="white-space: nowrap">
            <span
              v-if="riskLevel"
              :class="riskLevel"
              class="risk-level__icon"
            ></span>
            <span style="white-space: nowrap">
              {{ riskLevelLabel }}
            </span>
          </span>
        </template>
        <template v-else-if="column.isCustomField">
          <span>
            <a-tooltip placement="bottomLeft">
              <template slot="title">
                <span
                  style="display: block; padding: 2px 6px"
                  v-if="
                    customFieldValue(result, column.field) &&
                    customFieldValue(result, column.field).length > 40
                  "
                >
                  {{ customFieldValue(result, column.field) }}
                </span>
              </template>
              <span>{{
                replaceLongtext(customFieldValue(result, column.field), 40)
              }}</span>
            </a-tooltip>
          </span>
        </template>
        <template v-else>
          <span>{{
            result[column.field] !== null && result[column.field] !== undefined
              ? result[column.field]
              : "-"
          }}</span>
        </template>
      </td>
    </template>
  </tr>
</template>

<script>
const riskLevelType = {
  low: "Low Risk",
  medium: "Medium Risk",
  high: "High Risk",
};
module.exports = {
  props: {
    result: Object,
    listChecked: Array,
    checkSelect: Function,
    check: Function,
    showDrowdown: Function,
    changeShow: Function,
    showSystemNoteModal: Function,
    showDetailModal: Function,
    columnList: Array,
    triggerClose: Number,
    isShowingChild: Boolean,
    index: {
      type: [String, Number],
      default: () => "",
    },
    parentIndex: {
      type: [String, Number],
      default: () => null,
    },
  },
  data() {
    return {
      isHover: false,
      isShowCommonBarChart: false,
    };
  },
  components: {
    "common-barchart": httpVueLoader("/components/common-barchart.vue"),
  },
  computed: {
    riskLevel() {
      return this?.result?.deliveryRiskLevel?.toLowerCase() || "";
    },
    riskLevelLabel() {
      return this.riskLevel ? riskLevelType[this.riskLevel] : "-";
    },
    getLevelName() {
      if (!this.result.id) return "";
      else {
        switch (this.result.level) {
          case 1:
            return "Category";
          case 2:
            return "Brand";
          case 3:
            return "Product";
        }
      }
    },
  },
  watch: {
    triggerClose() {
      if (this.isShowCommonBarChart) {
        this.isShowCommonBarChart = false;
      }
    },
  },
  methods: {
    openCustomChartBar() {
      setTimeout(() => {
        this.isShowCommonBarChart = true;
      }, 200);
    },
    closeCommonChartBar() {
      if (this.isShowCommonBarChart) {
        this.isShowCommonBarChart = false;
      }
    },
    getTruncateText(text) {
      return text.substr(0, 20) + "...";
    },
    showRevisionHistory: function (id) {
      console.log(id);
      vm.showRevisionHistory(id);
    },
    showDetail(result) {
      if (result.id) {
        this.showDetailModal(result);
      }
    },
    deletePop(user) {
      if (user.length <= 1) {
        this.deleteCategory(user[0]);
      } else {
        this.deleteBatch(user, true);
      }
    },
    deleteCategory: function (category) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: category.id,
      };
      axios
        .delete("/api/categories/" + category.id, param)
        .then(function (response) {
          if (response.data.success) {
            vm.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    deleteBatch(user) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      const idArr = user.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
      };
      axios
        .post("/api/categories/delete-in-batch", param)
        .then(function (response) {
          if (response.data.success) {
            vm.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error.response);
        })
        .finally(() => {
          this.listChecked = [];
          this.isAll = false;
          this.showDropdown = false;
        });
    },
    formatNumber(num) {
      return num.toString().replace(/\B(?<!\.\d*)(?=(\d{3})+(?!\d))/g, ",");
    },
    showChildren() {
      console.log("showChildren", this.index, this.parentIndex);
      this.$emit("show-children", this.index, this.parentIndex);
    },
  },
  watch: {
    // result(newVal) {
    //   console.log('result', newVal)
    // },
    // columnList(newVal) {
    //   console.log('columnList', newVal)
    // }
  },
};
</script>

<style scoped>
.text-right {
  padding-right: 2%;
  text-align: right;
}
.text-left {
  text-align: left;
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
.text-gray {
  color: #4b4b4b !important;
}
.background-level-1 {
  background-color: #f2f2f2 !important;
}
.background-level-2 {
  background-color: #ffffff !important;
}
</style>