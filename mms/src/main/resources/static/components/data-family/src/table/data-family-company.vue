<template>
  <div>
    <div class="row">
      <div class="col-lg-12">
        <div>
          <div class="card-header" style="display: none">
            <i class="fa fa-align-justify"></i>
            <span>{{ resources["striped_tbl"] }}</span>
          </div>
          <div class="card-overflow-reset">
            <div class="data-table-tabs d-flex justify-space-between">
              <ul
                class="nav nav-tabs"
                style="margin-bottom: -1px; width: calc(100% - 210px)"
              >
                <template v-for="(tabItem, index) in listSelectedTabs">
                  <li
                    v-if="
                      tabItem.show ||
                      (tabItem.isDefaultTab && tabItem.name === 'Enabled')
                    "
                    :key="index"
                    class="nav-item"
                  >
                    <a
                      class="nav-link"
                      :class="{ active: checkStatusParam(tabItem) }"
                      href="javascript:void(0)"
                      @click.prevent="tab(tabItem)"
                    >
                      <div
                        v-if="tabItem.name && tabItem.name.length > 20"
                        class="part-name-drop-down one-line"
                      >
                        <a-tooltip placement="bottom">
                          <template slot="title">
                            <div style="padding: 6px; font-size: 13px">
                              {{ tabItem.name }}
                            </div>
                          </template>
                          <span>
                            {{ truncateText(tabItem.name, 20) }}
                          </span>
                        </a-tooltip>
                      </div>
                      <span v-else class="part-name-drop-down one-line">
                        {{ truncateText(tabItem.name, 20) }}
                      </span>
                      <span class="badge badge-light badge-pill">{{
                        tabItem.totalItem
                      }}</span>
                      <span
                        v-if="
                          tabItem.name !== 'Enabled' || !tabItem.isDefaultTab
                        "
                        @click.stop="closeTab(tabItem)"
                        class="close-tab"
                      >
                        <svg
                          width="9"
                          height="8"
                          viewBox="0 0 9 8"
                          fill="none"
                          xmlns="http://www.w3.org/2000/svg"
                        >
                          <path
                            d="M8.08333 0.76375L7.31958 0L4.29167 3.02792L1.26375 0L0.5 0.76375L3.52792 3.79167L0.5 6.81958L1.26375 7.58333L4.29167 4.55542L7.31958 7.58333L8.08333 6.81958L5.05542 3.79167L8.08333 0.76375Z"
                            fill="#888888"
                          />
                        </svg>
                      </span>
                    </a>
                  </li>
                </template>
              </ul>
              <div class="data-table-top-action" style="width: 210px">
                <customization-modal
                  :all-columns-list="allColumnList"
                  @save="saveSelectedList"
                  :resources="resources"
                  :is-more-action="true"
                ></customization-modal>
                <div
                  class="assign-more d-flex align-items-center position-relative"
                >
                  <div>
                    <a
                      @click="Common.onChangeHref('/admin/companies/new')"
                      href="javascript:void(0)"
                      class="btn-custom btn-custom-primary animationPrimary all-time-filters btn-custom-focus"
                    >
                      <span>{{ resources["new_company"] }}</span>
                    </a>
                  </div>
                </div>
                <a-popover
                  v-model="visibleMoreAction"
                  placement="bottomLeft"
                  trigger="click"
                >
                  <a-tooltip color="white">
                    <template slot="title">
                      <div
                        style="
                          padding: 6px 8px;
                          font-size: 13px;
                          color: #4b4b4b;
                          background: #fff;
                          border-radius: 4px;
                          height: 32px;
                          box-shadow: 0px 0px 4px 1px #e5dfdf;
                        "
                      >
                        More
                        <div class="custom-arrow-tooltip"></div>
                      </div>
                    </template>
                    <a
                      style="padding: 6px"
                      @click.stop="handleToggleMoreActions"
                      href="javascript:void(0)"
                      class="customize-hint btn-custom customize-btn btn-outline-custom-primary"
                    >
                      <svg
                        width="16"
                        height="16"
                        viewBox="0 0 16 16"
                        fill="none"
                        xmlns="http://www.w3.org/2000/svg"
                      >
                        <path
                          fill-rule="evenodd"
                          clip-rule="evenodd"
                          d="M8.5002 13.6C7.56133 13.6 6.80021 14.3611 6.80021 15.3C6.80021 16.2389 7.56133 17 8.5002 17C9.43908 17 10.2002 16.2389 10.2002 15.3C10.2002 14.3611 9.43908 13.6 8.5002 13.6V13.6ZM8.5002 6.80003C7.56133 6.80003 6.80021 7.56115 6.80021 8.50003C6.80021 9.4389 7.56133 10.2 8.5002 10.2C9.43908 10.2 10.2002 9.4389 10.2002 8.50003C10.2002 7.56115 9.43908 6.80003 8.5002 6.80003V6.80003ZM8.5002 0C7.56133 0 6.80021 0.761119 6.80021 1.69999C6.80021 2.63887 7.56133 3.39999 8.5002 3.39999C9.43908 3.39999 10.2002 2.63887 10.2002 1.69999C10.2002 0.761119 9.43908 0 8.5002 0V0Z"
                          fill="#3491FF"
                        />
                      </svg>
                    </a>
                  </a-tooltip>
                  <a-menu
                    v-if="!isTabView"
                    slot="content"
                    class="wrapper-action-dropdown"
                    style="
                      border-right: unset !important;
                      max-height: 250px;
                      overflow: auto;
                    "
                  >
                    <a-menu-item @click="isTabView = true">
                      <span>Tab view</span>
                    </a-menu-item>
                    <a-menu-item @click="openCustomizationModal()">
                      <span>Column customization</span>
                    </a-menu-item>
                  </a-menu>
                  <div v-else slot="content">
                    <ul class="child-item">
                      <li
                        v-for="(subItem, index2) in listSelectedTabs"
                        :key="`${index2}`"
                      >
                        <label :for="`dropdown-input-${index2}`">
                          <template>
                            <input
                              :id="`dropdown-input-${index2}`"
                              type="checkbox"
                              :value="subItem.id"
                              :checked="subItem.show"
                              :disabled="
                                subItem.name === 'Enabled' &&
                                subItem.isDefaultTab
                              "
                              @change="selectTab(subItem, index2)"
                            />
                            <div
                              class="checkbox-custom"
                              :class="{
                                checkboxDisabled:
                                  subItem.name === 'Enabled' &&
                                  subItem.isDefaultTab,
                              }"
                            ></div>
                          </template>
                          {{ convertCodeToName(subItem) }}
                        </label>
                      </li>
                    </ul>
                    <ul class="child-item">
                      <li>
                        <label>
                          <a href="javascript:void(0)" @click="createTabView()"
                            >+ Create tab view</a
                          >
                        </label>
                      </li>
                      <li>
                        <label>
                          <a
                            href="javascript:void(0)"
                            @click="openManageTabView"
                            >Manage tab view</a
                          >
                        </label>
                      </li>
                    </ul>
                  </div>
                </a-popover>
              </div>
            </div>

            <table
              class="table table-responsive-sm table-striped custom-table-container"
            >
              <colgroup>
                <col />
                <col />
                <col />
                <col />
                <col />
              </colgroup>
              <thead id="thead-actionbar" class="custom-header-table">
                <tr
                  :class="{ invisible: listChecked.length != 0 }"
                  style="height: 57px"
                  class="tr-sort"
                >
                  <th class="custom-td">
                    <div class="custom-td_header">
                      <select-all
                        :resources="resources"
                        :show="allColumnList.length > 0"
                        :checked="isAll"
                        :total="total"
                        :count="results.length"
                        :unit="['company', 'companies']"
                        @select-all="handleSelectAll('all')"
                        @select-page="handleSelectAll('page')"
                        @deselect="handleSelectAll('deselect')"
                      ></select-all>
                    </div>
                  </th>
                  <template v-for="(item, index) in allColumnList">
                    <th
                      v-if="item.enabled && !item.hiddenInToggle"
                      :key="index"
                      :class="[
                        {
                          __sort: item.sortable,
                          active: currentSortType === item.sortField,
                        },
                        isDesc ? 'desc' : 'asc',
                        item.label === 'Status' ? '' : 'text-left',
                      ]"
                      @click="sortBy(item.sortField)"
                    >
                      <span v-if="item.field === 'name'">{{
                        resources["company_name"]
                      }}</span>
                      <span v-else-if="item.field === 'companyCode'">{{
                        resources["company_id"]
                      }}</span>
                      <span v-else-if="item.field === 'address'">{{
                        resources["address"]
                      }}</span>
                      <span v-else-if="item.field === 'enable'">{{
                        resources["status"]
                      }}</span>
                      <span v-else-if="item.field === 'moldCount'">{{
                        resources["total_mold_count"]
                      }}</span>
                      <span v-else>{{ item.label }}</span>
                      <span v-if="item.sortable" class="__icon-sort"></span>
                    </th>
                  </template>
                </tr>
                <tr
                  :class="{ 'd-none zindexNegative': listChecked.length == 0 }"
                  id="action-bar"
                  class="tr-sort empty-tr"
                >
                  <th class="empty-th">
                    <div class="first-checkbox-zone2">
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
                          <select-all
                            :resources="resources"
                            :show="allColumnList.length > 0"
                            :checked="isAll"
                            :total="total"
                            :count="results.length"
                            :unit="['company', 'companies']"
                            @select-all="handleSelectAll('all')"
                            @select-page="handleSelectAll('page')"
                            @deselect="handleSelectAll('deselect')"
                          ></select-all>
                        </div>
                      </div>
                      <div class="action-bar">
                        <div
                          v-if="
                            listChecked.length == 1 &&
                            requestParam.status != 'DELETED'
                          "
                          @click="goToEdit"
                          class="action-item"
                        >
                          <span>Edit</span>
                          <i class="icon-action edit-icon"></i>
                        </div>
                        <div
                          v-if="
                            listChecked.length == 1 &&
                            requestParam.status != 'DELETED'
                          "
                          @click="showRevisionHistory(listChecked[0])"
                          class="action-item"
                        >
                          <span>View Edit History</span>
                          <i class="icon-action view-edit-history-icon"></i>
                        </div>
                        <div
                          v-if="requestParam.status != 'DELETED'"
                          class="action-item"
                          @click="showSystemNoteModal()"
                        >
                          <span>Memo</span>
                          <i class="icon-action memo-icon"></i>
                        </div>
                        <div
                          v-if="
                            requestParam.status !== 'disabled' &&
                            requestParam.status != 'DELETED'
                          "
                          class="action-item"
                          @click.prevent="disable()"
                        >
                          <span>Disable</span>
                          <div class="icon-action disable-icon"></div>
                        </div>
                        <div
                          v-if="
                            requestParam.status === 'disabled' &&
                            requestParam.status != 'DELETED'
                          "
                          class="action-item"
                          @click.prevent="enable()"
                        >
                          <span>Enable</span>
                          <div class="icon-action enable-icon"></div>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            !currentActiveTab.isDefaultTab
                          "
                          class="action-item"
                        >
                          <move-to-action
                            :type-view="'company'"
                            :visible-move="visibleMove"
                            :current-active-tab="currentActiveTab"
                            :resources="resources"
                            :list-checked="
                              total === listAllIds.length
                                ? listAllIds
                                : listChecked
                            "
                            @move-success="moveSuccess"
                            :list-selected-tabs="listSelectedTabs"
                            @open-move="visibleMove = true"
                            @create-tab-view="createTabView"
                          ></move-to-action>
                        </div>

                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            !currentActiveTab.isDefaultTab
                          "
                          class="action-item"
                          @click="removeFromTab"
                        >
                          <span class="mr-1">Remove From Tab</span>
                          <svg
                            width="14"
                            height="13"
                            viewBox="0 0 14 13"
                            fill="none"
                            xmlns="http://www.w3.org/2000/svg"
                          >
                            <path
                              fill-rule="evenodd"
                              clip-rule="evenodd"
                              d="M2.88267 3.55238C2.78294 3.88285 2.73763 4.35445 2.73763 5.04427V7.95626C2.73763 8.53627 2.76942 8.96538 2.8411 9.28794C2.91142 9.60438 3.01274 9.78411 3.12909 9.90046C3.24544 10.0168 3.42518 10.1182 3.7416 10.1885C4.06417 10.2601 4.49329 10.2919 5.0733 10.2919H9.06863C10.0399 10.2919 10.5581 10.1994 10.8558 10.0216L11.4112 10.9517C10.8311 11.2981 10.0343 11.3753 9.06863 11.3753H5.0733C4.46856 11.3753 3.94564 11.3436 3.50658 11.246C3.06135 11.1471 2.66986 10.9733 2.36306 10.6665C2.05626 10.3597 1.88252 9.96822 1.78357 9.52297C1.686 9.0839 1.6543 8.56103 1.6543 7.95626V5.04427C1.6543 4.33306 1.69776 3.72906 1.84554 3.23938C2.00047 2.72602 2.27596 2.32041 2.73074 2.04883L3.28619 2.97893C3.10601 3.08653 2.97526 3.2456 2.88267 3.55238Z"
                              fill="#4B4B4B"
                            />
                            <path
                              fill-rule="evenodd"
                              clip-rule="evenodd"
                              d="M4.89551 2.16667C4.89551 1.86751 5.13802 1.625 5.43717 1.625H5.94939C6.49271 1.625 7.00009 1.89654 7.30148 2.34861L7.74158 3.0088C7.84206 3.15948 8.01117 3.25 8.1923 3.25H10.9306C11.796 3.25 12.4974 3.93312 12.4891 4.81282C12.4882 4.90968 12.4875 4.98977 12.4875 5.044V7.956C12.4875 8.22239 12.4813 8.47075 12.4665 8.70133C12.4474 8.99985 12.1899 9.22637 11.8913 9.20725C11.5928 9.18808 11.3663 8.93057 11.3854 8.632C11.3983 8.43072 11.4042 8.20647 11.4042 7.956V5.044C11.4042 4.9838 11.4049 4.89856 11.4058 4.80263C11.4082 4.53957 11.2081 4.33333 10.9306 4.33333H8.1923C7.64901 4.33333 7.14158 4.0618 6.84019 3.60972L6.40009 2.94954C6.29961 2.79885 6.1305 2.70833 5.94939 2.70833H5.43717C5.13802 2.70833 4.89551 2.46582 4.89551 2.16667Z"
                              fill="#4B4B4B"
                            />
                            <path
                              fill-rule="evenodd"
                              clip-rule="evenodd"
                              d="M1.81295 1.24214C2.02448 1.03061 2.36744 1.03061 2.57898 1.24214L12.329 10.9921C12.5405 11.2037 12.5405 11.5467 12.329 11.7582C12.1175 11.9697 11.7745 11.9697 11.563 11.7582L1.81295 2.00818C1.60141 1.79664 1.60141 1.45368 1.81295 1.24214Z"
                              fill="#4B4B4B"
                            />
                          </svg>
                        </div>
                      </div>
                    </div>
                  </th>
                </tr>
              </thead>
              <tbody class="op-list">
                <tr
                  v-for="(result, index) in results"
                  :key="index"
                  :id="result.id"
                >
                  <td class="custom-td">
                    <template v-if="listChecked.length >= 0">
                      <input
                        @click="(e) => check(e)"
                        :checked="checkSelect(result.id)"
                        class="checkbox"
                        type="checkbox"
                        :id="result.id + 'checkbox'"
                        :value="result.id"
                      />
                    </template>
                  </td>
                  <template
                    v-for="(column, index) in allColumnList"
                    :key="index"
                    v-if="
                      (column.enableTemp ||
                        (!column.disableTemp && column.enabled)) &&
                      !column.hiddenInToggle
                    "
                  >
                    <td class="text-left" v-if="column.field == 'name'">
                      <div>
                        <a href="#" @click.prevent="showCompanyDetails(result)">
                          {{ result.name }}
                        </a>
                      </div>

                      <div class="small text-muted font-size-11-2">
                        <span>{{ resources["registered"] }}:</span>
                        {{ formatToDate(result.createdAt) }}
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field == 'companyCode'"
                    >
                      {{ result.companyCode }}
                    </td>
                    <td
                      class="text-left company-address"
                      v-else-if="column.field == 'address'"
                      data-html="true"
                    >
                      <a-tooltip
                        v-if="result.address && result.address.length > 40"
                        placement="bottom"
                      >
                        <template slot="title">
                          <div class="tooltips-content">
                            <span>{{ result.address }}</span>
                          </div>
                        </template>
                        <span>{{ replaceLongtext(result.address, 40) }}</span>
                      </a-tooltip>
                      <span v-else>{{ result.address }}</span>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                      v-else-if="column.field == 'enable'"
                    >
                      <span v-if="result.enabled" class="label label-success">{{
                        resources["enabled"]
                      }}</span>
                      <span
                        v-else-if="!result.enabled"
                        class="label label-danger"
                        >{{ resources["disabled"] }}</span
                      >
                    </td>
                    <td
                      class="text-left cta-anchor"
                      v-else-if="
                        column.field == 'moldCount' &&
                        result.moldCount != undefined
                      "
                    >
                      <a
                        href="javascript:;"
                        @click="directLink('TOOLING', { companyId: result.id })"
                      >
                        {{ result.moldCount }}
                      </a>
                      <span
                        style="display: block"
                        v-if="result.moldCount <= 1"
                        >{{ resources["tooling"] }}</span
                      >
                      <span v-else style="display: block">
                        {{ resources["toolings"] }}
                      </span>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="
                        column.field == 'moldCount' &&
                        result.moldCount == undefined
                      "
                    ></td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'upperTierCompanies'"
                      style="max-width: 600px"
                    >
                      <upper-company-dropdown
                        v-if="
                          result.upperTierCompanies &&
                          result.upperTierCompanies.length > 0
                        "
                        :data-list="result.upperTierCompanies"
                        :index="index"
                        :show-company-details-by-id="showCompanyDetailsById"
                      >
                      </upper-company-dropdown>
                    </td>
                    <td class="text-left" v-else>{{ result[column.field] }}</td>
                  </template>
                </tr>
              </tbody>
            </table>

            <div class="no-results d-none" style="margin-top: -1rem">
              {{ resources["no_results"] }}
            </div>

            <div class="row">
              <div class="col-md-8">
                <ul class="pagination" style="display: flex">
                  <template v-for="(data, index) in pagination" :key="index">
                    <li class="page-item" :class="{ active: data.isActive }">
                      <a class="page-link" @click="paging(data.pageNumber)">{{
                        data.text
                      }}</a>
                    </li>
                  </template>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <revision-history :resources="resources"></revision-history>
    <system-note
      ref="system-note"
      system-note-function="COMPANY_SETTING"
      :resources="resources"
    ></system-note>
    <action-bar-feature feature="ACTION_BAR"></action-bar-feature>
    <manage-tab-view
      :resources="resources"
      :object-type="'COMPANY'"
      @open-edit="openEdit"
      @open-duplicate="openDuplicate"
      @move-success="getTabsByCurrentUser"
      :trigger-update="triggerUpdateManageTab"
    ></manage-tab-view>
    <tab-view-modal
      :resources="resources"
      :object-type="'COMPANY'"
      :all-tooling-list="allToolingList"
      :page-type="pageType"
      @edit-success="editSuccess"
      @create-success="createSuccess"
      @show-company-details="showCompanyDetails"
    ></tab-view-modal>
    <toast-alert
      @close-toast="closeToastAlert"
      :show="toastAlert.value"
      :title="toastAlert.title"
      :content="toastAlert.content"
      ref-custom="toolingToast"
    ></toast-alert>
    <company-details :resources="resources"></company-details>
  </div>
