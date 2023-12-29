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
                style="margin-bottom: -1px; width: calc(100% - 260px)"
              >
                <template v-for="(tabItem, index) in listSelectedTabs">
                  <li
                    v-if="
                      tabItem.show ||
                      (tabItem.isDefaultTab && tabItem.name === 'All')
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
                        v-if="tabItem.name !== 'All' || !tabItem.isDefaultTab"
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
              <div class="data-table-top-action" style="width: 260px">
                <customization-modal
                  :all-columns-list="allColumnList"
                  @save="saveSelectedList"
                  :resources="resources"
                  :is-more-action="true"
                ></customization-modal>
                <div>
                  <a
                    @click="Common.onChangeHref('/admin/terminals/new')"
                    href="javascript:void(0)"
                    class="btn-custom btn-custom-primary animationPrimary all-time-filters btn-custom-focus"
                  >
                    <span>{{ resources["install_terminal"] }}</span>
                  </a>
                </div>
                <div>
                  <div class="assign-more">
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
                        :class="{ cust_focus: showFilter }"
                      >
                        <img src="/images/icon/Icon_feather-filter.svg" />
                        <span v-show="isFilterApplied" class="pink-dot"></span>
                      </a>
                    </a-tooltip>
                    <common-popover
                      :is-visible="showFilter"
                      @close="handleCloseFilter"
                      class="filter-custom filter-custom-terminal"
                    >
                      <common-treeselect-dropdown
                        v-show="dropDownType == 'main'"
                        class="filter-custom-dropdown"
                        :style="{
                          position: 'absolute',
                          width: '420px !important',
                          transform: 'translate(-55px, -42px)',
                        }"
                        :checkbox="true"
                        :items="ListFilterItems"
                        :reset-handler="resetHandler"
                        id="filter"
                        :apply-handler="checkedItems"
                        :handle-check="handleCheckStatus"
                        :is-mold-list="true"
                      ></common-treeselect-dropdown>
                    </common-popover>
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
                        <label
                          :for="`dropdown-input-${index2}`"
                          style="
                            line-height: 17px;
                            display: flex;
                            align-items: center;
                            letter-spacing: 0px;
                            color: #595959;
                            height: 32px;
                            padding: 0px 8px;
                            cursor: pointer;
                          "
                        >
                          <template>
                            <input
                              :id="`dropdown-input-${index2}`"
                              type="checkbox"
                              :value="subItem.id"
                              :checked="subItem.show"
                              :disabled="
                                subItem.name === 'All' && subItem.isDefaultTab
                              "
                              @change="selectTab(subItem, index2)"
                            />
                            <div
                              class="checkbox-custom"
                              :class="{
                                checkboxDisabled:
                                  subItem.name === 'All' &&
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
                        :unit="['terminal', 'terminals']"
                        @select-all="handleSelectAll('all')"
                        @select-page="handleSelectAll('page')"
                        @deselect="handleSelectAll('deselect')"
                      ></select-all>
                    </div>
                  </th>
                  <template v-for="(item, index) in allColumnList">
                    <th
                      v-if="item.enabled"
                      :key="index"
                      :class="[
                        {
                          __sort: item.sortable,
                          active: currentSortType === item.sortField,
                        },
                        isDesc ? 'desc' : 'asc',
                        item.label === 'Status' || item.label === 'OP'
                          ? ''
                          : 'text-left',
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
                            :unit="['terminal', 'terminals']"
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
                          class="action-item"
                          v-click-outside="closeShowDropDown"
                        >
                          <div class="change-status-dropdown drowdown d-inline">
                            <template
                              v-if="
                                listCheckedFull[0].equipmentStatus ==
                                'DISCARDED'
                              "
                            >
                              <div
                                title="Change status"
                                class="change-status"
                                @click.prevent="
                                  changeEquipmentStatus(
                                    listCheckedFull[0],
                                    'INSTALLED'
                                  )
                                "
                              >
                                <span>Change Status</span>
                                <i class="icon-action change-status-icon"></i>
                              </div>
                            </template>
                            <template v-else>
                              <div
                                title="Change status"
                                class="change-status"
                                @click="showDropdown = !showDropdown"
                              >
                                <span>Change Status</span>
                                <i class="icon-action change-status-icon"></i>
                              </div>
                              <ul
                                class="dropdown-menu"
                                :class="[showDropdown ? 'show' : '']"
                              >
                                <template
                                  v-for="(code, index) in codes.EquipmentStatus"
                                >
                                  <li
                                    v-if="
                                      code.code !=
                                        listCheckedFull[0].equipmentStatus &&
                                      code.code != 'DETACHED' &&
                                      listCheckedFull[0].equipmentStatus !=
                                        'DISCARDED' &&
                                      listCheckedFull[0].equipmentStatus !=
                                        'FAILURE'
                                    "
                                    :key="index"
                                  >
                                    <div
                                      v-if="code.code === 'INSTALLED'"
                                      class="dropdown-item"
                                      @click.prevent="
                                        changeEquipmentStatus(
                                          listCheckedFull[0],
                                          code.code
                                        )
                                      "
                                    >
                                      <span>{{ resources["register"] }}</span>
                                    </div>
                                    <div
                                      v-if="code.code === 'DISCARDED'"
                                      class="dropdown-item"
                                      @click.prevent="
                                        changeEquipmentStatus(
                                          listCheckedFull[0],
                                          code.code
                                        )
                                      "
                                    >
                                      <span>{{ resources["discard"] }}</span>
                                    </div>
                                    <div
                                      v-if="code.code === 'FAILURE'"
                                      class="dropdown-item"
                                      @click.prevent="
                                        changeEquipmentStatus(
                                          listCheckedFull[0],
                                          code.code
                                        )
                                      "
                                    >
                                      <span>{{
                                        resources["report_failure"]
                                      }}</span>
                                    </div>
                                    <div
                                      v-if="code.code === 'AVAILABLE'"
                                      class="dropdown-item"
                                      @click.prevent="
                                        changeEquipmentStatus(
                                          listCheckedFull[0],
                                          code.code
                                        )
                                      "
                                    >
                                      <span>{{ resources["unregister"] }}</span>
                                    </div>
                                  </li>
                                </template>
                                <template
                                  v-for="(code, index) in codes.EquipmentStatus"
                                >
                                  <li
                                    v-if="
                                      code.code !=
                                        listCheckedFull[0].equipmentStatus &&
                                      code.code != 'DETACHED' &&
                                      listCheckedFull[0].equipmentStatus ===
                                        'FAILURE'
                                    "
                                    :key="index"
                                  >
                                    <div
                                      v-if="code.code === 'DISCARDED'"
                                      class="dropdown-item"
                                      @click.prevent="
                                        changeEquipmentStatus(
                                          listCheckedFull[0],
                                          code.code
                                        )
                                      "
                                    >
                                      <span>{{ resources["discard"] }}</span>
                                    </div>
                                  </li>
                                </template>
                                <template
                                  v-for="(code, index) in codes.EquipmentStatus"
                                >
                                  <li
                                    v-if="
                                      code.code !=
                                        listCheckedFull[0].equipmentStatus &&
                                      code.code != 'DETACHED' &&
                                      listCheckedFull[0].equipmentStatus ===
                                        'DISCARDED'
                                    "
                                    :key="index"
                                  >
                                    <div
                                      v-if="code.code === 'INSTALLED'"
                                      class="dropdown-item"
                                      @click.prevent="
                                        changeEquipmentStatus(
                                          listCheckedFull[0],
                                          code.code
                                        )
                                      "
                                    >
                                      <span>{{ resources["reinstall"] }}</span>
                                    </div>
                                  </li>
                                </template>
                                <template
                                  v-for="(code, index) in codes.EquipmentStatus"
                                >
                                  <li
                                    v-if="
                                      code.code !=
                                        listCheckedFull[0].equipmentStatus &&
                                      code.code != 'DETACHED' &&
                                      listCheckedFull[0].equipmentStatus ===
                                        'FAILURE'
                                    "
                                    :key="index"
                                  >
                                    <div
                                      v-if="code.code === 'AVAILABLE'"
                                      class="dropdown-item"
                                      @click.prevent="
                                        changeEquipmentStatus(
                                          listCheckedFull[0],
                                          code.code
                                        )
                                      "
                                    >
                                      <span>{{ resources["unregister"] }}</span>
                                    </div>
                                  </li>
                                </template>
                              </ul>
                            </template>
                          </div>
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
                            listChecked.length >= 1 &&
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
                            listChecked.length >= 1 &&
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
                          v-if="listChecked.length == 1"
                          class="action-item"
                          @click="showCreateWorkorder"
                          style="align-items: flex-start"
                        >
                          <span>Create Workorder</span>
                          <img
                            src="/images/icon/icon-workorder.svg"
                            style="margin-left: 4px; height: 15px; width: 15px"
                          />
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            !currentActiveTab.isDefaultTab
                          "
                          class="action-item"
                        >
                          <move-to-action
                            :type-view="'terminal'"
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

              <tbody class="op-list" style="display: none">
                <tr v-for="result in results" :key="result.id" :id="result.id">
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
                    v-if="column.enabled"
                  >
                    <td
                      :key="columnIndex + 'equipmentCode'"
                      v-if="column.field === 'equipmentCode'"
                      style="max-width: 150px"
                      class="text-left"
                    >
                      <a href="#" @click.prevent="showTerminalDetails(result)">
                        {{ result.equipmentCode }}
                      </a>

                      <div class="small text-muted font-size-11-2">
                        <span>{{ resources["registered"] }}:</span>
                        {{ formatToDate(result.createdAtDate) }}
                      </div>
                    </td>
                    <td
                      :key="columnIndex + 'numberCounter'"
                      class="text-left"
                      v-else-if="column.field === 'numberCounter'"
                    >
                      <div
                        v-on:click="showNumberOfCounter(result.id)"
                        class="font-size-14 anchor-cpy"
                      >
                        {{ result.numberOfCounter }}
                      </div>
                    </td>
                    <td
                      :key="columnIndex + 'company'"
                      class="text-left"
                      v-else-if="column.field === 'company'"
                    >
                      <div v-if="result.companyName">
                        <a
                          class="font-size-14"
                          href="#"
                          @click.prevent="
                            showCompanyDetailsById(result.companyId)
                          "
                        >
                          {{ result.companyName }}
                        </a>
                        <div class="small text-muted font-size-11-2">
                          {{ result.companyCode }}
                        </div>
                      </div>
                    </td>
                    <td
                      :key="columnIndex + 'location'"
                      class="text-left"
                      v-else-if="column.field === 'location'"
                    >
                      <div v-if="result.locationCode">
                        <div
                          class="font-size-14"
                          v-html="
                            locationName(
                              result.locationName,
                              result.locationEnabled
                            )
                          "
                        ></div>
                        <div class="small text-muted font-size-11-2">
                          {{ result.locationCode }}
                        </div>
                      </div>
                    </td>
                    <td
                      :key="columnIndex + 'activationDate'"
                      class="text-left"
                      v-else-if="column.field === 'activationDate'"
                    >
                      <span class="font-size-14">
                        {{ convertDate(result.activationDate) }}
                      </span>
                      <div>
                        <span class="font-size-11-2">
                          {{ convertTimeString(result.activationDate) }}
                        </span>
                      </div>
                    </td>
                    <td
                      :key="columnIndex + 'installationDate'"
                      class="text-left"
                      v-else-if="column.field === 'installationDate'"
                    >
                      <span class="font-size-14">
                        {{ convertDate(result.installationDate) }}
                      </span>
                      <div>
                        <span class="font-size-11-2">
                          {{ convertTimeString(result.installationDate) }}
                        </span>
                      </div>
                    </td>
                    <!-- <td
                      class="text-left"
                      v-else-if="column.field === 'installationArea'"
                    >
                      {{ result.installationArea }}
                    </td> -->
                    <td
                      :key="columnIndex + 'area'"
                      class="text-left"
                      v-else-if="column.field === 'area'"
                    >
                      <div>
                        <div v-if="result.areaName" class="font-size-14">
                          {{ result.areaName }}
                        </div>
                        <div
                          v-if="result.areaType"
                          class="small text-muted font-size-11-2"
                        >
                          {{ areaTypes[result.areaType].name }}
                        </div>
                      </div>
                    </td>
                    <td
                      :key="columnIndex + 'operatedAt'"
                      class="text-left"
                      v-else-if="column.field === 'operatedAt'"
                    >
                      {{
                        result.operatedDateTime
                          ? moment(result.operatedDateTime).format(
                              "YYYY-MM-DD HH:MM"
                            )
                          : "-"
                      }}
                    </td>
                    <td
                      :key="columnIndex + 'connectionStatus'"
                      v-else-if="column.field === 'connectionStatus'"
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                    >
                      <span
                        v-if="result.connectionStatus == 'OFFLINE'"
                        style="background: #ff8489"
                        class="on-off-tag"
                        >Offline</span
                      >
                      <span
                        v-else-if="result.connectionStatus == 'ONLINE'"
                        style="background: #d2f8e2"
                        class="on-off-tag"
                        >Online</span
                      >
                    </td>
                    <td
                      :key="columnIndex + 'equipmentStatus'"
                      style="vertical-align: middle !important"
                      class="text-center op-status-td connection-status"
                      v-else-if="column.field === 'equipmentStatus'"
                    >
                      <div v-if="result.equipmentStatus == 'INSTALLED'">
                        <div class="dot-status dot-status-type1"></div>
                        <span>Registered</span>
                      </div>
                      <div v-if="result.equipmentStatus == 'AVAILABLE'">
                        <div class="dot-status dot-status-type2"></div>
                        <span>Available</span>
                      </div>
                      <div v-if="result.equipmentStatus == 'FAILURE'">
                        <div class="dot-status dot-status-type3"></div>
                        <span>Failure</span>
                      </div>
                      <div v-if="result.equipmentStatus == 'DISCARDED'">
                        <div class="dot-status dot-status-type4"></div>
                        <span>Discarded</span>
                      </div>
                    </td>
                    <td
                      :key="columnIndex + column.field"
                      :class="{
                        'text-left': !['equipmentStatus'].includes(
                          column.field
                        ),
                      }"
                      v-else
                      class="titleCase"
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

            <div class="row">
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
          </div>
        </div>
      </div>
    </div>
    <number-of-counter :resources="resources"></number-of-counter>
    <revision-history :resources="resources"></revision-history>
    <system-note
      ref="system-note"
      :paramid="param"
      system-note-function="TERMINAL_SETTING"
      :resources="resources"
    ></system-note>
    <company-details :resources="resources"></company-details>
    <location-history :resources="resources"></location-history>
    <action-bar-feature feature="ACTION_BAR"></action-bar-feature>
    <creator-work-order-dialog
      :resources="resources"
      :enable-asset-selection="false"
      :is-show="workorderModalVisible"
      :asset-selected="terminalSelectedToCreateWorkorder"
      :modal-type="'CREATE'"
      :is-terminal-asset-selected="true"
      @close="closeCreateWorkOrder"
    ></creator-work-order-dialog>
    <manage-tab-view
      :resources="resources"
      :object-type="'TERMINAL'"
      @open-edit="openEdit"
      @open-duplicate="openDuplicate"
      @move-success="getTabsByCurrentUser"
      :trigger-update="triggerUpdateManageTab"
    ></manage-tab-view>
    <tab-view-modal
      :resources="resources"
      :object-type="'TERMINAL'"
      @show-terminal-details="showTerminalDetails"
      @show-company-details-by-id="showCompanyDetailsById"
      :all-tooling-list="allToolingList"
      :page-type="pageType"
      @edit-success="editSuccess"
      @create-success="createSuccess"
    ></tab-view-modal>
    <terminal-details :resources="resources"></terminal-details>
    <company-details :resources="resources"></company-details>
    <toast-alert
      @close-toast="closeToastAlert"
      :show="toastAlert.value"
      :title="toastAlert.title"
      :content="toastAlert.content"
      ref-custom="toolingToast"
    ></toast-alert>
  </div>
