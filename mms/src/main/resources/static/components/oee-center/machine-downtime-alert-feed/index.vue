<template>
  <custom-card
    style="align-self: stretch"
    card-style="style-2"
    :title="resources['machine_downtime_events']"
  >
    <div class="downtime-wrap" style="margin-top: 8px">
      <ul class="list-feeds" v-if="!hasNoData">
        <li v-for="item in listFeeds" :key="item.id" class="list-feeds-item">
          <div class="feed-time">
            <div>{{ formatToDateTimeMinute(item.startTime) }}</div>
            <div :class="item.endTime ? '' : 'on-going'">
              {{ renderEndtime(item.endTime) }}
            </div>
          </div>
          <div class="feed-status">
            <!-- SWITCH ICON -->
            <span
              v-if="item.downtimeStatus === FEED_STATUS.CONFIRMED"
              class="icon icon-confirmed"
            ></span>
            <span
              v-else-if="item.downtimeStatus === FEED_STATUS.DOWNTIME"
              class="icon icon-created"
            ></span>
            <span
              v-else-if="item.downtimeStatus === FEED_STATUS.REGISTERED"
              class="icon icon-registered"
            ></span>
            <span
              v-else-if="item.downtimeStatus === FEED_STATUS.UNCONFIRMED"
              class="icon icon-unconfirmed"
            ></span>
            <!-- SWITCH ICON -->
            <span class="feed-status-notification">
              <div class="feed-lead">
                <span style="position: relative">
                  <a
                    class="hyperlink"
                    href="javascript:void(0)"
                    @click="handleClickFeed(item)"
                  >
                    [{{ item.machineCode }}] {{ resources["downtime_alert"] }}
                  </a>
                  <base-popover
                    :is-visible="showHandleFeedOptions === item.id"
                    class="feed-popover"
                    @close="showHandleFeedOptions = ''"
                  >
                    <ul class="feed-popover__list">
                      <li
                        v-if="
                          enableConfirmDowntimeReason &&
                          item.downtimeStatus === FEED_STATUS.UNCONFIRMED
                        "
                        class="feed-popover__option"
                        @click="handleClickDowntimeReason(item, 'CONFIRM')"
                      >
                        {{ resources["confirm_downtime"] }}
                      </li>
                      <li
                        v-if="
                          enableRegisterDowntimeReason &&
                          item.downtimeStatus === FEED_STATUS.DOWNTIME
                        "
                        class="feed-popover__option"
                        @click="handleClickDowntimeReason(item, 'REGISTER')"
                      >
                        {{ resources["register_downtime_reason"] }}
                      </li>
                      <li
                        v-if="
                          enableEditDowntimeReason &&
                          item.downtimeStatus === FEED_STATUS.REGISTERED
                        "
                        class="feed-popover__option"
                        @click="handleClickDowntimeReason(item, 'EDIT')"
                      >
                        {{ resources["edit_downtime_reason"] }}
                      </li>

                      <!-- CASE EDIT AFTER END TIME FOR CONFIRMED / UNCONFIRMED -->
                      <li
                        v-if="
                          enableEditEndedDowntimeReason &&
                          [
                            FEED_STATUS.UNCONFIRMED,
                            FEED_STATUS.CONFIRMED,
                          ].includes(item.downtimeStatus)
                        "
                        class="feed-popover__option"
                        @click="handleClickDowntimeReason(item, 'EDIT')"
                      >
                        {{ resources["edit_downtime_reason"] }}
                      </li>
                      <!-- CASE EDIT AFTER END TIME FOR CONFIRMED / UNCONFIRMED -->

                      <li
                        v-if="
                          !enableConfirmDowntimeReason &&
                          !enableEditDowntimeReason &&
                          !enableRegisterDowntimeReason
                        "
                        class="feed-popover__option disable"
                      >
                        No options available
                      </li>
                    </ul>
                  </base-popover>
                </span>
                <span v-if="item.downtimeStatus === FEED_STATUS.UNCONFIRMED">
                  &nbsp;{{ resources["is_unconfirmed"] }}.
                </span>
                <span v-else>
                  &nbsp;{{ resources["has_been"] }}
                  {{ resources[renderStatus(item.downtimeStatus)] }}.
                </span>
              </div>
              <div
                v-if="item.machineDowntimeReasonList.length"
                class="feed-reasons"
              >
                <base-drop-count
                  :is-hyper-link="false"
                  :data-list="item.machineDowntimeReasonList"
                  :title="item.machineDowntimeReasonList[0].codeData.title"
                >
                  <div style="padding: 6px 8px">
                    <div
                      v-for="(i, index) in item.machineDowntimeReasonList"
                      :key="index"
                      @click="() => {}"
                      style="
                        display: inline-flex;
                        width: fit-content;
                        white-space: nowrap;
                        cursor: pointer;
                      "
                    >
                      {{ i.codeData.title }}
                    </div>
                  </div>
                </base-drop-count>
              </div>
            </span>
          </div>
        </li>
      </ul>
      <div
        v-else
        style="
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
        "
      >
        No events within the selected date period
      </div>
    </div>
    <div
      class="category-data-table__footer d-flex justify-content-end"
      v-if="!hasNoData && pagination.totalPages > 1"
      style="padding: 6px 8px"
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
          @click="$emit('paginate', pagination.pageNumber - 1)"
        >
          <img src="/images/icon/category/paging-arrow.svg" alt="" />
        </a>
        <a
          href="javascript:void(0)"
          class="paging-button"
          @click="$emit('paginate', pagination.pageNumber + 1)"
          :class="{
            'inactive-button': pagination.pageNumber >= pagination.totalPages,
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
  </custom-card>
