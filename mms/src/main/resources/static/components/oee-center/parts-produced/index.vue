<template>
  <div>
    <div
      class="d-flex justify-end align-center"
      style="
        margin: -35px 0 20px;
        position: relative;
        width: fit-content;
        float: right;
      "
    >
      <div v-if="!colorIndicatorHidden">
        <label for="" style="margin: 0 8px">{{
          resources["color_indicator_by"]
        }}</label>
        <base-button type="dropdown" @click="toggleColorIndicator">
          {{ selectedColorType.title }}
        </base-button>
        <common-popover
          @close="closeColorIndicator"
          :is-visible="visibleColorIndicator"
          :style="{ width: '210px', marginTop: '4px', top: '100%' }"
        >
          <common-select
            :style="{ position: 'static', width: '100%' }"
            :items="colorIndicatorTypes"
            :multiple="false"
            :has-toggled-all="false"
            @close="closeColorIndicator"
            @on-select="handleSelectColorIndicator"
          >
          </common-select>
        </common-popover>
      </div>
      <div style="margin-left: 8px">
        <customization-modal
          :all-columns-list="tableConfig"
          @save="handleSaveColConfig"
          :resources="resources"
          :max-columns="7"
        ></customization-modal>
      </div>
    </div>
    <div
      v-if="!hasData"
      class="no-results w-100"
      v-text="resources['no_results']"
    ></div>
    <div
      v-else
      style="border: 1px solid #c8ced3; border-radius: 0.25rem; padding: 10px"
    >
      <div class="part-produced-small">
        <couple-table
          :data-list="listPartsProduced"
          :columns="listDisplayedColumns"
          :show-shift-progress="showShiftProgress"
          :list-displayed-shifts="listDisplayedShifts"
          :get-risk-color="getRiskColor"
          :percent-color="percentColor"
          @show-machine-details="(machineId) => showMachineDetails(machineId)"
          @shift-click="handleClickShiftTable"
        >
        </couple-table>
      </div>
      <div
        class="part-produced-large"
        style="justify-content: space-between; padding: 10px"
      >
        <parts-produced-table
          :data-list="listPartsProduced"
          :columns="listDisplayedColumns"
          :percent-color="percentColor"
          @show-machine-details="(machineId) => showMachineDetails(machineId)"
        ></parts-produced-table>
        <shift-table
          style="margin-top: 5px"
          :number-of-shift="listPartsProduced[0].numberOfShift"
          :list-displayed-shifts="listDisplayedShifts"
          :data-list="listPartsProduced"
          :show-shift-progress="showShiftProgress"
          :get-risk-color="getRiskColor"
          :raw-shifts="rawShifts"
          @click="handleClickShiftTable"
        >
        </shift-table>
      </div>
      <div class="parts-produced-paging d-flex">
        <div class="modal-body__content__footer">
          <span>{{
            `${pagination.pageNumber} of ${pagination.totalPages}`
          }}</span>
        </div>
        <div class="paging__arrow">
          <a
            href="javascript:void(0)"
            class="paging-button"
            :class="{ 'inactive-button': pagination.pageNumber <= 1 }"
            @click="changePage('-')"
          >
            <img src="/images/icon/category/paging-arrow.svg" alt="" />
          </a>
          <a
            href="javascript:void(0)"
            class="paging-button"
            @click="changePage('+')"
            :class="{
              'inactive-button':
                pagination.pageNumber === pagination.totalPages,
            }"
          >
            <img
              src="/images/icon/category/paging-arrow.svg"
              style="transform: rotate(180deg)"
              alt=""
            />
          </a>
        </div>
      </div>
    </div>
    <reject-rate-modal
      ref="rejectRateModalRef"
      type="machine"
      :resources="resources"
      :is-visible="showRejectRateModal"
      :current-date="filter.currentDate"
      :frequency="filter.frequency"
      :on-paging="handlePaging"
      :machine-code="selectedMachineCode"
      :hour="selectedHour"
      @update-done="updateDataDone"
      @on-success="reloadData"
    ></reject-rate-modal>
    <toast-alert
      @close-toast="closeSuccessToast"
      :show="toastAlert.isShow"
      :ref-custom="'parts-produced'"
      :title="toastAlert.title"
      :content="toastAlert.content"
    ></toast-alert>
  </div>