</template>

<script>
const Page = Common.getPage("terminals");
const AREA_TYPES = {
  MAINTENANCE_AREA: { name: "Maintenance", value: "MAINTENANCE_AREA" },
  PRODUCTION_AREA: { name: "Production", value: "PRODUCTION_AREA" },
  WAREHOUSE: { name: "Warehouse", value: "WAREHOUSE" },
};
const NEW_TABLE_URL = Common.getNewTableUrl();
const PAGE_SIZE = 20;
module.exports = {
  components: {
    "terminal-details": httpVueLoader("/components/terminal-details.vue"),
    "number-of-counter": httpVueLoader("/components/number-of-counter.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "company-details": httpVueLoader("/components/company-details.vue"),
    "location-history": httpVueLoader("/components/location-history.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "creator-work-order-dialog": httpVueLoader(
      "/components/work-order/creator/creator-work-order-dialog.vue"
    ),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-terminal-modal.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action.vue"),
  },
  props: {
    resources: Object,
    searchText: String,
    reloadKey: [String, Number],
    filterCode: String,
  },
  data() {
    return {
      listOfItemsForStatus: [],
      listOfItemsForFilter: [{ title: "By Status" }],
      ListFilterItems: [
        {
          id: "connection",
          title: "By Connection Status",
          childrens: [
            { title: "Offline", code: "OFFLINE" },
            { title: "Online", code: "ONLINE" },
          ],
        },
        {
          id: "terminal",
          title: "By Terminal Status",
          childrens: [
            { code: "AVAILABLE", title: "Available" },
            { code: "INSTALLED", title: "Registered" },
            { code: "FAILURE", title: "Failure" },
            { code: "DISCARDED", title: "Discarded" },
          ],
        },
      ],
      showFilter: false,
      dropDownType: "main",
      showDrowdown: null,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        id: "",
        query: "",
        status: "all",
        sort: "id,desc",
        page: 1,
        terminalStatusList: ["DISCARDED", "INSTALLED", "FAILURE", "AVAILABLE"],
        connectionStatusList: ["ONLINE", "OFFLINE"],
        equipmentStatus: "",
        pageType: "TERMINAL_SETTING",
        tabId: "",
        isTerminalScreen: true,
      },
      codes: {},
      sortType: {
        TERMINAL_ID: "equipmentCode",
        NO_COUNTER: "numberCounter",
        COMPANY: "location.company.name",
        LOCATION: "location.name",
        AREA: "installationArea",
        OP: "connectionStatus",
        STATUS: "equipmentStatus",
      },
      param: "",
      resultId: "",
      currentSortType: "id",
      isDesc: true,
      pageType: "TERMINAL_SETTING",
      allColumnList: [],
      showDropdown: false,
      listAllIds: [],
      listChecked: [],
      listCheckedFull: [],
      cancelToken: undefined,
      workorderModalVisible: false,
      assetSelected: null,
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
      isFilterChanging: false,
      defaultStatusListLength: 0,
      areaTypes: { ...AREA_TYPES },
      terminalSelectedToCreateWorkorder: [],
    };
  },
  computed: {
    isAll() {
      if (!this.listChecked.length) {
        return false;
      }
      return (
        this.results.length &&
        this.results.every((item) =>
          this.listChecked.some((checked) => checked === item.id)
        )
      );
    },
    isFilterApplied() {
      return this.isFilterChanging;
    },
  },
  watch: {
    reloadKey() {
      this.paging(1);
    },
    searchText: {
      handler(newVal) {
        console.log("searchText", newVal);
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
    showFilter(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.dropDownType = "main";
      }
    },
  },
  methods: {
    handleCheckStatus(items) {
      console.log("handleCheckStatus", items);
      this.showFilter = false;
      let opStatus = [];
      let eqStatus = [];
      let isAllEqStatus = false;
      let isAllOpStatus = false;
      items.forEach((item) => {
        if (
          item.title === this.ListFilterItems[0].title &&
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
      this.requestParam.connectionStatusList = opStatus;

      items.forEach((item) => {
        if (
          item.title == this.ListFilterItems[1].title &&
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
      this.requestParam.terminalStatusList = eqStatus;
      const currentStatusListLength =
        this.requestParam.connectionStatusList.length +
        this.requestParam.terminalStatusList.length;
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
      this.requestParam.connectionStatusList = ["ONLINE", "OFFLINE"];
      this.requestParam.terminalStatusList = [
        "DISCARDED",
        "INSTALLED",
        "FAILURE",
        "AVAILABLE",
      ];
      this.isFilterChanging = false;
      this.search();
    },
    openCustomizationModal() {
      this.visibleMoreAction = false;
      var child = Common.vue.getChild(this.$children, "customization-modal");
      if (child != null) {
        child.openModal();
      }
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
      axios.post("/api/tab-table/save-tab-table-terminal", params).then(() => {
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
        child.showCreateTabView("DUPLICATE", tab, this.getStatusType(tab.name));
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
    truncateText(text, length) {
      if (text.length > length) {
        return text.substring(0, length) + "...";
      } else {
        return text;
      }
    },
    async getTabsByCurrentUser(isCallAtMounted) {
      await axios
        .get("/api/tab-table/by-current-user?objectType=TERMINAL")
        .then((res) => {
          this.listSelectedTabs = res.data.data;
          if (isCallAtMounted) {
            this.setNewActiveTab();
          }
        });
    },
    setNewActiveTab() {
      this.tab(this.listSelectedTabs[0]);
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
    getStatusType(tabname) {
      let statusType = "";
      switch (tabname) {
        case "Enabled":
          statusType = "all";
          break;
        case "In-house":
          statusType = "IN_HOUSE";
          break;
        case "Supplier":
          statusType = "SUPPLIER";
          break;
        case "Toolmaker":
          statusType = "TOOL_MAKER";
          break;
        case "Disabled":
          statusType = "disabled";
          break;
      }
      return statusType;
    },
    showCreateWorkorder() {
      console.log("this.listCheckedFull[0]", this.listCheckedFull[0]);
      this.assetSelected = this.listCheckedFull[0].id;
      this.terminalSelectedToCreateWorkorder = [
        {
          ...this.listCheckedFull[0],
          checked: true,
          assetView: "terminal",
          title: this.listCheckedFull[0]?.equipmentCode,
        },
      ];
      this.workorderModalVisible = true;
    },
    closeCreateWorkOrder() {
      this.workorderModalVisible = false;
      this.assetSelected = null;
    },
    openExtendedDropdown(item) {
      this.dropDownType = item.title;
      console.log(item);
    },
    onItemSelect(item) {
      console.log(item);
    },
    handleShowFilter() {
      this.showFilter = true;
    },
    handleCloseFilter() {
      this.showFilter = false;
    },
    handleToggleFilter() {
      if (!this.showFilter) {
        this.handleShowFilter();
      } else {
        this.handleCloseFilter();
      }
    },
    handlerForFilter(items) {
      let opStatus = [];
      items.forEach((item) => {
        opStatus.push(item.code);
      });
      this.requestParam.terminalStatusList = opStatus;
      this.search();
    },
    showCompanyDetailsById: function (company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(company);
      }
    },
    showLocationHistory: function (mold) {
      var child = Common.vue.getChild(this.$children, "location-history");
      if (child != null) {
        child.showLocationHistory(mold);
      }
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },

    checkSelect: function (id) {
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
      window.location.href = `/admin/terminals/${this.listChecked[0]}`;
    },
    setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["terminal_id"],
          field: "equipmentCode",
          default: true,
          mandatory: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["no_od_counters"],
          field: "numberCounter",
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
          default: true,
          sortable: true,
          sortField: "location.name",
          defaultSelected: true,
          defaultPosition: 3,
        },
        // {
        //     label: this.resources['installation_area'],
        //     field: 'installationArea',
        //     default: true,
        //     sortable: true,
        //     defaultSelected: true,
        //     defaultPosition: 4
        // },
        {
          label: this.resources["plant_area"],
          field: "area",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["connection_status"],
          field: "connectionStatus",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["terminal_status"],
          field: "equipmentStatus",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: this.resources["activation_date"],
          field: "activationDate",
          sortable: true,
          sortField: "activatedAt",
        },
        // {label: 'Installation Area', field: 'installationArea'},
        {
          label: this.resources["installation_date"],
          field: "installationDate",
          sortable: true,
          sortField: "installedAt",
        },
        {
          label: this.resources["installation_personnel"],
          field: "installedBy",
          sortable: true,
        },
        { label: this.resources["memo"], field: "memo", sortable: true },
        {
          label: this.resources["last_connection"],
          field: "operatedAt",
          default: true,
          sortable: true,
        },
      ];
      this.resetColumnsListSelected();
      this.getColumnListSelected();
    },
    resetColumnsListSelected() {
      this.allColumnList.forEach((item) => {
        item.enabled = !!item.default;
        if (item.sortable && !item.sortField) {
          item.sortField = item.field;
        }
      });
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
    saveBatch(idArr, boolean) {
      const self = this;
      var param = {
        ids: idArr,
        enabled: boolean,
      };
      axios
        .post("/api/terminals/change-status-in-batch", param)
        .then(function (response) {
          self.resetAfterUpdate();
          self.getTabsByCurrentUser();
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          self.listChecked = [];
          self.listCheckedFull = [];
          self.showDropdown = false;
        });
    },

    save: function (checklist) {
      const self = this;
      var param = {
        id: checklist.id,
        enabled: checklist.enabled,
      };
      axios
        .put(Page.API_BASE + "/" + checklist.id + "/enable", param)
        .then(function (response) {
          self.resetAfterUpdate();
          self.getTabsByCurrentUser();
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          self.listChecked = [];
          self.listCheckedFull = [];
          self.showDropdown = false;
        });
    },
    handleResetColumnsListSelected: function () {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
    },
    convertTime: function (s) {
      const dtFormat = new Intl.DateTimeFormat("en-GB", {
        timeStyle: "medium",
        timeZone: "UTC",
      });

      return s != null ? dtFormat.format(new Date(s * 1e3)) : "";
    },
    convertDate: function (s) {
      return s?.slice(0, 10);
    },

    convertTimeString: function (s) {
      return s?.slice(10);
    },
    getColumnListSelected() {
      axios
        .get(`/api/config/column-config?pageType=${this.pageType}`)
        .then((response) => {
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
            this.allColumnList.sort(function (a, b) {
              return a.position - b.position;
            });
            this.$forceUpdate();
            let child = Common.vue.getChild(this.$children, "show-columns");
            if (child != null) {
              child.$forceUpdate();
            }
          }
        })
        .catch(function (error) {
          // Common.alert(error.data);
        });
    },
    handleChangeValueCheckBox: function (value) {
      let column = this.allColumnList.filter((item) => item.field === value)[0];
      column.enabled = !column.enabled;
      this.saveSelectedList();
      this.$forceUpdate();
    },

    saveSelectedList: function (dataCustomize, list) {
      const dataFake = list.map((item, index) => {
        if (item.field) {
          return { ...item, position: index };
        }
      });
      this.allColumnList = dataFake;
      const self = this;
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
        .then((response) => {
          let hashedByColumnName = {};
          response.data.forEach((item) => {
            hashedByColumnName[item.columnName] = item;
          });
          self.allColumnList.forEach((item) => {
            if (hashedByColumnName[item.field] && !item.id) {
              item.id = hashedByColumnName[item.field].id;
              item.position = hashedByColumnName[item.field].position;
            }
          });
        })
        .finally(() => {
          self.allColumnList.sort(function (a, b) {
            return a.position - b.position;
          });
          self.$forceUpdate();
        });
    },
    changeShow(resultId) {
      if (resultId != null) {
        this.showDrowdown = resultId;
      } else {
        this.showDrowdown = null;
      }
    },
    showRevisionHistory: function (id) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "TERMINAL");
      }
    },

    showNumberOfCounter: function (terminalId) {
      var child = Common.vue.getChild(this.$children, "number-of-counter");
      if (child != null) {
        child.showDetails(terminalId);
      }
    },
    tab(tab) {
      this.currentActiveTab = tab;
      this.total = null;
      this.requestParam.tabId = "";
      this.tabCurrent = tab.name;
      this.listChecked = [];
      this.listCheckedFull = [];
      if (tab.name === "Disabled") {
        this.requestParam.status = this.getStatusType(tab.name);
        this.paging(1);
      } else {
        this.requestParam.status = this.getStatusType(tab.name);
        this.currentSortType = "id";
        this.requestParam.sort = "id,desc";
        delete this.requestParam.deleted;
        if (!tab.isDefaultTab) {
          this.requestParam.tabId = tab.id;
        }
        this.resetAfterUpdate();
        this.paging(1);
      }
      this.changeShow(null);
    },
    search: function (page) {
      this.resetAfterUpdate();
      this.paging(1);
    },

    deleteTerminal: function (terminal) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: terminal.id,
      };
      const self = this;
      axios
        .delete(Page.API_BASE + "/" + terminal.id, param)
        .then(function (response) {
          if (response.data.success) {
            self.resetAfterUpdate();
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },

    changeEquipmentStatus: function (data, status) {
      var param = {
        id: data.id,
        equipmentStatus: status,
      };
      const self = this;
      axios
        .put(Page.API_BASE + "/" + param.id + "/equipment-status", param)
        .then(function (response) {
          self.resetAfterUpdate();
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    paging(pageNumber, addedParam) {
      var self = this;
      self.requestParam.page = pageNumber == undefined ? 1 : pageNumber;
      const page = pageNumber == undefined ? 1 : pageNumber;
      const payload = {
        filterCode: self.filterCode,
        query: this.requestParam.query,
        tabName: this.currentActiveTab.name
          ? this.currentActiveTab.name
          : "All",
        page: page,
        sort: this.requestParam.sort,
        size: PAGE_SIZE,
        equipmentStatus: this.requestParam.equipmentStatus,
        connectionStatus: this.requestParam.connectionStatusList,
      };

      let objectId = Common.getParameter("objectId");
      self.requestParam.id = objectId;

      let requestParamConvert;
      if (this.requestParam.terminalStatusList.length <= 0) {
        requestParamConvert = {
          id: this.requestParam.id,
          tabId: this.requestParam.tabId,
          pageType: this.requestParam.pageType,
          query: this.requestParam.query,
          status: this.requestParam.status,
          sort: this.requestParam.sort,
          page: this.requestParam.page,
          isTerminalScreen: true,
          connectionStatusList: this.requestParam.connectionStatusList,
          equipmentStatus: this.requestParam.equipmentStatus,
        };
      } else {
        requestParamConvert = this.requestParam;
      }

      if (self.requestParam.connectionStatusList === "ALL") {
        requestParamConvert.connectionStatusList = "";
      }

      if (self.requestParam.equipmentStatus === "ALL") {
        requestParamConvert.equipmentStatus = "";
      }
      if (addedParam?.areaIdList?.length > 0) {
        requestParamConvert = { ...requestParamConvert, ...addedParam };
      }
      var param = Common.param(requestParamConvert);
      //console.log('param-----============', param)
      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      const newUrl = NEW_TABLE_URL.TERMINAL;

      this.cancelToken = axios.CancelToken.source();
      const newParam = Common.param(payload);
      axios
        .get("/api/terminals?" + param, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          console.log("responseresponse: ", response.data.content);
          self.total = response.data.totalElements;
          const currentTabIndex = self.listSelectedTabs.findIndex(
            (item) => item.id === self.currentActiveTab.id
          );
          if (currentTabIndex >= 0) {
            self.listSelectedTabs[currentTabIndex].totalItem =
              response.data.totalElements;
          }
          self.results = response.data.content.map((value) => {
            value.numberOfCounter = value.counterCount;
            if (value.activationDate != null) {
              value.activationDate = moment
                .unix(value.activationDate)
                .format("YYYY-MM-DD HH:mm:ss");
            }
            if (value.installedAt != null) {
              value.installationDate = value.installedAt;
            }
            return value;
          });
          console.log(this.results, "-----------------results");
          self.pagination = Common.getPagingData(response.data);

          Common.handleNoResults("#app", self.results.length);
          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },

    showTerminalDetails: function (part) {
      var child = Common.vue.getChild(this.$children, "terminal-details");
      if (child != null) {
        child.showDetails(part);
      }
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
      this.resetAfterUpdate();
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
    resetAfterUpdate() {
      this.listChecked = [];
      this.listCheckedFull = [];
    },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} terminals on this current page are selected.`,
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
          title: `All ${this.total} terminals are selected.`,
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
          frameType: "TERMINAL_SETTING",
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
  },
  created() {
    this.$watch(
      () => headerVm?.resourcesFake,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.resources = Object.assign({}, this.resources, newVal);
          this.setAllColumnList();
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (newVal && Object.keys(newVal)?.length) {
          this.codes = Object.assign({}, this.codes, newVal);
          this.listOfItemsForStatus = this.codes.EquipmentStatus;
          this.listOfItemsForStatus = this.listOfItemsForStatus.filter(
            (item) => item.title != "Detached"
          );
          this.listOfItemsForStatus.forEach((item) => {
            if (item.title == "Installed") {
              item.title = "Registered";
            }
          });
        }
      },
      { immediate: true }
    );
  },
  async mounted() {
    const url = new URL(location.href);
    const urlParam = url.hash.replace("#", "").split("?")[1];
    const params = Common.parseParams(urlParam);

    // TODO: RECHECK FOR CONSISTENCE USAGE params has already been retrieved
    // RECHECK FOR getTabsByCurrentUser already call paging

    await this.getTabsByCurrentUser(true);
    // NEED RECHECK FUNCTION: paging, tab and getCurrentTabByUsers
    let addedParam = [];
    if (params?.areaIds) {
      const areaIdsParam = params.areaIds;
      const areaIdList = areaIdsParam ? areaIdsParam.split("-") : [];
      addedParam = areaIdList ? { areaIdList } : null;
    }
    this.paging(1, addedParam);
  },
};
</script>

<style scoped>
.tabview-dropdown-item {
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  display: flex;
  align-items: center;
  letter-spacing: 0px;
  color: #595959;
  height: 32px;
  padding: 0px 8px;
  cursor: pointer;
}
</style>
