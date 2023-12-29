<template>
  <div class="version-manage-table">
    <table class="data-grid__table">
      <thead class="data-grid__head">
        <tr class="data-grid__row">
          <th
            v-for="col in columnConfig"
            :key="col.field"
            class="data-grid__cell"
          >
            <span class="cell-inner">
              <span class="cell-inner__label">
                {{ col.label }}
              </span>
              <span class="cell-inner__sort"></span>
            </span>
          </th>
          <th class="data-grid__cell"></th>
        </tr>
      </thead>
      <tbody class="data-grid__body">
        <tr
          v-for="(row, index) in listVersions"
          :key="`${index}-${row.fileGroupStatus}`"
          class="data-grid__row"
        >
          <td
            v-for="col in columnConfig"
            :key="col.field"
            class="data-grid__cell"
          >
            <!-- HAS SUBFIELD -->
            <div v-if="col.subField" class="cell-inner">
              <span class="cell-inner__text">
                {{
                  col.formatter ? col.formatter(row[col.field]) : row[col.field]
                }}
              </span>
              <span class="cell-inner__subText">
                {{
                  col.subFormatter
                    ? col.subFormatter(row[col.subField])
                    : row[col.subField]
                }}
              </span>
            </div>

            <!-- IS STATUS -->
            <div
              v-else-if="col.status && row[col.field]"
              class="cell-inner cell-inner__status"
            >
              <span
                class="cell-inner__iconStatus"
                :style="`background-color: ${col.status[row[col.field]].color}`"
              ></span>
              <span class="cell-inner__text">
                {{ col.status[row[col.field]].label }}
              </span>
            </div>

            <!-- DEFAULT -->
            <div v-else class="cell-inner">
              {{ col.formatter ? col.formatter() : row[col.field] }}
            </div>
          </td>
          <td class="table-cell">
            <span
              v-if="row.fileGroupStatus"
              class="cell-inner"
              :class="row.fileGroupStatus"
            >
              <base-dropdown
                style="border: none"
                :options="
                  row.fileGroupStatus === 'UNRELEASED'
                    ? LIST_ACTIONS_UNRELEASE
                    : LIST_ACTIONS_RELEASE
                "
                :sort="false"
                :primitive="true"
                :default="false"
                :multiple="false"
                :value="null"
                @input="(value) => handleChangeStatus(row, value)"
              >
                Take Actions
              </base-dropdown>
            </span>
          </td>
        </tr>
      </tbody>
    </table>
    <toast-alert
      :resources="resources"
      :title="toastState.title"
      :content="toastState.content"
      :show="toastState.show"
    ></toast-alert>
  </div>
</template>

<script>
const LIST_ACTIONS = [
  {
    label: "Unrelease",
    value: "unrelease",
  },
  {
    label: "Release",
    value: "release",
  },
  {
    label: "Delete",
    value: "delete",
  },
];

