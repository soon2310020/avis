<template>
  <base-dialog
    :visible="visible"
    :title="getLabel.title"
    dialog-classes="dialog-md"
    :nested="true"
    body-classes="custom-body"
    @close="$emit('close')"
  >
    <template>
      <div class="" style="margin-bottom: 40px">
        <label>{{ resources["reason"] }}:</label>
        <div style="flex: 1">
          <textarea
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
        <i>*{{ getWarningMessage }}</i>
      </div>
      <div
        class="form-item form-item__action d-flex"
        style="justify-content: flex-end"
      >
        <base-button
          level="primary"
          type="reject"
          @click="handleCancelDataRequest"
          >{{ resources["cancel_request"] }}
        </base-button>
      </div>
    </template>
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
      default: () => false,
    },
  },
  setup(props, ctx) {
    // STATE //
    const reason = ref("");

    // METHOD //
    const handleCancelDataRequest = () => {
      ctx.emit("cancel-data-request", reason.value);
    };

    // COMPUTED //
    const getLabel = computed(() => {
      return {
        title: props.resources["reason_for_cancelling_request"],
        createBy: "",
      };
    });

    const getWarningMessage = computed(() => {
      return props.resources[
        "cancelled_request_can_be_re_opened_from_request_history_log"
      ];
    });

    const getPlaceHolder = computed(() => {
      return props.resources[
        "enter_the_reason_for_cancelling_the_request_here"
      ];
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
      getWarningMessage,
      getPlaceHolder,
      handleCancelDataRequest,
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
  display: flex;
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
