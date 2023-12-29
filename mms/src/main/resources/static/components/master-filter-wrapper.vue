<script>
/**
 * You must import `emdn-master-filter` from `@emoldino/components` to use this component.
 * using `Vue.use('emdn-master-filter', MasterFilter)`.
 *
 * See also: https://docs.google.com/presentation/d/12PjC5v3wI_5Qrpt0k5UxhLxMSflYlATO82cvoshJqag/edit#slide=id.g21fc669aeea_0_0
 */
module.exports = {
  props: {
    filterCode: {
      type: String,
      required: true,
    },
    notifyFilterUpdated: {
      type: Function,
      required: true,
    },
  },
  components: {
    "emdn-master-filter": EmoldinoComponents.MasterFilter,
  },
  methods: {
    /**
     * Get saved filter list of each resource types.
     * @param {string} filterCode Each filter has a unique code. But now there is only one filter named "COMMON".
     */
    async getSavedFilterList(filterCode) {
      const res = await axios.get(`/api/common/flt/${filterCode}`);
      return res.data;
    },

    /**
     * Get resource types that user can filter with each resource.
     * @param {string} filterCode Each filter has a unique code. But now there is only one filter named "COMMON".
     */
    async getFilterResourceTypes(filterCode) {
      const res = await axios.get(
        `/api/common/flt/${filterCode}/resource-types`
      );
      return res.data;
    },

    /**
     * Convert resource type to plural form for BackEnd API.
     * @param {string} resourceType
     */
    convertResourceType(resourceType) {
      if (resourceType === "TOOLING") return "molds";
      return resourceType.toLowerCase() + "s";
    },

    /**
     * Get filtered items. `page`, `size` and `query` are optional.
     * See also: https://docs.google.com/presentation/d/1twRaS_qA3zcC1hcswwlVUzugYxYjJwYC5sz0hFi_4cE/edit#slide=id.g20c2030ee16_0_10
     *
     * @param {Object} param
     * @param {string} param.filterCode Each filter has a unique code. But now there is only one filter named "COMMON".
     * @param {string} param.resourceType Resource type to filter.
     * @param {number=} param.page Page number.
     * @param {number=} param.size Page size.
     * @param {string=} param.query Query string.
     */
    async getFilterItems({ filterCode, resourceType, page, size, query }) {
      const res = await axios.get(
        `/api/common/flt/${this.convertResourceType(resourceType)}`,
        {
          params: {
            filterCode,
            page,
            size,
            query,
          },
        }
      );
      return res.data;
    },

    /**
     * Save filter items.
     * @param {Object} param Parameter for saving filter items.
     * @param {string} param.filterCode Each filter has a unique code. But now there is only one filter named "COMMON".
     * @param {string} param.mode Mode of saving filter items. "SELECTED" or "UNSELECTED".
     * @param {string} param.resourceType Resource type to filter.
     * @param {number[]=} param.selectedIds Array of selected item ids.
     * @param {number[]=} param.unselectedIds Array of unselected item ids.
     */
    async saveFilterItems({
      filterCode,
      mode,
      resourceType,
      selectedIds,
      unselectedIds,
    }) {
      const res = await axios.post(
        `/api/common/flt/${filterCode}/${resourceType}`,
        {
          filterCode,
          mode,
          selectedIds,
          unselectedIds,
        }
      );
      return res.data;
    },

    /**
     * Clear all filter items.
     * @param {string} filterCode Each filter has a unique code. But now there is only one filter named "COMMON".
     */
    clearAllFilterItems(filterCode) {
      const res = axios.delete(`/api/common/flt/${filterCode}`);
      return res.data;
    },

    /**
     * Clear filter items of `resourceType`j.
     * @param {string} filterCode Each filter has a unique code. But now there is only one filter named "COMMON".
     * @param {string} resourceType Resource type to filter.
     */
    clearFilterItems({ filterCode, resourceType }) {
      const res = axios.delete(`/api/common/flt/${filterCode}/${resourceType}`);
      return res.data;
    },
  },
};
</script>
<template>
  <emdn-master-filter
    :get-saved-filter-list="() => getSavedFilterList(filterCode)"
    :get-filter-resource-types="() => getFilterResourceTypes(filterCode)"
    :get-filter-items="(args) => getFilterItems({ filterCode, ...args })"
    :save-filter-items="(args) => saveFilterItems({ filterCode, ...args })"
    :clear-all-filter-items="() => clearAllFilterItems(filterCode)"
    :clear-filter-items="
      (resourceType) => clearFilterItems({ filterCode, resourceType })
    "
    :notify-filter-updated="notifyFilterUpdated"
  ></emdn-master-filter>
</template>
