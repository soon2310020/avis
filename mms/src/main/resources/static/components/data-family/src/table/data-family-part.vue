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
                style="margin-bottom: -1px; width: calc(100% - 510px)"
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
              <div class="data-table-top-action" style="width: 510px">
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
                    <week-picker-popup
                      :key="weekKey"
                      @on-close="handleChangeDate"
                      :current-date="currentDate"
                      :date-range="timeRange"
                      :button-type="'secondary'"
                    ></week-picker-popup>
                  </div>
                </div>
                <div
                  class="assign-more d-flex align-items-center position-relative"
                >
                  <div>
                    <div class="assign-more">
                      <cta-button
                        :active="showPartPicker"
                        color-type="blue-fill"
                        button-type="dropdown"
                        :click-handler="handleToggle"
                      >
                        <span>{{ resources["create_part"] }}</span>
                      </cta-button>
                      <common-select-popover
                        :is-visible="showPartPicker"
                        @close="handleClosePartPicker"
                      >
                        <common-select-dropdown
                          :style="{ top: 0, left: 0 }"
                          :class="{ show: showPartPicker }"
                          :searchbox="false"
                          :checkbox="false"
                          :items="listOfItems"
                          :click-handler="onItemSelect"
                          id="createPart"
                        ></common-select-dropdown>
                      </common-select-popover>
                    </div>
                  </div>
                </div>
                <div
                  class="assign-more d-flex align-items-center position-relative"
                >
                  <div>
                    <div class="assign-more">
                      <a
                        @click.stop="handleToggleFilter"
                        href="javascript:void(0)"
                        class="btn-custom customize-btn btn-outline-custom-primary position-relative"
                        :class="{ cust_focus: showFilter }"
                      >
                        <img
                          style="margin-bottom: 3px"
                          src="/images/icon/Icon_feather-filter.svg"
                        />
                        <span v-show="isFilterChanging" class="pink-dot"></span>
                      </a>
                      <common-select-popover
                        :is-visible="showFilter"
                        @close="handleCloseFilter"
                        class="filter-custom"
                        :class="{ 'filter-custom-dropdown': showFilter }"
                      >
                        <common-select-dropdown
                          v-show="dropDownType == 'main'"
                          class="filter-custom-dropdown"
                          :style="{
                            left: 'unset',
                            right: '-100px',
                            top: '3px',
                          }"
                          :class="{ show: showFilter }"
                          :searchbox="false"
                          :checkbox="false"
                          :items="listOfItemsForFilter"
                          :click-handler="openExtendedDropdown"
                          id="filter"
                        ></common-select-dropdown>
                        <common-select-dropdown
                          v-show="dropDownType == 'By Category'"
                          class="filter-custom-dropdown"
                          :style="{
                            left: 'unset',
                            right: '-100px',
                            top: '3px',
                            width: 'fit-content !important',
                          }"
                          :class="{ show: showFilter }"
                          :searchbox="false"
                          :checkbox="false"
                          :items="listOfItemsForCategory"
                          :click-handler="handlerForFilter"
                          id="categoryDropDown"
                        ></common-select-dropdown>
                        <common-select-dropdown
                          v-show="dropDownType == 'By Product'"
                          class="filter-custom-dropdown"
                          :style="{
                            left: 'unset',
                            right: '-100px',
                            top: '3px',
                            width: 'fit-content !important',
                          }"
                          :class="{ show: showFilter }"
                          :searchbox="false"
                          :chekbox="false"
                          :items="listOfItemsForProduct"
                          :click-handler="handlerForFilter"
                          id="productdropdown"
                        ></common-select-dropdown>
                      </common-select-popover>
                    </div>
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
                        :unit="['part', 'parts']"
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
                      <span>{{ item.label }}</span>
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
                            :unit="['part', 'parts']"
                            @select-all="handleSelectAll('all')"
                            @select-page="handleSelectAll('page')"
                            @deselect="handleSelectAll('deselect')"
                          ></select-all>
                        </div>
                      </div>
                      <div class="action-bar">
                        <!-- <div v-if="listChecked.length > 1 && requestParam.status == 'DELETED'">
                          No batch action is available
                        </div> -->
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
                          @click="showChart(listCheckedFull[0], 'switch-notes')"
                        >
                          <span>Add Note</span>
                          <i class="icon-action memo-icon"></i>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            requestParam.status !== 'disabled'
                          "
                          class="action-item"
                          @click.prevent="disable(listCheckedFull)"
                        >
                          <span>Disable</span>
                          <div class="icon-action disable-icon"></div>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            requestParam.status === 'disabled'
                          "
                          class="action-item"
                          @click.prevent="enable(listCheckedFull)"
                        >
                          <span>Enable</span>
                          <div class="icon-action enable-icon"></div>
                        </div>
                        <div
                          v-if="listChecked.length >= 1"
                          @click="exportPart()"
                          class="action-item"
                        >
                          <span>Export</span>
                          <i class="icon-action export-icon"></i>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            !currentActiveTab.isDefaultTab
                          "
                          class="action-item"
                        >
                          <move-to-action
                            :type-view="'part'"
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
              <tbody v-show="!isLoading" class="op-list" style="display: none">
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
                    v-for="(column, columnIndex) in allColumnList"
                    v-if="column.enabled && !column.hiddenInToggle"
                  >
                    <td
                      :key="index + 'partCode'"
                      v-if="column.field === 'partCode'"
                      class="text-left"
                    >
                      <a
                        href="#"
                        @click.prevent="showChart(result)"
                        style="color: #1aaa55"
                        class="mr-1"
                      >
                        <i class="fa fa-bar-chart"></i
                      ></a>
                      <a href="#" @click.prevent="showChart(result)">
                        {{ result.partCode }}</a
                      >
                      <div class="small text-muted font-size-11-2">
                        Registered:
                        {{ formatToDate(result.createdAt) }}
                      </div>
                    </td>
                    <td
                      :key="index + 'name'"
                      class="text-left admin-part"
                      v-else-if="column.field === 'name'"
                      v-bind:data-tooltip-text="result.name"
                    >
                      {{ replaceLongtext(result.name, 40) }}
                    </td>
                    <td
                      :key="index + 'category'"
                      class="text-left"
                      v-else-if="column.field === 'category'"
                    >
                      <span class="font-size-14">{{
                        result.categoryName
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.projectName
                        }}</span>
                      </div>
                    </td>
                    <td
                      :key="index + 'enabled'"
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                      v-else-if="column.field === 'enabled'"
                    >
                      <span v-if="result.enabled" class="label label-success">
                        {{ resources["enabled"] }}
                      </span>
                      <span v-if="!result.enabled" class="label label-danger">
                        {{ resources["disabled"] }}
                      </span>
                    </td>
                    <td
                      :key="index + 'totalMolds'"
                      class="text-left"
                      v-else-if="column.field === 'totalMolds'"
                    >
                      <a
                        href="javascript:;"
                        @click="directLink('TOOLING', { partId: result.id })"
                      >
                        {{ formatNumber(result.totalMolds) }}
                      </a>
                    </td>
                    <td
                      :key="index + 'activeMolds'"
                      class="text-left"
                      v-else-if="column.field === 'activeMolds'"
                    >
                      <a
                        href="javascript:;"
                        @click="
                          directLink('TOOLING', {
                            partId: result.id,
                            op: 'active',
                          })
                        "
                      >
                        {{ formatNumber(result.activeMolds) }}
                      </a>
                    </td>
                    <td
                      :key="index + 'idleMolds'"
                      class="text-left"
                      v-else-if="column.field === 'idleMolds'"
                    >
                      <a
                        href="javascript:;"
                        @click="
                          directLink('TOOLING', {
                            partId: result.id,
                            op: 'idle',
                          })
                        "
                      >
                        {{ formatNumber(result.idleMolds) }}
                      </a>
                    </td>
                    <td
                      :key="index + 'inactiveMolds'"
                      class="text-left"
                      v-else-if="column.field === 'inactiveMolds'"
                    >
                      <a
                        href="javascript:;"
                        @click="
                          directLink('TOOLING', {
                            partId: result.id,
                            op: 'inactive',
                          })
                        "
                      >
                        {{ formatNumber(result.inactiveMolds) }}
                      </a>
                    </td>
                    <td
                      :key="index + 'disconnectedMolds'"
                      class="text-left"
                      v-else-if="column.field === 'disconnectedMolds'"
                    >
                      <a
                        href="javascript:;"
                        @click="
                          directLink('TOOLING', {
                            partId: result.id,
                            op: 'disconnected',
                          })
                        "
                      >
                        {{ formatNumber(result.disconnectedMolds) }}
                      </a>
                    </td>
                    <td
                      :key="index + 'totalProduced'"
                      class="text-left"
                      v-else-if="column.field === 'totalProduced'"
                    >
                      {{ formatNumber(result.totalProduced) }}
                      <template v-if="result.statisticsPartData">
                        <template
                          v-if="result.statisticsPartData.isUpper === true"
                        >
                          <a-icon
                            type="caret-up"
                            style="color: #4cbd73; transform: translateY(-4px)"
                          />
                        </template>
                        <template
                          v-else-if="
                            result.statisticsPartData.isUpper === false
                          "
                        >
                          <a-icon
                            type="caret-down"
                            style="color: #db2526; transform: translateY(-4px)"
                          />
                        </template>
                      </template>
                    </td>
                    <td
                      :key="index + 'partSize'"
                      class="text-left"
                      v-else-if="column.field === 'partSize'"
                    >
                      <span class="font-size-14">{{
                        result.partSizeNoUnit
                      }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.partSize ? getUnitName(result.sizeUnit) : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      :key="index + 'partWeight'"
                      class="text-left"
                      v-else-if="column.field === 'partWeight'"
                    >
                      <span class="font-size-14">{{ result.weight }}</span>
                      <div>
                        <span class="font-size-11-2">{{
                          result.weight ? getUnitName(result.weightUnit) : ""
                        }}</span>
                      </div>
                    </td>
                    <td
                      :key="index + 'machineCode'"
                      class="text-left"
                      v-else-if="column.field === 'machineCode'"
                    >
                      <template v-if="result.machineList.length > 0">
                        <base-drop-count
                          :is-hyper-link="true"
                          :data-list="result.machineList"
                          :title="
                            result.machineList[0]
                              ? result.machineList[0].name
                              : ''
                          "
                          style="margin: 0 auto 0 0; padding: 0"
                          @title-click="
                            showMachineDetails(result.machineList[0].id)
                          "
                        >
                          <common-select-dropdown
                            :style="{
                              position: 'static',
                              maxWidth: 'fit-content',
                              margin: '-4px',
                            }"
                            :class="{ show: true }"
                            :searchbox="false"
                            :checkbox="false"
                            :items="
                              result.machineList.map((o) => ({
                                ...o,
                                title: o.name,
                              }))
                            "
                            :click-handler="
                              (item) => showMachineDetails(item.id)
                            "
                          ></common-select-dropdown>
                        </base-drop-count>
                      </template>
                    </td>

                    <td
                      :key="index + 'customField'"
                      class="text-left"
                      v-else-if="column.isCustomField"
                    >
                      {{ customFieldValue(result, column.field) }}
                    </td>
                    <td
                      class="text-left"
                      v-else
                      :key="index + 'notCustomField'"
                    >
                      {{ result[column.field] }}
                    </td>
                  </template>
                </tr>
              </tbody>
            </table>

            <div class="no-results d-none" style="margin-top: -1rem">
              {{ resources["no_results"] }}
            </div>

            <div v-show="!isLoading" class="row">
              <div class="col-md-8">
                <ul class="pagination">
                  <li
                    v-for="(data, index) in pagination"
                    :key="index"
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
                      <p style="color: #615ea9">
                        {{ resources["loading"] }}...
                      </p>
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

            <create-property-drawer
              :resources="resources"
              :visible="visible"
              @open="showDrawer()"
              @close="closeDrawer()"
              is-type="PART"
            ></create-property-drawer>
          </div>
        </div>
      </div>
    </div>
    <revision-history :resources="resources"></revision-history>
    <delete-popup
      :confirm-delete="confirmDelete"
      :is-show-warning-again="isShowWarningAgain"
      :list="listChecked"
      :resources="resources"
      :name="'Part'"
    ></delete-popup>
    <action-bar-feature feature="ACTION_BAR"></action-bar-feature>
    <machine-details :resources="resources"></machine-details>
    <manage-tab-view
      :resources="resources"
      :object-type="'PART'"
      @open-edit="openEdit"
      @open-duplicate="openDuplicate"
      @move-success="getTabsByCurrentUser"
      :trigger-update="triggerUpdateManageTab"
    ></manage-tab-view>
    <tab-view-modal
      :resources="resources"
      :object-type="'PART'"
      :all-tooling-list="allToolingList"
      :page-type="pageType"
      @edit-success="editSuccess"
      @create-success="createSuccess"
      @show-chart="showChart"
    ></tab-view-modal>
    <toast-alert
      @close-toast="closeToastAlert"
      :show="toastAlert.value"
      :title="toastAlert.title"
      :content="toastAlert.content"
      ref-custom="toolingToast"
    ></toast-alert>
    <chart-part
      ref="chartPart"
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
    ></chart-part>
    <part-details
      :resources="resources"
      :config-delete-field="configDeleteField"
    ></part-details>
    <file-previewer :back="backToMoldDetail"></file-previewer>
  </div>
