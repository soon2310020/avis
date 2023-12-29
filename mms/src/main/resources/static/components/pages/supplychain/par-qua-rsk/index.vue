<script>
module.exports = {
  name: "par-qua-rsk-page",
  components: {
    "master-filter-wrapper": httpVueLoader(
      "/components/master-filter-wrapper.vue"
    ),
  },
  setup() {
    const filterCode = ref("COMMON");
    const notifyCount = ref(0);
    const fromDate = ref(
      moment().clone().startOf("isoWeek").format("YYYYMMDD")
    );
    const toDate = ref(moment().clone().endOf("isoWeek").format("YYYYMMDD"));
    const timeScale = ref("WEEK");
    const dateRange = ref({ start: moment(), end: moment() });

    const displayDate = computed(() => {
      return Common.getDatePickerButtonDisplay(
        fromDate.value,
        toDate.value,
        timeScale.value
      );
    });

    const timeValue = computed(() => {
      return Common.getDatePickerTimeValue(
        fromDate.value,
        toDate.value,
        timeScale.value
      );
    });

    const getPartQualityRisk = async ({ page, size, sort }) => {
      try {
        const { data } = await axios.get(`/api/supplychain/par-qua-rsk`, {
          params: {
            page,
            size,
            sort,
            filterCode: filterCode.value,
            timeScale: timeScale.value,
            timeValue: timeValue.value,
            fromDate: fromDate.value,
            toDate: toDate.value,
          },
        });

        return data;
      } catch (error) {
        console.warn(error);
      }
    };

    const getHeatmapItems = async ({ moldId }) => {
      try {
        const { data } = await axios.get(
          `/api/supplychain/par-qua-rsk/heatmap`,
          {
            params: {
              moldId,
              filterCode: filterCode.value,
              timeScale: timeScale.value,
              timeValue: timeValue.value,
              fromDate: fromDate.value,
              toDate: toDate.value,
            },
          }
        );

        return data;
      } catch (error) {
        console.warn(error);
      }
    };

    const getMoldItems = async ({ page, size, query }) => {
      try {
        const { data } = await axios.get(`/api/supplychain/par-qua-rsk/molds`, {
          params: {
            page,
            size,
            query,
            timeScale: timeScale.value,
            timeValue: timeValue.value,
            fromDate: fromDate.value,
            toDate: toDate.value,
          },
        });

        return data;
      } catch (error) {
        console.warn(error);
      }
    };

    const getDateRange = (range) => {
      dateRange.value = range;
      fromDate.value = range.start.format("YYYYMMDD");
      toDate.value = range.end.format("YYYYMMDD");
    };

    return {
      filterCode,
      notifyCount,
      timeScale,
      dateRange,
      displayDate,
      getPartQualityRisk,
      getHeatmapItems,
      getMoldItems,
      getDateRange,
    };
  },
};
</script>

<template>
  <div class="pqr-main-container">
    <div class="pqr-header-container">
      <div>
        <master-filter-wrapper
          :filter-code="filterCode"
          :notify-filter-updated="() => notifyCount++"
        ></master-filter-wrapper>
      </div>
      <div>
        <emdn-dropdown-root position="bottom-right">
          <emdn-dropdown-trigger>
            <emdn-cta-button button-type="date-picker" color-type="blue">{{
              displayDate
            }}</emdn-cta-button>
          </emdn-dropdown-trigger>
          <emdn-dropdown-portal>
            <emdn-dropdown-content>
              <emdn-date-picker
                :time-scale="timeScale"
                :date-range="dateRange"
                @get-date-range="getDateRange"
              ></emdn-date-picker>
            </emdn-dropdown-content>
          </emdn-dropdown-portal>
        </emdn-dropdown-root>
      </div>
    </div>

    <div class="pqr-body-container">
      <part-quality-risk-content
        :refetch-trigger="notifyCount"
        :display-date="displayDate"
        :get-part-quality-risk="getPartQualityRisk"
        :get-heatmap-items="getHeatmapItems"
        :get-mold-items="getMoldItems"
      ></part-quality-risk-content>
    </div>
  </div>
</template>

<style>
.pqr-main-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 2rem 1.25rem;
}

.pqr-header-container {
  width: 100%;
  height: 2.75rem;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-shrink: 0;
}

.pqr-body-container {
  flex-grow: 1;
  overflow: hidden;
}
</style>
