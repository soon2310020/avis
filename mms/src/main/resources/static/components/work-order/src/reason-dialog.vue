<template>
  <base-dialog
    :visible="visible"
    :title="getLabel.title"
    size="sm"
    :nested="true"
    body-classes="custom-body"
    @close="$emit('close')"
  >
    <template v-if="mode === 'view'">
      <div class="form-item">
        <label>{{ resources["work_order_id"] }}</label>
        <base-input width="200px" :readonly="true" :value="getWorkOrderId">
        </base-input>
      </div>
      <div class="form-item">
        <template v-if="item.orderType === 'GENERAL'">
          <label>{{ resources["asset_s"] }}</label>
          <div>
            <base-chip
              v-for="(i, index) in getSelectedMold"
              :key="index"
              :close-able="false"
              :selection="i"
            >
            </base-chip>
            <base-chip
              v-for="(i, index) in getSelectedMachine"
              :key="index"
              :close-able="false"
              :color="'red'"
              :selection="i"
            >
            </base-chip>
            <base-chip
              v-for="(i, index) in getSelectedTerminal"
              :key="index"
              :close-able="false"
              :color="'teal-deer'"
              :selection="i"
            >
            </base-chip>
            <base-chip
              v-for="(i, index) in getSelectedSensor"
              :key="index"
              :close-able="false"
              :color="'atomic-tangerine'"
              :selection="i"
            >
            </base-chip>
          </div>
        </template>
        <template v-else>
          <label>{{ resources["asset"] }}</label>
          <template v-if="getToolingTitle.shouldTruncate">
            <base-tooltip color="white" background="grey">
              <template slot="content">
                <div>
                  <span>{{ getToolingTitle.rawText }}</span>
                </div>
              </template>
              <base-input
                width="200px"
                :readonly="true"
                :value="getToolingTitle.displayText"
              >
              </base-input>
            </base-tooltip>
          </template>
          <template v-else>
            <base-input
              width="200px"
              :readonly="true"
              :value="getToolingTitle.displayText"
            >
            </base-input>
          </template>
        </template>
      </div>
      <div class="form-item">
        <label>{{ resources["plant_s"] }}</label>
        <div>{{ listPlants }}</div>
      </div>
      <div class="form-item">
        <label>{{ getLabel.byUser }}</label>
        <div v-if="getReviewUser">
          <base-avatar :item="getReviewUser"></base-avatar>
        </div>
      </div>
      <div class="" style="margin-bottom: 40px">
        <label>{{ resources["reason"] }}</label>
        <div style="flex: 1">
          <textarea
            :readonly="mode === 'view'"
            rows="5"
            :value="getReason"
            style="
              width: 100%;
              border-radius: 3px;
              padding: 4px 8px;
              border: 1px solid #acacac;
              background-color: var(--grey-5);
              border-color: var(--grey-5);
            "
          >
          </textarea>
        </div>
      </div>
      <div
        v-if="reasonType === 'WO_CPT_REJECTED'"
        class="form-item form-item__action d-flex"
        style="justify-content: flex-end"
      >
        <div class="d-flex align-end flex-column">
          <i style="margin-bottom: 4px">{{
            resources["resubmission_requests_can_be_found_in_my_orders"]
          }}</i>
          <base-button level="primary" @click="handleResubmit"
            >{{ resources["re_submit_work_order_report"] }}
          </base-button>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="" style="margin-bottom: 40px">
        <label>{{ resources["reason"] }}</label>
        <div style="flex: 1">
          <textarea
            :readonly="mode === 'view'"
            rows="5"
            :placeholder="getPlaceHolder"
            v-model="reason"
            style="
              width: 100%;
              border-radius: 3px;
              padding: 4px 8px;
              border: 1px solid #acacac;
            "
          >
          </textarea>
        </div>
      </div>
      <div class="d-flex justify-end">
        <i v-show="getWarningMessage">*{{ getWarningMessage }}</i>
      </div>
      <div
        v-if="mode === 'edit'"
        class="form-item form-item__action d-flex"
        style="justify-content: flex-end"
      >
        <div v-if="reasonType === 'WO_CPT_APPR_REQUESTED'">
          <base-button
            level="secondary"
            type="reject"
            @click="handleShowConfirm('cancel')"
            >{{ resources["cancel_work_order"] }}
          </base-button>
          <base-button level="primary" @click="handleReject"
            >{{ resources["request_re_submission"] }}
          </base-button>
        </div>
        <base-button
          v-else-if="reasonType === 'WO_CREATED'"
          level="primary"
          type="reject"
          @click="handleDeclineCreatedWorkOrder"
          >{{ resources["decline_work_order"] }}
        </base-button>
        <base-button
          v-else-if="reasonType === 'WO_CRT_APPR_REQUESTED'"
          level="primary"
          type="reject"
          @click="handleShowConfirm"
          >{{ resources["decline_work_order_request"] }}
        </base-button>
        <base-button
          v-else-if="reasonType === 'WO_MOD_APPR_REQUESTED'"
          level="primary"
          type="reject"
          @click="handleRejectChange"
          >{{ resources["reject_changes"] }}
        </base-button>
        <base-button
          v-else-if="reasonType === 'CANCEL_CREATED'"
          level="primary"
          type="reject"
          @click="handleCancelCreatedWorkOrder"
          >{{ resources["cancel_work_order"] }}
        </base-button>
      </div>
    </template>
    <warning-modal
      :visible="showConfirm"
      :nested="true"
      dialog-classes="dialog-sm"
      :hide-close-btn="true"
    >
      <div class="d-flex">
        <div
          class="warning-icon"
          style="
            height: 21px;
            width: 24px;
            transform: scale(0.7);
            margin-right: 4px;
          "
        ></div>
        <strong> {{ getComfirmContent.title }} </strong>
      </div>
      <div>
        {{ getComfirmContent.body }}
      </div>
      <div class="d-flex justify-end" style="margin-top: 42px">
        <base-button
          level="secondary"
          type="cancel"
          @click="getComfirmContent.cancel"
          style="margin-right: 8px"
          >{{ getComfirmContent.cancelTitle }}</base-button
        >
        <base-button
          level="primary"
          type="reject"
          @click="getComfirmContent.confirm"
          >{{ getComfirmContent.confirmTitle }}
        </base-button>
      </div>
    </warning-modal>
  </base-dialog>
