<template>
  <div>
    <div class="back-title-wrapper">
      <div class="back-icon" @click="handleBack">
        <span class="icon back-arrow"></span>
        <span class="back-icon__text text--blue">
          {{ resources["back_to_data_request"] }}
        </span>
      </div>

      <div style="display: flex; align-items: center">
        <base-search
          v-model="searchText"
          placeholder="Search by selected columns below"
          class="search-bar__input"
          style="width: 289px; margin-right: 8px"
        ></base-search>
        <div class="assign-more" style="position: relative">
          <a
            @click.stop="handleToggleFilter"
            href="javascript:void(0)"
            class="btn-custom customize-btn btn-outline-custom-primary"
            :class="{ cust_focus: showFilter }"
          >
            <img
              style="margin-bottom: 3px"
              src="/images/icon/Icon_feather-filter.svg"
            />
          </a>
          <common-select-popover
            :is-visible="showFilter"
            @close="handleCloseFilter"
            class="filter-custom"
            :class="{ 'filter-custom-dropdown': showFilter }"
            style="left: -173px !important; min-width: 205px; margin-top: 4px"
          >
            <common-select
              class="filter-custom-dropdown"
              :style="{
                position: 'static',
                width: `205px !important`,
                transform: `translate(0px, -12px)`,
              }"
              :class="{ show: showFilter }"
              :items="companiesList"
              :searchbox="true"
              :multiple="true"
              id="filter"
              @on-select="onSelectCompany"
              :placeholder="resources['search_company_name']"
              :all-checkbox="true"
            ></common-select>
          </common-select-popover>
        </div>
      </div>
    </div>
    <header-area
      :resources="resources"
      :object-type="objectType"
      :number-of-object-type="numberOfObjectType"
      :completion-rate="completionRate"
      :request-list="requestList"
      @handle-click-request="onOpenRequest"
    ></header-area>
    <data-completion-report-table
      :resources="resources"
      :object-type="objectType"
      :table-results="tableResults"
      :pagination="pagination"
      :is-loading="isLoading"
      @paging="(pageNumber) => getCompletionReportData(pageNumber)"
      @show-completion="handleShowCompletion"
    ></data-completion-report-table>

    <machine-details-form
      ref="machine-details-form"
      :resources="resources"
      :updated="getCompletionReportData"
    ></machine-details-form>
    <location-form
      ref="location-form"
      :resources="resources"
      :updated="getCompletionReportData"
    ></location-form>
    <part-details-form
      ref="part-details-form"
      :resources="resources"
      :updated="getCompletionReportData"
    ></part-details-form>
    <company-details-form
      ref="company-details-form"
      :resources="resources"
      :updated="getCompletionReportData"
    ></company-details-form>
    <mold-details-form
      ref="mold-details-form"
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
      :updated="getCompletionReportData"
    ></mold-details-form>
    <file-previewer ref="file-previewer"></file-previewer>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: {
      type: Object,
      default: () => ({}),
    },
    objectType: {
      type: String,
      default: () => "",
    },
  },
  components: {
    "header-area": httpVueLoader(
      "/components/data-requests/data-completion-report/header-area.vue"
    ),
    "data-completion-report-table": httpVueLoader(
      "/components/data-requests/data-completion-report/data-completion-report-table.vue"
    ),
    "company-details-form": httpVueLoader(
      "/components/company-details-form.vue"
    ),
    "part-details-form": httpVueLoader("/components/part-details-form.vue"),
    "mold-details-form": httpVueLoader("/components/mold-details-form.vue"),
    "machine-details-form": httpVueLoader(
      "/components/machine-details-form.vue"
    ),
    "location-form": httpVueLoader("/components/location-form.vue"),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "common-select": httpVueLoader(
      "/components/@base/dropdown/common-select.vue"
    ),
  },
  setup(props, ctx) {
    // STATE //
    const searchText = ref("");
    const showFilter = ref(false);
    let companiesList = ref([]);
    const currentSortType = ref("id");
    const sortAsc = ref(true);
    const selectedCompanies = ref([]);
    const numberOfObjectType = ref(0);
    const completionRate = ref(0);
    const requestList = ref([]);
    const tableResults = ref({});
    const pagination = ref([]);
    const isLoading = ref(false);

    // COMPUTED //

    // const getSelectedType = computed(() => {
    //   return types.value
    // });

    // WATCH //
    watch(
      () => props.objectType,
      (newVal) => {
        console.log("props.objectType", newVal);
        if (newVal) {
          console.log("watch props.objectType getCompletionReportData");
          getCompletionReportData();
        }
      }
    );

    watch(
      () => searchText.value,
      (newVal) => {
        console.log("watch searchText.value getCompletionReportData");
        getCompletionReportData();
      }
    );

    // METHOD //

    const onOpenRequest = (item) => {
      ctx.emit("handle-click-request", item, true);
    };
    const handleToggleFilter = () => {
      showFilter.value = !showFilter.value;
    };
    const handleCloseFilter = () => {
      showFilter.value = false;
    };
    // const getCompanyList = async () => {
    //   const res = await axios.get('/api/companies?query&size=999999&status=active&sort=id%2Cdesc&page=1&forReport=true');
    //   companiesList.value = res.data.content.map(item => ({ ...item, title: item.name.slice(0, 15) + '...' }))
    // }

    watch(
      () => headerVm?.listCompanies,
      (newVal) => {
        console.log("header?.listCompanies", newVal);
        companiesList.value = newVal.map((item) => ({
          ...item,
          title: item.name
            ? item.name.length > 15
              ? item.name?.slice(0, 15) + "..."
              : item.name
            : "",
          checked: false,
        }));
      }
    );

    const onSelectCompany = (data) => {
      if (!selectedCompanies.value.includes(data)) {
        selectedCompanies.value.push(data);
      } else {
        selectedCompanies.value = selectedCompanies.value.filter(
          (company) => company.id !== data.id
        );
      }
      console.log(
        "onSelectCompany getCompletionReportData",
        data,
        selectedCompanies.value
      );
      getCompletionReportData();
    };

    const getCompletionReportData = async (pageNumber = 1) => {
      try {
        isLoading.value = true;
        if (props.objectType) {
          const param = Common.param({
            sort: currentSortType.value + "," + (sortAsc ? "asc" : "desc"),
            objectType: props.objectType,
            companyId: selectedCompanies.value.map((item) => item.id),
            isDataRequest: true,
            query: searchText.value,
            size: 15,
            page: pageNumber,
          });
          const res = await axios.get(`/api/data-completion?${param}`);
          numberOfObjectType.value = res.data.data.totalElements;
          completionRate.value = res.data.avgRate;
          requestList.value = res.data.dataRequestList;
          tableResults.value = res.data.data;
          pagination.value = Common.getPagingData(res.data.data);
          console.log("completion", res.data);
        }
      } catch (e) {
        console.log("error", e);
      } finally {
        isLoading.value = false;
      }
    };

    const handleShowCompletion = (item) => {
      console.log("handleShowCompletion", item, props.objectType);
      const page = "modal";
      switch (props.objectType) {
        case "COMPANY":
          showCompanyDetailsForm(item.data, page);
          break;
        case "TOOLING":
          let dataFake = item;
          dataFake.id = item.data.id;
          showMoldDetailsForm(dataFake, page);
          break;
        case "MACHINE":
          showMachineDetailsForm(item.data, page);
          break;
        case "PART":
          showPartDetailsForm(item.data, page);
          break;
        case "LOCATION":
          showLocationForm(item.data, page);
          break;
      }
    };

    const showCompanyDetailsForm = (company, page) => {
      console.log(`Company: `, company);
      var child = ctx.refs["company-details-form"];
      if (child) {
        child.showDetails(company, page);
      }
    };
    const showMachineDetailsForm = (company, page) => {
      var child = ctx.refs["machine-details-form"];
      if (child) {
        child.showDetails(company, page);
      }
    };
    const showPartDetailsForm = (part, page) => {
      var child = ctx.refs["part-details-form"];
      if (child) {
        child.showPartDetails(part, page);
      }
    };
    const showLocationForm = (location, page) => {
      console.log(location, "location");
      var child = ctx.refs["location-form"];
      if (child) {
        child.showForm(location, page);
      }
    };
    const showMoldDetailsForm = (mold, page) => {
      var child = ctx.refs["mold-details-form"];
      console.log("@showMoldDetailsForm", child, mold, page);
      if (child) {
        console.log("as");
        let reasonData = { reason: "", approvedAt: "" };
        if (mold.dataSubmission === "DISAPPROVED") {
          axios
            .get(`/api/molds/data-submission/${mold.id}/disapproval-reason`)
            .then(function (response) {
              reasonData = response.data;
              // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
              reasonData.approvedAt = reasonData.approvedAt
                ? vm.convertTimestampToDateWithFormat(
                    reasonData.approvedAt,
                    "MMMM DD YYYY HH:mm:ss"
                  )
                : "-";
              mold.notificationStatus = mold.dataSubmission;
              mold.reason = reasonData.reason;
              mold.approvedAt = reasonData.approvedAt;
              mold.approvedBy = reasonData.approvedBy;
              child.showMoldDetails(mold, page);
            });
        } else {
          child.showMoldDetails(mold, page);
        }
      }
    };

    const showFilePreviewer = (file) => {
      var child = ctx.refs["file-previewer"];
      if (child) {
        child.showFilePreviewer(file);
      }
    };

    const handleBack = () => {
      ctx.emit("back");
      companiesList.value = companiesList.value.map((item) => ({
        ...item,
        checked: false,
      }));
      selectedCompanies.value = [];
      console.log("handleBack", companiesList.value);
    };

    // created method //
    const initModal = async () => {
      selectedCompanies.value = [];
      completionRate.value = 0;
      // getCompanyList()
      searchText.value = "";
    };

    // CREATED //
    initModal();

    return {
      // STATE //
      searchText,
      showFilter,
      companiesList,
      numberOfObjectType,
      completionRate,
      requestList,
      tableResults,
      pagination,
      isLoading,
      // COMPUTED //
      // METHOD //
      handleToggleFilter,
      handleCloseFilter,
      onSelectCompany,
      onOpenRequest,
      getCompletionReportData,
      handleShowCompletion,
      showCompanyDetailsForm,
      showMachineDetailsForm,
      showPartDetailsForm,
      showLocationForm,
      showMoldDetailsForm,
      showFilePreviewer,
      handleBack,
    };
  },
};
</script>

<style scoped>
.back-title-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
}

.back-icon {
  display: flex;
  align-items: center;
}
</style>
