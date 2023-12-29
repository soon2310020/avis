const _companyInstance = {
  address: "",
  companyCode: "",
  companyType: "",
  companyTypeText: "",
  createdAt: 0,
  createdDate: "",
  createdDateTime: "",
  email: null,
  emoldino: false,
  enabled: false,
  id: 0,
  manager: "",
  memo: "",
  moldCount: 0,
  name: "",
  partCount: 0,
  phone: "",
  predictedQuantity: 0,
  totalMaxCapacity: 0,
  totalProduced: 0,
  updatedAt: 0,
  updatedDate: "",
  updatedDateTime: "",
};

const VIEW_NAME = {
  LIST_SUPPLIERS: "LIST_SUPPLIERS",
  DETAIL_SUPPLIER: "DETAIL_SUPPLIER",
  LIST_TOOLINGS: "LIST_TOOLINGS",
};

module.exports = {
  name: "SourceSupplierModal",
  components: {
    "source-supplier-table": httpVueLoader(
      "/components/category/source-supplier/source-supplier-table.vue"
    ),
    "week-picker-popup": httpVueLoader(
      "/components/category/week-picker-popup.vue"
    ),
    "company-details-data": httpVueLoader(
      "/components/company/company-details-data.vue"
    ),
    "tooling-table": httpVueLoader(
      "/components/category/source-supplier/tooling-table.vue"
    ),
  },
  props: {
    isVisible: Boolean,
    selectedProductProp: {
      type: Object,
      default: () => null,
    },
    resources: Object,
    currentDate: Object,
  },
  setup(props, ctx) {
    const modalTitle = ref("Source Supplier");

    // dialog state
    const isVisibleRef = toRef(props, "isVisible");
    const showListProductsDropdown = ref(false);
    const showListPartsDropdown = ref(false);
    const breadcrumb = ref([]);
    const navigationStack = ref([
      {
        name: VIEW_NAME.LIST_SUPPLIERS,
        prev: "",
      },
    ]);
    const indicator = computed(
      () => navigationStack.value[navigationStack.value.length - 1].name
    );
    const isListSuppliers = computed(
      () => indicator.value === VIEW_NAME.LIST_SUPPLIERS
    );
    const isDetailSupplier = computed(
      () => indicator.value === VIEW_NAME.DETAIL_SUPPLIER
    );
    const isListToolings = computed(
      () => indicator.value === VIEW_NAME.LIST_TOOLINGS
    );

    // pure state
    const listToolings = ref([]);
    const selectedSupplier = ref({ ..._companyInstance });
    const selectedSupplierId = computed(() => selectedSupplier.value?.id || "");
    const selectedSupplierName = computed(
      () => selectedSupplier.value?.name || ""
    );
    const timeRange = ref({
      minDate: null,
      maxDate: new Date("2100-01-01"),
    });

    // list options
    const listProducts = ref([]);
    const listParts = ref([]);

    // state
    const selectedProduct = ref(null);
    const selectedProductId = computed(() => selectedProduct?.value?.id || "");
    const selectedPart = ref(null);
    const selectedPartId = computed(() => selectedPart?.value?.id || "");
    const productionDemand = ref(0);
    const productionDemandFormatted = computed(() => {
      return Common.formatter.appendThousandSeperator(productionDemand.value);
    });
    const unfulfilledDemand = ref(0);
    const unfulfilledDemandFormatted = computed(() => {
      return Common.formatter.appendThousandSeperator(unfulfilledDemand.value);
    });
    const listSuppliers = ref([]);
    const timeState = ref({
      from: new Date(),
      to: new Date(),
      fromTitle: moment().format("YYYY-ww"),
      toTitle: moment().format("YYYY-ww"),
    });
    watch(
      () => props.currentDate,
      () => Object.assign(timeState.value, props.currentDate)
    );
    const periodValue = computed(() =>
      timeState?.value?.fromTitle.replaceAll("-", "")
    );

    // pagination
    const toolingPagination = reactive({
      currentPage: 0,
      totalPage: 0,
      size: 10,
    });
    const currentPage = computed(() => toolingPagination.currentPage + 1);
    const totalPage = computed(() => toolingPagination.totalPage);

    // api
    const fetchListProducts = async () => {
      try {
        const response = await axios.get(`/api/products/lite?size=1000`);
        if (!response || !response.data || !response.data.content)
          throw new Error();
        listProducts.value =
          response.data?.content?.map((item) => ({
            ...item,
            title: item.name,
          })) || [];
      } catch (error) {
        console.log(error);
      }
    };

    const fetchListParts = async () => {
      const param = Common.param({
        productId: selectedProductId.value,
        size: 1000,
      });
      try {
        const response = await axios.get(`/api/parts/lite?${param}`);
        if (!response || !response.data || !response.data.content)
          throw new Error();
        listParts.value =
          response.data?.content?.map((item) => ({
            ...item,
            title: item.name,
          })) || [];
      } catch (error) {
        console.log(error);
      }
    };

    const fetchListSuppliers = async () => {
      const param = Common.param({
        productId: selectedProductId.value,
        partId: selectedPartId.value,
        periodType: "WEEKLY",
        periodValue: periodValue.value,
      });
      try {
        const response = await axios.get(`/api/supplier-sourcing?${param}`);
        if (!response || !response.data || !response.data.content)
          throw new Error();
        productionDemand.value = response.data.totalProductionDemand;
        unfulfilledDemand.value = response.data.unfulfilledDemand;
        listSuppliers.value = response.data.content;
      } catch (error) {
        console.log(error);
      }
    };

    const applySupplierSourcing = async () => {
      const param = Common.param({
        productId: selectedProductId.value,
        partId: selectedPartId.value,
        periodType: "WEEKLY",
        periodValue: periodValue.value,
      });
      const payload = {};
      payload.totalProductionDemand = productionDemand.value;
      payload.unfulfilledDemand = unfulfilledDemand.value;
      payload.content = listSuppliers.value.map((item) => ({
        ...item,
        totalProductionDemand:
          Math.floor(Number(item.totalProductionDemand)) || 0,
      }));
      try {
        const response = await axios.post(
          `/api/supplier-sourcing/temp?${param}`,
          payload
        );
        unfulfilledDemand.value = response.data.unfulfilledDemand;
        productionDemand.value = response.data.totalProductionDemand;
        listSuppliers.value = response.data.content;
      } catch (error) {
        console.log(error);
      }
    };

    const updateSupplierSourcing = async () => {
      const param = Common.param({
        productId: selectedProductId.value,
        partId: selectedPartId.value,
        periodType: "WEEKLY",
        periodValue: periodValue.value,
      });
      const payload = {};
      payload.totalProductionDemand = productionDemand.value;
      payload.unfulfilledDemand = unfulfilledDemand.value;
      payload.content = listSuppliers.value.map((item) => ({
        ...item,
        totalProductionDemand:
          Math.floor(Number(item.totalProductionDemand)) || 0,
      }));
      try {
        const response = await axios.post(
          `/api/supplier-sourcing?${param}`,
          payload
        );
        Common.alert("success");
      } catch (error) {
        console.log(error);
      }
    };

    const fetchDetailSupplier = async (supplierId) => {
      try {
        const response = await axios.get(`/api/companies/${supplierId}`);
        selectedSupplier.value = response.data;
      } catch (error) {
        console.log(error);
      }
    };

    const fetchListToolings = async () => {
      const param = Common.param({
        projectId: selectedProductId.value,
        supplierId: selectedSupplierId.value,
        partId: selectedPartId.value,
        periodType: "WEEKLY",
        periodValue: periodValue.value,
        page: toolingPagination.currentPage,
        size: toolingPagination.size,
      });
      try {
        const response = await axios.get(
          `/api/categories/mold-by-project?${param}`
        );
        console.log("response", response);
        listToolings.value = response.data.data.content;
        toolingPagination.totalPage = response.data.data.totalPages;
      } catch (error) {
        console.log(error);
      }
    };

    // handler
    const handleCloseDialog = () => {
      ctx.emit("close");
      _cleanState();
    };
    const handleShowListProducts = () => {
      showListProductsDropdown.value = true;
      showListPartsDropdown.value = false;
    };
    const handleShowListParts = () => {
      showListProductsDropdown.value = false;
      showListPartsDropdown.value = true;
    };
    const handleSelectProduct = (event) => {
      selectedProduct.value = JSON.parse(event.target.value);
      showListProductsDropdown.value = false;
    };
    const handleSelectPart = (event) => {
      selectedPart.value = JSON.parse(event.target.value);
      showListPartsDropdown.value = false;
    };
    const handleChangeDate = (date) => {
      timeState.value = date;
      console.log(timeState.value);
    };
    const handleSubmit = () => updateSupplierSourcing();
    const handleChangeProductionDemand = () => applySupplierSourcing();
    const handleClickCell = (row, col, index) => {
      if (col.field === "name") {
        _navigateNext(VIEW_NAME.DETAIL_SUPPLIER);
        fetchDetailSupplier(row.id);
      }
      if (col.field === "moldCount") {
        selectedSupplier.value = row;
        _navigateNext(VIEW_NAME.LIST_TOOLINGS);
        fetchListToolings();
      }
    };
    const handleNavigateBack = (viewName) => _navigateBack(viewName);

    const handleChangePage = (type) => {
      if (type === "increment" && currentPage.value < totalPage.value) {
        toolingPagination.value++;
      }
      if (type === "decrement" && currentPage.value <= 1) {
        toolingPagination.value--;
      }
    };

    // internal function
    const _initCurrentDate = () => {
      const today = new Date();
      timeState.value.from = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay()
      );
      timeState.value.to = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay() + 6
      );
      const weekFrom = Common.getCurrentWeekNumber(timeState.value.from);
      const weekTo = Common.getCurrentWeekNumber(timeState.value.to);
      timeState.value.fromTitle = `${timeState.value.from.getFullYear()}-${weekFrom}`;
      timeState.value.toTitle = `${timeState.value.from.getFullYear()}-${weekTo}`;
    };
    const _cleanState = () => {
      showListProductsDropdown.value = false;
      showListPartsDropdown.value = false;
      listParts.value = [];
      listProducts.value = [];
      listSuppliers.value = [];
      selectedProduct.value = null;
      selectedPart.value = null;
      selectedSupplier.value = { ..._companyInstance };
      productionDemand.value = 0;
      unfulfilledDemand.value = 0;
      navigationStack.value = [{ name: VIEW_NAME.LIST_SUPPLIERS, prev: "" }];
    };
    const _navigateNext = (viewName) => {
      const _currentViewIndex = navigationStack.value.length - 1;
      const nextView = {
        name: viewName,
        prev: navigationStack.value[_currentViewIndex],
      };
      navigationStack.value.push(nextView);
    };
    const _navigateBack = (viewName) => {
      if (!viewName) navigationStack.value.pop();
      else {
      }
    };

    // track
    watch(
      () => props.selectedProductProp,
      (newVal) => {
        selectedProduct.value = { ...newVal };
      }
    );
    watch(selectedProductId, (newVal, prevVal) => {
      if (prevVal) {
        selectedPart.value = null;
        listSuppliers.value = [];
      }
      newVal && fetchListParts();
    });
    watch(isVisibleRef, (newVal) => newVal && fetchListProducts());
    watch(selectedPartId, (newVal) => newVal && fetchListSuppliers());
    watch(timeState, (newVal) => newVal && fetchListSuppliers());
    watch(toolingPagination, (newVal) => newVal && fetchListToolings(), {
      deep: true,
    });
    watch(
      () => props.currentDate,
      (newVal) => Object.assign(timeState.value, newVal)
    );

    // life cycle
    onMounted(() => _initCurrentDate());

    // debug
    // watchEffect(() => console.log("isVisible", isVisible.value));
    // watchEffect(() => console.log("indicator", indicator.value));
    // watchEffect(() => console.log("listParts", listParts.value));
    // watchEffect(() => console.log("listProducts", listProducts.value));
    // watchEffect(() => console.log("listSuppliers", listSuppliers.value))
    // watchEffect(() => console.log("selectedProduct", selectedProduct.value));
    // watchEffect(() => console.log("selectedPart", selectedPart.value));
    // watchEffect(() => console.log("selectedSupplier", selectedSupplier.value))
    // watchEffect(() => console.log("timeState", timeState.value))
    // watchEffect(() => console.log("productionDemandFormatted", productionDemandFormatted.value))
    // watchEffect(() => console.log("unfulfilledDemandFormatted", unfulfilledDemandFormatted.value))
    // watchEffect(() => console.log("navigationStack", navigationStack.value))
    // watchEffect(() => console.log("listToolings", listToolings.value))
    // watchEffect(() => console.log("toolingPagination", toolingPagination))
    // watchEffect(() => console.log("currentPage", currentPage.value))
    // watchEffect(() => console.log("totalPage", totalPage.value))

    return {
      modalTitle,
      showListProductsDropdown,
      showListPartsDropdown,
      breadcrumb,
      isListSuppliers,
      isListToolings,
      isDetailSupplier,
      selectedProduct,
      selectedPart,
      selectedSupplier,
      selectedSupplierName,
      productionDemandFormatted,
      unfulfilledDemandFormatted,
      timeState,
      timeRange,
      listProducts,
      listParts,
      listSuppliers,
      listToolings,
      toolingPagination,
      currentPage,
      totalPage,
      handleSelectProduct,
      handleSelectPart,
      handleShowListProducts,
      handleShowListParts,
      handleChangeDate,
      handleCloseDialog,
      handleSubmit,
      handleChangeProductionDemand,
      handleClickCell,
      handleNavigateBack,
      handleChangePage,
    };
  },
};
