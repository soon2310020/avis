<template>
  <div>
    <table class="table table-responsive-sm table-striped">
      <colgroup>
        <col />
        <col />
        <col />
        <col />
        <col />
      </colgroup>
      <thead id="thead-actionbar" class="custom-header-table">
        <tr
          style="height: 57px"
          :class="{ invisible: checkedIdList.length !== 0 }"
        >
          <th>
            <input type="checkbox" class="checkbox" v-model="isCheckAll" />
          </th>
          <th
            v-for="(item, index) in partsProducesFields"
            :key="index"
            class="text-left"
          >
            <span>{{ item }}</span>
          </th>
        </tr>
        <tr
          style="height: 57px"
          :class="{ 'd-none zindexNegative': checkedIdList.length === 0 }"
          class="tr-sort empty-tr"
        >
          <th class="empty-th">
            <div class="first-checkbox-zone2" style="left: unset !important">
              <div
                class="first-checkbox2"
                style="
                  display: flex;
                  align-items: center;
                  padding-left: 0;
                  width: 110px;
                "
              >
                <div>
                  <input
                    id="checkbox-all-2"
                    class="checkbox"
                    type="checkbox"
                    v-model="isCheckAll"
                  />
                </div>
              </div>
              <div class="action-bar">
                <div
                  v-if="canRegisterReason"
                  class="action-item"
                  @click="showDowntimeReason('REGISTER')"
                >
                  <span>{{ resources["register_downtime_reason"] }}</span>
                  <i class="icon-action register-icon"></i>
                </div>
                <div
                  class="action-item"
                  v-if="canConfirmDowntime"
                  @click="showDowntimeReason('CONFIRM')"
                >
                  <span>{{ resources["confirm_downtime"] }}</span>
                  <i class="icon-action confirm-icon"></i>
                </div>
                <div
                  class="action-item"
                  v-if="canEdit"
                  @click="showDowntimeReason('EDIT')"
                >
                  <span>{{ resources["edit"] }}</span>
                  <i class="icon-action edit-icon"></i>
                </div>
                <div
                  v-if="!canRegisterReason && !canConfirmDowntime && !canEdit"
                  class="action-item"
                >
                  No action is available
                </div>
              </div>
            </div>
          </th>
        </tr>
      </thead>
      <tbody class="op-list">
        <template v-if="!hasNoData">
          <tr v-for="(item, index) in dataList" :key="item.id">
            <td>
              <input
                type="checkbox"
                class="checkbox"
                :checked="isChecked(item.id)"
                @click="check"
                :value="item.id"
              />
            </td>
            <td class="text-left">
              <span class="hyperlink" @click="showMachineDetails(item)">{{
                item.machineCode
              }}</span>
            </td>
            <td class="text-left">
              <div v-if="item.mold">
                <a
                  href="#"
                  @click.prevent="showMoldChart(item.mold.id)"
                  style="color: #1aaa55"
                  class="mr-1 font-size-14"
                  ><i class="fa fa-bar-chart"></i
                ></a>
                <a href="#" @click.prevent="showMoldChart(item.mold.id)">
                  {{ item.mold.equipmentCode }}
                </a>
                <div class="small text-muted font-size-11-2">
                  <span v-text="resources['updated_on']"></span>
                  {{ formatToDate(item.mold.lastShotAt) }}
                </div>
              </div>
            </td>
            <td class="text-left">
              <span>{{ convertSecToHour(item.filteredDuration) }}</span>
            </td>
            <td class="text-left">
              <span>{{ item.startTimeStr }}</span>
            </td>
            <td class="text-left">
              <span>{{ item.endTimeStr }}</span>
            </td>
            <td class="text-left">
              <span>{{ convertSecToHour(item.duration) }}</span>
            </td>
            <td class="text-left">
              <reason-dropdown
                :reasons="item.machineDowntimeReasonList"
                :index="index"
              />
            </td>
            <td class="text-left">
              <a-tooltip placement="bottom">
                {{ countNotes(item.machineDowntimeReasonList) }}
                <template slot="title">
                  <ul
                    class="list-note"
                    style="
                      list-style-type: none;
                      margin-bottom: 0;
                      padding: 6px 8px;
                    "
                  >
                    <li
                      v-for="(reason, index) in item.machineDowntimeReasonList"
                      :style="{ 'padding-top': index > 0 ? '0.5rem' : '' }"
                    >
                      <div
                        class="list-note-item"
                        style="display: flex; flex-direction: column"
                      >
                        <span
                          class="list-note-item__title"
                          style="font-weight: bold"
                          >{{ reason.codeData.title }}</span
                        >
                        <span class="list-note-item__note">{{
                          reason.note
                        }}</span>
                      </div>
                    </li>
                  </ul>
                </template>
              </a-tooltip>
            </td>
            <td class="text-left">
              <div class="status-column d-flex">
                <div
                  class="icon-downtime-status"
                  :class="[
                    item.downtimeStatus === 'DOWNTIME'
                      ? 'created-icon'
                      : item.downtimeStatus === 'REGISTERED'
                      ? 'in_progress-icon'
                      : item.downtimeStatus === 'UNCONFIRMED'
                      ? 'confirming-icon'
                      : 'completed-icon',
                  ]"
                ></div>
                <div
                  v-text="
                    item.downtimeStatus === 'DOWNTIME'
                      ? resources['downtime']
                      : item.downtimeStatus === 'REGISTERED'
                      ? resources['registered']
                      : item.downtimeStatus === 'UNCONFIRMED'
                      ? resources['unconfirmed']
                      : resources['confirmed']
                  "
                ></div>
              </div>
            </td>
          </tr>
        </template>
        <tr v-else>
          <td colspan="100" style="background-color: #fff">
            <div
              style="
                display: flex;
                height: 300px;
                justify-content: center;
                align-items: center;
              "
            >
              No result
            </div>
          </td>
        </tr>
      </tbody>
    </table>
    <div
      v-if="pagination.totalPages !== 0"
      class="category-data-table__footer d-flex justify-content-end"
    >
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
          @click="handlePagination(pagination.pageNumber - 1)"
        >
          <img src="/images/icon/category/paging-arrow.svg" alt="" />
        </a>
        <a
          href="javascript:void(0)"
          class="paging-button"
          @click="handlePagination(pagination.pageNumber + 1)"
          :class="{
            'inactive-button': pagination.pageNumber === pagination.totalPages,
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
</template>

