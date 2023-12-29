<template>
  <base-dialog
    :visible="visible"
    :title="getLabel.title"
    dialog-classes="dialog-md"
    :nested="true"
    body-classes="custom-body"
    @close="$emit('close')"
  >
    <div class="form-item">
      <label>{{ resources["request_id"] }}:</label>
      <base-input width="200px" :readonly="true" :value="request.requestId">
      </base-input>
    </div>
    <div class="form-item">
      <label>{{ getLabel.type }}:</label>
      <div v-if="getReviewUser">
        <base-avatar :item="getReviewUser"></base-avatar>
      </div>
    </div>
    <div class="" style="margin-bottom: 40px">
      <label>{{ resources["reason"] }}:</label>
      <div style="flex: 1">
        <textarea
          readonly
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
  </base-dialog>
</template>

<script>
module.exports = {
  components: {},
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    visible: {
      type: Boolean,
      default: false,
    },
    request: {
      type: Object,
      default: () => ({}),
    },
  },
  setup(props, ctx) {
    // STATE //
    const reason = ref("");

    // METHOD //

    // COMPUTED //
    const getLabel = computed(() => {
      if (props.request.dataRequestStatus === "DECLINED") {
        return {
          title: props.resources["reason_for_declining_data_request"],
          type: props.resources["declined_by"],
        };
      }
      if (props.request.dataRequestStatus === "CANCELLED") {
        return {
          title: props.resources["reason_for_cancelling_data_request"],
          type: props.resources["cancelled_by"],
        };
      }
      return {
        title: "",
        type: "",
      };
    });

    const getReviewUser = computed(() => {
      if (props.request.dataRequestStatus === "DECLINED") {
        return props.request.declinedBy;
      } else if (props.request.dataRequestStatus === "CANCELLED") {
        return props.request.cancelledBy;
      }
    });

    const getReason = computed(() => {
      if (props.request.dataRequestStatus === "DECLINED") {
        return props.request.declineReason;
      } else if (props.request.dataRequestStatus === "CANCELLED") {
        return props.request.cancelReason;
      }
    });

    // WATCH
    watch(
      () => props.visible,
      (newVal) => {
        if (!newVal) {
          reason.value = "";
        }
      }
    );

    return {
      reason,
      getLabel,
      getReviewUser,
      getReason,
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
}

.form-item__details textarea::placeholder {
  padding: 8px 6px;
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