const LIST_ACTIONS_RELEASE = LIST_ACTIONS.filter(
  (i) => i.value === "unrelease"
);
const LIST_ACTIONS_UNRELEASE = LIST_ACTIONS.filter(
  (i) => i.value !== "unrelease"
);
module.exports = {
  name: "VersionManageTable",
  props: {
    resources: Object,
    formData: Object,
    listData: Array,
    refetch: Function,
  },
  setup(props, { emit, root }) {
    // ALERT
    const toastState = ref({
      show: false,
      title: "",
      content: "",
    });
    const closeAlert = () => {
      setTimeout(() => {
        toastState.value.show = false;
        setTimeout(() => {
          toastState.value.title = "";
          toastState.value.content = "";
        }, 1000);
      }, 3000);
    };
    const setAlert = ({ title, content, show }) => {
      toastState.value.title = title;
      toastState.value.content = content;
      root.$nextTick(() => {
        toastState.value.show = show;
      });
      closeAlert();
    };

    const columnConfig = ref([
      {
        label: "Version",
        field: "version",
        sort: true,
      },
      {
        label: "Recently Released Version",
        field: "releasedVersion",
        sort: true,
      },
      {
        label: "Released Date",
        field: "releasedDate",
        subField: "releasedTime",
        sort: true,
        enabled: true,
      },
      {
        label: "Status",
        field: "fileGroupStatus",
        sort: true,
        status: {
          UNMANAGED: { label: "Unmanaged", color: "var(--grey-dark)" },
          UNRELEASED: { label: "Unreleased", color: "#E34537" },
          RELEASED: { label: "Released", color: "#41CE77" },
        },
      },
    ]);

    const listVersions = ref([]);
    const isProcessing = ref(false);

    const fetchUpdateReleaseStatus = async (formData, newStatus) => {
      isProcessing.value = true;
      try {
        const payload = {};
        payload.fileGroupCode = props.formData.fileGroupCode;
        payload.fileGroupName = props.formData.fileGroupName;
        payload.version = props.formData.version;
        payload.description = props.formData.description;

        payload.fileRefs = props.formData.fileRefs;
        payload.enabled = props.formData.enabled;
        payload.fileGroupType = props.formData.fileGroupType;

        console.log("payload", payload);
        if (newStatus === "delete") {
          await axios.delete(`/api/common/fle-mng/${formData.id}`);
        }
        if (["release", "unrelease"].includes(newStatus)) {
          await axios.put(
            `/api/common/fle-mng/${formData.id}/${newStatus}`,
            payload
          );
        }

        props.refetch({ ...payload, id: formData.id });
        setAlert({
          title: "Success!",
          content: "Your file has been updated.",
          show: true,
        });
      } catch (error) {
        console.log(error);
      } finally {
        isProcessing.value = false;
      }
    };

    watch(
      () => props.listData,
      () => {
        console.log("props.listData", props.listData);
        listVersions.value = props.listData.map((i) => ({
          ...i,
          releasedDate: root
            .formatToDateTime(i.releasedAt)
            .slice(0, "YYYY-MM-DD".length),
          releasedTime: root
            .formatToDateTime(i.releasedAt)
            .slice("YYYY-MM-DD".length, "YYYY-MM-DD HH:mm:ss".length),
          checked: false,
        }));
      }
    );

    const handleChangeStatus = (file, status) => {
      console.log("@handleChangeStatus", file, status);
      status && fetchUpdateReleaseStatus({ id: file.id }, status);
    };

    return {
      LIST_ACTIONS,
      LIST_ACTIONS_RELEASE,
      LIST_ACTIONS_UNRELEASE,

      toastState,

      columnConfig,
      listVersions,
      handleChangeStatus,
    };
  },
};
</script>

<style scoped>
/* DATA GRID */
.data-grid__table {
  width: 100%;
  overflow-x: auto;
  border: 1px solid rgb(200, 206, 211);
  border-radius: 3px;
  margin-bottom: 0px;
}
.data-grid__head {
  background: #ffffff;
  position: sticky;
  top: 6px;
  z-index: 99;
}
.data-grid__body {
}
.data-grid__row {
  background: #ffffff;
  border-bottom: 2px solid #c8ced3;
}
.data-grid__body .data-grid__cell {
  padding: 6px 8px;
}

.data-grid__head .data-grid__cell {
  padding: 7px 14px 7px 7px;
  background-color: #f5f8ff;
}
.data-grid__head .cell-inner {
  display: flex;
}
.data-grid__body .data-grid__cell {
}

.cell-checkbox {
  width: 27px;
  padding: 0.5em 0;
  vertical-align: middle;
}
.cell-inner {
  cursor: pointer;
  font-size: 14.66px;
}
.cell-inner__text {
  display: block;
  font-size: 14.66px;
}
.cell-inner__subText {
  font-size: 12px;
}
.cell-inner__sort {
  display: flex;
  align-items: center;
  justify-content: center;
}
.cell-inner__sort-icon {
  display: inline-block;
  width: 100%;
  height: 100%;
  background-color: var(--grey-8);
  mask-image: url("/images/icon/sort.svg");
  mask-size: contain;
  mask-position: center;
  mask-repeat: no-repeat;
  -webkit-mask-image: url("/images/icon/sort.svg");
  -webkit-mask-size: contain;
  -webkit-mask-position: center;
  -webkit-mask-repeat: no-repeat;
}

.cell-inner__status {
  display: flex;
  align-items: center;
}

.cell-inner__iconStatus {
  display: inline-block;
  width: 15px;
  height: 15px;
  border-radius: 25px;
  margin-right: 6px;
}
</style>