</template>

<script>
const Page = Common.getPage("parts");
module.exports = {
  components: {
    "chart-part": httpVueLoader("/components/chart-part.vue"),
    "part-details": httpVueLoader("/components/part-details.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "part-filter": httpVueLoader("/components/part/part-filter.vue"),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "delete-popup": httpVueLoader("/components/delete-popup.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "machine-details": httpVueLoader("/components/machine-details.vue"),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-part-modal.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action.vue"),
    "week-picker-popup": httpVueLoader(
      "/components/category/week-picker-popup.vue"
    ),
  },
  props: {
    resources: Object,
    searchText: String,
    reloadKey: [String, Number],
  },
  data() {
    return {
      showFilter: false,
      dropDownType: "main",
      listOfItemsForCategory: [],
      listOfItemsForProduct: [],
      listOfItemsForFilter: [{ title: "By Category" }, { title: "By Product" }],
      isShowWarningAgain: !localStorage.getItem("dontShowWarning"),
      deletePopup: false,
      showDrowdown: null,
      listOfItems: [
        { title: "Create Part" },
        { title: "Create Part Property" },
      ],
      showPartPicker: false,
      isLoading: false,
      optimalCycleTime: {},
      octs: {
        ACT: "Approved CT",
        WACT: "Weighted Average CT",
      },
      results: [],
      total: 0,
      dropdownData: null,
      pagination: [],
      requestParam: {
        accessType: "ADMIN_MENU",
        categoryId: "",
        query: "",
        status: "active",
        sort: "id,desc",
        page: 1,
        id: "",
        pageType: "PART_SETTING",
        tabId: "",
      },
      firstCompare: "eq",
      secondCompare: "and",
      thirdCompare: "eq",
      firstCaviti: "",
      secondCaviti: "",
      counterId: "",
      toolingId: "",
      categories: [],
      onAddPart: {
        type: Function,
        default: () => {
          console.log(this.dropdownData);
        },
      },
      sortType: {
        PART_ID: "partCode",
        NAME: "name",
        CATEGORY: "category.name",
        STATUS: "enabled",
      },
      redirectParams: {
        partId: "",
        timePeriod: "",
      },
      isCompareWithPrevious: false,
      currentSortType: "id",
      isDesc: true,
      pageType: "PART_SETTING",
      allColumnList: [],
      visible: false,
      dropdownOpening: false,
      objectType: "PART",
      showDropdown: false,
      currentIntervalIds: [],
      isCallingDone: false,
      isStopedExport: false,
      isShowExportButton: false,
      initTimePeriod: null,
      initPeriodType: null,
      listAllIds: [],
      listChecked: [],
      listCheckedFull: [],
      configDeleteField: {},
      cancelToken: undefined,
      displayCaret: false,
      showAnimation: false,
      visiblePopover: false,
      listSelectedTabs: [],
      currentActiveTab: {},
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
      isFilterChanging: false,
      currentDate: {
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      },
      timeRange: {
        minDate: null,
        maxDate: new Date(),
      },
      weekKey: 1,
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
    currentDate(newVal) {
      if (newVal) this.weekKey++;
    },
    reloadKey() {
      this.paging(1);
      this.getTabsByCurrentUser();
    },
    searchText: {
      handler(newVal) {
        this.requestParam.query = newVal;
        this.paging(1);
        this.getTabsByCurrentUser();
      },
      immediate: true,
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
      axios.post("/api/tab-table/save-tab-table-part", params).then(() => {
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
    visibleMoreAction(newVal) {
      if (!newVal) {
        this.updateTabStatus();
      }
    },
    showFilter(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.dropDownType = "main";
      }
    },
  },
  methods: {
    handleChangeDate(date) {
      console.log("handleChangeDate", date);
      this.requestParam.timePeriod = `W${date.fromTitle.replaceAll("-", "")}`;
      this.paging(1);
    },
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
      axios.post("/api/tab-table/save-tab-table-part", params).then(() => {
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
    openExtendedDropdown(item) {
      this.dropDownType = item.title;
    },
    onItemSelect(item) {
      if (item.title === this.listOfItems[0].title) {
        this.onChangeHref("/admin/parts/new");
      } else if (item.title === this.listOfItems[1].title) {
        this.showDrawer();
      }
    },
    showMachineDetails: function (machineId) {
      let child = Common.vue.getChild(this.$children, "machine-details");
      if (child != null) {
        child.showDetailsById(machineId);
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
    },
    handleShowFilter() {
      this.showFilter = true;
    },
    handleToggleFilter() {
      this.showPartPicker = false;
      if (!this.showFilter) {
        this.handleShowFilter();
      } else {
        this.handleCloseFilter();
      }
    },
    handleCloseFilter() {
      this.showFilter = false;
    },
    handlerForFilter(item) {
      console.log("handlerForFilter", item);
      if (this.requestParam.categoryId === item.id) {
        this.requestParam.categoryId = "";
      } else {
        this.requestParam.categoryId = item.id;
      }
      this.category();
      this.showFilter = false;
      this.isFilterChanging = true;
    },
    animationOutlineDropdown() {
      this.displayCaret = true;
      if (!this.showPartPicker) {
        this.showAnimation = true;
        setTimeout(() => {
          this.showAnimation = false;
        }, 700);
      }
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
        fromPopup || "chart-part"
      );
      if (child != null) {
        child.backFromPreview();
      }
    },
    timeFilter(timeParams, isCompareWithPrevious) {
      this.isCompareWithPrevious = isCompareWithPrevious;
      // remove old time params
      let removalFields = ["startDate", "endDate", "timePeriod"];
      removalFields.forEach((removalField) => {
        delete this.requestParam[removalField];
      });

      Object.keys(timeParams).forEach((key) => {
        this.requestParam[key] = timeParams[key];
      });
      this.paging(1);
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    deletePop(user) {
      if (user.length <= 1) {
        this.deleteLocation(user[0]);
      } else {
        this.deleteBatch(user, true);
      }
    },
    deleteBatch(user) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      const idArr = user.map((item) => {
        return item.id;
      });
      var param = {
        ids: idArr,
      };
      const self = this;
      axios.post("/api/parts/delete-in-batch", param).then(
        (response) => {
          if (response.data.success) {
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        },
        (error) => {
          console.log(error);
        },
        () => {
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        }
      );
    },
    deleteLocation(location) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: location.id,
      };
      const self = this;
      axios.delete(Page.API_BASE + "/" + location.id, param).then(
        (response) => {
          if (response.data.success) {
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        },
        (error) => {
          console.log(error);
        }
      );
    },
    async confirmDelete(checked) {
      if (checked) {
        localStorage.setItem("dontShowWarning", "true");
      }
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      const params = { ids: listIds };
      try {
        await axios.post(`/api/parts/delete-in-batch`, params);
        $("#op-delete-popup").modal("hide");
        this.paging(1);
      } catch (error) {
        console.log(error);
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
          .post(`/api/parts/delete-in-batch`, this.listChecked)
          .then((res) => {
            self.paging(1);
          })
          .catch((e) => {
            console.log(e);
          });
      }
    },
    getUnitName(unit) {
      let convertUnitName = "";
      if (unit === "MM") return (convertUnitName = "mm³");
      else if (unit === "CM") return (convertUnitName = "cm³");
      else if (unit === "M") return (convertUnitName = "m³");
      else return (convertUnitName = unit?.toLowerCase());
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
      window.location.href = `/admin/parts/${this.listChecked[0]}`;
    },
    onChangeHref(href) {
      setTimeout(() => {
        window.location.href = href;
      }, 700);
    },
    restoreItem() {
      const self = this;
      axios
        .post(`/api/parts/restore/${this.listChecked[0]}`)
        .then((res) => {
          console.log(res, "resss");
          self.paging(this.requestParam.page);
        })
        .finally(() => {
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        });
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
    log(res) {
      console.log("res", res);
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
    changeDropdown() {
      this.dropdownOpening = false;
    },
    openDropdown() {
      if (this.dropdownOpening === true) {
        this.dropdownOpening = false;
      } else {
        this.dropdownOpening = true;
      }
    },
    setAllColumnList() {
      console.log("this.allColumnList", this.allColumnList);
      this.allColumnList = [
        {
          label: this.resources["part_id"],
          field: "partCode",
          default: true,
          mandatory: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["part_name"],
          field: "name",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["category_name"],
          field: "category",
          default: true,
          sortable: true,
          sortField: "category.parent.name",
          defaultSelected: true,
          defaultPosition: 2,
        },
        {
          label: this.resources["status"],
          field: "enabled",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 3,
        },

        {
          label: this.resources["project_name"],
          field: "projectName",
          sortable: true,
          sortField: "category.name",
        },
        {
          label: this.resources["quantity_required"],
          field: "quantityRequired",
          sortable: true,
          sortField: "quantityRequired",
        },
        {
          label: this.resources["front_total_tool"],
          field: "totalMolds",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["front_active_tool"],
          field: "activeMolds",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["front_idle_tool"],
          field: "idleMolds",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: this.resources["front_inactive_tool"],
          field: "inactiveMolds",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 7,
        },
        {
          label: this.resources["front_disconnected_tool"],
          field: "disconnectedMolds",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 8,
        },
        {
          label: this.resources["quantity_produced"],
          field: "totalProduced",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 9,
        },
        {
          label: this.resources["design_revision_level"],
          field: "designRevision",
          sortable: true,
        },
        {
          label: this.resources["part_resin_code"],
          field: "resinCode",
          sortable: true,
        },
        {
          label: this.resources["part_resin_grade"],
          field: "resinGrade",
          sortable: true,
        },
        {
          label: this.resources["part_volume"],
          field: "partSize",
          sortable: true,
          sortField: "size",
        },
        {
          label: this.resources["part_weight"],
          field: "partWeight",
          sortable: true,
          sortField: "weight",
        },
        {
          label: this.resources["machine_id"],
          field: "machineCode",
          sortable: true,
          sortField: "mold.machine.machineCode",
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
    getCustomColumnAndInit() {
      Common.getCustomColumn(this.objectType)
        .then(async (data) => {
          this.allColumnList.push(...data);
          // this.resetColumnsListSelected();
          // this.getColumnListSelected();
        })
        .finally((error) => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        });
    },
    showDrawer() {
      this.visible = true;
    },
    closeDrawer() {
      this.visible = false;
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
    },
    downloadTooling(type) {
      this.isStopedExport = false;
      this.isCallingDone = false;
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

      document.getElementById("toggleExportProgressBar").click();
      this.progressBarRun();
      let $this = this;
      let sort = this.requestParam.sort
        ? `&sort=${this.requestParam.sort}`
        : ``;
      let url = `${
        Page.API_BASE
      }/${path}?ids=${downloadParams}&fileName=${fileName}&timezoneOffsetClient=${new Date().getTimezoneOffset()}${sort}`;
      if (this.listChecked.length === 0) {
        url = `${
          Page.API_BASE
        }/${path}?fileName=${fileName}&timezoneOffsetClient=${new Date().getTimezoneOffset()}${sort}`;
      }
      axios.get(url, { responseType: "blob", observe: "response" }).then(
        (response) => {
          if (!this.isStopedExport) {
            this.isCallingDone = true;
            setTimeout(() => {
              var blob = new Blob([response.data], {
                type: "text/plain;charset=utf-8",
              });
              if (type === EXPORT_TYPE.PDF) {
                saveAs(blob, `${fileName}.pdf`);
              } else {
                saveAs(blob, `${fileName}.xlsx`);
              }
            }, 2000);
          }
        },
        (error) => {
          console.log(error);
          this.isStopedExport = true;
        }
      );
    },

    handleResetColumnsListSelected() {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
    },
    getColumnListSelected() {
      axios.get(`/api/config/column-config?pageType=${this.pageType}`).then(
        (response) => {
          if (response.data.length) {
            let hashedByColumnName = {};
            response.data.forEach((item) => {
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
            let child = Common.vue.getChild(this.$children, "show-columns");
            if (child != null) {
              child.$forceUpdate();
            }
          }
        },
        (error) => {
          // Common.alert(error.data);
        }
      );
    },
    handleChangeValueCheckBox(value) {
      let column = this.allColumnList.filter((item) => item.field === value)[0];
      column.enabled = !column.enabled;
      this.saveSelectedList();
      this.$forceUpdate();
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
    showRevisionHistory(id) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "PART");
      }
    },
    showChart(part, tab) {
      const child = this.$refs.chartPart;
      if (child != null) {
        child.showChart(
          part,
          "QUANTITY",
          "DAY",
          this.octs[this.optimalCycleTime.strategy],
          tab
        );
      } else {
        setTimeout(() => {
          this.showChart(
            part,
            "QUANTITY",
            "DAY",
            this.octs[this.optimalCycleTime.strategy],
            tab
          );
        }, 300);
        console.log("cannot found chart part element");
      }
    },
    showPartDetails(part) {
      var child = Common.vue.getChild(this.$children, "part-details");
      if (child != null) {
        child.showPartDetails(part);
      }
    },
    category() {
      this.paging(1);
    },

    tab(tab) {
      this.currentActiveTab = tab;
      this.total = null;
      this.listChecked = [];
      this.requestParam.tabId = "";
      if (tab.name === "Disabled") {
        console.log("come1");
        this.requestParam.status = "disabled";
        // this.requestParam.deleted = true;
        this.paging(1);
      } else {
        console.log("come2");
        this.currentSortType = "id";
        this.requestParam.sort = "id,desc";
        this.requestParam.status = "active";
        delete this.requestParam.deleted;
        if (!tab.isDefaultTab) {
          this.requestParam.tabId = tab.id;
        }
        this.paging(1);
      }
      this.changeShow(null);
    },
    search(page) {
      this.paging(1);
    },

    deletePart(part) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: part.id,
      };
      const self = this;
      axios.delete(Page.API_BASE + "/" + part.id, param).then(
        (response) => {
          if (response.data.success) {
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        },
        (error) => {
          console.log(error);
        }
      );
    },

    enable() {
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, true);
    },
    disable() {
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, false);
    },
    async saveBatch(idArr, boolean) {
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      try {
        await axios.post("/api/parts/change-status-in-batch", param);
      } catch (error) {
        console.log(error);
      } finally {
        this.listChecked = [];
        this.isAll = false;
        this.showDropdown = false;
        this.paging(1);
        this.getTabsByCurrentUser();
      }
    },
    async paging(pageNumber) {
      this.requestParam.page = pageNumber == undefined ? 1 : pageNumber;

      let objectId = Common.getParameter("objectId");
      this.requestParam.id = objectId;

      if (!this.requestParam.categoryId) {
        delete this.requestParam.categoryId;
      }
      var param = Common.param(this.requestParam);

      console.log(`param`, param);

      const {
        firstCompare,
        secondCompare,
        thirdCompare,
        firstCaviti,
        secondCaviti,
        counterId,
        toolingId,
      } = this;
      let queryParams = "";
      if (counterId || toolingId) {
        queryParams = `&where=counter${counterId},tool${toolingId}`;
      } else {
        queryParams = `&where=op${secondCompare},${firstCompare}${firstCaviti},${thirdCompare}${secondCaviti}`;
      }

      param = param + queryParams;

      this.isLoading = true;

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      this.cancelToken = axios.CancelToken.source();

      try {
        const response = await axios.get(Page.API_BASE + "?" + param, {
          cancelToken: this.cancelToken.token,
        });
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

        Common.handleNoResults("#app", this.results.length);

        if (this.results.length > 0) {
          Common.triggerShowActionbarFeature(this.$children);
        }
      } catch (error) {
        console.log(error);
      } finally {
        this.isLoading = false;
      }
    },

    categoryLocation(categoryId) {
      var location = "";
      for (var i = 0; i < this.categories.length; i++) {
        var parent = this.categories[i];
        for (var j = 0; j < parent.children.length; j++) {
          var category = parent.children[j];

          if (category.id == categoryId) {
            var parentText =
              parent.enabled == false
                ? '<span class="disabled">' + parent.name + "</span>"
                : parent.name;
            var categoryText =
              category.enabled == false
                ? '<span class="disabled">' + category.name + "</span>"
                : category.name;

            return parentText + " > " + categoryText;
          }
        }
      }
      return "";
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
    showSystemNoteModal(result) {
      var child = Common.vue.getChild(this.$children, "system-note");
      if (child != null) {
        child.showSystemNote({ id: result });
      }
    },
    progressBarRun() {
      this.currentIntervalIds.forEach((id) => {
        clearInterval(id);
      });
      let $vm = this;
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
    exportPart() {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_part_data`;

      document.getElementById("toggleExportProgressBar").click();
      this.progressBarRun();
      let downloadRequestParams = Object.assign({}, this.requestParam);

      const listChecked =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      downloadRequestParams.ids = listChecked.join(",");
      downloadRequestParams = Common.param(downloadRequestParams);
      let url = `${Page.API_BASE}/export-excel?${downloadRequestParams}`;

      axios.get(url, { responseType: "blob", observe: "response" }).then(
        (response) => {
          if (!this.isStopedExport) {
            this.isCallingDone = true;
            setTimeout(() => {
              var blob = new Blob([response.data], {
                type: "text/plain;charset=utf-8",
              });
              saveAs(blob, `${fileName}.xlsx`);
            }, 2000);
          }
        },
        (error) => {
          console.log(error);
          this.isStopedExport = true;
        }
      );
    },
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    assignToProject() {
      console.log("listChecked", this.listChecked);
      console.log("listCheckedFull", this.listCheckedFull);
      console.log("listCheckedTracked", this.listCheckedTracked);
    },
    async getTabsByCurrentUser(isCallAtMounted) {
      try {
        const res = await axios.get(
          "/api/tab-table/by-current-user?objectType=PART"
        );
        this.listSelectedTabs = res.data.data;
        if (isCallAtMounted) {
          this.setNewActiveTab();
        }
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
      this.tab(this.listSelectedTabs[0]);
    },
    checkStatusParam(tabItem) {
      console.log("checkStatusParam", tabItem, this.currentActiveTab);
      return (
        (this.currentActiveTab.id != null &&
          tabItem.id != null &&
          this.currentActiveTab.id === tabItem.id) ||
        (this.currentActiveTab.id == null &&
          tabItem.id == null &&
          this.currentActiveTab.name === tabItem.name)
      );
    },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} parts on this current page are selected.`,
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
          title: `All ${this.total} parts are selected.`,
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
          frameType: "PART_SETTING",
        });
        const response = await axios.get(
          "/api/batch/all-ids-for-full-frame?" + param
        );
        this.listAllIds = response.data.data;
      } catch (error) {
        console.log(error);
      }
    },
    async popup(popupType) {
      // const url = new URL(location.href);
      // console.log("🚀 ~ file: data-family-part.vue:2306 ~ popup ~ url:", url)
      const searchParams = new URLSearchParams(location.hash.split("?")[1]);
      const partId = searchParams.get("objectFunctionId");
      if (["NOTE_MENTIONED", "NOTE_REPLIED"].includes(popupType)) {
        try {
          const response = await axios.get(`/api/parts/${partId}`);
          const part = response.data;
          this.$nextTick(() => {
            this.showChart(part, "switch-notes");
          });
        } catch (error) {
          console.error("Error when get part to show popup", error);
        }
      }
    },
    directLink(page, dataObj) {
      location.href = `${Common.PAGE_URL[page]}?${Common.param(dataObj)}`;
    },
    captureQuery() {
      // capture query
      var url = new URL(location.href);
      const urlParam = url.hash.replace("#", "").split("?")[1];
      const params = Common.parseParams(urlParam);
      const timePeriod = params.timePeriod;
      const periodType = params.periodType;
      const query = params.query;
      const toolingId = params.toolingId;

      this.initTimePeriod = timePeriod;
      this.initPeriodType = periodType;

      console.log({ url, params });

      if (timePeriod && periodType) {
        // this.redirectParams.partId = partId;

        if (query) {
          this.requestParam.query = query;
        }

        if (toolingId) {
          this.toolingId = toolingId;
        }
      }

      if (params?.from === Common.PAGE_URL.TABBED_DASHBOARD) {
        this.requestParam.tabbedDashboardRedirected = true;
        if (params.categoryId) {
          this.requestParam.categoryId = params.categoryId;
        } else {
          delete this.requestParam.categoryId;
          this.requestParam.noProjectAssigned = true;
        }
      }

      if (params?.from === Common.PAGE_URL.DASHBOARD) {
        this.requestParam.dashboardRedirected = false;
      }

      if (params.ids) {
        this.requestParam.ids = params.ids;
      }

      if (params?.popup) {
        this.popup(params.popup);
      }
    },
  },
  created() {
    // observe global state
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
      () => headerVm?.options,
      (newVal) => {
        if (!newVal) return;
        const options = newVal;
        this.optimalCycleTime = options.OPTIMAL_CYCLE_TIME;
      },
      { immediate: true }
    );

    window.addEventListener("popstate", this.captureQuery);
    this.captureQuery();
  },
  async mounted() {
    this.getTabsByCurrentUser(true);
    this.categories = await headerVm.getListCategories(true);
    // history.replaceState(null, null, Common.PAGE_URL.PART);
    this.categories.forEach((item) => {
      this.listOfItemsForCategory.push({ title: item.name, id: item.id });
      item.children.forEach((product) => {
        this.listOfItemsForProduct.push({
          title: product.name,
          id: product.id,
        });
      });
    });
  },
  destroyed() {
    window.removeEventListener("popstate", this.captureQuery);
  },
};
</script>

<style></style>
