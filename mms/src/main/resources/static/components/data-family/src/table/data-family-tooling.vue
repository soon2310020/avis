<template>
  <div>
    <div class="data-table row">
      <div v-show="!showMultiChart" class="col-lg-12">
        <div class="card-overflow-reset">
          <div class="data-table-status"></div>
          <div class="data-table-tabs d-flex justify-space-between">
            <ul
              class="nav nav-tabs tooling-tab-hint"
              style="width: calc(100% - 260px)"
            >
              <li
                v-if="tabItem.shown"
                v-for="(tabItem, index) in listSelectedTabs"
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
                          {{
                            tabItem.defaultTab
                              ? resources[tabItem.name]
                              : tabItem.name
                          }}
                        </div>
                      </template>
                      <span>
                        {{ convertTextTabName(tabItem) }}
                      </span>
                    </a-tooltip>
                  </div>
                  <span v-else class="part-name-drop-down one-line">
                    {{ convertTextTabName(tabItem) }}
                  </span>
                  <span class="badge badge-light badge-pill">{{
                    tabItem.total
                  }}</span>
                  <span
                    v-if="tabItem.name !== 'All' || !tabItem.defaultTab"
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
            </ul>
            <div class="data-table-top-action" style="width: 260px">
              <customization-modal
                ref="customization-modal"
                :all-columns-list="allColumnList"
                @save="saveSelectedList"
                :resources="resources"
                :is-more-action="true"
              ></customization-modal>
              <div class="assign-more">
                <cta-button
                  :active="showPartPicker"
                  color-type="blue-fill"
                  button-type="dropdown"
                  :click-handler="handleToggle"
                >
                  <span>{{ resources["new_tooling"] }}</span>
                </cta-button>
                <common-select-popover
                  :is-visible="showPartPicker"
                  @close="handleClosePartPicker"
                  class="common-popover-for-tooling"
                >
                  <common-select-dropdown
                    :style="{ position: 'static' }"
                    :class="{ show: showPartPicker }"
                    :searchbox="false"
                    :checkbox="false"
                    :items="listOfItems"
                    :click-handler="onItemSelect"
                    class="dropdown-wrap-for-tooling"
                  ></common-select-dropdown>
                </common-select-popover>
              </div>
              <div
                v-if="
                  requestParam.tabName !== 'DISABLED' &&
                  requestParam.tabName !== 'DISPOSED'
                "
                class="assign-more d-flex align-items-center position-relative"
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
                      Filter
                      <div class="custom-arrow-tooltip"></div>
                    </div>
                  </template>
                  <a
                    @click.stop="handleToggleFilter"
                    href="javascript:void(0)"
                    class="btn-custom customize-btn btn-outline-custom-primary position-relative"
                  >
                    <img
                      class="filter-icon-size"
                      src="/images/icon/Icon_feather-filter.svg"
                    />
                    <span v-show="isFilterApplied" class="pink-dot"></span>
                  </a>
                </a-tooltip>

                <common-popover
                  :is-visible="showFilter"
                  @close="handleCloseFilter"
                  class="filter-custom"
                >
                  <common-treeselect-dropdown
                    v-show="dropDownType == 'main'"
                    class="filter-custom-dropdown"
                    :style="{
                      position: 'absolute',
                      transform: 'translate(-190px, -27px)',
                      width: '420px !important',
                    }"
                    :checkbox="true"
                    :items="listFilterItems"
                    :reset-handler="resetHandler"
                    id="mold-filter"
                    :apply-handler="checkedItems"
                    :handle-check="handleCheckStatus"
                    :is-mold-list="true"
                  ></common-treeselect-dropdown>
                </common-popover>
              </div>
              <!--          More actions below here-->
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
                            :checked="subItem.shown"
                            :disabled="
                              subItem.name === 'All' && subItem.defaultTab
                            "
                            @change="selectTab(subItem, index2)"
                          />
                          <div
                            class="checkbox-custom"
                            :class="{
                              checkboxDisabled:
                                subItem.name === 'All' && subItem.defaultTab,
                            }"
                          ></div>
                        </template>
                        {{ convertTextTabName(subItem) }}
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
                        <a href="javascript:void(0)" @click="openManageTabView"
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
            style="
              overflow-x: auto !important;
              border: 1px solid #c8ced3;
              border-radius: 3px;
              margin-bottom: 0;
            "
            class="table table-responsive-sm table-striped"
          >
            <colgroup>
              <col />
              <col />
              <col />
              <col />
              <col />
              <col />
            </colgroup>
            <thead
              :style="[
                isShowDisableWarning ? { 'z-index': '0' } : { 'z-index': '99' },
              ]"
              id="thead-actionbar"
              class="custom-header-table"
            >
              <tr
                :class="{ invisible: listChecked.length != 0 }"
                style="height: 57px"
                class="header-s tr-sort"
              >
                <th class="custom-td">
                  <div class="custom-td_header">
                    <select-all
                      :resources="resources"
                      :show="true"
                      :checked="isAll"
                      :total="total"
                      :count="results.length"
                      :unit="['tooling', 'toolings']"
                      @select-all="handleSelectAll('all')"
                      @select-page="handleSelectAll('page')"
                      @deselect="handleSelectAll('deselect')"
                    ></select-all>
                  </div>
                </th>
                <template v-for="(item, index) in displayColumns">
                  <th
                    v-if="item.sortField === 'lastShot'"
                    v-click-outside="closeLastShot"
                    :class="[
                      {
                        __sort: item.sortable,
                        active: currentSortType === item.sortField,
                      },
                      isDesc ? 'desc' : 'asc',
                      item.label === 'OP' ||
                      item.label === 'Status' ||
                      item.field === 'dataSubmission'
                        ? ''
                        : 'text-left',
                    ]"
                    @click="sortBy(item.sortField)"
                  >
                    <span>
                      {{ item.label }}
                    </span>
                    <span v-if="item.sortable" class="__icon-sort"></span>
                    <last-shot-filter
                      v-show="lastShotDropdown && item.sortField === 'lastShot'"
                      :last-shot-data="lastShotData"
                      @update-variable="updateVariable"
                    ></last-shot-filter>
                  </th>
                  <th
                    v-else-if="item.label === 'S.L. Depreciation'"
                    :class="[
                      {
                        __sort: item.sortable,
                        active: currentSortType === item.sortField,
                      },
                      isDesc ? 'desc' : 'asc',
                      item.label === 'OP' ||
                      item.label === 'Status' ||
                      item.field === 'dataSubmission'
                        ? ''
                        : 'text-left',
                    ]"
                    @click="sortBy(item.sortField)"
                  >
                    <span>{{ item.label }} </span>
                    <span v-if="item.sortable" class="__icon-sort"></span>
                    <a-tooltip placement="bottom">
                      <template slot="title">
                        <div style="padding: 6px; font-size: 13px">
                          Straight Line Depreciation: a method of accounting for
                          depreciation that assumes the asset loses value at a
                          constant rate.
                        </div>
                      </template>
                      <span
                        style="
                          margin-left: 23px;
                          width: 15px;
                          position: absolute;
                        "
                      >
                        <img
                          src="/images/icon/tooltip_info.svg"
                          width="15"
                          height="15"
                        />
                      </span>
                    </a-tooltip>
                  </th>
                  <th
                    v-else-if="item.label === 'U.P. Depreciation'"
                    :class="[
                      {
                        __sort: item.sortable,
                        active: currentSortType === item.sortField,
                      },
                      isDesc ? 'desc' : 'asc',
                      item.label === 'OP' ||
                      item.label === 'Status' ||
                      item.field === 'dataSubmission'
                        ? ''
                        : 'text-left',
                    ]"
                    @click="sortBy(item.sortField)"
                  >
                    <span>{{ item.label }} </span>
                    <span v-if="item.sortable" class="__icon-sort"></span>
                    <a-tooltip placement="bottom">
                      <template slot="title">
                        <div style="padding: 6px; font-size: 13px">
                          Units of Production Depreciation: a depreciation
                          method that records depreciation expense for an asset
                          based on the production levels of the asset over the
                          useful life.
                        </div>
                      </template>
                      <span
                        style="
                          margin-left: 23px;
                          width: 15px;
                          position: absolute;
                        "
                      >
                        <img
                          src="/images/icon/tooltip_info.svg"
                          width="15"
                          height="15"
                        />
                      </span>
                    </a-tooltip>
                  </th>
                  <th
                    v-else-if="'utilNextPm' === item.field"
                    :key="index"
                    :class="[
                      {
                        __sort: item.sortable,
                        active: currentSortType === item.sortField,
                      },
                      isDesc ? 'desc' : 'asc',
                      'text-left',
                    ]"
                    @click="sortBy(item.sortField)"
                  >
                    <div class="d-flex align-center">
                      <span v-text="resources['pm_checkpoint_progress']"></span>
                      <span
                        v-if="item.sortable"
                        class="__icon-sort"
                        style="
                          position: static;
                          display: inline-block;
                          margin-right: 4px;
                          width: 18px;
                          vertical-align: middle;
                        "
                      ></span>
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 16px; font-size: 13px">
                            {{ resources["util_next_pm_tooltip"] }}
                          </div>
                        </template>
                        <a-icon type="info-circle" />
                      </a-tooltip>
                    </div>
                  </th>

                  <th
                    v-else-if="['ctCompliance'].includes(item.field)"
                    :class="[
                      {
                        __sort: item.sortable,
                        active: currentSortType === item.sortField,
                      },
                      isDesc ? 'desc' : 'asc',
                      'text-right',
                    ]"
                    @click="sortBy(item.sortField)"
                  >
                    <div>
                      <span v-text="resources['ct_compliance']"></span>
                      <span
                        v-if="item.sortable"
                        class="__icon-sort"
                        style="
                          position: static;
                          display: inline-block;
                          vertical-align: middle;
                        "
                      ></span>
                    </div>
                  </th>

                  <th
                    v-else
                    :class="[
                      {
                        __sort: item.sortable,
                        active: currentSortType === item.sortField,
                      },
                      isDesc ? 'desc' : 'asc',
                      item.label === 'OP' ||
                      item.label === 'Status' ||
                      item.field === 'dataSubmission'
                        ? ''
                        : 'text-left',
                    ]"
                    @click="sortBy(item.sortField)"
                  >
                    <span>{{ item.label }} </span>
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
                          :show="true"
                          :checked="isAll"
                          :total="total"
                          :count="results.length"
                          :unit="['tooling', 'toolings']"
                          @select-all="handleSelectAll('all')"
                          @select-page="handleSelectAll('page')"
                          @deselect="handleSelectAll('deselect')"
                        ></select-all>
                      </div>
                    </div>
                    <div class="action-bar">
                      <div
                        v-if="listChecked.length == 1"
                        @click="goToEdit"
                        class="action-item"
                      >
                        <span>Edit</span>
                        <i class="icon-action edit-icon"></i>
                      </div>
                      <div
                        v-if="listChecked.length == 1"
                        @click="showRevisionHistory(listChecked[0])"
                        class="action-item"
                      >
                        <span>View Edit History</span>
                        <i class="icon-action view-edit-history-icon"></i>
                      </div>
                      <div
                        v-if="listChecked.length == 1"
                        class="action-item"
                        @click="
                          showChart(
                            listCheckedFull[0],
                            null,
                            null,
                            'switch-notes'
                          )
                        "
                      >
                        <span>Add Note</span>
                        <i class="icon-action memo-icon"></i>
                      </div>
                      <div
                        v-if="
                          listChecked.length >= 1 &&
                          requestParam.tabName !== 'DISABLED' &&
                          requestParam.tabName !== 'DISPOSED'
                        "
                        class="action-item"
                        @click.prevent="showDisableWarning()"
                      >
                        <span>Disable</span>
                        <i class="icon-action disable-icon"></i>
                      </div>
                      <div
                        v-if="
                          listChecked.length >= 1 &&
                          requestParam.tabName === 'DISABLED' &&
                          !checkedDisposedItem
                        "
                        class="action-item"
                        @click.prevent="enable()"
                      >
                        <span>Enable</span>
                        <i class="icon-action enable-icon"></i>
                      </div>

                      <div
                        v-click-outside="closeShowExport"
                        v-if="listChecked.length >= 1"
                        @click="showExport = !showExport"
                        class="action-item"
                      >
                        <!--  -->
                        <div
                          class="change-status-dropdown drowdown action-item"
                        >
                          <span>Export</span>
                          <i class="icon-action export-icon"></i>
                        </div>
                        <ul
                          style="left: auto !important"
                          class="dropdown-menu"
                          :class="[showExport ? 'show' : '']"
                        >
                          <li>
                            <span
                              class="dropdown-item"
                              onmouseover="this.style.color='black';"
                              onmouseout="this.style.color='black';"
                              @click="openExportModal"
                            >
                              Dynamic Data
                            </span>
                          </li>
                          <li>
                            <span
                              class="dropdown-item"
                              onmouseover="this.style.color='black';"
                              onmouseout="this.style.color='black';"
                              @click="downloadTooling(EXPORT_TYPE.XLSX)"
                            >
                              Static Data
                            </span>
                          </li>
                          <li>
                            <span
                              class="dropdown-item"
                              onmouseover="this.style.color='black';"
                              onmouseout="this.style.color='black';"
                              @click="exportLifeCycle()"
                            >
                              {{ resources["DATA_EXPORT.TOL_RPT"] }}
                            </span>
                          </li>
                        </ul>
                      </div>
                      <div
                        class="change-status-dropdown drowdown d-inline"
                        v-click-outside="closeWorkorderDropdown"
                      >
                        <div
                          v-if="
                            listChecked.length === 1 &&
                            requestParam.tabName !== 'DISPOSED'
                          "
                          class="action-item"
                          @click="
                            showWorkOrderDropdown = !showWorkOrderDropdown
                          "
                          style="align-items: flex-start"
                        >
                          <span>Create Workorder</span>
                          <img
                            src="/images/icon/icon-workorder.svg"
                            style="margin-left: 4px; height: 15px; width: 15px"
                          />
                        </div>
                        <ul
                          class="dropdown-menu"
                          :class="[showWorkOrderDropdown ? 'show' : '']"
                        >
                          <li v-for="item in workOrderType" :key="item.value">
                            <div
                              class="dropdown-item"
                              @click.prevent="
                                showWorkOrderDropdown = false;
                                showCreateWorkorder(listChecked[0], item);
                              "
                            >
                              {{ item.name }}
                            </div>
                          </li>
                        </ul>
                      </div>

                      <div
                        v-if="enableShowMultiChart"
                        class="action-item"
                        @click.prevent="showMultiChart = true"
                      >
                        <span>Multi-chart view</span>
                        <img
                          src="/images/icon/chart-mold/multi-chart-icon.svg"
                          width="12"
                          height="12"
                          alt=""
                        />
                      </div>

                      <div
                        v-if="
                          listChecked.length >= 1 &&
                          !currentActiveTab.defaultTab
                        "
                        class="action-item"
                      >
                        <move-to-action
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
                          @close-move="visibleMove = false"
                          @create-tab-view="createTabView"
                          @move-to-tab="moveToTab"
                        ></move-to-action>
                      </div>

                      <div
                        v-if="
                          listChecked.length >= 1 &&
                          !currentActiveTab.defaultTab
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

            <tbody class="op-list" style="display: none">
              <tr v-for="(result, index, id) in results" :id="result.id">
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
                <template v-for="column in displayColumns">
                  <td class="text-left" v-if="column.field === 'toolingId'">
                    <div>
                      <a
                        href="#"
                        @click.prevent="showChart(result)"
                        style="color: #1aaa55"
                        class="mr-1 font-size-14"
                        ><i class="fa fa-bar-chart"></i
                      ></a>
                      <a href="#" @click.prevent="showMoldDetails(result)">
                        {{ result.equipmentCode }}
                      </a>
                    </div>

                    <div class="small text-muted font-size-11-2">
                      <span>{{ resources["updated_on"] }}</span>
                      {{
                        result.dataFilterEnabled
                          ? result.lastShotDateVal
                          : result.lastShotDate
                      }}
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'part'"
                    style="max-width: 600px"
                  >
                    <mold-parts-dropdown-view
                      :parts="result.parts"
                      :mold="result"
                      :index="index"
                      :show-part-chart="showPartChart"
                    ></mold-parts-dropdown-view>
                  </td>
                  <td class="text-left" v-else-if="column.field === 'company'">
                    <a
                      class="font-size-14"
                      href="#"
                      @click.prevent="
                        showCompanyDetailsById(result.companyIdByLocation)
                      "
                    >
                      {{ result.companyName }}
                    </a>
                    <div class="small text-muted font-size-11-2">
                      {{ result.companyCode }}
                    </div>
                  </td>
                  <td
                    id="slDepreciationFieldId"
                    class="text-left"
                    v-else-if="column.field === 'slDepreciation'"
                    style="position: relative"
                  >
                    <span
                      v-if="result.poDate === null"
                      style="font-style: italic"
                      >P.O. date not entered</span
                    >
                    <progress-bar
                      v-else
                      :progress-percentage="
                        getDepreciationPercentage(
                          result.slDepreciation,
                          result.slDepreciationTerm
                        )
                      "
                      :title="
                        result.slDepreciation +
                        ' of ' +
                        result.slDepreciationTerm +
                        ' years'
                      "
                      @on-click="toggleSlDepreciationPopup(result.id)"
                      :clickable="true"
                      ref="slDepreciationRef"
                    ></progress-bar>
                    <depreciation-popup
                      :mold="result"
                      :is-visible="
                        showSlDepreciationPopup &&
                        result.id === selectedToolingId
                      "
                      @close="toggleSlDepreciationPopup"
                      style="right: 0%; top: 50%"
                      :is-straight-line="true"
                      id="slDepreciationId"
                    >
                    </depreciation-popup>
                  </td>
                  <td
                    id="upDepreciationFieldId"
                    class="text-left"
                    v-else-if="column.field === 'upDepreciation'"
                    style="position: relative"
                  >
                    <span
                      v-if="result.poDate === null"
                      style="font-style: italic"
                      >P.O. date not entered</span
                    >
                    <progress-bar
                      v-else
                      :progress-percentage="
                        getDepreciationPercentage(
                          result.upDepreciation,
                          result.upDepreciationTerm
                        )
                      "
                      :title="
                        getDepreciationPercentage(
                          result.upDepreciation,
                          result.upDepreciationTerm
                        ) + '%'
                      "
                      @on-click="toggleUpDepreciationPopup(result.id)"
                      :clickable="true"
                      ref="upDepreciationRef"
                    ></progress-bar>
                    <depreciation-popup
                      :mold="result"
                      :is-visible="
                        showUpDepreciationPopup &&
                        result.id === selectedToolingId
                      "
                      @close="toggleUpDepreciationPopup"
                      style="right: 0%; top: 50%"
                      :is-straight-line="false"
                      id="upDepreciationId"
                    >
                    </depreciation-popup>
                  </td>
                  <td class="text-left" v-else-if="column.field === 'location'">
                    <div v-if="result.locationCode">
                      <div class="font-size-14">
                        <a
                          href="#"
                          @click.prevent="showLocationHistory(result)"
                        >
                          {{ result.locationName }}
                        </a>
                      </div>

                      <div class="small text-muted font-size-11-2">
                        {{ result.locationCode }}
                      </div>
                    </div>
                  </td>

                  <td class="text-left" v-else-if="column.field === 'lastShot'">
                    <span>{{
                      result.accumulatedShot
                        ? formatNumber(result.accumulatedShot)
                        : 0
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.accumulatedShot === 1 ? "shot" : "shots"
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'remainingPartsCount'"
                  >
                    <span>{{
                      result.remainingPartsCount
                        ? formatNumber(result.remainingPartsCount)
                        : 0
                    }}</span>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'lastShotCheckpoint'"
                  >
                    <div v-if="!shouldShowNoDataForTimeBaseMaintenance(result)">
                      <div class="clearfix">
                        <div class="float-left font-size-14">
                          <strong>{{
                            result.lastShot ? formatNumber(result.lastShot) : 0
                          }}</strong>
                          <!--                          <span v-if="result.moldMaintenance && result.moldMaintenance.period" >/-->
                          <!--                            {{-->
                          <!--                              result.moldMaintenance.period ? formatNumber(result.moldMaintenance.period) : 0-->
                          <!--                            }}</span>-->
                          <span>/ {{ formatNumber(result.pmCheckpoint) }}</span>
                        </div>
                      </div>
                      <div
                        style="display: flex; align-items: center; width: 120px"
                      >
                        <small class="text-muted font-size-11-2">
                          <strong>{{ ratioMaintenance2(result) }}%</strong>
                        </small>
                        <div
                          style="width: 100%; margin-left: 5px"
                          class="progress progress-xs"
                        >
                          <div
                            class="progress-bar"
                            :class="ratioMaintenanceColor(result)"
                            role="progressbar"
                            :style="ratioMaintenanceStyle(result)"
                            :aria-valuenow="ratioMaintenance2(result)"
                            aria-valuemin="0"
                            aria-valuemax="100"
                          ></div>
                        </div>
                      </div>
                    </div>
                    <div v-else>-</div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'utilNextPm'"
                  >
                    <div v-if="!shouldShowNoDataForTimeBaseMaintenance(result)">
                      <div class="clearfix">
                        <div class="float-left font-size-14">
                          <!--                          <strong v-if="result.moldMaintenance && result.moldMaintenance.lastShotMade">{{-->
                          <!--                            formatNumber(result.moldMaintenance.lastShotMade)-->
                          <!--                          }}</strong>-->
                          <strong>{{
                            result.untilNextPm
                              ? formatNumber(result.untilNextPm)
                              : 0
                          }}</strong>
                          <span
                            >/
                            {{ formatNumber(result.preventCycle) }}
                          </span>
                        </div>
                      </div>

                      <div
                        style="display: flex; align-items: center; width: 120px"
                      >
                        <small class="text-muted font-size-11-2">
                          <strong>{{ ratioMaintenance2(result) }}%</strong>
                        </small>
                        <div
                          style="width: 100%; margin-left: 5px"
                          class="progress progress-xs"
                        >
                          <div
                            class="progress-bar"
                            :class="ratioMaintenanceColor2(result)"
                            role="progressbar"
                            :style="ratioMaintenanceStyle2(result)"
                            :aria-valuenow="ratioMaintenance2(result)"
                            aria-valuemin="0"
                            aria-valuemax="100"
                          ></div>
                        </div>
                      </div>
                    </div>
                    <div v-else>-</div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'pmCheckpointPrediction'"
                  >
                    <span v-if="shouldShowNoDataForTimeBaseMaintenance(result)"
                      >-</span
                    >
                    <!--                    <span-->
                    <!--                      v-else-if="-->
                    <!--                        result &&-->
                    <!--                        result.moldMaintenance &&-->
                    <!--                        result.moldMaintenance.pmCheckpointPrediction-->
                    <!--                      "-->
                    <!--                    >-->
                    <!--                      {{-->
                    <!--                        showPmCheckpointPrediction(-->
                    <!--                          result?.moldMaintenance?.pmCheckpointPrediction,-->
                    <!--                          result?.moldMaintenance?.maintenanceStatus-->
                    <!--                        )-->
                    <!--                      }}-->
                    <!--                    </span>-->
                    <span v-else>
                      {{
                        showPmCheckpointPrediction(
                          result?.pmCheckpointPrediction,
                          result?.maintenanceStatus
                        )
                      }}
                    </span>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'cycleTime'"
                  >
                    {{
                      result.lastCycleTime
                        ? Math.round(
                            (result.lastCycleTime / 10 + Number.EPSILON) * 100
                          ) / 100
                        : 0
                    }}
                    <div>
                      <span class="font-size-11-2">seconds</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'weightedAverageCycleTime'"
                  >
                    {{
                      result.weightedAverageCycleTime
                        ? Math.round(
                            (result.weightedAverageCycleTime / 10 +
                              Number.EPSILON) *
                              100
                          ) / 100
                        : 0
                    }}
                    <div>
                      <span class="font-size-11-2">seconds</span>
                    </div>
                  </td>
                  <td
                    style="vertical-align: middle !important"
                    class="text-center op-status-td"
                    v-else-if="column.field === 'op'"
                  >
                    <span
                      v-if="
                        result.operatingStatus == 'WORKING' &&
                        ['INSTALLED', 'DETACHED', 'CHECK'].includes(
                          result.equipmentStatus
                        )
                      "
                      class="op-active label label-success"
                    ></span>
                    <span
                      v-if="
                        result.operatingStatus == 'IDLE' &&
                        ['INSTALLED', 'DETACHED', 'CHECK'].includes(
                          result.equipmentStatus
                        )
                      "
                      class="op-active label label-warning"
                    ></span>
                    <span
                      v-if="
                        result.operatingStatus == 'NOT_WORKING' &&
                        ['INSTALLED', 'DETACHED', 'CHECK'].includes(
                          result.equipmentStatus
                        )
                      "
                      class="op-active label label-inactive"
                    ></span>
                    <span
                      v-if="
                        result.operatingStatus == 'DISCONNECTED' &&
                        ['INSTALLED', 'DETACHED', 'CHECK'].includes(
                          result.equipmentStatus
                        )
                      "
                      class="op-active label label-danger"
                    ></span>
                  </td>
                  <td
                    style="vertical-align: middle !important"
                    class="text-center op-status-td"
                    v-else-if="column.field === 'equipmentStatus'"
                  >
                    <span v-if="result.equipmentStatus === 'INSTALLED'"
                      >MATCHED</span
                    >
                    <span v-else>{{ result.equipmentStatus }}</span>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'cycleTimeToleranceL1'"
                  >
                    <span class="font-size-14">{{
                      result.cycleTimeLimit1
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.cycleTimeLimit1
                          ? handleCycleTimeTolerance1(result)
                          : ""
                      }}</span>
                    </div>
                  </td>
                  <td class="text-left" v-else-if="column.field === 'areaName'">
                    <span class="font-size-14">{{ result?.areaName }}</span>
                    <div>
                      <span class="font-size-11-2">{{ result?.areaType }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'contractedCycleTimeSeconds'"
                  >
                    <template v-if="result.contractedCycleTimeSeconds">
                      <span class="font-size-14">{{
                        result.contractedCycleTimeSeconds
                      }}</span>
                      <div>
                        <span class="font-size-11-2">seconds</span>
                      </div>
                    </template>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="
                      column.field === 'toolmakerContractedCycleTimeSeconds'
                    "
                  >
                    <template v-if="result.toolmakerContractedCycleTimeSeconds">
                      <span class="font-size-14">{{
                        result.toolmakerContractedCycleTimeSeconds
                      }}</span>
                      <div>
                        <span class="font-size-11-2">seconds</span>
                      </div>
                    </template>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'cycleTimeToleranceL2'"
                  >
                    <span class="font-size-14">{{
                      result.cycleTimeLimit2
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.cycleTimeLimit2
                          ? handleCycleTimeTolerance2(result)
                          : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'uptimeTarget'"
                  >
                    <span class="font-size-14" style="margin-left: 3px"
                      >{{ result.uptimeTarget }}
                      <span class="font-size-11-2">{{
                        result.uptimeTarget ? "%" : ""
                      }}</span>
                    </span>
                  </td>
                  <td
                    style="vertical-align: middle !important"
                    class="text-left"
                    v-else-if="column.field === 'engineerInCharge'"
                  >
                    <user-list-cell
                      :resources="resources"
                      :user-list="result.engineers"
                    ></user-list-cell>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'utilizationRate'"
                  >
                    <div v-if="result.lastShot">
                      <div class="clearfix">
                        <div class="float-left font-size-14">
                          <strong>{{ formatNumber(result.lastShot) }}</strong>
                          <span>/ {{ formatNumber(result.designedShot) }}</span>
                        </div>
                      </div>
                      <div
                        style="display: flex; align-items: center; width: 120px"
                      >
                        <small class="text-muted font-size-11-2">
                          <strong>{{ ratio(result) }}%</strong>
                        </small>
                        <div
                          style="width: 100%; margin-left: 5px"
                          class="progress progress-xs"
                        >
                          <div
                            class="progress-bar"
                            :class="ratioColor(result)"
                            role="progressbar"
                            :style="ratioStyle(result)"
                            :aria-valuenow="ratio(result)"
                            aria-valuemin="0"
                            aria-valuemax="100"
                          ></div>
                        </div>
                      </div>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'weightOfRunnerSystem'"
                  >
                    <span class="font-size-14">
                      {{ result.weightRunner ? result.weightRunner : "" }}
                    </span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.weightRunner
                          ? getUnitName(result.weightUnitTitle)
                          : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'designedShot'"
                  >
                    <span class="font-size-14">{{
                      result.designedShot
                        ? formatNumber(result.designedShot)
                        : 0
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.designedShot === 1 ? "shot" : "shots"
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'quotedMachineTonnage'"
                  >
                    <span class="font-size-14">{{
                      result.quotedMachineTonnage
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.quotedMachineTonnage ? "tonnes" : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'preventCycle'"
                  >
                    <span class="font-size-14">{{ result.preventCycle }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.preventCycle ? "shots" : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'maxCapacityPerWeek'"
                  >
                    <span class="font-size-14">{{
                      result.maxCapacityPerWeek
                        ? formatNumber(result.maxCapacityPerWeek)
                        : 0
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.maxCapacityPerWeek === 1 ? "shot" : "shots"
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'preventOverdue'"
                  >
                    <span class="font-size-14">{{
                      result.preventOverdue
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.preventOverdue ? "shots" : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'shiftsPerDay'"
                  >
                    <span class="font-size-14">{{ result.shiftsPerDay }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.shiftsPerDay ? "hours" : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'productionDays'"
                  >
                    <span class="font-size-14">{{
                      result.productionDays
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.productionDays ? "days" : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'toolingSizeView'"
                  >
                    <span class="font-size-14">{{ result.size }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.toolingSizeView
                          ? getUnitName(result.sizeUnit)
                          : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'toolingWeightView'"
                  >
                    <span class="font-size-14">{{ result.weight }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.toolingWeightView
                          ? getUnitName(result.weightUnit)
                          : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'preventUpcoming'"
                  >
                    <span class="font-size-14">{{
                      result.preventUpcoming
                    }}</span>
                    <div>
                      <span class="font-size-11-2">{{
                        result.preventUpcoming ? "shots" : ""
                      }}</span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'uptimeLimitL1'"
                  >
                    <span class="font-size-14" style="margin-left: 3px"
                      >{{ result.uptimeLimitL1 }}
                      <span class="font-size-11-2">{{
                        result.uptimeLimitL1 ? "%" : ""
                      }}</span>
                    </span>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'uptimeLimitL2'"
                  >
                    <span class="font-size-14" style="margin-left: 3px"
                      >{{ result.uptimeLimitL2 }}
                      <span class="font-size-11-2">{{
                        result.uptimeLimitL2 ? "%" : ""
                      }}</span>
                    </span>
                  </td>
                  <td
                    style="vertical-align: middle !important"
                    class="text-left"
                    v-else-if="column.field === 'dataSubmission'"
                  >
                    <div
                      class="data-submission"
                      :style="{
                        width: '87px',
                        height: '31px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        backgroundColor:
                          result.dataSubmission == 'PENDING'
                            ? '#ac24d3'
                            : result.dataSubmission == 'APPROVED'
                            ? '#26a958'
                            : '#b8b8b8',
                        borderRadius: '4px',
                      }"
                    >
                      <span
                        :style="{
                          color: '#fff',
                        }"
                      >
                        {{
                          result.dataSubmission == "PENDING"
                            ? "Pending"
                            : result.dataSubmission == "APPROVED"
                            ? "Approved"
                            : "Disapproved"
                        }}
                      </span>
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'inactivePeriod'"
                  >
                    <div style="font-size: 14px">
                      {{ result.inactivePeriod }}
                    </div>
                    <span style="font-size: 11.2px">
                      {{ result.inactivePeriod > 1 ? "Months" : "Month" }}
                    </span>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'accumulatedMaintenanceCost'"
                  >
                    <div style="font-size: 14px">
                      {{
                        result.accumulatedMaintenanceCost == null
                          ? ""
                          : result.costCurrencyTypeTitle != null
                          ? result.costCurrencyTypeTitle
                          : "$"
                      }}{{ formatNumber(result.accumulatedMaintenanceCost) }}
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'costTooling'"
                  >
                    <div style="font-size: 14px">
                      {{
                        result.cost == null
                          ? ""
                          : result.costCurrencyTypeTitle != null
                          ? result.costCurrencyTypeTitle
                          : "$"
                      }}{{ formatNumber(result.cost) }}
                    </div>
                  </td>
                  <td class="text-left" v-else-if="column.field === 'tco'">
                    <div style="font-size: 14px">
                      {{
                        result.tco == null
                          ? ""
                          : result.costCurrencyTypeTitle != null
                          ? result.costCurrencyTypeTitle
                          : "$"
                      }}{{ formatNumber(result.tco) }}
                    </div>
                  </td>
                  <td class="text-left" v-else-if="column.field === 'memo'">
                    <div style="font-size: 14px">
                      {{ result.memo }}
                      {{ result.memo }}
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'lastShotDate'"
                  >
                    {{
                      result.dataFilterEnabled && result.lastShotDateVal
                        ? result.lastShotDateVal
                        : result.lastShotDate
                    }}
                  </td>
                  <td class="text-left" v-else-if="column.isCustomField">
                    {{ customFieldValueTooling(result, column.columnName) }}
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'productAndCategory'"
                    style="max-width: 600px"
                  >
                    <category-product-dropdown
                      v-if="Object.entries(resources).length"
                      :list-parts="result.parts"
                      :mold="result"
                      :index="index"
                      :show-project-detail="showProjectDetail"
                      :user-type="userType"
                    >
                    </category-product-dropdown>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'lastMaintenanceDate'"
                  >
                    <div class="font-size-14" style="margin-left: 3px">
                      {{ formatToDate(result.lastMaintenanceDate) }}
                    </div>
                    <div class="font-size-11-2">
                      {{ getTimeData(result.lastMaintenanceDate) }}
                    </div>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'lastRefurbishmentDate'"
                  >
                    <div class="font-size-14" style="margin-left: 3px">
                      {{ formatToDate(result.lastRefurbishmentDate) }}
                    </div>
                    <div class="font-size-11-2">
                      {{ getTimeData(result.lastRefurbishmentDate) }}
                    </div>
                  </td>
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
                      :disabled="disabledUpperTierCompanies"
                      :show-company-details-by-id="showCompanyDetailsById"
                    >
                    </upper-company-dropdown>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'machineCode'"
                  >
                    <div v-if="result.machineMini">
                      <a
                        href="#"
                        @click.prevent="
                          showMachineDetails(result.machineMini.id)
                        "
                      >
                        {{ result.machineMini.name }}
                      </a>
                    </div>
                  </td>
                  <td
                    class="text-left tooling-status"
                    v-else-if="column.field === 'toolingStatus'"
                  >
                    <a-tooltip class="mold-status-col" placement="bottomLeft">
                      <template slot="title">
                        <div
                          class="tooltip-label"
                          v-text="getTooltipMoldStatus(result.toolingStatus)"
                        ></div>
                      </template>
                      <span
                        v-if="
                          ['IN_PRODUCTION', 'WORKING'].includes(
                            result.toolingStatus
                          )
                        "
                        class="op-active label label-working"
                      ></span>
                      <span
                        v-if="result.toolingStatus === 'IDLE'"
                        class="op-active label label-idle"
                      ></span>
                      <span
                        v-if="
                          ['NOT_WORKING', 'INACTIVE'].includes(
                            result.toolingStatus
                          )
                        "
                        class="op-active label label-not-working"
                      ></span>
                      <span
                        v-if="
                          ['SENSOR_OFFLINE', 'DISCONNECTED'].includes(
                            result.toolingStatus
                          )
                        "
                        class="op-active label label-sensor-offline"
                      ></span>
                      <span
                        v-if="result.toolingStatus === 'SENSOR_DETACHED'"
                        class="op-active label label-sensor-detached"
                      ></span>
                      <span
                        v-if="result.toolingStatus === 'NO_SENSOR'"
                        class="op-active label label-no-sensor"
                      ></span>
                      <span
                        v-if="result.toolingStatus === 'DISABLED'"
                        class="op-active label label-disabled"
                      ></span>
                      <span
                        v-if="result.moldStatus === 'DISPOSED'"
                        class="op-active label label-disposed"
                      ></span>
                      <span
                        v-if="result.toolingStatus === 'ON_STANDBY'"
                        class="op-active label label-on-standby"
                      ></span>
                      <span
                        class="mold-status-text"
                        v-text="getMoldStatusText(result.toolingStatus)"
                      ></span>
                    </a-tooltip>
                  </td>
                  <td
                    class="text-left"
                    v-else-if="column.field === 'sensorStatus'"
                  >
                    <a-tooltip placement="bottomLeft">
                      <template slot="title">
                        <div
                          class="tooltip-label"
                          v-text="getTooltipCounterStatus(result.counterStatus)"
                        ></div>
                      </template>
                      <div
                        v-if="result.counterStatus === 'INSTALLED'"
                        class="counter-status-label counter-installed-label"
                      >
                        Installed
                      </div>
                      <div
                        v-if="result.counterStatus === 'NOT_INSTALLED'"
                        class="counter-status-label counter-not-installed-label"
                      >
                        Not Installed
                      </div>
                      <div
                        v-if="result.counterStatus === 'DETACHED'"
                        class="counter-status-label counter-detached-label"
                      >
                        Detached
                      </div>
                    </a-tooltip>
                  </td>
                  <td
                    class="text-right"
                    v-else-if="column.field === 'ctCompliance'"
                  >
                    {{
                      result.ctCompliance != null
                        ? result.ctCompliance.toFixed(1) + "%"
                        : "-"
                    }}
                  </td>
                  <td class="text-left" v-else>
                    {{ result[column.field] }}
                  </td>
                </template>
              </tr>
            </tbody>
          </table>

          <div class="no-results d-none">{{ resources["no_results"] }}</div>

          <div class="row">
            <div class="col-md-8 mt-3">
              <ul class="pagination">
                <li
                  v-for="(data, index) in pagination"
                  class="page-item"
                  :class="{ active: data.isActive }"
                >
                  <a class="page-link" @click="paging(data.pageNumber)">{{
                    data.text
                  }}</a>
                </li>
              </ul>
            </div>
          </div>
          <create-property-drawer
            :resources="resources"
            :visible="visible"
            @open="showDrawer()"
            @close="closeDrawer()"
            is-type="TOOLING"
          ></create-property-drawer>

          <button
            type="button"
            class="btn btn-primary"
            data-toggle="modal"
            data-target="#exampleModal"
            id="toggleExportProgressBar"
            style="display: none"
          >
            <span>{{ resources["demo_model"] }}</span>
          </button>

          <!-- Modal -->
          <div
            class="modal fade"
            id="exampleModal"
            tabindex="-1"
            role="dialog"
            aria-labelledby="exampleModalLabel"
            aria-hidden="true"
            style="top: 30%"
          >
            <div class="modal-dialog" role="document" style="width: 350px">
              <div class="modal-content" style="border: none">
                <div class="modal-header">
                  <h5
                    class="modal-title"
                    id="exampleModalLabel"
                    style="font-weight: bold"
                  >
                    {{ resources["exporting_data"] }}
                  </h5>
                  <button
                    type="button"
                    class="close"
                    data-dismiss="modal"
                    aria-label="Close"
                    @click="stopExport"
                  >
                    <span aria-hidden="true">&times;</span>
                  </button>
                </div>
                <div class="modal-body" style="height: 100px">
                  <div style="text-align: center">
                    <p style="color: #615ea9">{{ resources["loading"] }}...</p>
                  </div>
                  <div id="myProgress">
                    <div id="myBar" style="display: inline-block"></div>
                    <div
                      style="
                        display: inline-block;
                        width: 24px;
                        height: 24px;
                        background: #615ea9;
                        margin-left: -16px;
                        border-radius: 50%;
                      "
                    ></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div
      id="disable-modal"
      class="matching-modal"
      v-show="isShowDisableWarning"
    >
      <div class="modal-wrapper duplicated-modal" style="color: #4b4b4b">
        <div class="modal-container">
          <div class="modal-body">
            <div class="modal-body-content">
              <div class="warning-title">
                <img
                  src="/images/icon/warning-2.svg"
                  width="16"
                  height="16"
                  alt="warning"
                />
                <p v-if="listCheckedFull.length === 1">
                  Disable tooling {{ listCheckedFull[0].equipmentCode }}
                </p>
                <p v-else>
                  Disable
                  {{
                    selectType === "all"
                      ? total - (requestParam.size - listCheckedFull.length)
                      : listCheckedFull.length
                  }}
                  toolings.
                </p>
              </div>
              <div class="duplicated-title" style="font-size: 19px">
                <p>Disabling a tooling will un-match it from the sensor.</p>
                <p v-if="listCheckedFull.length === 1">
                  Are you sure you want to disable this tooling?
                </p>
                <p v-else>Are you sure you want to disable these toolings?</p>
              </div>
              <div class="duplicated-action">
                <button
                  class="btn btn-back"
                  @click="closeWarning"
                  v-text="resources['cancel']"
                ></button>
                <button
                  class="btn btn-delete"
                  style="margin-left: 5px"
                  @click="disable()"
                  v-text="resources['yes_disable']"
                ></button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <toast-alert
      @close-toast="closeToastAlert"
      :show="toastAlert.value"
      :title="toastAlert.title"
      :content="toastAlert.content"
      ref-custom="toolingToast"
    ></toast-alert>
    <manage-tab-view
      ref="manage-tab-view"
      :resources="resources"
      :object-type="'TOOLING'"
      @open-edit="openEdit"
      @open-duplicate="openDuplicate"
      @move-success="getTabsByCurrentUser"
      :trigger-update="triggerUpdateManageTab"
    ></manage-tab-view>
    <tab-view-modal
      ref="tab-view-modal"
      :resources="resources"
      :object-type="'TOOLING'"
      :all-tooling-list="allToolingList"
      :page-type="pageType"
      @show-company-details="showCompanyDetailsById"
      @show-part-chart="showPartChart"
      @edit-success="editSuccess"
      @create-success="createSuccess"
    ></tab-view-modal>
    <company-details :resources="resources"></company-details>
    <chart-part
      :show-file-previewer="
        (file) => {
          showFilePreviewer(file, 'chart-part');
        }
      "
      :resources="resources"
    >
    </chart-part>
    <chart-mold
      ref="chart-mold"
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
      :user-type="userType"
      :handle-submit="handleSubmitNote"
    ></chart-mold>
    <mold-details
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
    ></mold-details>
    <file-previewer :back="backToMoldDetail"></file-previewer>
    <corrective-form :resources="resources"></corrective-form>
    <location-history :resources="resources"></location-history>
    <delete-popup
      :confirm-delete="confirmDelete"
      :is-show-warning-again="isShowWarningAgain"
      :list="listChecked"
      :resources="resources"
    ></delete-popup>
    <revision-history :resources="resources"></revision-history>

    <action-bar-feature
      v-for="(feature, index) in featureList"
      :key="feature"
      :feature="feature"
      @feature-notify-next="featureNotifyNext"
      @feature-notify-done="featureNotifyDone"
    >
    </action-bar-feature>
    <custom-export-data-modal
      v-if="showExportModal"
      :show-export-modal="showExportModal"
      @export-dynamic="exportDynamic"
      @close-export="closeExport"
      :resources="resources"
      :is-old-gen-included="isOldGenIncluded"
    ></custom-export-data-modal>
    <category-library-modal
      :show-file-previewer="showFilePreviewer"
      :trigger-save-modal="triggerSaveModal"
      @open-edit-product-demand="openEditProductDemand"
      :resources="resources"
    ></category-library-modal>
    <edit-product-demand
      ref="editProductDemand"
      :resources="resources"
      @load-success="loadSuccess"
    >
    </edit-product-demand>
    <creator-work-order-dialog
      :resources="resources"
      :enable-asset-selection="false"
      :is-show="workorderModalVisible"
      :asset-selected="toolingSelectedToCreateWorkorder"
      :modal-type="'CREATE'"
      :work-order-type="selectedWorkOrderType"
      :is-tooling-asset-selected="true"
      @close="closeCreateWorkOrderModal"
    ></creator-work-order-dialog>

    <!--MULTI CHART-->
    <multi-chart
      v-show="showMultiChart"
      :show="showMultiChart"
      :resources="resources"
      :list-checked="listChecked"
      :list-checked-full="listCheckedFull"
      :octs="octs[optimalCycleTime.strategy]"
      @close="closeMultiChart"
    ></multi-chart>
    <machine-details :resources="resources"></machine-details>
  </div>
</template>

<script>
var Page = Common.getPage("molds");
const API_BASE_TOL_STP = Common.getApiBase().MOLD;
const API_BASE_TAB_STP = Common.getApiBase().TAB;

const DEFAULT_PARAMS = {
  query: "",
  sort: "lastShotAt,desc",
  page: 1,
  size: 20,
  toolingStatus: [
    "IN_PRODUCTION",
    "IDLE",
    "INACTIVE",
    "SENSOR_OFFLINE",
    "SENSOR_DETACHED",
    "NO_SENSOR",
    "ON_STANDBY",
  ],
  counterStatus: ["INSTALLED", "NOT_INSTALLED", "DETACHED"],
  filterCode: "COMMON",
  tabName: "All",
};

const EXPORT_TYPE = {
  PDF: "PDF",
  XLSX: "XLSX",
};

const DIRECT_FROM = {
  WIDGET_TOTAL_COST_OWNER_SHIP: "WIDGET_TOTAL_COST_OWNER_SHIP",
  WIDGET_INACTIVE_TOOLING: "WIDGET_INACTIVE_TOOLING",
  WIDGET_CYCLE_TIME_COMPLIANCE: "WIDGET_CYCLE_TIME_COMPLIANCE",
  WIDGET_TOTAL_TOOLINGS: "WIDGET_TOTAL_TOOLINGS",
  WIDGET_OVERALL_UTILIZATION: "WIDGET_OVERALL_UTILIZATION",
  WIDGET_OPERATIONAL_SUMMARY: "WIDGET_OPERATIONAL_SUMMARY",
};

const LIST_COLUMN_SET = {
  WIDGET_TOTAL_COST_OWNER_SHIP: [
    "toolingId",
    "costTooling",
    "accumulatedMaintenanceCost",
    "lastShot",
    "slDepreciation",
    "upDepreciation",
    "tco",
  ],
  WIDGET_CYCLE_TIME_COMPLIANCE: [
    "toolingId",
    "supplierCompanyName",
    "toolmakerContractedCycleTimeSeconds",
    "contractedCycleTimeSeconds",
    "cycleTime",
    "weightedAverageCycleTime",
    "ctCompliance",
  ],
};

module.exports = {
  components: {
    "chart-part": httpVueLoader("/components/chart-part.vue"),
    "chart-mold": httpVueLoader("/components/chart-mold/chart-mold-modal.vue"),
    "mold-details": httpVueLoader("/components/mold-details.vue"),
    "corrective-form": httpVueLoader("/components/corrective-form.vue"),
    "company-details": httpVueLoader("/components/company-details.vue"),
    "location-history": httpVueLoader("/components/location-history.vue"),
    "delete-popup": httpVueLoader("/components/delete-popup.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "op-status": httpVueLoader("/components/configuration/OpStatus.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "export-data-modal": httpVueLoader("/components/export-data-modal.vue"),
    "custom-export-data-modal": httpVueLoader(
      "/components/custom-export-data-modal.vue"
    ),
    "common-select-popover": httpVueLoader(
      "/components/common/dc-component/common-select-popover.vue"
    ),
    // "date-picker-modal": httpVueLoader("/components/@base/date-picker/date-picker-modal.vue"),
    "category-product-dropdown": httpVueLoader(
      "/components/category-product-dropdown.vue"
    ),
    "category-library-modal": httpVueLoader(
      "/components/category/category-library/index.vue"
    ),
    "edit-product-demand": httpVueLoader(
      "/components/category/edit-product-demand.vue"
    ),
    "progress-bar": httpVueLoader(
      "/components/@base/progress-bar/progress-bar.vue"
    ),
    "depreciation-popup": httpVueLoader(
      "/components/mold-popup/depreciation-popup.vue"
    ),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view-new.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-modal-new.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action-new.vue"),
    "multi-chart": httpVueLoader(
      "/components/chart-mold/multi-chart/multi-chart.vue"
    ),
    "upper-company-dropdown": httpVueLoader(
      "/components/upper-company-dropdown.vue"
    ),
    "creator-work-order-dialog": httpVueLoader(
      "/components/work-order/creator/creator-work-order-dialog.vue"
    ),
    "machine-details": httpVueLoader("/components/machine-details.vue"),
  },
  props: {
    searchText: String,
    reloadKey: [String, Number],
    filterCode: String,
  },
  data() {
    return {
      resources: {},
      listOfItems: [
        { title: "Create Tooling" },
        { title: "Create Tooling Property" },
      ],
      showPartPicker: false,
      listFilterItems: [
        {
          id: "OP",
          title: "By Tooling Status",
          childrens: [
            { title: "On Standby", code: "ON_STANDBY" },
            { title: "In Production", code: "IN_PRODUCTION" },
            { title: "Idle", code: "IDLE" },
            { title: "Inactive", code: "INACTIVE" },
            { title: "Sensor Offline", code: "SENSOR_OFFLINE" },
            { title: "Sensor Detached", code: "SENSOR_DETACHED" },
            { title: "No Sensor", code: "NO_SENSOR" },
          ],
        },
        {
          id: "status",
          title: "By Sensor Status",
          childrens: [
            { code: "INSTALLED", title: "Installed" },
            { code: "NOT_INSTALLED", title: "Not Installed" },
            { code: "DETACHED", title: "Detached" },
          ],
        },
      ],
      dropDownType: "main",
      showPartPicker: false,
      showFilter: false,
      isShowWarningAgain: !localStorage.getItem("dontShowWarning"),
      deletePopup: false,
      serverName: "",
      showDropdown: false,
      errorFieldName: false,
      usetType: "",
      optimalCycleTime: {},
      octs: {
        ACT: "Approved CT",
        WACT: "Weighted Average CT",
      },
      results: [],
      total: 0,
      pagination: [],
      requestParam: { ...DEFAULT_PARAMS },

      firstCompare: "eq",
      secondCompare: "and",
      thirdCompare: "eq",
      firstCaviti: "",
      secondCaviti: "",
      counterId: "",
      partId: "",
      equipmentStatusCodes: [],
      listCategories: [],
      codes: {},
      sortType: {
        TOOLING_ID: "equipmentCode",
        // COUNTER: 'counter.equipmentCode',
        PART: "part",
        COMPANY: "location.company.name",
        LOCATION: "location.name",
        LAST_SHOT: "lastShot",
        OP: "operatingStatus",
        STATUS: "equipmentStatus",
        DATA_SUBMISSION: "dataSubmission",
      },
      currentSortType: "lastShotAt",
      isDesc: true,
      listAllIds: [],
      listChecked: [],
      listCheckedFull: [],

      pageType: "TOOLING_SETTING",
      allColumnList: [],
      optionParams: {
        dateViewType: "",
        fromPage: "",
      },
      visible: false,
      objectType: "TOOLING",
      selectedDropdown: null,
      nextSelectDropdown: null,
      isShowingDropDown: false,
      showingClass: false,
      configDeleteField: {},
      showExportModal: false,
      dataExport: "",
      showExport: false,
      EXPORT_TYPE: EXPORT_TYPE,
      selectedDownloadType: "default",
      currentIntervalIds: [],
      isCallingDone: false,
      isStopedExport: false,
      dashboardChartType: null,
      lastShotDropdown: false,
      lastShotData: {
        filter: null,
        sort: "",
      },
      cancelToken: undefined,
      featureList: ["ACTION_BAR", "TOOLING_TAB", "DATA_TABLE_TAB"],
      visiblePopover: false,
      displayCaret: false,
      showAnimation: false,
      //
      triggerSaveModal: 0,
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-ww"),
        toTitle: moment().format("YYYY-ww"),
      },
      editFromChild: false,
      userType: null,
      listSelectedTabs: [],
      visibleMoreAction: false,
      currentActiveTab: {
        id: null,
        defaultTab: true,
        name: "All",
        show: true,
      },
      isTabView: false,
      allToolingList: [],
      toastAlert: {
        value: false,
        title: "",
        content: "",
      },
      visibleMove: false,
      showMultiChart: false,
      workorderModalVisible: false,
      showUpDepreciationPopup: false,
      showSlDepreciationPopup: false,
      selectedToolingId: null,
      options: {},
      triggerUpdateManageTab: 0,
      showWorkOrderDropdown: false,
      workOrderType: [
        {
          name: "General",
          value: "GENERAL",
        },
        {
          name: "Inspection",
          value: "INSPECTION",
        },
        {
          name: "Emergency",
          value: "EMERGENCY",
        },
        {
          name: "Preventive Maintenance",
          value: "PREVENTATIVE_MAINTENANCE",
        },
        {
          name: "Corrective Maintenance",
          value: "CORRECTIVE_MAINTENANCE",
        },
        {
          name: "Disposal",
          value: "DISPOSAL",
        },
        {
          name: "Refurbishment",
          value: "REFURBISHMENT",
        },
      ],
      selectedWorkOrderType: null,
      isShowDisableWarning: false,
      ruleOpStatus: {
        inProduct: "",
        idle: "",
        inactive: "",
        sensorOffline: "",
        sensorDetached: "The sensor was physically detached from the tooling.",
        disabled: "The tooling has been  manually disabled.",
        disposed: "The tooling has been through the disposal process.",
        noSensor: "The tooling has no matched sensor.",
        onStandby:
          "The tooling has been matched with a sensor but not made any shot yet.",
      },
      isFilterChanging: false,
      defaultStatusListLength: 0,
      from: "",
      toolingSelectedToCreateWorkorder: [],
      isNoteTabReady: false,
      selectType: "",
    };
  },
  watch: {
    reloadKey() {
      this.paging(1);
    },
    searchText(newVal) {
      this.requestParam.query = newVal;
      this.paging(1);
    },
    visibleMoreAction(newVal) {
      if (!newVal) {
        // this.updateTabStatus();
      }
    },
    visiblePopover(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.displayCaret = false;
      }
    },
    isDataSubmissionEnabled(newVal) {
      if (newVal) {
        this.allColumnList.push({
          label: this.resources["data_approval"],
          field: "dataSubmission",
          default: true,
          sortable: true,
          sortField: "dataSubmission",
        });
      }
    },
  },
  methods: {
    async popup(popupType) {
      console.log("popupType", popupType);
      const searchParams = new URLSearchParams(location.href.split("?")[1]);
      console.log("searchParams");
      if (["NOTE_MENTIONED", "NOTE_REPLIED"].includes(popupType)) {
        const moldId = searchParams.get("objectFunctionId");
        const noteId = searchParams.get("noteId");

        console.log(moldId, noteId);
        if (!moldId || !noteId) return;

        try {
          const res = await axios.get(`${Page.API_BASE}/${moldId}`);
          const mold = res.data;
          this.showChart(mold, null, null, "switch-notes");
        } catch (error) {
          console.error("Error when getting mold data to show popup", error);
        }
      }
    },
    shouldShowNoDataForTimeBaseMaintenance(result) {
      return result.pmStrategy === "TIME_BASED";
    },
    getTooltipCounterStatus(status) {
      switch (status) {
        case "INSTALLED":
          return "The sensor has been matched with a tooling.";
        case "NOT_INSTALLED":
          return "The sensor has no matched tooling.";
        case "DETACHED":
          return "The sensor has become physically detached from the tooling.";
        default:
          return "";
      }
    },
    getTooltipMoldStatus(status) {
      switch (status) {
        // TODO: RECHECK
        case "WORKING":
        case "IN_PRODUCTION":
          return this.ruleOpStatus.inProduct;
        case "IDLE":
          return this.ruleOpStatus.idle;
        case "NOT_WORKING":
        case "INACTIVE":
          return this.ruleOpStatus.inactive;
        case "SENSOR_OFFLINE":
        case "DISCONNECTED":
          return this.ruleOpStatus.sensorOffline;
        case "SENSOR_DETACHED":
          return this.ruleOpStatus.sensorDetached;
        case "NO_SENSOR":
          return this.ruleOpStatus.noSensor;
        case "DISABLED":
          return this.ruleOpStatus.disabled;
        case "DISPOSED":
          return this.ruleOpStatus.disposed;
        case "ON_STANDBY":
          return this.ruleOpStatus.onStandby;
        default:
          return "";
      }
    },
    getTimeUnit(timeUnit) {
      return timeUnit === "HOURS" ? " hour(s) " : " day(s) ";
    },
    getMoldStatusText(status) {
      switch (status) {
        case "WORKING":
        case "IN_PRODUCTION":
          return "In Production";
        case "IDLE":
          return "Idle";
        case "NOT_WORKING":
        case "INACTIVE":
          return "Inactive";
        case "SENSOR_OFFLINE":
          return "Sensor Offline";
        case "SENSOR_DETACHED":
          return "Sensor detached";
        case "NO_SENSOR":
          return "No Sensor";
        case "DISABLED":
          return "Disabled";
        case "ON_STANDBY":
          return "On Standby";
        case "DISPOSED":
          return "Disposed";
      }
    },
    closeWarning() {
      this.isShowDisableWarning = false;
    },
    async selectTab(tabCheck, index) {
      let __listSelectedTabs = this.listSelectedTabs;
      __listSelectedTabs[index].shown = !tabCheck.shown;
      if (tabCheck.shown) {
        await this.paging(1);
        this.showTab(tabCheck);
      } else {
        this.closeTab(tabCheck);
      }
      this.listSelectedTabs = __listSelectedTabs;
    },
    showMachineDetails: function (machineId) {
      let child = Common.vue.getChild(this.$children, "machine-details");
      if (child != null) {
        child.showDetailsById(machineId);
      }
    },
    closeWorkorderDropdown() {
      this.showWorkOrderDropdown = false;
    },
    showCreateWorkorder(item, type) {
      console.log("item", item, type);
      this.selectedWorkOrderType = type;
      const {
        locationCode,
        equipmentCode,
        id,
        locationId,
        locationName,
        pmStrategy,
      } = this.listCheckedFull[0];
      this.toolingSelectedToCreateWorkorder = [
        {
          assetView: "tooling",
          checked: true,
          equipmentCode,
          id,
          locationCode,
          locationId,
          locationName,
          title: equipmentCode,
          pmStrategy,
        },
      ];
      this.workorderModalVisible = true;
    },
    closeCreateWorkOrderModal() {
      this.workorderModalVisible = false;
    },
    toggleUpDepreciationPopup(toolingId) {
      console.log("toolingId", toolingId);

      if (!this.showUpDepreciationPopup) {
        setTimeout(() => {
          this.showUpDepreciationPopup = true;
          this.selectedToolingId = toolingId;
        }, 200);
        this.showSlDepreciationPopup = false;
      } else {
        this.showUpDepreciationPopup = false;
        this.selectedToolingId = null;
      }
    },
    toggleSlDepreciationPopup(toolingId) {
      if (!this.showSlDepreciationPopup) {
        setTimeout(() => {
          this.selectedToolingId = toolingId;
          this.showSlDepreciationPopup = true;
        }, 200);
        this.showUpDepreciationPopup = false;
      } else {
        this.showSlDepreciationPopup = false;
        this.selectedToolingId = null;
      }
    },
    getDepreciationPercentage(numerator, denominator) {
      return Math.min(100, Math.round((numerator / denominator) * 1000) / 10);
    },
    async removeFromTab() {
      try {
        let listId = [];
        if (this.selectType === "all") {
          listId = this.results
            .map((i) => i.id)
            .filter((x) => !this.listChecked.includes(x));
        } else {
          listId = this.listChecked;
        }
        const param = Common.param({
          ...this.requestParam,
          selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
          [this.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
        });
        await axios.delete(`${API_BASE_TOL_STP}/tab-items-batch?${param}`);
        this.moveSuccess({
          title: "Success!",
          content: `Your data has been removed from ${this.currentActiveTab.name} tab`,
        });
      } catch (error) {
        console.log("data-family-tooling:::removeFromTab:::error", error);
      }
    },
    async moveToTab(tab) {
      let listId = [];
      if (this.selectType === "all") {
        listId = this.results
          .map((i) => i.id)
          .filter((x) => !this.listChecked.includes(x));
      } else {
        listId = this.listChecked;
      }
      const param = Common.param({
        ...this.requestParam,
        toTabName: tab.name,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
      });
      try {
        await axios.put(`${API_BASE_TOL_STP}/move-tab-items-batch?${param}`);
        this.moveSuccess({
          title: "Success!",
          content: `Your data has been moved to ${tab.name} tab`,
        });
      } catch (error) {
        console.log(error);
      }
    },
    openCustomizationModal() {
      const customizationModalEl = this.$refs["customization-modal"];
      console.log("customizationModalEl", customizationModalEl);
      if (customizationModalEl) {
        customizationModalEl.openModal();
        this.visibleMoreAction = false;
      }
    },
    openDuplicate(tab) {
      const tabViewModalEl = this.$refs["tab-view-modal"];
      if (tabViewModalEl) {
        tabViewModalEl.showCreateTabView("DUPLICATE", tab);
        this.visibleMoreAction = false;
      }
    },
    openEdit(tab) {
      const tabViewModalEl = this.$refs["tab-view-modal"];
      if (tabViewModalEl) {
        tabViewModalEl.showCreateTabView("EDIT", tab);
        this.visibleMoreAction = false;
      }
    },
    truncateText(text, size) {
      text = text && text.trim();
      if (text && text.length > size) {
        text = text.slice(0, size) + "...";
      }
      return text;
    },
    convertTextTabName(tab) {
      if (tab.defaultTab) {
        return this.resources[tab.name];
      } else {
        return this.truncateText(tab.name, 20);
      }
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
    closeToastAlert() {
      this.toastAlert.value = false;
    },
    createTabView() {
      const tabViewModalEl = this.$refs["tab-view-modal"];
      if (tabViewModalEl) {
        tabViewModalEl.showCreateTabView("CREATE");
        this.visibleMoreAction = false;
      }
    },
    async getTabsByCurrentUser(isCallAtMounted) {
      try {
        const { data } = await axios.get(`${API_BASE_TAB_STP}/TOOLING`);
        this.listSelectedTabs = data.content;
        if (isCallAtMounted) {
          this.setNewActiveTab(isCallAtMounted);
        }
      } catch (error) {
        console.log(
          "data-family-tooling:::getTabsByCurrentUser:::error",
          error
        );
      }
    },
    setNewActiveTab() {
      const listShowed = this.listSelectedTabs.filter((t) => t.shown);
      if (listShowed.length) {
        this.tab(listShowed[0]);
      }
    },
    async showTab(tab) {
      try {
        tab.shown = true;
        if (tab.defaultTab) {
          await axios.put(
            `${API_BASE_TAB_STP}/TOOLING/names/${tab.name}/shown`
          );
        } else {
          await axios.put(`${API_BASE_TAB_STP}/TOOLING/${tab.id}/shown`);
        }
      } catch (error) {
        console.log("data-family-company:::showTab:::error", error);
      }
    },
    async closeTab(tab) {
      try {
        tab.shown = false;
        if (tab.name === this.currentActiveTab.name) {
          this.setNewActiveTab();
        }
        if (tab.defaultTab) {
          await axios.put(
            `${API_BASE_TAB_STP}/TOOLING/names/${tab.name}/hidden`
          );
        } else {
          await axios.put(`${API_BASE_TAB_STP}/TOOLING/${tab.id}/hidden`);
        }
      } catch (error) {
        console.log("data-family-company:::closeTab:::error", error);
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
    closeCreateWorkOrderModal() {
      this.workorderModalVisible = false;
    },
    toggleUpDepreciationPopup(toolingId) {
      console.log("toolingId", toolingId);

      if (!this.showUpDepreciationPopup) {
        setTimeout(() => {
          this.showUpDepreciationPopup = true;
          this.selectedToolingId = toolingId;
        }, 200);
        this.showSlDepreciationPopup = false;
      } else {
        this.showUpDepreciationPopup = false;
        this.selectedToolingId = null;
      }
    },
    toggleSlDepreciationPopup(toolingId) {
      if (!this.showSlDepreciationPopup) {
        setTimeout(() => {
          this.selectedToolingId = toolingId;
          this.showSlDepreciationPopup = true;
        }, 200);
        this.showUpDepreciationPopup = false;
      } else {
        this.showSlDepreciationPopup = false;
        this.selectedToolingId = null;
      }
    },
    getDepreciationPercentage(numerator, denominator) {
      return Math.min(100, Math.round((numerator / denominator) * 1000) / 10);
    },
    getTimeData(timeData) {
      if (!timeData) return "";
      let time = moment.unix(timeData).format("YYYY-MM-DD HH:mm:ss");
      if (time) {
        return time?.slice(10, 16);
      } else return "";
    },
    onItemSelect(item) {
      if (item.title === this.listOfItems[0].title) {
        Common.onChangeHref("/admin/molds/new");
      } else if (item.title === this.listOfItems[1].title) {
        this.showDrawer();
      }
    },
    handleShowPartPicker() {
      this.showPartPicker = true;
    },
    handleClosePartPicker() {
      this.showPartPicker = false;
    },
    handleToggle() {
      this.showFilter = false;
      if (!this.showPartPicker) {
        this.handleShowPartPicker();
      } else {
        this.handleClosePartPicker();
      }
      this.displayCaret = true;
      if (!this.showPartPicker) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700);
      }
    },
    handleCheckStatus(items) {
      console.log("handleCheckStatus", items);
      this.showFilter = false;
      let opStatus = [];
      let eqStatus = [];
      let isAllEqStatus = false;
      let isAllOpStatus = false;
      items.forEach((item) => {
        if (
          item.title === this.listFilterItems[0].title &&
          item.childrens.length
        ) {
          item.childrens.forEach((children) => {
            if (children.title === "All") {
              isAllOpStatus = true;
            } else {
              opStatus.push(children.code);
            }
          });
        }
      });
      if (isAllOpStatus) {
        opStatus = [];
      }
      this.requestParam.toolingStatus = opStatus;

      items.forEach((item) => {
        if (
          item.title == this.listFilterItems[1].title &&
          item.childrens.length > 0
        ) {
          item.childrens.forEach((children) => {
            if (children.title == "All") {
              isAllEqStatus = true;
            } else {
              eqStatus.push(children.code);
            }
          });
        }
      });
      if (isAllEqStatus) {
        eqStatus = [];
      }

      this.requestParam.counterStatus = eqStatus;
      const currentStatusListLength =
        this.requestParam.toolingStatus.length +
        this.requestParam.counterStatus.length;
      if (this.defaultStatusListLength > currentStatusListLength) {
        this.isFilterChanging = true;
      } else {
        this.isFilterChanging = false;
      }
    },
    checkedItems(items) {
      this.handleCheckStatus(items);
      this.search();
    },
    resetHandler(items) {
      console.log("resetHandler", items);
      this.showFilter = false;
      this.requestParam.toolingStatus = [
        "ON_STANDBY",
        "IN_PRODUCTION",
        "IDLE",
        "INACTIVE",
        "SENSOR_OFFLINE",
        "SENSOR_DETACHED",
        "NO_SENSOR",
      ];
      this.requestParam.counterStatus = [
        "INSTALLED",
        "NOT_INSTALLED",
        "DETACHED",
      ];
      this.isFilterChanging = false;
      this.search();
    },
    handleCloseFilter() {
      this.showFilter = false;
      this.search();
    },
    handleToggleFilter() {
      this.showPartPicker = false;
      if (!this.showFilter) {
        this.handleShowFilter();
      } else {
        this.handleCloseFilter();
      }
    },
    handleShowFilter() {
      this.showFilter = true;
    },
    handleToggleMoreActions() {
      this.visibleMoreAction = true;
      this.isTabView = false;
    },
    animationOutlineDropdown() {
      this.displayCaret = true;
      if (!this.visiblePopover) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700);
      }
    },
    featureNotifyNext() {
      Common.triggerShowNewFeatureNotify(
        this.$children,
        "action-bar-feature",
        null,
        {
          key: "feature",
          value: "DATA_TABLE_TAB",
        }
      );
    },
    featureNotifyDone(feature) {
      this.featureList = this.featureList.filter((f) => f !== feature);
      if (this.featureList.length > 0) {
        Common.triggerShowNewFeatureNotify(
          this.$children,
          "action-bar-feature",
          null,
          {
            key: "feature",
            value: this.featureList[0],
          }
        );
      }
    },
    updateVariable(data) {
      this.lastShotData = data;
    },
    closeLastShot() {
      if (this.lastShotDropdown) {
        console.log(this.lastShotData, "closeLastShot");
        if (this.lastShotData && this.lastShotData.sort) {
          this.currentSortType = "lastShot";
          this.isDesc = this.lastShotData.sort === "desc";
          this.requestParam.sort = `${
            this.lastShotData.filter === null
              ? "lastShot"
              : `accumulatedShot.${this.lastShotData.filter}`
          },${this.isDesc ? "desc" : "asc"}`;
        }
        this.sort();
        this.lastShotDropdown = false;
      }
    },
    showDisableWarning() {
      this.isShowDisableWarning = true;
    },
    enable() {
      this.saveBatch("enable");
    },
    disable() {
      this.saveBatch("disable");
    },
    async saveBatch(action) {
      console.log("saveBatch");
      const { filterCode, query, tabName, counterStatus, toolingStatus } =
        this.requestParam;
      let listId = [];
      if (this.selectType === "all") {
        listId = this.results
          .map((i) => i.id)
          .filter((x) => !this.listChecked.includes(x));
      } else {
        listId = this.listChecked;
      }
      const param = Common.param({
        filterCode,
        query,
        tabName,
        counterStatus,
        toolingStatus,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
      });
      try {
        await axios.put(`${API_BASE_TOL_STP}/${action}-batch?${param}`);
      } catch (error) {
        console.log(error);
      } finally {
        this.paging(1);
        this.listChecked = [];
        this.listCheckedFull = [];
        this.closeWarning();
        this.getTabsByCurrentUser();
      }
    },
    progressBarRun() {
      this.currentIntervalIds.forEach((id) => {
        clearInterval(id);
      });
      let $this = this;
      let flag = 0;
      if (flag == 0) {
        flag = 1;
        var elem = document.getElementById("myBar");
        elem.style.width = "0%";
        var width = 1;
        var id = setInterval(frame, 40);

        this.currentIntervalIds = [];
        this.currentIntervalIds.push(id);

        function frame() {
          if (width >= 100) {
            clearInterval(id);
            flag = 0;
            document.getElementById("toggleExportProgressBar").click();
          } else {
            if ($this.isStopedExport) {
              document.getElementById("toggleExportProgressBar").click();
              $this.currentIntervalIds.forEach((id) => {
                clearInterval(id);
              });
            }
            console.log($this.isCallingDone);
            if ($this.isCallingDone) {
              width = width + 1;
            }
            if (width < 30) {
              width = width + 0.5;
            } else if (width >= 30 && width < 50) {
              width = width + 0.3;
            } else if (width >= 50 && width < 75) {
              width = width + 0.1;
            } else if (width >= 75 && width < 85) {
              width = width + 0.05;
            } else if (width >= 85 && width < 99.5) {
              width = width + 0.01;
            }
            elem.style.width = width + "%";
          }
        }
      }
    },

    stopExport() {
      this.isStopedExport = true;
    },
    downloadToolingDynamic(type, timeWaiting) {
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_tooling_data`;

      let path = "";

      const listChecked =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;

      let downloadParams = listChecked.join(",");

      if (type === EXPORT_TYPE.PDF) {
        path = "exportPdfDetailMolds";
      } else {
        path = "exportExcel";
      }
      setTimeout(() => {
        this.isStopedExport = false;
        this.isCallingDone = false;
        this.closeExport();

        document.getElementById("toggleExportProgressBar").click();
        this.progressBarRun();
      }, Common.getNumMillisecondsToTime(timeWaiting));
      let $this = this;

      let params = null;
      if (["CUSTOM_RANGE", "DAILY"].includes(this.dataExport.rangeType)) {
        params = {
          ids: downloadParams,
          timezoneOffsetClient: new Date().getTimezoneOffset(),
          sort: this.requestParam.sort ?? "",
          rangeType:
            this.dataExport.rangeType === "DAILY"
              ? ""
              : this.dataExport.rangeType,
          dataTypes: this.dataExport.dataType,
          frequency: this.dataExport.dataFrequency,
          fromDate: this.dataExport.startDate,
          toDate: this.dataExport.endDate,
        };
      } else {
        params = {
          ids: downloadParams,
          timezoneOffsetClient: new Date().getTimezoneOffset(),
          sort: this.requestParam.sort ?? "",
          rangeType: this.dataExport.rangeType,
          dataTypes: this.dataExport.dataType,
          frequency: this.dataExport.dataFrequency,
          time: this.dataExport.time,
        };
      }
      console.log(params);
      const params2 = Common.param(params);
      let url = `/api/molds/exportExcelDynamicNew?` + params2;

      axios.get(url, { responseType: "blob", observe: "response" }).then(
        (response) => {
          setTimeout(() => {
            if (!this.isStopedExport) {
              this.isCallingDone = true;
              // this.closeExport()
              setTimeout(() => {
                let headerLine = response.headers["content-disposition"];
                console.log("res:", headerLine);
                if (headerLine != null)
                  fileName =
                    headerLine.split("filename=")[1] || fileName + ".xlsx";

                var blob = new Blob([response.data], {
                  type: "text/plain;charset=utf-8",
                });
                saveAs(blob, `${fileName}`);
              }, 2000);
            }
          }, Common.getNumMillisecondsToTime(timeWaiting) + 10);
        },
        (error) => {
          setTimeout(() => {
            console.log(error);
            this.isStopedExport = true;
            // this.closeExport();
            error.data.text().then((text) => Common.alert(text));
          }, Common.getNumMillisecondsToTime(timeWaiting) + 1000);
        }
      );
    },
    exportDynamic(data, timeWaiting) {
      console.log(data, "data ne");
      this.dataExport = data;
      this.downloadToolingDynamic(EXPORT_TYPE.XLSX, timeWaiting);
    },
    openExportModal() {
      this.showExportModal = true;
    },
    closeExport() {
      const root = document.getElementById("export-data-modal");
      const modalBody = root.querySelectorAll(".ant-modal-content");
      if (modalBody) {
        const el = modalBody[0];
        el.classList.add("fade-out-animate");
        setTimeout(() => {
          el.classList.add("hidden");
          setTimeout(() => {
            const btn = document.getElementById("btn-export");
            console.log("btn classlist", btn && btn.classList);
            if (btn && btn.classList && btn.classList.contains("disable-cta")) {
              btn.classList.remove("disable-cta");
            }
            this.showExportModal = false;
            setTimeout(() => {
              el.classList.remove("fade-out-animate");
            }, 50);
          }, 200);
        }, 250);
      }
    },
    closeShowExport() {
      this.showExport = false;
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    goToEdit() {
      console.log(this.listChecked[0], "this.listChecked[0]");
      window.location.href = `/admin/molds/${this.listChecked[0]}`;
    },
    confirmDelete(checked) {
      if (checked) {
        localStorage.setItem("dontShowWarning", "true");
      }
      const listInstalled = this.listCheckedFull
        .filter((item) => item.equipmentStatus !== "INSTALLED")
        .map((item) => {
          return item.id;
        });
      if (listInstalled.length >= 1) {
        const self = this;
        axios
          .post(`/api/molds/delete-in-batch`, listInstalled)
          .then((res) => {
            $("#op-delete-popup").modal("hide");
            self.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      } else {
        $("#op-delete-popup").modal("hide");
        this.paging(1);
      }
    },
    showDeletePopup() {
      if (!localStorage.getItem("dontShowWarning")) {
        var child = Common.vue.getChild(this.$children, "delete-popup");
        if (child != null) {
          child.showDeletePopup();
        }
      } else {
        const self = this;
        axios
          .post(`/api/molds/delete-in-batch`, this.listChecked)
          .then((res) => {
            self.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      }
    },
    animationPrimary() {
      $(".animationPrimary").click(() => {
        $(this).addClass("primary-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          (event) => {
            $(this).removeClass("primary-animation");
          }
        );
      });
    },
    animationOutline() {
      $(".animationOutline").click(() => {
        $(this).addClass("outline-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          (event) => {
            $(this).removeClass("outline-animation");
          }
        );
      });
    },
    showDrawer() {
      // this.resetDrawer()
      this.visible = true;
    },
    closeDrawer() {
      this.visible = false;
    },

    ratio(mold) {
      return ((mold.lastShot / mold.designedShot) * 100).toFixed(1);
    },
    ratioStyle(mold) {
      return "width: " + this.ratio(mold) + "%";
    },
    ratioColor(mold) {
      var ratio = this.ratio(mold);

      if (ratio < 25) return "bg-info";
      else if (ratio < 50) return "bg-success";
      else if (ratio < 75) return "bg-warning";
      else return "bg-danger";
    },

    ratioMaintenance(result) {
      return ((result.lastShot / result.pmCheckpoint) * 100).toFixed(1);
    },
    ratioUtilizationRate(result) {
      return ((result.lastShot / result.designedShot) * 100).toFixed(1);
    },

    ratioMaintenance2(result) {
      return ((result.untilNextPm / result.preventCycle) * 100).toFixed(2);
    },
    ratioMaintenanceStyle(result) {
      return "width: " + this.ratioMaintenance2(result) + "%";
    },
    ratioMaintenanceStyleUtilizationRate(result) {
      return "width: " + this.ratioUtilizationRate(result) + "%";
    },
    ratioMaintenanceStyle2(result) {
      return "width: " + this.ratioMaintenance2(result) + "%";
    },
    ratioMaintenanceColor(result) {
      if (result.maintenanceStatus === "UPCOMING") return "bg-warning";
      else return "bg-danger";
    },
    ratioColorUtilizationRate: function (result) {
      var ratio = this.ratioUtilizationRate(result);

      if (ratio < 25) return "bg-info";
      else if (ratio < 50) return "bg-success";
      else if (ratio < 75) return "bg-warning";
      else return "bg-danger";
    },
    ratioMaintenanceColor2: function (result) {
      if (result.maintenanceStatus === "UPCOMING") return "bg-warning";
      else return "bg-danger";
    },
    showPmCheckpointPrediction: function (
      pmCheckpointPrediction,
      maintenanceStatus
    ) {
      if (pmCheckpointPrediction == null) return "-";
      if (pmCheckpointPrediction > 90) {
        return "More than 90 days";
      }
      let dayName = pmCheckpointPrediction == 1 ? " day" : " days";
      let print =
        pmCheckpointPrediction == 0
          ? "Today"
          : maintenanceStatus == "OVERDUE"
          ? "Overdue " + pmCheckpointPrediction + dayName
          : "In " + pmCheckpointPrediction + dayName;
      console.log("showPmCheckpointPrediction", print);
      return print;
    },
    async setAllColumnList() {
      const baseColumns = [
        {
          label: this.resources["tooling_id"],
          field: "toolingId",
          mandatory: true,
          default: true,
          sortable: true,
          sortField: "equipmentCode",
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["part"],
          field: "part",
          mandatory: true,
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "location.company.name",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["location"],
          field: "location",
          sortable: true,
          sortField: "location.name",
        },
        {
          label: this.resources["accumulated_shots"],
          field: "lastShot",
          default: true,
          sortable: true,
        },
        {
          label: this.resources["accumulated_shots_checkpoint"],
          field: "lastShotCheckpoint",
          sortable: true,
        },
        {
          label: this.resources["pm_checkpoint_progress"],
          field: "utilNextPm",
          sortable: true,
        },
        {
          label: this.resources["pm_checkpoint_prediction"],
          field: "pmCheckpointPrediction",
          sortable: true,
          sortField: "dueDate",
        },
        {
          label: this.resources["latest_cycle_time"],
          field: "cycleTime",
          sortable: true,
          sortField: "lastCycleTime",
        },
        {
          label: this.resources["tooling_status"],
          field: "toolingStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "toolingStatus",
          defaultPosition: 5,
        },
        {
          label: this.resources["sensor_status"],
          field: "sensorStatus",
          sortable: true,
          default: true,
          defaultSelected: true,
          sortField: "sensorStatus",
          defaultPosition: 6,
        },
        {
          label: this.resources["cycle_time_tolerance_l1"],
          field: "cycleTimeToleranceL1",
          sortable: true,
          sortField: "cycleTimeLimit1",
        },
        {
          label: this.resources["cycle_time_tolerance_l2"],
          field: "cycleTimeToleranceL2",
          sortable: true,
          sortField: "cycleTimeLimit2",
        },
        {
          label: this.resources["weighted_average_cycle_time"],
          field: "weightedAverageCycleTime",
          sortable: true,
          sortField: "weightedAverageCycleTime",
        },
        {
          label: this.resources["uptime_target"],
          field: "uptimeTarget",
          sortable: true,
        },
        {
          label: this.resources["engineer_in_charge"],
          field: "engineerInCharge",
          sortable: true,
          sortField: "engineer",
        },
        {
          label: this.resources["weight_of_runner_system_o"],
          field: "weightOfRunnerSystem",
          sortable: true,
          sortField: "weightRunner",
        },
        {
          label: this.resources["supplier_approved_cycle_time"],
          field: "contractedCycleTimeSeconds",
          sortable: true,
          sortField: "contractedCycleTime",
        },
        {
          label: this.resources["toolmaker_approved_cycle_time"],
          field: "toolmakerContractedCycleTimeSeconds",
          sortable: true,
          sortField: "toolmakerContractedCycleTime",
        },
        {
          label: this.resources["counter_id"],
          field: "counterCode",
          sortable: true,
          sortField: "counter.equipmentCode",
        },
        {
          label: this.resources["reset.forecastedMaxShots"],
          field: "designedShot",
          sortable: true,
        },
        {
          label: this.resources["hot_runner_number_of_drop"],
          field: "hotRunnerDrop",
          sortable: true,
        },
        {
          label: this.resources["hot_runner_zone"],
          field: "hotRunnerZone",
          sortable: true,
        },
        {
          label: this.resources["front_injection"],
          field: "injectionMachineId",
          sortable: true,
        },
        {
          label: this.resources["required_labor"],
          field: "labour",
          sortable: true,
        },
        {
          label: this.resources["last_date_of_shots"],
          field: "lastShotDate",
          sortable: true,
          sortField: "lastShotAt",
        },
        {
          label: this.resources["machine_ton"],
          field: "quotedMachineTonnage",
          sortable: true,
        },
        {
          label: this.resources["maintenance_interval"],
          field: "preventCycle",
          sortable: true,
        },
        {
          label: this.resources["maker_of_runner_system"],
          field: "runnerMaker",
          sortable: true,
        },
        {
          label: this.resources["maximum_capacity_per_week"],
          field: "maxCapacityPerWeek",
          sortable: true,
        },
        {
          label: this.resources["production_hour_per_day"],
          field: "shiftsPerDay",
          sortable: true,
        },
        {
          label: this.resources["production_day_per_week"],
          field: "productionDays",
          sortable: true,
        },
        {
          label: this.resources["shot_weight"],
          field: "shotSize",
          sortable: true,
        },
        {
          label: this.resources["supplier"],
          field: "supplierCompanyName",
          sortable: true,
          sortField: "supplier.name",
        },
        {
          label: this.resources["tool_description"],
          field: "toolDescription",
          sortable: true,
        },
        {
          label: this.resources["front_tool_size"],
          field: "toolingSizeView",
          sortable: true,
          sortField: "size",
        },
        {
          label: this.resources["tool_weight"],
          field: "toolingWeightView",
          sortable: true,
          sortField: "weight",
        },
        {
          label: this.resources["tooling_complexity"],
          field: "toolingComplexity",
          sortable: true,
        },
        {
          label: this.resources["tooling_letter"],
          field: "toolingLetter",
          sortable: true,
        },
        {
          label: this.resources["tooling_type"],
          field: "toolingType",
          sortable: true,
        },
        {
          label: this.resources["toolmaker"],
          field: "toolMakerCompanyName",
          sortable: true,
          sortField: "toolMaker.name",
        },
        {
          label: this.resources["type_of_runner_system"],
          field: "runnerTypeTitle",
          sortable: true,
          sortField: "runnerType",
        },
        {
          label: this.resources["upcoming_maintenance_tolerance"],
          field: "preventUpcoming",
          sortable: true,
        },
        {
          label: this.resources["uptime_tolerance_l1"],
          field: "uptimeLimitL1",
          sortable: true,
        },
        {
          label: this.resources["uptime_tolerance_l2"],
          field: "uptimeLimitL2",
          sortable: true,
        },
        {
          label: this.resources["utilization_rate"],
          field: "utilizationRate",
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["year_of_tool_made"],
          field: "madeYear",
          sortable: true,
        },
        {
          label: this.resources["inactive_period"],
          field: "inactivePeriod",
          sortable: true,
          sortField: "inactivePeriod",
        },
        {
          label: this.resources["product_and_category"],
          field: "productAndCategory",
          sortable: true,
          sortField: "productAndCategory",
        },
        {
          label: this.resources["memo"],
          field: "memo",
          sortable: true,
          sortField: "memo",
        },
        {
          label: this.resources["acquisition_cost"],
          field: "costTooling",
          sortable: true,
          sortField: "cost",
        },
        {
          label: this.resources["accumulated_maintenance_cost"],
          field: "accumulatedMaintenanceCost",
          sortable: true,
          sortField: "accumulatedMaintenanceCost",
        },
        {
          label: this.resources["total_cost_of_ownership"],
          field: "tco",
          sortable: true,
          sortField: "tco",
        },
        {
          label: this.resources["last_maintenance_date"],
          field: "lastMaintenanceDate",
          sortable: true,
          sortField: "lastMaintenanceDate",
        },
        {
          label: this.resources["last_refurbishment_date"],
          field: "lastRefurbishmentDate",
          sortable: true,
          sortField: "lastRefurbishmentDate",
        },
        {
          label: this.resources["upper_tier_company"],
          field: "upperTierCompanies",
          sortable: true,
          sortField: "upperTierCompanies",
        },
        {
          label: this.resources["sl_depreciation"] || "S.L. Depreciation",
          field: "slDepreciation",
          sortable: true,
          sortField: "slDepreciation",
        },
        {
          label: this.resources["up_depreciation"] || "U.P. Depreciation",
          field: "upDepreciation",
          sortable: true,
          sortField: "upDepreciation",
        },
        {
          label: this.resources["machine_id"],
          field: "machineCode",
          sortable: true,
          sortField: "machine.machineCode",
        },
        // NEW FIELD 11/07/2023
        {
          label: this.resources["ct_compliance"],
          field: "ctCompliance",
          sortable: true,
          sortField: "ctCompliance",
        },
        {
          label: this.resources["status"],
          field: "relocationStatus",
          sortable: true,
          sortField: "relocationAlertStatus",
        },
        {
          label: this.resources["plant_area"],
          field: "areaName",
          sortable: true,
          sortField: "areaName",
        },
        {
          label: this.resources["remaining_no_of_parts"],
          field: "remainingPartsCount",
          sortable: true,
          sortField: "remainingPartsCount",
        },
        {
          label: this.resources["supplier_tooling_id"],
          field: "supplierMoldCode",
          sortable: true,
          sortField: "supplierMoldCode",
        },
      ];
      const toolingConfig = this.options.TOOLING;

      let defaultColumns = [];
      let addedColumns = [];
      let listColumnAvailable = [];

      if (toolingConfig.enabled) {
        // TODO(ducnm2010): Check with backend to see which fields now are available
        // get list custom columns if config is enabled
        addedColumns = toolingConfig.customFields
          // .filter((i) => i?.active) // exclude those being inactive
          .map((j) => ({
            label: j.fieldName,
            columnName: j.fieldName,
            field: j.id, // ask BE for right format
            sortable: true,
            isCustomField: true,
            sortField: "customField-" + j.id, // ask be for right format,
            // enabled: j.enabled || j.active,
          }));

        // get list columns by available fields
        // a column is invalid if being deleted, has empty fieldName or does not match with base column
        baseColumns.forEach((i) => {
          const isValid = !toolingConfig.fields.some(
            (j) =>
              i.field === j.fieldName &&
              (j.deletedField || !j.fieldName?.trim())
          );
          if (isValid) defaultColumns.push(i);
        });

        if (_.isEmpty(defaultColumns) && _.isEmpty(addedColumns)) {
          defaultColumns = baseColumns
            .filter((column) => column.default)
            .map((field) => ({ ...field, enabled: true }));
        }

        listColumnAvailable = [...defaultColumns, ...addedColumns];
      } else {
        // set list to base columns if field set is disabled
        listColumnAvailable = [...baseColumns];
      }

      // set full list availabled columns
      console.log("listColumnAvailable", listColumnAvailable);
      // handle fixed set case
      const listFixedSet = [
        DIRECT_FROM.WIDGET_TOTAL_COST_OWNER_SHIP,
        DIRECT_FROM.WIDGET_CYCLE_TIME_COMPLIANCE,
      ];
      // handle set column based on default
      const listBasedOnDefaultSet = [
        DIRECT_FROM.WIDGET_TOTAL_TOOLINGS,
        DIRECT_FROM.WIDGET_OVERALL_UTILIZATION,
        DIRECT_FROM.WIDGET_OPERATIONAL_SUMMARY,
        DIRECT_FROM.WIDGET_INACTIVE_TOOLING,
      ];

      if (listFixedSet.includes(this.from) && !_.isEmpty(this.from)) {
        listColumnAvailable.forEach((item) => {
          const currentColumnSet = LIST_COLUMN_SET[this.from];
          item.enabled = currentColumnSet.includes(item.field);
          item.position = currentColumnSet.findIndex((j) => item.field === j);
        });
        listColumnAvailable.sort((a, b) => a.position - b.position);

        this.allColumnList = listColumnAvailable;
        return;
      }

      // APPLY NEW LOGIC: FROM 03/08/2023
      if (!listBasedOnDefaultSet.includes(this.from)) {
        const listColumnPreconfig = await this.getColumnListSelected();
        if (_.isEmpty(listColumnPreconfig)) {
          // no default config case
          temp = listColumnAvailable.map((i) => ({
            ...i,
            enabled: !!i.defaultSelected,
            sortField: i.sortField || i.field,
          }));
        } else {
          // has default config case
          const listColumnPreconfigEnabled = listColumnPreconfig.filter(
            (i) => i.enabled
          );
          console.log("listColumnPreconfigEnabled", listColumnPreconfigEnabled);
          temp = listColumnAvailable
            .map((i) => {
              const findPosition = listColumnPreconfigEnabled.findIndex(
                (j) => i.field?.toString() === j.columnName || i.mandatory
              );
              return {
                ...i,
                enabled: findPosition > -1,
                position: findPosition,
                sortField: i.sortField || i.field,
              };
            })
            .sort((a, b) => a.position - b.position);
          console.log("temp>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", temp);
        }
      } else {
        temp = listColumnAvailable.map((i) => ({
          ...i,
          enabled: !!i.defaultSelected,
          sortField: i.sortField || i.field,
        }));
      }

      // special column set mixed with preconfig
      if (this.from === DIRECT_FROM.WIDGET_INACTIVE_TOOLING) {
        const findIndex = temp.findIndex(
          (item) => item.field === "inactivePeriod"
        );
        if (findIndex < 0) return;
        // find, remove and append to last
        const found = temp.splice(findIndex, 1)[0];
        temp = temp.concat({ ...found, enabled: true });
      }

      // set column index follow by
      this.allColumnList = temp.map((item, index) => ({
        ...item,
        // position: index,
      }));

      console.log("@this.allColumnList", this.allColumnList);
    },
    async getColumnListSelected() {
      try {
        const response = await axios.get(
          `/api/config/column-config?pageType=${this.pageType}`
        );
        return response?.data || [];
      } catch (e) {
        return new Error();
      }
    },
    async saveSelectedList(dataCustomize, list) {
      console.log("@savedSelectedList");
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
      await axios.post("/api/config/update-column-config", {
        pageType: this.pageType,
        columnConfig: data,
      });
    },
    showCompanyDetailsById(company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(company);
      }
    },
    showLocationHistory(mold) {
      var child = Common.vue.getChild(this.$children, "location-history");
      if (child != null) {
        child.showLocationHistory(mold);
      }
    },
    getUnitName(unit) {
      let convertUnitName = "";
      if (unit === "MM") {
        convertUnitName = "mm³";
      } else if (unit === "CM") {
        convertUnitName = "cm³";
      } else if (unit === "M") {
        convertUnitName = "m³";
      } else {
        convertUnitName = unit?.toLowerCase();
      }
      return convertUnitName;
    },
    handleCycleTimeTolerance1(result) {
      if (!result.cycleTimeLimit1) {
        return "";
      } else {
        if (result.cycleTimeLimit1Unit === "SECOND") {
          return "seconds";
        }
        if (result.cycleTimeLimit1Unit === "PERCENTAGE") {
          return "%";
        }
        return "";
      }
    },

    handleCycleTimeTolerance2(result) {
      if (!result.cycleTimeLimit2) {
        return "";
      } else {
        if (result.cycleTimeLimit2Unit === "SECOND") {
          return "seconds";
        }
        if (result.cycleTimeLimit2Unit === "PERCENTAGE") {
          return "%";
        }
        return "";
      }
    },
    showRevisionHistory(id) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "MOLD");
      }
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
    tab(tab) {
      console.log("@tab", tab);
      this.currentActiveTab = tab;
      this.total = null;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.requestParam.tabName = tab.name;
      this.currentSortType = "lastShotAt";
      this.requestParam.sort = "lastShotAt,desc";
      this.paging(1);
    },
    search(page) {
      this.paging(1);
    },

    changeEquipmentStatus(data, status) {
      var param = {
        id: data.id,
        equipmentStatus: status,
      };
      const self = this;
      axios
        .put(Page.API_BASE + "/" + param.id + "/equipment-status", param)
        .then(
          (response) => {
            self.paging(1);
          },
          (error) => {
            console.log(error);
          }
        );
    },
    showChart: function (mold, type = null, dateViewType = null, tab, oct) {
      if (!oct) oct = this.octs[this.optimalCycleTime.strategy];
      if (!type) type = "QUANTITY";
      if (!dateViewType) dateViewType = "DAY";
      const child = this.$refs["chart-mold"];
      if (child != null) {
        child.showChart(
          mold,
          type,
          dateViewType,
          tab ? tab : "switch-graph",
          oct
        );
      } else {
        setTimeout(() => {
          this.showChart(
            mold,
            type,
            dateViewType,
            tab ? tab : "switch-graph",
            oct
          );
        }, 200);
        console.log(
          "mold, type, dateViewType, tab, ,oct",
          mold,
          type,
          dateViewType,
          tab,
          oct
        );
      }
    },
    showPartChart(part, mold) {
      part.id = part.partId;
      var child = Common.vue.getChild(this.$children, "chart-part");
      if (child != null) {
        child.showChartPartMold(part, mold, "QUANTITY", "DAY");
      }
    },
    showProjectDetail(result) {
      const child = Common.vue.getChild(
        this.$children,
        "category-library-modal"
      );
      const firstStep = "project-profile";
      if (child != null) {
        const date = { ...this.currentDate };
        child.showModal(result, date, firstStep);
      }
    },
    async downloadTooling(type) {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_tooling_data`;

      document.getElementById("toggleExportProgressBar").click();
      this.progressBarRun();
      const tabName = this.currentActiveTab.name;

      const { filterCode, query } = this.requestParam;

      let listId = [];
      if (this.selectType === "all") {
        listId = this.results
          .map((i) => i.id)
          .filter((x) => !this.listChecked.includes(x));
      } else {
        listId = this.listChecked;
      }
      const param = Common.param({
        filterCode,
        query,
        tabName,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
      });
      try {
        const { data: fileData } = await axios.get(
          `${API_BASE_TOL_STP}/export?${param}`,
          { responseType: "blob", observe: "response" }
        );
        if (!this.isStopedExport) {
          this.isCallingDone = true;
          setTimeout(() => {
            const blob = new Blob([fileData], {
              type: "text/plain;charset=utf-8",
            });
            saveAs(blob, `${fileName}.xlsx`);
          }, 2000);
        }
      } catch (error) {
        console.log("data-family-tooling:::downloadTooling:::error:::", error);
        this.isStopedExport = true;
      }
    },
    async paging(pageNumber) {
      this.requestParam.page = pageNumber ?? 1;

      if (this.cancelToken) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }
      this.cancelToken = axios.CancelToken.source();

      const url = new URL(API_BASE_TOL_STP, location.origin);
      const params = Common.param(this.requestParam);
      url.search = params;

      try {
        const { data: toolingData } = await axios.get(url, {
          cancelToken: this.cancelToken.token,
        });
        this.total = toolingData.totalElements;
        this.results = toolingData.content;
        this.pagination = Common.getPagingData(toolingData);

        // Reset select all when change page
        if (this.selectType === "all") {
          this.listAllIds = [];
          this.listChecked = [];
          this.listCheckedFull = [];
          this.selectType = "";
        }

        // Get tab count
        // await this.getTabsByCurrentUser();
        this.listSelectedTabs = this.listSelectedTabs.map((tab) => {
          const total =
            toolingData.tabs.find(
              (toolingTab) => toolingTab.tabName === tab.name
            )?.totalElements || 0;
          return {
            ...tab,
            total,
          };
        });

        // Results데이터가 숫자로 넘어오는 경우 object로 데이터 변환
        this.setResultObject();

        // 카테고리 정보 저장.
        this.setCategories();

        Common.handleNoResults("#app", this.results.length);

        // open chart when from dashboard
        if (this.from === "DASHBOARD") {
          this.showChart(
            this.results[0],
            "CYCLE_TIME",
            this.dateViewType,
            null,
            this.octs[this.optimalCycleTime.strategy]
          );
        }
        if (this.results.length > 0) {
          Common.triggerShowNewFeatureNotify(
            this.$children,
            "action-bar-feature",
            null,
            { key: "feature", value: "DATA_TABLE_TAB" }
          );
        } else {
          Common.triggerShowNewFeatureNotify(
            this.$children,
            "action-bar-feature",
            null,
            { key: "feature", value: "TOOLING_TAB" }
          );
        }
        if (this.total === this.listAllIds.length) {
          // refresh checklist in case select all items
          this.listChecked = [];
          this.listCheckedFull = [];
        }
      } catch (err) {
        console.log("data-family-tooling:::paging", err);
      }
    },
    setResultObject() {
      // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
      for (var i = 0; i < this.results.length; i++) {
        if (typeof this.results[i] !== "object") {
          var moldId = this.results[i];
          this.results[i] = this.findMold(this.results, moldId);
        }
      }
    },
    findMold(results, moldId) {
      for (var j = 0; j < results.length; j++) {
        if (typeof results[j] !== "object") {
          continue;
        }

        var mold = results[j];
        if (moldId == mold.id) {
          return mold;
        }

        // parts에서 검색
        if (mold.part != null && mold.part.molds != null) {
          var findMold = this.findMold(mold.part.molds, moldId);
          if (typeof findMold === "object") {
            return findMold;
          }
        }
      }
    },
    setCategories() {
      // Mold 가 id로만 넘어오는 경우 mold(Object)를 찾아서 SET.
      for (var i = 0; i < this.results.length; i++) {
        if (
          this.results[i].part != null &&
          this.results[i].part.category != null
        ) {
          if (
            typeof this.results[i].part.category !== "object" &&
            this.results[i].part.categoryId > 0
          ) {
            var categoryId = this.results[i].part.categoryId;
            this.results[i].part.category = this.findCategory(
              this.results,
              categoryId
            );
          }
        }
      }

      for (var i = 0; i < this.results.length; i++) {
        if (
          this.results[i].part != null &&
          this.results[i].part.category != null
        ) {
          if (typeof this.results[i].part.category.parent !== "object") {
            var categoryId = this.results[i].part.category.parent;
            this.results[i].part.category.parent = this.findCategory(
              this.results,
              categoryId
            );
          }
        }
      }
    },
    findCategory(results, categoryId) {
      for (var i = 0; i < results.length; i++) {
        var mold = results[i];
        if (
          mold.part == null ||
          mold.part.category == null ||
          typeof mold.part.category !== "object"
        ) {
          continue;
        }

        var category = mold.part.category;
        if (categoryId == category.id) {
          return category;
        }

        if (category.parent != null && categoryId == category.parent.id) {
          return category.parent;
        }

        // 부모의 자삭에서 찾기
        if (category.parent != null && category.parent.children != null) {
          for (var j = 0; j < category.parent.children.length; j++) {
            var parentChild = category.parent.children[j];
            if (typeof parentChild !== "object") {
              continue;
            }
            if (categoryId == parentChild.id) {
              return parentChild;
            }
          }
        }

        // parts에서 검색
        if (mold.part.molds != null) {
          var findCategory = this.findCategory(mold.part.molds, categoryId);
          if (typeof findCategory === "object") {
            return findCategory;
          }
        }
      }
    },
    showMoldDetails(mold) {
      var child = Common.vue.getChild(this.$children, "mold-details");
      console.log("child", child);
      if (child != null) {
        let reasonData = { reason: "", approvedAt: "" };
        const tab = "switch-detail";
        if (
          mold.dataSubmission === "DISAPPROVED" ||
          mold.dataSubmission === "APPROVED"
        ) {
          let urlDataSubmission = `/api/molds/data-submission/${mold.id}/${
            mold.dataSubmission === "APPROVED"
              ? "approval-reason"
              : "disapproval-reason"
          }`;
          axios.get(urlDataSubmission).then((response) => {
            reasonData = response.data;
            // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
            reasonData.approvedAt = reasonData.approvedAt
              ? this.convertTimestampToDateWithFormat(
                  reasonData.approvedAt,
                  "MMMM DD YYYY HH:mm:ss"
                )
              : "-";
            mold.notificationStatus = mold.dataSubmission;
            mold.reason = reasonData.reason;
            mold.approvedAt = reasonData.approvedAt;
            mold.approvedBy = reasonData.approvedBy;
            // child.showMoldDetails(mold);
            this.showChart(mold, tab);
          });
        } else {
          // child.showMoldDetails(mold);
          this.showChart(mold, tab);
        }
      }
    },

    // 'failure' 선택 시 고장시간 등록 Modal
    showCorrective(mold) {
      var child = Common.vue.getChild(this.$children, "corrective-form");
      if (child != null) {
        child.showModal(mold);
      }
    },
    callbackCorrectiveForm(mold) {
      this.paging(1);
    },
    sortBy(type) {
      if (type === "lastShot") {
        this.lastShotDropdown = true;
      } else {
        this.lastShotData.sort = "";
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
      }
    },
    sort() {
      this.paging(1);
    },
    showFilePreviewer(file, fromPopup) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file, fromPopup);
      }
    },
    backToMoldDetail(fromPopup) {
      console.log("back to molds detail");
      // var child = Common.vue.getChild(this.$children, 'mold-details');
      var child = Common.vue.getChild(
        this.$children,
        fromPopup || "chart-mold"
      );
      if (child != null) {
        child.backFromPreview();
      }
    },
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },

    initCurrentDate() {
      if (!this.currentDate) return;
      let today = new Date();
      this.currentDate.from = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay()
      );
      this.currentDate.to = new Date(
        today.getFullYear(),
        today.getMonth(),
        today.getDate() - today.getDay() + 6
      );
    },

    openEditProductDemand(dataFromChild) {
      this.editFromChild = !!dataFromChild;
      const currentData = dataFromChild
        ? dataFromChild
        : this.listCheckedFull[0];
      var child = Common.vue.getChild(this.$children, "edit-product-demand");
      if (child != null) {
        child.showModal(currentData);
      }
    },

    loadSuccess() {
      this.reloadPage();
      if (this.editFromChild) {
        this.triggerSaveModal++;
      }
    },
    reloadPage() {
      this.paging(1);
      this.uncheckAll();
    },
    uncheckAll() {
      this.listChecked = [];
      this.listCheckedFull = [];
    },
    openManageTabView() {
      const child = this.$refs["manage-tab-view"];
      if (child) {
        child.show();
        this.visibleMoreAction = false;
      }
    },
    closeMultiChart() {
      this.listChecked = [];
      this.listCheckedFull = [];
      this.showMultiChart = false;
    },
    handleSelectAll(actionType) {
      this.selectType = actionType;
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} toolings on this current page are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
      }
      if (actionType === "all") {
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.total} toolings are selected.`,
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
    captureQuery() {
      const [hash, stringParams] = location.hash.slice("#").split("?");
      const params = Common.parseParams(stringParams);

      this.requestParam = Object.assign({}, this.requestParam, {
        ...DEFAULT_PARAMS,
      });

      // TODO: WHY SET WHEN RIGHT-HAND KEY HASN'T BEEN SET
      this.defaultStatusListLength =
        this.requestParam.toolingStatus.length +
        this.requestParam.counterStatus.length;

      const isToolingBarEnabled = Boolean(
        this.options?.CLIENT?.moldList?.features?.toolingBarEnabled
      );

      this.featureList = !isToolingBarEnabled
        ? ["ACTION_BAR", "DATA_TABLE_TAB"]
        : ["TOOLING_TAB", "ACTION_BAR", "DATA_TABLE_TAB"];

      // TODO: RECHECK LOGIC VALIDITY AND MADE IT MORE UNDERSTANDABLE
      const op = params.op;
      this.dateViewType = params.dateViewType;
      var uri_map = [];

      var uri = URI.parse(location.href);

      if (uri.fragment && uri.fragment.includes("&")) {
        uri_map = uri.fragment
          .replace("%20", " ")
          .replace("%20", " ")
          .replace("%20", " ")
          .replace("%20", " ")
          .split("&");
      }

      console.log("uri & uri_map", { uri, uri_map });

      if (uri.fragment == "active" || op == "active") {
        this.requestParam.operatingStatus = "WORKING";
      } else if (uri.fragment == "idle" || op == "idle") {
        this.requestParam.operatingStatus = "IDLE";
      } else if (uri.fragment == "inactive" || op == "inactive") {
        this.requestParam.operatingStatus = "NOT_WORKING";
      } else if (uri.fragment == "disconnected" || op == "disconnected") {
        this.requestParam.operatingStatus = "DISCONNECTED";
      } else if (uri_map[0] == "active") {
        this.requestParam.query = uri_map[1];
        this.requestParam.operatingStatus = "WORKING";
      } else if (uri_map[0] == "idle") {
        this.requestParam.query = uri_map[1];
        this.requestParam.operatingStatus = "IDLE";
      } else if (uri_map[0] == "disconnected") {
        this.requestParam.query = uri_map[1];
        this.requestParam.operatingStatus = "DISCONNECTED";
      } else if (uri_map[0] == "inactive") {
        this.requestParam.query = uri_map[1];
        this.requestParam.operatingStatus = "NOT_WORKING";
      }
      if (params?.dashboardChartType) {
        this.dashboardChartType = params.dashboardChartType;
      }

      if (params?.cost) {
        this.cost = params.cost;
      }

      if (params?.partCode) {
        this.requestParam.partCode = params.partCode;
      }
      if (params?.partId) {
        this.requestParam.partId = params.partId;
      }
      if (params?.companyId) {
        this.requestParam.companyId = params.companyId;
      }
      if (params?.moldCode) {
        this.requestParam.query = params.moldCode;
      }
      if (params?.equipmentStatus) {
        this.requestParam.equipmentStatus = params.equipmentStatus;
      }
      if (params?.inactiveLevel) {
        this.requestParam.inactiveLevel = params.inactiveLevel;
      }
      if (params?.ids) {
        this.requestParam.id = params.ids;
      }
      if (params?.utilizationStatus) {
        this.requestParam.utilizationStatus = params.utilizationStatus;
      }
      if (params?.moldStatusList) {
        this.requestParam.toolingStatus = params.moldStatusList.split(",");
      }
      // from which page
      if (params?.from) {
        this.from = params.from;
      }

      if (params?.popup) {
        this.popup(params?.popup);
      }

      if (params?.tab) {
        this.tab({ name: params.tab });
        this.getTabsByCurrentUser();
        return;
      } else {
        this.getTabsByCurrentUser(true);
      }
      console.log("requestParam", this.requestParam);
    },
    async exportLifeCycle() {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_tooling_data`;

      document.getElementById("toggleExportProgressBar").click();
      this.progressBarRun();

      const { filterCode, query } = this.requestParam;
      let listId = [];
      if (this.selectType === "all") {
        listId = this.results
          .map((i) => i.id)
          .filter((x) => !this.listChecked.includes(x));
      } else {
        listId = this.listChecked;
      }
      const param = Common.param({
        filterCode,
        query,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
      });
      try {
        const { data } = await axios.get(
          `/api/analysis/tol-rpt/export?${param}`,
          { responseType: "blob", observe: "response" }
        );
        if (!this.isStopedExport) {
          this.isCallingDone = true;
          setTimeout(() => {
            var blob = new Blob([data], {
              type: "text/plain;charset=utf-8",
            });
            saveAs(blob, `${fileName}.xlsx`);
          }, 2000);
        }
      } catch (error) {
        console.log("data-family-tooling:::exportLifeCycle:::error", error);
        this.isStopedExport = true;
      } finally {
        this.listChecked = [];
        this.listCheckedFull = [];
        this.showExport = false;
      }
    },
    async handleSubmitNote(data) {
      console.log("handleSubmitNote:::data", data);
      const { filterCode, query, tabName, counterStatus, toolingStatus } =
        this.requestParam;
      let listId = [];
      if (this.selectType === "all") {
        listId = this.results
          .map((i) => i.id)
          .filter((x) => !this.listChecked.includes(x));
      } else {
        listId = this.listChecked;
      }
      const param = Common.param({
        filterCode,
        query,
        tabName,
        counterStatus,
        toolingStatus,
        selectionMode: this.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [this.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
      });
      try {
        await axios.post(`${API_BASE_TOL_STP}/note-batch?${param}`, data);
      } catch (error) {
        console.log(error);
      }
    },
    customFieldValueTooling(result, fieldName) {
      if (result && fieldName) {
        const customField = result.customFields.find(
          (item) => item.fieldName === fieldName
        );
        if (customField) {
          return customField.value;
        }
      }
      return null;
    },
  },
  computed: {
    checkedDisposedItem() {
      return this.listCheckedFull.some(
        (item) => item.moldStatus === "DISPOSED"
      );
    },
    disabledUpperTierCompanies() {
      return this.userType !== "OEM";
    },
    displayColumns() {
      return this.allColumnList.filter((item) => item.enabled);
    },
    enableBatchDisableAction() {
      return (
        this.listChecked.length >= 1 && this.requestParam.tabName != "DISABLED"
      );
    },
    enableBatchEnableAction() {
      return (
        this.listChecked.length >= 1 && this.requestParam.tabName === "DISABLED"
      );
    },
    enableShowMultiChart() {
      return this.listChecked.length >= 1 && this.listChecked.length <= 4;
    },
    isDigitalEnabled() {
      return Boolean(this.options?.CLIENT?.moldList?.tabs?.digital?.enabled);
    },
    isNonDigitalEnabled() {
      return Boolean(this.options?.CLIENT?.moldList?.tabs?.nonDigital?.enabled);
    },
    isDigitablActive() {
      return Boolean(this.options?.CLIENT?.moldList?.tabs?.digital?.active);
    },
    isNonDigitalActive() {
      return Boolean(this.options?.CLIENT?.moldList?.tabs?.nonDigital?.active);
    },
    isDataSubmissionEnabled() {
      return Boolean(
        this.options?.CLIENT?.moldList?.cols?.dataSubmission?.show
      );
    },
    isLastShotCheckpoint() {
      return Boolean(
        this.options?.CLIENT?.moldList?.cols?.lastShotCheckpoint?.show
      );
    },
    isUtilNextPmDefaultShow() {
      return Boolean(
        this.options?.CLIENT?.moldMaintenance?.cols?.utilNextPm?.show
      );
    },
    isFilterApplied() {
      return this.isFilterChanging;
    },
    isOldGenIncluded() {
      const oldGen = ["CMS", "NCM"];
      const hasOldGenCounter = this.listCheckedFull.some((o) => {
        return (
          !o?.counterCode || oldGen.some((i) => o?.counterCode?.startsWith(i))
        );
      });
      return hasOldGenCounter;
    },
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
  async created() {
    this.$watch(
      () => headerVm?.userType,
      (newVal) => {
        if (!newVal) return;
        this.userType = newVal;
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (!newVal) return;
        this.codes = Object.assign({}, this.codes, newVal);
      },
      { immediate: true }
    );

    this.$watch(
      () => [headerVm?.options, headerVm?.resourcesFake],
      ([newValOptions, newValResources]) => {
        const isOptionsExisted =
          newValOptions && Object.keys(newValOptions)?.length;
        const isResourcesExisted =
          newValResources && Object.keys(newValResources)?.length;

        // make sure options and localization existed
        if (isOptionsExisted && isResourcesExisted) {
          this.options = Object.assign({}, this.options, newValOptions);
          this.resources = Object.assign({}, this.resources, newValResources);
          this.optimalCycleTime = this.options.OPTIMAL_CYCLE_TIME;
          this.$nextTick(() => {
            // wait for vue to capture the params
            this.setAllColumnList();
          });
        }

        // set rule config
        const toolingConfigRules =
          this.options?.OP &&
          this.options?.OP?.enabled &&
          this.options?.OP?.rules?.length;
        if (toolingConfigRules) {
          const status = {};
          this.options.OP.rules.forEach((item) => {
            if (item.equipmentType === "MOLD") {
              status[item.operatingStatus] = item;
            }
          });
          this.ruleOpStatus.inProduct = `There has been a shot made within the last ${
            status.WORKING.time
          } ${this.getTimeUnit(status.WORKING.timeUnit)}`;

          this.ruleOpStatus.idle = `There has been a shot made after ${
            status.WORKING.time
          } ${this.getTimeUnit(status.WORKING.timeUnit)} but within ${
            status.IDLE.time
          } ${this.getTimeUnit(status.IDLE.timeUnit)}`;

          this.ruleOpStatus.inactive = `There is no shot made within ${
            status.NOT_WORKING.time
          } ${this.getTimeUnit(status.NOT_WORKING.timeUnit)}`;

          this.ruleOpStatus.sensorOffline = `There is no signal from the sensor within ${
            status.DISCONNECTED.time
          } ${this.getTimeUnit(status.DISCONNECTED.timeUnit)}`;
        }
      },
      { immediate: true }
    );

    window.addEventListener("popstate", this.captureQuery);
    this.captureQuery();
  },
  mounted() {
    this.animationOutline();
    this.animationPrimary();
    this.initCurrentDate();
  },
  destroyed() {
    window.removeEventListener("popstate", this.captureQuery);
  },
};
</script>

<style scoped>
.matching-modal {
  position: absolute;
  background: rgba(0, 0, 0, 0.1);
  top: 0;
  width: 100%;
  height: 100%;
  padding-top: 50px;
  z-index: 5;
  right: 0px;
}

.matching-modal p {
  margin-top: 0;
  margin-bottom: 1em;
}

.matching-modal .warning-title {
  display: flex;
  margin-bottom: 11px;
}

.matching-modal .warning-title p {
  margin-left: 7px;
  margin-bottom: 0px;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  color: #4b4b4b;
}

.matching-modal .modal-wrapper {
  width: 325px;
  margin: 33vh auto;
  background: #fff;
  border-radius: 5px;
}

.matching-modal .modal-container .modal-header {
  padding: 5px 15px 10px;
}

.duplicated-title p {
  font-weight: 400;
  font-size: 14px;
  line-height: 17px;
  color: #4b4b4b;
}

.btn-delete {
  background-color: #e34537;
  color: #ffffff;
  border-radius: 3px;
  line-height: 17px;
}

.btn-delete :hover,
.btn-delete :active {
  background-color: #c92617;
}

.btn-back {
  background-color: #ffffff;
  border: 1px solid #d6dade;
  color: #595959;
  padding: 6px 8px;
  line-height: 17px;
}

.btn-back:focus {
  box-shadow: unset;
}

.btn-back:active {
  background-color: #f4f4f4;
  border: 1px solid #d6dade !important;
  box-shadow: 0 0 0 1px #d6dade;
}

.btn-back:hover {
  background-color: #f4f4f4;
  border: 1px solid #595959;
}

.btn {
  border-radius: 3px;
}

.duplicated-action {
  text-align: end;
}

.matching-modal .modal-container .modal-header button {
  color: #777;
  cursor: pointer;
}

.matching-modal .importing-modal .modal-container .modal-body {
  padding: 15px 5px;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content {
  text-align: center;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-title {
  color: #463da5;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-progress {
  padding: 15px 15px;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-progress
  .progress-bar-wrapper {
  border: 1px solid #463da5;
  height: 23px;
  border-radius: 15px;
  padding: 2px;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-progress
  .progress-bar-wrapper
  .progress-bar {
  background: #263162;
  height: 100%;
  border-radius: 15px;
  position: relative;
}

.matching-modal
  .importing-modal
  .modal-container
  .modal-body
  .modal-body-content
  .loading-progress
  .progress-bar-wrapper
  .progress-bar
  .direction-icon {
  position: absolute;
  height: 100%;
  width: 20px;
  border-radius: 15px;
  background: #615ea8;
  right: 0;
}

td.text-right {
  padding-right: 20px;
}
</style>