</template>

<script>
const FEED_STATUS = {
  CONFIRMED: "CONFIRMED",
  DOWNTIME: "DOWNTIME",
  REGISTERED: "REGISTERED",
  UNCONFIRMED: "UNCONFIRMED",
};

module.exports = {
  name: "MachineDowntimeAlertFeed",
  components: {
    "custom-card": httpVueLoader("/components/common/card-custom.vue"),
  },
  props: {
    listData: Array,
    resources: Object,
    pagination: {
      type: Object,
      default: () => ({ pageNumber: 1, totalPages: 1 }),
    },
    permissions: Object,
    onClickReason: Function,
  },
  setup(props, ctx) {
    const listFeeds = ref([]);
    const showHandleFeedOptions = ref("");
    const selectedFeed = ref(null);
    const hasNoData = ref(true);
    watch(
      () => props.listData,
      (newVal, prevVal) => {
        listFeeds.value = newVal.map((item) => ({
          ...item,
        }));
        console.log("listFeeds.value", listFeeds.value);
        hasNoData.value = listFeeds.value.length === 0;
      },
      { deep: true }
    );

    const renderStatus = (status) => {
      if (status === FEED_STATUS.DOWNTIME) return "created";
      return status?.toLowerCase() || "";
    };

    const renderReasons = (listReasons) => {
      if (!listReasons.length) return "";
      const listCodes = listReasons.map((i) => i.codeData.code);
      if (listCodes.length <= 1) {
        return `Code: ${listCodes[0]}`;
      }
      return "Codes: " + listCodes.join(", ");
    };

    const renderEndtime = (endTime) => {
      if (!endTime) return `- ${props.resources["on_going"]}`;
      return ctx.root.formatToDateTimeMinute(endTime);
    };

    const handleClickDowntimeReason = (item, type) => {
      props.onClickReason(item, type);
      showHandleFeedOptions.value = "";
    };
    const handleClickFeed = (machine) => {
      showHandleFeedOptions.value = machine.id;
      selectedFeed.value = machine;
    };

    const enableEditDowntimeReason = ref(false);
    const enableEditEndedDowntimeReason = ref(false);
    const enableConfirmDowntimeReason = ref(false);
    const enableRegisterDowntimeReason = ref(false);

    watch(
      () => props.permissions,
      async (newVal) => {
        if (newVal) {
          [
            enableEditDowntimeReason.value,
            enableEditEndedDowntimeReason.value,
            enableConfirmDowntimeReason.value,
            enableRegisterDowntimeReason.value,
          ] = await Promise.all([
            Common.isMenuPermitted(
              Common.PERMISSION_CODE.OEE_CENTER,
              Common.PERMISSION_CODE.OEE_CENTER_MACHINE_DOWNTIME_DETAIL,
              "btnEditRegisteredReason"
            ),
            Common.isMenuPermitted(
              Common.PERMISSION_CODE.OEE_CENTER,
              Common.PERMISSION_CODE.OEE_CENTER_MACHINE_DOWNTIME_DETAIL,
              "btnEditEndedDowntimeReason"
            ),
            Common.isMenuPermitted(
              Common.PERMISSION_CODE.OEE_CENTER,
              Common.PERMISSION_CODE.OEE_CENTER_MACHINE_DOWNTIME_DETAIL,
              "btnConfirmDowntime"
            ),
            Common.isMenuPermitted(
              Common.PERMISSION_CODE.OEE_CENTER,
              Common.PERMISSION_CODE.OEE_CENTER_MACHINE_DOWNTIME_DETAIL,
              "btnRegisterReason"
            ),
          ]);
        }
      },
      { immediate: true }
    );

    return {
      FEED_STATUS,
      showHandleFeedOptions,
      listFeeds,
      hasNoData,
      enableEditDowntimeReason,
      enableEditEndedDowntimeReason,
      enableConfirmDowntimeReason,
      enableRegisterDowntimeReason,
      renderStatus,
      renderReasons,
      renderEndtime,
      handleClickFeed,
      handleClickDowntimeReason,
    };
  },
};
</script>