</template>

<script>
const partProducedInstance = {
  machineCode: "",
  producedPart: 0,
  fq: 0,
  fp: 0,
  fa: 0,
  oee: 0,
  numberOfShift: 0,
};

const minutes = 10;
const timeStep = minutes * 60 * 1000;

module.exports = {
  props: {
    resources: Object,
    filter: Object,
    fetchData: Function,
    listDisplayedShifts: Array,
    listPartsProduced: Array,
    getRiskColor: Function,
    rawShifts: Array,
    pagination: {
      type: Object,
      default: () => ({
        pageNumber: 1,
        totalPages: 1,
      }),
    },
    permissions: {
      type: Object,
      default: () => ({}),
    },
    detailPermissions: {
      type: Object,
      default: () => ({}),
    },
    colorIndicatorTypes: {
      type: Array,
      default: () => [],
    },
    selectedColorType: {
      type: Object,
      default: () => ({}),
    },
    percentColor: Function,
    colorIndicatorHidden: Boolean,
  },
  components: {
    "parts-produced-table": httpVueLoader(
      "/components/oee-center/parts-produced/parts-produced-table.vue"
    ),
    "shift-table": httpVueLoader(
      "/components/oee-center/parts-produced/shift-table.vue"
    ),
    "couple-table": httpVueLoader(
      "/components/oee-center/parts-produced/couple-table.vue"
    ),
    "reject-rate-modal": httpVueLoader(
      "/components/reject-rates/reject-rate-modal.vue"
    ),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
  },
  data() {
    return {
      isLoading: false,
      pageType: "OEE_PART_PRODUCED",
      listPartsProduced: [partProducedInstance],
      // partsProducedFields: [],
      getNotifyInterval: null,
      startingTime: "",
      counter: 0,
      tableConfig: [
        {
          enabled: true,
          label: this.resources["machine_id"],
          field: "machineCode",
          isHyperLink: true,
          textLeft: true,
          formatter: (val) => val,
          default: true,
          defaultSelected: true,
          defaultPosition: 0,
          mandatory: true,
        },
        {
          label: this.resources["good_production"],
          field: "goodProduction",
          isHyperLink: false,
          textLeft: false,
          formatter: Common.formatter.appendThousandSeperator,
          default: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["produced"],
          field: "totalPartProduced",
          isHyperLink: false,
          textLeft: true,
          formatter: Common.formatter.appendThousandSeperator,
          default: true,
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["exp_hr_output"],
          field: "expectedHourlyProduction",
          isHyperLink: false,
          textLeft: true,
          default: true,
          defaultSelected: true,
          defaultPosition: 3,
        },
        {
          label: this.resources["performance"],
          field: "fp",
          isHyperLink: false,
          textLeft: true,
          formatter: Common.formatter.appendPercent,
          default: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["yield"],
          field: "fq",
          isHyperLink: false,
          textLeft: true,
          formatter: Common.formatter.appendPercent,
          default: true,
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["availability"],
          field: "fa",
          isHyperLink: false,
          textLeft: true,
          formatter: Common.formatter.appendPercent,
          default: true,
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: this.resources["oee"],
          field: "oee",
          isHyperLink: false,
          textLeft: true,
          formatter: Common.formatter.appendPercent,
          default: false,
          defaultSelected: false,
        },
      ],
      showRejectRateModal: false,
      toastAlert: {
        isShow: false,
        title: "Success!",
        content: "",
      },
      selectedMachineCode: "",
      selectedHour: {},
      visibleColorIndicator: false,
    };
  },
  computed: {
    delayTime() {
      return moment(this.startingTime).format("DD-MM-YYYY HH:mm");
    },
    hasData() {
      return this?.listPartsProduced?.length > 0;
    },
    showShiftProgress() {
      return true;
    },
    canInputRejectParts() {
      return this.permissions?.items?.btnInputRejectParts?.permitted;
    },
    listDisplayedColumns() {
      console.log("tableConfig", this.tableConfig);
      console.log(
        "listDisplayedColumns",
        this.tableConfig.filter((col) => col.enabled ?? col.default)
      );
      return this.tableConfig.filter((col) => col.enabled ?? col.default);
    },
  },
  methods: {
    async getPartProducedData(params) {
      try {
        await this.fetchData(params);
      } catch (error) {
        console.log(error);
      }
    },
    async changePage(sign) {
      let page = this.pagination.pageNumber;
      if (sign == "+") {
        page += 1;
      } else if (sign == "-") {
        page -= 1;
      }
      await this.getPartProducedData({ page });
    },
    showMachineDetails(machineId) {
      this.$emit("show-machine-details", machineId);
    },
    async openRejectRateModal(params) {
      this.$refs.rejectRateModalRef.openRejectRateModal(params);
    },
    handlePaging(page) {
      console.log(page);
    },
    async fetchRejectedPartByMachine(params) {
      const queryParams = Common.param({
        machineId: params?.machineId,
        hour: params?.hour,
        ...params,
      });
      return await axios.get(
        "/api/rejected-part/get-by-hour-and-machine?" + queryParams
      );
    },
    updateDataDone() {
      console.log("@updateDataDone");
    },
    reloadData(machineId, hour) {
      console.log("on success", machineId, hour);
      const page = this.pagination.pageNumber;
      this.toastAlert.content = `${machineId} reject part entry has been completed for ${hour.from}:00 - ${hour.to}:00.`;
      this.toastAlert.isShow = true;
      setTimeout(() => {
        this.toastAlert.isShow = false;
      }, 3000);
      this.getPartProducedData(page);
    },
    async handleClickShiftTable(event, shiftDay, shiftHour) {
      console.log("canInputRejectParts", this.canInputRejectParts);
      if (this.canInputRejectParts) {
        console.log("@handleClickShiftTable", shiftDay, shiftHour);
        this.selectedMachineCode = shiftDay.machineCode;
        this.selectedHour = {
          from: shiftHour.start.slice(0, 2),
          to: shiftHour.end.slice(0, 2),
        };
        let time;
        let date = "";
        const currentDay = moment(this.filter?.currentDate?.from);
        const isToday =
          Number(shiftDay?.start?.slice(0, 2)) <=
          Number(shiftHour?.start?.slice(0, 2));
        if (!isToday) {
          date = moment(currentDay).add(1, "d");
        } else {
          date = currentDay;
        }
        time =
          Common.formatter.numberOnly(date.format(Common.dateFns.dateFormat)) +
          shiftHour?.start?.slice(0, 2);
        const machineId = shiftDay?.machineId;
        const dateRange =
          date.format(Common.dateFns.dateFormat) +
          " to " +
          date.format(Common.dateFns.dateFormat);
        const timeRange = `${shiftHour.start.slice(
          0,
          2
        )}:${shiftHour.start.slice(2, 4)} to ${shiftHour.end.slice(
          0,
          2
        )}:${shiftHour.end.slice(2, 4)}`;

        if (shiftHour.oee || shiftHour.oee === 0) {
          const response = await this.fetchRejectedPartByMachine({
            hour: time,
            machineId,
          });
          if (!response?.data?.data) return;
          const rejectRateMachine = {
            ...response.data.data,
            machineId,
            machineCode: shiftDay?.machineCode,
            dateRange: dateRange,
            timeRange: timeRange,
            totalPartProduced: shiftHour.partProduced,
          };
          this.openRejectRateModal([rejectRateMachine]);
        }
      }
    },
    closeSuccessToast() {
      this.toastAlert.isShow = false;
    },
    toggleColorIndicator() {
      this.visibleColorIndicator = !this.visibleColorIndicator;
    },
    closeColorIndicator() {
      this.visibleColorIndicator = false;
    },
    handleSelectColorIndicator(item) {
      this.$emit("change-color-config", item);
      this.closeColorIndicator();
    },
    async fetchColumnConfig() {
      try {
        const response = await axios.get(
          `/api/config/column-config?pageType=${this.pageType}`
        );
        console.log("@getColumnConfig");
        if (!response?.data?.length) throw "No data";
        const hashedByColumnName = {};
        response.data.forEach((item) => {
          hashedByColumnName[item.columnName] = item;
        });
        this.tableConfig.forEach((item) => {
          if (hashedByColumnName[item.field]) {
            item.enabled = hashedByColumnName[item.field].enabled;
            item.position = hashedByColumnName[item.field].position;
          }
        });
        this.tableConfig.sort((a, b) => a.position - b.position);
        // this.tableConfig = this.tableConfig.map(i => ({ ...i, label: this.resources[i.label] ?? i.label }))
        console.log("@fetchColumnConfig>this.tableConfig", this.tableConfig);
      } catch (error) {
        console.log(error);
      } finally {
        this.$forceUpdate();
      }
    },
    async handleSaveColConfig(dataCustomize, list) {
      console.log("@handleSaveColConfig", dataCustomize);

      // data config for saving in DB
      const data = list.map((item, index) => {
        const response = {};
        response.columnName = item.field;
        response.enabled = item.enabled;
        response.position = index;
        if (item.id) response.id = item.id;
        return response;
      });

      try {
        // save config
        const response = await axios.post("/api/config/update-column-config", {
          pageType: this.pageType,
          columnConfig: data,
        });

        // re-assign value
        this.tableConfig.forEach((col) => {
          response.data.forEach((item) => {
            if (item.columnName === col.field) {
              col.enabled = item.enabled;
              col.id = item.id;
              col.position = item.position;
            }
          });
        });

        // re-sort
        this.tableConfig.sort((a, b) => a.position - b.position);
        console.log("@handleSaveColConfig>this.tableConfig", this.tableConfig);
      } catch (error) {
        console.log(error);
      } finally {
        this.$forceUpdate();
      }
    },
  },
  async created() {
    this.startingTime = moment().startOf("date").unix(); // second
    const server = await Common.getSystem("server");
    if (server) this.server = server;
  },
  mounted() {
    this.getNotifyInterval = setInterval(() => {
      this.fetchData({});
    }, timeStep);
    this.fetchColumnConfig();
  },
  destroyed() {
    clearInterval(this.getNotifyInterval);
  },
  watch: {
    // resources: {
    //   handler(newVal) {
    //     if (Object.keys(newVal).length) {
    //       console.log('@watch:resources', newVal)
    //       // this.tableConfig = this.tableConfig.map(i => ({ ...i, label: newVal[i.label] ?? i.label }))
    //     }
    //   },
    //   immediate: true
    // },
    listDisplayedShifts(newVal) {
      console.log("listDisplayedShifts", newVal);
    },
  },
};
</script>