</template>

<script>
module.exports = {
  components: {
    "warning-modal": httpVueLoader(
      "/components/@base/dialog/warning-dialog.vue"
    ),
  },
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    item: {
      type: Object,
      default: () => ({
        workOrderId: "",
        workOrderAssets: [],
        reviewedBy: {
          name: "",
        },
        rejectedReason: "",
      }),
    },
    visible: {
      type: Boolean,
      default: () => false,
    },
    mode: {
      type: String,
      default: () => "edit",
      validator(val) {
        return ["edit", "view"].includes(val);
      },
    },
    reasonType: {
      type: String,
      default: () => "",
    },
  },
  setup(props, ctx) {
    // STATE //
    const reason = ref("");
    const showConfirm = ref(false);
    const confirmType = ref("");
    // METHOD //
    const handleCancel = () => {
      ctx.emit("cancel", reason.value);
    };

    const handleReject = () => {
      ctx.emit("reject", reason.value);
    };

    const handleResubmit = () => {
      ctx.emit("re-submit", props.item.id);
    };

    const handleDeclineCreatedWorkOrder = () => {
      ctx.emit("decline-created-work-order", reason.value);
    };

    const handleRejectChange = () => {
      ctx.emit("reject-change", reason.value);
    };

    const handleCancelCreatedWorkOrder = () => {
      ctx.emit("cancel-created-work-order", reason.value);
    };

    const handleDeclineApproval = () => {
      handleCloseConfirm();
      ctx.emit("decline-approval", reason.value);
      ctx.emit("close");
    };

    const handleShowConfirm = (type) => {
      confirmType.value = type;
      showConfirm.value = true;
    };

    const handleConfirm = () => {
      if (confirmType.value === "cancel") {
        handleCancel();
      }
      handleCloseConfirm();
    };

    const handleCloseConfirm = () => {
      showConfirm.value = false;
    };

    // COMPUTED //

    const listPlants = computed(() => {
      let result = "";
      if (props.item && props.item.plantList) {
        result = props.item.plantList.map((o) => " " + o.name).toString();
      }
      return result;
    });

    const getWorkOrderId = computed(() => {
      const result = props.item?.workOrderId;
      return result;
    });

    const getSelectedMold = computed(() => {
      if (props.item && props.item.workOrderAssets) {
        return props.item.workOrderAssets
          .filter((i) => i.type === "TOOLING")
          .map((o) => ({ o, title: o.assetCode }));
      }
      return [];
    });
    const getSelectedMachine = computed(() => {
      if (props.item && props.item.workOrderAssets) {
        return props.item.workOrderAssets
          .filter((i) => i.type === "MACHINE")
          .map((o) => ({ o, title: o.assetCode }));
      }
      return [];
    });
    const getSelectedTerminal = computed(() => {
      if (props.item && props.item.workOrderAssets) {
        return props.item.workOrderAssets
          .filter((i) => i.type === "TERMINAL")
          .map((o) => ({ o, title: o.assetCode }));
      }
      return [];
    });
    const getSelectedSensor = computed(() => {
      if (props.item && props.item.workOrderAssets) {
        return props.item.workOrderAssets
          .filter((i) => i.type === "COUNTER")
          .map((o) => ({ o, title: o.assetCode }));
      }
      return [];
    });

    const getToolingTitle = computed(() => {
      if (
        props.item &&
        props.item.workOrderAssets &&
        props.item.workOrderAssets.length > 0
      ) {
        const toolingAssets = props.item.workOrderAssets.filter(
          (item) => item.type === "TOOLING"
        );
        if (toolingAssets.length > 0) {
          const title = toolingAssets.map((item) => item.assetCode).join(", ");
          if (title.length > 20) {
            return {
              shouldTruncate: true,
              displayText: title.slice(0, 20) + "...",
              rawText: title,
            };
          }
          return {
            shouldTruncate: false,
            displayText: title,
            rawText: title,
          };
        }
      }

      return {
        shouldTruncate: false,
        displayText: "",
        rawText: "",
      };
    });

    const getMachineTitle = computed(() => {
      if (
        props.item &&
        props.item.workOrderAssets &&
        props.item.workOrderAssets.length > 0
      ) {
        const machineAssets = props.item.workOrderAssets.filter(
          (item) => item.type === "MACHINE"
        );
        if (machineAssets.length > 0) {
          const title = machineAssets.map((item) => item.assetCode).join(", ");
          if (title.length > 20) {
            return {
              shouldTruncate: true,
              displayText: title.slice(0, 20) + "...",
              rawText: title,
            };
          }
          return {
            shouldTruncate: false,
            displayText: title,
            rawText: title,
          };
        }
      }

      return {
        shouldTruncate: false,
        displayText: "",
        rawText: "",
      };
    });

    const getComfirmContent = computed(() => {
      if (props.reasonType === "WO_CRT_APPR_REQUESTED") {
        return {
          title: props.resources["are_you_sure_you_want_to_decline"],
          body: props.resources["approval_decline_message"],
          cancelTitle: props.resources["no_dont_decline"],
          confirmTitle: props.resources["yes_decline_work_order"],
          cancel: handleCloseConfirm,
          confirm: handleDeclineApproval,
        };
      }
      return {
        title: props.resources["are_you_sure_you_want_to_cancel"],
        body: props.resources[
          "cancelled_work_orders_can_be_re_opened_from_the_history_log"
        ],
        cancelTitle: props.resources["no_dont_cancel"],
        confirmTitle: props.resources["yes_cancel_work_order"],
        cancel: handleCloseConfirm,
        confirm: handleConfirm,
      };
    });

    const getLabel = computed(() => {
      if (props.mode === "view") {
        if (props.reasonType === "WO_CPT_CANCELLED") {
          return {
            title:
              props.resources["reason_for_cancelling_work_order_submission"],
            byUser: props.resources["cancelled_by"],
          };
        } else if (props.reasonType === "WO_CPT_REJECTED") {
          return {
            title:
              props.resources["reason_for_rejecting_work_order_submission"],
            byUser: props.resources["rejected_by"],
          };
        } else if (
          props.reasonType === "WO_DECLINED" ||
          props.reasonType === "WO_CRT_DECLINED"
        ) {
          return {
            title: props.resources["reason_for_declining_work_order_request"],
            byUser: props.resources["declined_by"],
          };
        } else if (props.reasonType === "WO_MOD_REJECTED") {
          return {
            title: props.resources["reason_for_rejecting_work_order_changes"],
            byUser: props.resources["rejected_by"],
          };
        } else if (props.reasonType === "WO_CANCELLED") {
          return {
            title: props.resources["reason_for_cancelling_work_order_request"],
            byUser: props.resources["cancelled_by"],
          };
        }
      } else {
        if (props.reasonType === "WO_CREATED") {
          return {
            title: props.resources["reason_for_declining_work_order"],
            byUser: "",
          };
        } else if (props.reasonType === "WO_MOD_APPR_REQUESTED") {
          return {
            title: props.resources["reason_for_rejecting_work_order_changes"],
            byUser: "",
          };
        } else if (props.reasonType === "CANCEL_CREATED") {
          return {
            title: props.resources["reason_for_cancelling_work_order"],
            byUser: "",
          };
        } else if (props.reasonType === "WO_CRT_APPR_REQUESTED") {
          return {
            title: props.resources["decline_work_order_request"],
            byUser: "",
          };
        } else if (props.reasonType === "WO_CPT_APPR_REQUESTED") {
          return {
            title:
              props.resources["reason_for_rejecting_work_order_submission"],
            byUser: "",
          };
        }
      }
      return {
        // title: props.resources["reason_for_rejecting_work_order_submission"],
        title: "Wrong case",
        byUser: "",
      };
    });

    const getWarningMessage = computed(() => {
      if (props.mode === "edit") {
        if (props.reasonType === "WO_CREATED") {
          return props.resources[
            "declined_work_orders_will_be_sent_to_history_log_as_declined"
          ];
        } else if (props.reasonType === "WO_MOD_APPR_REQUESTED") {
          return props.resources[
            "rejecting_changes_will_not_affect_any_previously_updated_information"
          ];
        } else if (props.reasonType === "CANCEL_CREATED") {
          return props.resources[
            "cancelled_work_orders_can_be_re_opened_from_work_order_history_log"
          ];
        }
      }
      return "";
    });

    const getReason = computed(() => {
      if (props.mode === "view") {
        if (props.reasonType === "WO_DECLINED") {
          return props.item.declinedReason;
        } else if (props.reasonType === "WO_MOD_REJECTED") {
          return props.item.rejectedChangeReason;
        } else if (props.reasonType === "WO_CPT_REJECTED") {
          return props.item.rejectedReason;
        } else if (
          ["WO_CANCELLED", "WO_CRT_DECLINED"].includes(props.reasonType)
        ) {
          return props.item.cancelledReason;
        }
      }
      return props.item.rejectedReason;
    });

    const getPlaceHolder = computed(() => {
      if (props.mode === "edit") {
        if (
          ["WO_CREATED", "WO_CRT_APPR_REQUESTED"].includes(props.reasonType)
        ) {
          return props.resources[
            "enter_the_reason_for_declining_the_work_order_here"
          ];
        } else if (props.reasonType === "WO_MOD_APPR_REQUESTED") {
          return props.resources[
            "enter_the_reason_for_rejecting_the_work_order_changes_here"
          ];
        } else if (props.reasonType === "CANCEL_CREATED") {
          return props.resources[
            "enter_the_reason_for_cancelling_the_work_order_here"
          ];
        }
        return props.resources[
          "enter_the_reason_for_rejecting_the_work_order_here"
        ];
      }
      return "";
    });

    const getReviewUser = computed(() => {
      if (props.mode === "view") {
        if (props.reasonType === "WO_DECLINED") {
          return props.item.declinedBy;
        } else if (props.reasonType === "WO_MOD_REJECTED") {
          return props.item.rejectedChangeBy;
        } else if (
          ["WO_CANCELLED", "WO_CRT_DECLINED"].includes(props.reasonType)
        ) {
          return props.item.cancelledBy;
        }
      }
      return props.item.reviewedBy;
    });

    // WATCH
    watch(
      () => props.visible,
      (newVal) => {
        console.log("show reason form", newVal, props.item);
        if (!newVal) {
          reason.value = "";
          confirmType.value = "";
          showConfirm.value = false;
        }
      }
    );

    watchEffect(() => console.log("props.reasonType", props.reasonType));
    watchEffect(() => console.log("props.mode", props.mode));
    watchEffect(() => console.log("props.item", props.item));

    return {
      reason,
      showConfirm,
      getLabel,
      listPlants,
      getWorkOrderId,
      getMachineTitle,
      getToolingTitle,
      getWarningMessage,
      getReason,
      getReviewUser,
      getPlaceHolder,
      getComfirmContent,
      getSelectedMold,
      getSelectedMachine,
      getSelectedTerminal,
      getSelectedSensor,
      handleReject,
      handleCancel,
      handleResubmit,
      handleShowConfirm,
      handleCloseConfirm,
      handleConfirm,
      handleDeclineCreatedWorkOrder,
      handleRejectChange,
      handleCancelCreatedWorkOrder,
      handleDeclineApproval,
    };
  },
};
</script>

<style scoped>
.form-item-wrap {
  display: flex;
}

.form-item {
  display: flex;
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.form-item__requested label {
  text-align: right;
  margin-left: 32px;
  margin-right: 20px;
}

.form-item__details label {
  align-self: flex-start;
}

.form-item__details textarea {
  width: 100%;
  padding: 6px 8px;
}

.form-item__details textarea::placeholder {
  padding: 6px 8px;
  font-size: 14.66px;
  line-height: 17px;
  color: var(--grey-3);
}

.form-item__details textarea:read-only {
  cursor: default;
  color: var(--grey-dark);
  border-color: var(--grey-5);
  background-color: var(--grey-5);
}

.form-item__details textarea:read-only::placeholder {
  color: var(--grey-dark);
}

.form-item__action {
  margin-top: 19px;
  justify-content: flex-end;
}

.form-item__action > * {
  margin-left: 8px;
}
</style>