</template>

<script>
var Page = Common.getPage("companies");
module.exports = {
  components: {
    "company-details": httpVueLoader("/components/company-details.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "upper-company-dropdown": httpVueLoader(
      "/components/upper-company-dropdown.vue"
    ),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-company-modal.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action.vue"),
  },
  props: {
    searchText: String,
    reloadKey: [String, Number],
  },
  data() {
    return {
      // it's not worked until we import Common here
      Common,

      // end
      userType: "",
      showDrowdown: null,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "active",
        sort: "id,desc",
        page: 1,
        tabId: "",
      },
      codes: {},
      sortType: {
        NAME: "name",
        COMPANY_CODE: "companyCode",
        ADDRESS: "address",
        STATUS: "enabled",
        TOTAL_MOLD_COUNT: "total_mold_count",
      },
      currentSortType: "id",
      isDesc: true,
      resources: {},
      showDropdown: false,
      listChecked: [],
      listCheckedFull: [],
      listAllIds: [],
      allColumnList: [],
      pageType: "COMPANY_SETTING",
      objectType: "COMPANY",
      cancelToken: undefined,
      visiblePopover: false,
      listSelectedTabs: [],
      currentActiveTab: null,
      visibleMoreAction: false,
      isTabView: false,
      allToolingList: [],
      triggerUpdateManageTab: 0,
      toastAlert: {
        value: false,
        title: "",
        content: "",
      },
      visibleMove: false,
      userType: "",
    };
  },
  computed: {
    isAll() {
      if (!this.results.length) return false;
      return (
        this.results.length &&
        this.results.every((item) =>
          this.listChecked.some((checked) => checked === item.id)
        )
      );
    },
  },
  watch: {
    reloadKey() {
      this.paging(1);
    },
    searchText: {
      handler(newVal) {
        this.requestParam.query = newVal;
        this.paging(1);
      },
      immediate: true,
    },
    visibleMoreAction(newVal) {
      if (!newVal) {
        this.updateTabStatus();
      }
    },
  },
  methods: {
    selectTab(tabCheck, index) {
      let __listSelectedTabs = this.listSelectedTabs;
      __listSelectedTabs[index].show = !tabCheck.show;
      if (
        !tabCheck.show &&
        ((tabCheck.isDefaultTab &&
          this.currentActiveTab.isDefaultTab &&
          tabCheck.name === this.currentActiveTab.name) ||
          (!tabCheck.isDefaultTab &&
            !this.currentActiveTab.isDefaultTab &&
            tabCheck.id === this.currentActiveTab.id))
      ) {
        this.closeTab(tabCheck);
      }
      this.listSelectedTabs = __listSelectedTabs;
    },
    removeFromTab() {
      const params = {
        id: this.currentActiveTab.id,
        name: this.currentActiveTab.name,
        isDuplicate: false,
        removeItemIdList:
          this.total === this.listAllIds.length
            ? this.listAllIds
            : this.listChecked,
        addItemIdList: [],
      };
      axios.post("/api/tab-table/save-tab-table-company", params).then(() => {
        this.createSuccess(params, {
          title: "Success!",
          content: `Your data has been removed from ${this.currentActiveTab.name} tab`,
        });
      });
    },
    moveSuccess(toast) {
      this.getTabsByCurrentUser();
      this.paging(1);
      this.listChecked = [];
      this.listCheckedFull = [];
      if (toast) {
        this.showToastAlert(toast);
        setTimeout(() => {
          this.closeToastAlert();
        }, 3000);
      }
    },
    closeTab(tab) {
      tab.show = false;
      if (tab.name === this.currentActiveTab.name) {
        this.setNewActiveTab();
      }
      let tabChange = [
        {
          id: tab.id,
          isShow: false,
          deleted: tab.deleted,
          name: tab.name,
          isDefaultTab: tab.isDefaultTab,
          objectType: tab.objectType,
        },
      ];
      axios.put("/api/tab-table", tabChange);
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    async getTabsByCurrentUser(isCallAtMounted = false) {
      try {
        const res = await axios.get(
          "/api/tab-table/by-current-user?objectType=COMPANY"
        );
        this.listSelectedTabs = res.data.data;
        if (isCallAtMounted) this.setNewActiveTab();
      } catch (error) {
        console.log(error);
      }
    },
    openManageTabView() {
      this.visibleMoreAction = false;
      let child = Common.vue.getChild(this.$children, "manage-tab-view");
      if (child != null) {
        child.show();
      }
    },
    setNewActiveTab() {
      const listShowed = this.listSelectedTabs.filter((t) => t.show);
      if (listShowed.length) {
        this.tab(listShowed[0]);
      }
    },
    checkStatusParam(tabItem) {
      return (
        (this.currentActiveTab.id != null &&
          tabItem.id != null &&
          this.currentActiveTab.id === tabItem.id) ||
        (this.currentActiveTab.id == null &&
          tabItem.id == null &&
          this.currentActiveTab.name === tabItem.name)
      );
    },
    closeToastAlert() {
      this.toastAlert.value = false;
    },
    showToastAlert(toast) {
      this.toastAlert.value = true;
      this.toastAlert.title = toast.title;
      this.toastAlert.content = toast.content;
    },
    editSuccess(tabCreated, toast) {
      this.getTabsByCurrentUser();
      this.tab(tabCreated);
      this.showToastAlert(toast);
      this.triggerUpdateManageTab++;
      setTimeout(() => {
        this.closeToastAlert();
      }, 3000);
      this.listChecked = [];
      this.listCheckedFull = [];
    },
    createSuccess(tabCreated, toast) {
      this.getTabsByCurrentUser();
      this.tab(tabCreated);
      this.showToastAlert(toast);
      setTimeout(() => {
        this.closeToastAlert();
      }, 3000);
      this.listChecked = [];
      this.listCheckedFull = [];
    },
    openDuplicate(tab) {
      var child = Common.vue.getChild(this.$children, "tab-view-modal");
      if (child != null) {
        child.showCreateTabView("DUPLICATE", tab);
      }
    },
    openEdit(tab) {
      var child = Common.vue.getChild(this.$children, "tab-view-modal");
      if (child != null) {
        child.showCreateTabView("EDIT", tab);
      }
    },
    openManageTabView() {
      this.visibleMoreAction = false;
      let child = Common.vue.getChild(this.$children, "manage-tab-view");
      if (child != null) {
        child.show();
      }
    },
    createTabView() {
      this.visibleMoreAction = false;
      var child = Common.vue.getChild(this.$children, "tab-view-modal");
      if (child != null) {
        child.showCreateTabView("CREATE");
      }
    },
    openCustomizationModal() {
      this.visibleMoreAction = false;
      var child = Common.vue.getChild(this.$children, "customization-modal");
      if (child != null) {
        child.openModal();
      }
    },
    handleToggleMoreActions() {
      this.visibleMoreAction = true;
      this.isTabView = false;
    },
    async updateTabStatus() {
      let tabsChange = this.listSelectedTabs.map((tab) => {
        return {
          id: tab.id,
          isShow: tab.show,
          deleted: tab.deleted,
          name: tab.name,
          isDefaultTab: tab.isDefaultTab,
          objectType: tab.objectType,
        };
      });
      axios.put("/api/tab-table", tabsChange);
    },
    convertCodeToName(tabItem) {
      if (tabItem.isDefaultTab) {
        if (tabItem.name === "DELETED") {
          return "Disabled";
        } else {
          return (
            tabItem.name.charAt(0).toUpperCase() +
            tabItem.name.slice(1).toLocaleLowerCase().replace("_", " ")
          );
        }
      } else {
        return this.truncateText(tabItem.name, 20);
      }
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    checkSelect(id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
    },
    check(e) {
      console.log("@check", {
        value: e.target.value,
        checked: e.target.checked,
      });
      const { checked, value } = e.target;
      const itemId = Number(value);
      if (checked) {
        this.listChecked.push(itemId);
        const foundIndex = this.results.findIndex((item) => item.id === itemId);
        this.listCheckedFull.push(this.results[foundIndex]);
        Common.setHeightActionBar();
      } else {
        this.listAllIds = [];
        this.listChecked = this.listChecked.filter((id) => id !== itemId);
        this.listCheckedFull = this.listCheckedFull.filter(
          (item) => item.id !== itemId
        );
      }
    },
    goToEdit() {
      console.log(this.listChecked[0], "this.listChecked[0]");
      window.location.href = `/admin/companies/${this.listChecked[0]}/edit`;
    },
    changeShow(resultId) {
      if (resultId != null) {
        this.showDrowdown = resultId;
      } else {
        this.showDrowdown = null;
      }
    },
    hideCalendarPicker(event) {
      if (event.toElement && event.toElement.localName != "img") {
        this.changeShow(null);
      }
    },
    showRevisionHistory: function (id) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "COMPANY");
      }
    },
    tab(tab) {
      console.log("@tab");
      this.currentActiveTab = tab;
      this.total = null;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.requestParam.status = this.getStatus(tab);
      if (!tab.isDefaultTab) {
        this.requestParam.tabId = tab.id;
      } else {
        this.requestParam.tabId = "";
      }
      this.paging(1);
    },
    getStatus(tab) {
      switch (tab.name) {
        case "Enabled":
          return "active";
        case "In-house":
          return "IN_HOUSE";
        case "Supplier":
          return "SUPPLIER";
        case "Toolmaker":
          return "TOOL_MAKER";
        case "Disabled":
          return "disabled";
        default:
          return "active";
      }
    },
    search(page) {
      this.paging(1);
    },
    deleteCompany: function (company) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: company.id,
      };
      const self = this;
      axios
        .delete(Page.API_BASE + "/" + company.id, param)
        .then(function (response) {
          if (response.data.success) {
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },

    enable() {
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, true);
    },

    showCompanyDetails(company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetails(company);
      }
    },
    showCompanyDetailsById(companyId) {
      let child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(companyId);
      }
    },

    disable() {
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, false);
    },
    saveBatch(idArr, boolean) {
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      const self = this;
      axios
        .post("/api/companies/change-status-in-batch", param)
        .then(function (response) {
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
          this.getTabsByCurrentUser();
        });
    },
    paging(pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      var param = Common.param(this.requestParam);

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      this.cancelToken = axios.CancelToken.source();

      axios
        .get(Page.API_BASE + "?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then((response) => {
          this.total = response.data.totalElements;
          const currentTabIndex = this.listSelectedTabs.findIndex(
            (item) => item.id === this.currentActiveTab.id
          );
          if (currentTabIndex >= 0) {
            this.listSelectedTabs[currentTabIndex].totalItem =
              response.data.totalElements;
          }
          this.results = response.data.content;
          this.pagination = Common.getPagingData(response.data);

          var noResult = document.querySelector(".no-results");
          if (noResult != null) {
            if (this.results && this.results.length == 0) {
              noResult.className = noResult.className.replace("d-none", "");
            } else {
              noResult.className =
                noResult.className.replace("d-none", "") + " d-none";
            }
          }

          if (this.results && this.results.length > 0) {
            Common.triggerShowActionbarFeature(this.$children);
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    sortBy(type) {
      if (this.currentSortType !== type) {
        this.currentSortType = type;
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc;
      }
      if (type) {
        this.requestParam.sort = `${type},${this.isDesc ? "desc" : "asc"}`;
        this.sort();
      }
    },
    sort() {
      this.paging(1);
    },

    showSystemNoteModal() {
      const child = this.$refs["system-note"];
      if (!child) return;
      const isTotal = this.total === this.listAllIds.length;
      const isMultiple = this.listChecked.length > 1;
      const listIds = isTotal ? this.listAllIds : this.listChecked;
      if (isMultiple) {
        child.showSystemNote(listIds, true);
      } else {
        child.showSystemNote({ id: listIds[0] }, false);
      }
    },

    // #850 Add Total Tooling column for Company tab
    setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["company_name"],
          field: "name",
          sortable: true,
          default: true,
          mandatory: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["company_id"],
          field: "companyCode",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["address"],
          field: "address",
          sortable: true,
          default: true,
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["total_mold_count"],
          field: "moldCount",
          sortable: true,
          default: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["status"],
          field: "enable",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 3,
          sortField: "enabled",
        },
        {
          label: this.resources["upper_tier_company"],
          field: "upperTierCompanies",
          sortable: true,
          sortField: "upperTierCompanies",
        },
      ];
      try {
        this.resetColumnsListSelected();
        Common.changeDeletedColumn(
          this,
          this.objectType,
          this.allColumnList,
          null,
          this.configDeleteField
        );
        this.getCustomColumnAndInit();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
    },
    getColumnListSelected() {
      console.log("/api/config/column-config?pageType=", this.pageType);
      axios.get(`/api/config/column-config?pageType=${this.pageType}`).then(
        (response) => {
          if (response.data.length) {
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              console.log(item.columnName);
              hashedByColumnName[item.columnName] = item;
            });
            this.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field]) {
                item.enabled = hashedByColumnName[item.field].enabled;
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
            this.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
            this.$forceUpdate();
            console.log("this is the child view obj", this.$children);
            let child = Common.vue.getChild(this.$children, "show-columns");
            if (child != null) {
              child.$forceUpdate();
            }
          }
        },
        (error) => {
          Common.alert(error.data);
        }
      );
    },
    getCustomColumnAndInit() {
      Common.getCustomColumn(this.objectType)
        .then(async (data) => {
          this.allColumnList.push(...data);
        })
        .finally((error) => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        });
    },
    saveSelectedList(dataCustomize, list) {
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return { ...item, position: index };
        }
      });
      this.allColumnList = dataFake;
      let data = list.map((item, index) => {
        let response = {
          columnName: item.field,
          enabled: item.enabled,
          position: index,
        };
        if (item.id) {
          response.id = item.id;
        }
        return response;
      });
      axios
        .post("/api/config/update-column-config", {
          pageType: this.pageType,
          columnConfig: data,
        })
        .then(
          (response) => {
            console.log("this is the current pagetype:", this.pageType);
            let hashedByColumnName = {};
            response.data.forEach((item) => {
              hashedByColumnName[item.columnName] = item;
            });
            this.allColumnList.forEach((item) => {
              if (hashedByColumnName[item.field] && !item.id) {
                item.id = hashedByColumnName[item.field].id;
                item.position = hashedByColumnName[item.field].position;
              }
            });
          },
          () => {
            this.allColumnList.sort((a, b) => {
              return a.position - b.position;
            });
            this.$forceUpdate();
          }
        );
    },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} companies on this current page are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
      }
      if (actionType === "all") {
        this.fetchAllListIds();
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.total} companies are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
      }
      if (actionType === "deselect") {
        this.listAllIds = [];
        this.listChecked = [];
        this.listCheckedFull = [];
      }

      console.log("@handleSelectAll", {
        actionType,
        listChecked: this.listChecked,
        listCheckedFull: this.listCheckedFull,
      });
    },
    async fetchAllListIds() {
      try {
        var param = Common.param({
          ...this.requestParam,
          frameType: "COMPANY_SETTING",
        });
        const response = await axios.get(
          "/api/batch/all-ids-for-full-frame?" + param
        );
        this.listAllIds = response.data.data;
        console.log("response.data", response.data);
        console.log("this.listAllIds.length", this.listAllIds);
        console.log("this.total", this.total);
      } catch (error) {
        console.log(error);
      }
    },
    directLink(page, objData) {
      location.href = `${Common.PAGE_URL[page]}?${Common.param(objData)}`;
    },
  },
  created() {
    this.$watch(
      () => headerVm?.systemCode,
      (newVal) => {
        this.codes = Object.assign({}, this.codes, newVal);
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (!newVal) return;
        this.resources = Object.assign({}, this.resources, newVal);
        this.setAllColumnList();
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.userType,
      (newVal) => {
        if (newVal) this.userType = newVal;
      },
      { immediate: true }
    );

    // capture query
    const url = new URL(location.href);
    const urlParam = url.hash.replace("#", "").split("?")[1];
    const params = Common.parseParams(urlParam);
    if (params.ids) {
      this.requestParam.ids = params.ids;
    }
  },
  mounted() {
    this.getTabsByCurrentUser(true);
  },
};
</script>

<style></style>