<script>
const TRUNCATE_LENGTH = 20;
module.exports = {
  components: {
    "reason-dropdown": httpVueLoader(
      "/components/alert-center/machine-downtime/reason-dropdown.vue"
    ),
  },
  props: {
    resources: Object,
    dataList: {
      type: Array,
      default: () => [],
    },
    fetchData: Function,
    pagination: Object,
    permissions: {
      type: Object,
      default: () => ({}),
    },
    detailPermissions: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      partsProducesFields: [
        this.resources["machine_id"],
        this.resources["tooling_id"],
        this.resources["filtered_duration"],
        this.resources["start_downtime"],
        this.resources["end_downtime"],
        this.resources["duration"],
        this.resources["reason"],
        this.resources["note"],
        this.resources["status"],
      ],
      isCheckAll: false,
      checkedIdList: [],
    };
  },
  computed: {
    isManager() {
      console.log("isManage", headerVm?.currentUser?.admin);
      return headerVm?.currentUser?.admin;
    },
    hasNoData() {
      return !this.dataList || this.dataList?.length === 0;
    },
    checkedList() {
      return this.dataList
        .filter((item) => item.checked)
        .map((item) => item.id);
    },
    downtimeStatus() {
      if (this.checkedIdList.length !== 1) return;
      const result = this.dataList.filter(
        (data) => data.id === this.checkedIdList[0]
      );
      if (!result) return;
      return result[0].downtimeStatus;
    },
    checkedItem() {
      if (this.checkedIdList.length === 1) {
        const result = this.dataList.filter(
          (data) => data.id === this.checkedIdList[0]
        );
        if (result) {
          return result[0];
        }
      }
    },
    canEdit() {
      if (this.checkedIdList.length === 1) {
        if (this.downtimeStatus === "REGISTERED") {
          return this.detailPermissions?.items?.btnEditRegisteredReason
            ?.permitted;
        } else if ("UNCONFIRMED" === this.downtimeStatus) {
          return this.detailPermissions?.items?.btnEditEndedDowntimeReason
            ?.permitted;
        } else if ("CONFIRMED" === this.downtimeStatus) {
          return this.detailPermissions?.items?.btnEditEndedDowntimeReason
            ?.permitted;
        }
      }
      return false;
    },
    canRegisterReason() {
      const isPermitted =
        this.detailPermissions?.items?.btnRegisterReason?.permitted;
      console.log("canRegisterReason:isPermitted", isPermitted);
      return (
        this.downtimeStatus === "DOWNTIME" &&
        this.checkedIdList.length === 1 &&
        isPermitted
      );
    },
    canConfirmDowntime() {
      const isPermitted =
        this.detailPermissions?.items?.btnConfirmDowntime?.permitted;
      console.log("canConfirmDowntime:isPermitted", isPermitted);
      return (
        this.downtimeStatus === "UNCONFIRMED" &&
        this.checkedIdList.length === 1 &&
        isPermitted
      );
    },
  },
  watch: {
    isCheckAll(newVal) {
      console.log("isCheckAll", newVal);
      if (newVal) {
        this.checkedIdList = this.dataList.map((item) => item.id);
        this.$emit("check-all", true);
      } else {
        this.checkedIdList = [];
        this.$emit("check-all", false);
      }
    },
    canRegisterReason(newVal) {
      console.log("canRegisterReason", newVal);
    },
    canConfirmDowntime(newVal) {
      console.log("canConfirmDowntime", newVal);
    },
  },
  methods: {
    convertDateString: function (s) {
      if (s) {
        return s;
      } else return "";
    },
    showMachineDetails(machine) {
      console.log("showMachineDetails", machine);
      this.$emit("show-machine-details", machine.machineId);
    },
    convertSecToHour(val) {
      console.log("convertSecToHour", val);
      const hour = Math.floor(val / 3600);
      const minutes = Math.round(((val / 3600) % 1) * 60);
      if (minutes > 0) {
        return hour + " h " + minutes + " m";
      }
      return (+val / 3600).toFixed(1) + " hrs";
    },
    showDowntimeReason(type) {
      this.$emit("show-downtime-reason", this.checkedItem, type);
    },
    check(e) {
      if (e.target.checked) {
        this.checkedIdList.push(+e.target.value);
      } else {
        this.checkedIdList = this.checkedIdList.filter(
          (value) => value !== +e.target.value
        );
      }

      console.log("check data", e.target);
    },
    isChecked(id) {
      const findIndex = this.checkedIdList.findIndex((value) => {
        return value === id;
      });
      return findIndex !== -1;
    },
    countNotes(listReasons) {
      const count = listReasons.filter((reason) => reason.note).length;
      if (count > 1) return count + " notes";
      if (count === 1) return count + " note";
      return "";
    },
    fetchPage() {
      this.fetchData({ page: this.pagination.pageNumber });
      this.checkedIdList = [];
    },
    handlePagination(pageNumber) {
      if (this.pagination.pageNumber < 0) return;
      if (this.pagination.pageNumber > this.pagination.totalPages) return;
      this.fetchData({ page: pageNumber });
    },
    showMoldChart(id) {
      this.$emit("show-mold-chart", id);
    },
  },
};
</script>

<style scoped>
.icon-downtime-status {
  margin-top: 4px;
  margin-right: 3px;
  width: 15px;
  height: 15px;
  border-radius: 50%;
}

.created-icon {
  background-color: #db3b21;
}

.in_progress-icon {
  background-color: #9521aa;
}

.confirming-icon {
  background-color: #ff6e00;
}

.completed-icon {
  background-color: #18aa55;
}
</style>