<style scoped>
.list-feeds {
  margin: 0px;
  padding: 0px 8px;
  overflow: scroll;
  max-height: 298px;
}

.list-feeds-item {
  display: flex;
  border: 1px solid transparent;
  background-color: #d6dade;
  border-radius: 3px;
  margin-bottom: 6px;
}

.list-feeds-item .feed-time {
  color: rgb(0, 0, 0);
  width: 120px;
  padding: 6px 8px;
  font-size: 12px;
  line-height: 16px;
}

.list-feeds-item .feed-time .on-going {
  font-style: italic;
}

.list-feeds-item .feed-status {
  flex: 1 1 0%;
  display: inline-flex;
  align-items: center;
  padding-left: 8px;
  padding-right: 8px;
  font-size: 15px;
  background-color: rgb(255, 255, 255);
}

.feed-status .icon {
  display: inline-block;
  width: 15px;
  height: 13px;
  margin-right: 8px;
  background-position: center center;
  background-size: contain;
  background-repeat: no-repeat;
}

.icon.icon-confirmed {
  background-image: url("/images/icon/oee/confirmed-icon.svg");
}

.icon.icon-created {
  background-image: url("/images/icon/oee/created-icon.svg");
}

.icon.icon-registered {
  background-image: url("/images/icon/oee/registered-icon.svg");
}

.icon.icon-unconfirmed {
  border-radius: 50%;
  width: 13px;
  height: 13px;
  background: #f7cc57;
}

.feed-status a {
}

.feed-status span {
}

.feed-popover {
  width: 250px;
  position: absolute;
  margin-top: 4px;
  left: 0;
  top: 100%;
}

.feed-popover__list {
  list-style-type: none;
  padding-left: 0;
}

.feed-popover__option {
  cursor: pointer;
  padding: 6px 8px;
  color: var(--grey-dark);
}

.feed-popover__option:hover {
  background-color: var(--blue-light-2);
}
</style>