<style>
.plugin-tooltip-container {
  background-color: #ffffff;
  padding: 10px 13px;
  color: #4b4b4b;
  border-radius: 3px;
}
</style>
<style scoped>
.parts-produced-table {
  width: 39%;
}

.parts-produced-shift {
  width: 59%;
}

.no-results {
  border: 1px solid rgb(200, 206, 211);
  border-radius: 0.25rem;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.part-produced-small {
  display: none;
  width: 100%;
}

.part-produced-large {
  display: flex;
  width: 100%;
  gap: 15px;
}

@media only screen and (max-width: 1300px) {
  .part-produced-large {
    display: none !important;
  }

  .part-produced-small {
    display: flex !important;
    gap: 15px;
  }
}

@media only screen and (max-width: 1450px) {
  .parts-produced-table {
    width: 39%;
  }

  .parts-produced-shift {
    width: 52% !important;
  }
}

.parts-produced-paging {
  height: 30px;
  margin-top: 5px;
  align-items: center;
  justify-content: flex-end;
}

.table td {
  height: 56px;
}

.shift-hour__indicator-container {
  position: absolute;
  width: 19px;
  height: 19px;
  right: -9px;
  top: -9px;
  z-index: 3;
}

.shift-hour__indicator-icon {
  width: 19px;
  height: 19px;
  background-image: url("/images/icon/oee/indicator-machine-default-icon.svg");
}

.shift-hour__indicator-container:hover {
  width: 23px;
  height: 23px;
  top: -11px;
  right: -11px;
}

.shift-hour__indicator-container:hover .shift-hour__indicator-icon {
  width: 23px;
  height: 23px;
  background-image: url("/images/icon/oee/indicator-machine-hover-icon.svg");
}

.ant-tooltip-arrow {
  border-color: white;
}

.shift-text-10 {
  font-size: 10px !important;
}
</style>
