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
            <div class="mb-3" style="display: none">
              <op-status
                v-if="Object.entries(resources).length"
                :resources="resources"
              ></op-status>
            </div>
            <div class="data-table-tabs d-flex justify-space-between">
              <ul
                class="nav nav-tabs"
                style="margin-bottom: -1px; width: calc(100% - 250px)"
              >
                <li
                  v-if="
                    tabItem.show ||
                    (tabItem.isDefaultTab && tabItem.name === 'All')
                  "
                  v-for="(tabItem, index) in listSelectedTabs"
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
              </ul>
              <div class="data-table-top-action" style="width: 250px">
                <customization-modal
                  :all-columns-list="allColumnList"
                  @save="saveSelectedList"
                  :resources="resources"
                  :is-more-action="true"
                ></customization-modal>
                <div>
                  <a
                    @click="Common.onChangeHref('/admin/counters/new')"
                    href="javascript:void(0)"
                    class="btn-custom btn-custom-primary animationPrimary all-time-filters btn-custom-focus"
                  >
                    <span>{{ resources["install_counter"] }}</span>
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
                          width: '420px !important',
                          transform: 'translate(-190px, -42px)',
                        }"
                        :checkbox="true"
                        :items="ListFilterItems"
                        :reset-handler="resetHandler"
                        :apply-handler="handlerForFilter"
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
                        :unit="['sensor', 'sensors']"
                        @select-all="handleSelectAll('all')"
                        @select-page="handleSelectAll('page')"
                        @deselect="handleSelectAll('deselect')"
                      ></select-all>
                    </div>
                  </th>
                  <th
                    v-for="(item, index) in allColumnList"
                    v-if="item.enabled"
                    :key="index"
                    :class="[
                      {
                        __sort: item.sortable,
                        active: currentSortType === item.sortField,
                      },
                      isDesc ? 'desc' : 'asc',
                      item.label === 'Battery' ||
                      item.label === 'OP' ||
                      item.label === 'Status' ||
                      item.label === 'Reset Status'
                        ? ''
                        : 'text-left',
                    ]"
                    @click="sortBy(item.sortField)"
                  >
                    <div
                      v-if="item.label === 'Shot Number'"
                      style="display: inline"
                    >
                      <span>{{ item.label }}</span>
                      <span
                        v-if="item.sortable"
                        style="
                          position: static;
                          display: inline-block;
                          margin-right: 4px;
                          width: 18px;
                          vertical-align: middle;
                        "
                        class="__icon-sort"
                      ></span>
                      <a-tooltip placement="bottom">
                        <template slot="title">
                          <div style="padding: 16px; font-size: 13px">
                            The shot number recorded and shown on the counter’s
                            monitor.
                          </div>
                        </template>
                        <img
                          style="width: 15px"
                          src="/images/icon/information.svg"
                          alt="error icon"
                        />
                      </a-tooltip>
                    </div>
                    <template v-else>
                      <span>{{ item.label }}</span>
                      <span v-if="item.sortable" class="__icon-sort"></span>
                    </template>
                  </th>
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
                            :unit="['sensor', 'sensors']"
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
                                  installCounter(listCheckedFull[0].id)
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
                                <li
                                  v-if="
                                    false &&
                                    listCheckedFull[0].equipmentStatus ==
                                      'DETACHED'
                                  "
                                >
                                  <div
                                    class="dropdown-item"
                                    @click.prevent="
                                      changeEquipmentStatus(
                                        listCheckedFull[0],
                                        'INSTALLED'
                                      )
                                    "
                                  >
                                    Reattach
                                  </div>
                                </li>
                                <li
                                  v-if="
                                    isUnchangeableEquipmentStatus(
                                      listCheckedFull[0]
                                    ) &&
                                    listCheckedFull[0].equipmentStatus ==
                                      'DISCARDED'
                                  "
                                >
                                  <div
                                    class="dropdown-item"
                                    @click.prevent="
                                      showDropdown = false;
                                      installCounter(listCheckedFull[0].id);
                                    "
                                  >
                                    {{ resources["reinstall"] }}
                                  </div>
                                </li>
                                <li
                                  v-for="code in codes.EquipmentStatus"
                                  v-if="
                                    isChangeableEquipmentStatus(
                                      listCheckedFull[0],
                                      code.code
                                    )
                                  "
                                >
                                  <div
                                    v-if="
                                      code.code === 'DISCARDED' &&
                                      listCheckedFull[0].equipmentStatus !=
                                        'FAILURE'
                                    "
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
                                    class="dropdown-item"
                                    v-if="
                                      code.code != 'AVAILABLE' &&
                                      code.code != 'DISCARDED' &&
                                      code.code != 'FAILURE'
                                    "
                                    @click.prevent="
                                      changeEquipmentStatus(
                                        listCheckedFull[0],
                                        code.code
                                      )
                                    "
                                  >
                                    {{ code.title }}
                                  </div>
                                </li>
                                <li
                                  v-if="
                                    listCheckedFull[0].equipmentStatus ==
                                    'AVAILABLE'
                                  "
                                >
                                  <div
                                    class="dropdown-item"
                                    @click.prevent="
                                      showDropdown = false;
                                      installCounter(listCheckedFull[0].id);
                                    "
                                  >
                                    <span>{{ resources["match"] }}</span>
                                  </div>
                                </li>
                                <li
                                  v-for="code in codes.EquipmentStatus"
                                  v-if="
                                    isChangeableEquipmentStatus(
                                      listCheckedFull[0],
                                      code.code
                                    )
                                  "
                                >
                                  <div
                                    v-if="
                                      code.code === 'DISCARDED' &&
                                      listCheckedFull[0].equipmentStatus ===
                                        'FAILURE'
                                    "
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
                                <li
                                  v-for="code in codes.EquipmentStatus"
                                  v-if="
                                    isChangeableEquipmentStatus(
                                      listCheckedFull[0],
                                      code.code
                                    )
                                  "
                                >
                                  <div
                                    class="dropdown-item"
                                    v-if="code.code == 'AVAILABLE'"
                                    @click.prevent="
                                      changeEquipmentStatus(
                                        listCheckedFull[0],
                                        code.code
                                      )
                                    "
                                  >
                                    {{ resources["unmatch"] }}
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
                                </li>
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
                          v-if="
                            listChecked.length == 1 &&
                            requestParam.status != 'DELETED' &&
                            renderKey
                          "
                          class="action-item"
                          v-click-outside="closeShowResetDropDown"
                        >
                          <div class="change-status-dropdown drowdown d-inline">
                            <template
                              v-if="checkShowReset('RESET', listCheckedFull[0])"
                            >
                              <div
                                title="Reset"
                                class="change-status"
                                @click="
                                  handleClickReset('RESET', listCheckedFull[0])
                                "
                              >
                                <span>Reset</span>
                                <img
                                  src="/images/icon/reset-gray.svg"
                                  width="12"
                                  height="12"
                                />
                              </div>
                            </template>
                            <template v-else>
                              <a-dropdown
                                v-model="showResetDropdown"
                                :trigger="['click']"
                              >
                                <a-menu
                                  slot="overlay"
                                  style="transform: translate(-12px, 0px)"
                                >
                                  <a-menu-item
                                    @click="
                                      handleClickReset(
                                        'EDIT_RESET',
                                        listCheckedFull[0]
                                      )
                                    "
                                    v-if="
                                      checkShowReset(
                                        'EDIT_RESET',
                                        listCheckedFull[0]
                                      )
                                    "
                                    v-text="resources['edit_reset']"
                                  ></a-menu-item>
                                  <a-menu-item
                                    @click="
                                      handleClickReset(
                                        'CANCEL_RESET',
                                        listCheckedFull[0]
                                      )
                                    "
                                    v-if="
                                      checkShowReset(
                                        'CANCEL_RESET',
                                        listCheckedFull[0]
                                      )
                                    "
                                    v-text="resources['cancel_reset']"
                                  ></a-menu-item>
                                </a-menu>
                                <div
                                  title="Reset"
                                  class="change-status"
                                  @click="
                                    showResetDropdown = !showResetDropdown
                                  "
                                >
                                  <span>Reset</span>
                                  <img
                                    src="/images/icon/reset-gray.svg"
                                    width="12"
                                    height="12"
                                  />
                                </div>
                              </a-dropdown>
                            </template>
                          </div>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            !currentActiveTab.isDefaultTab
                          "
                          class="action-item"
                        >
                          <move-to-action
                            :type-view="'counter'"
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
                        <div
                          v-if="
                            currentUser?.company.companyCode?.toLowerCase() ==
                              'emoldino' &&
                            listChecked.length >= 1 &&
                            requestParam.status !== 'disabled' &&
                            requestParam.status != 'DELETED'
                          "
                          class="action-item"
                          v-click-outside="closeShowDropDownTerm"
                        >
                          <div class="change-status-dropdown drowdown d-inline">
                            <template>
                              <div
                                title="Change status"
                                class="change-status"
                                @click="showDropdownTerm = !showDropdownTerm"
                              >
                                <span>{{
                                  resources["subscription_term"]
                                }}</span>
                                <i class="icon-action subscription-term"></i>
                              </div>
                              <ul
                                class="dropdown-menu"
                                :class="[showDropdownTerm ? 'show' : '']"
                              >
                                <li
                                  v-for="item in listTerms"
                                  :key="item.value"
                                  v-if="item.value != null"
                                >
                                  <div
                                    class="dropdown-item"
                                    @click.prevent="
                                      showDropdownTerm = false;
                                      subscriptionTermBatch(item.value);
                                    "
                                  >
                                    {{ item.title }}
                                  </div>
                                </li>
                              </ul>
                            </template>
                          </div>
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
                  <template
                    v-for="(column, columnIndex) in allColumnList"
                    v-if="column.enabled"
                  >
                    <td
                      v-if="column.field === 'equipmentCode'"
                      class="text-left"
                    >
                      <div>
                        <a
                          class="font-size-14"
                          href="#"
                          @click.prevent="showCounterDetails(result)"
                          >{{ result.equipmentCode }}</a
                        >
                      </div>

                      <div class="small text-muted font-size-11-2">
                        <span>{{ resources["updated_at"] }}</span>
                        {{ formatToDate(result.updatedAt) }}
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'mold.equipmentCode'"
                    >
                      <div v-if="result.mold != null">
                        {{ result.mold.equipmentCode }}
                      </div>
                    </td>
                    <td
                      v-else-if="column.field === 'company'"
                      class="company-cell text-left"
                    >
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
                      class="text-left"
                      v-else-if="column.field === 'shotCount'"
                    >
                      <span>{{ formatNumber(result.shotCount) }}</span>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="titleCase text-center"
                      v-else-if="column.field === 'batteryStatus'"
                    >
                      <span>{{ result.batteryStatus }}</span>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="op-status-td text-center"
                      v-else-if="column.field === 'operatingStatus'"
                    >
                      <span
                        v-if="result.operatingStatus == 'WORKING'"
                        class="op-active label label-success titleCase"
                      ></span>
                      <span
                        v-if="result.operatingStatus == 'IDLE'"
                        class="op-active label label-warning"
                      ></span>
                      <span
                        v-if="result.operatingStatus == 'NOT_WORKING'"
                        class="op-active label label-inactive"
                      ></span>
                      <span
                        v-if="result.operatingStatus == 'DISCONNECTED'"
                        class="op-active label label-danger"
                      ></span>
                    </td>
                    <td
                      class="titleCase text-center"
                      style="vertical-align: middle !important"
                      v-else-if="column.field === 'presetStatus'"
                    >
                      <span
                        v-if="result.presetStatus == 'READY'"
                        class="label label-danger"
                        >{{ resources["in_progress"] }}</span
                      >
                    </td>

                    <td
                      class="titleCase text-left"
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
                      class="titleCase text-left"
                      v-else-if="column.field === 'subscriptionExpiry'"
                    >
                      <span class="font-size-14">
                        {{ formatToDate(result.subscriptionExpiry) }}
                      </span>
                    </td>
                    <td
                      class="text-left tooling-status"
                      v-else-if="column.field === 'toolingStatus'"
                    >
                      <a-tooltip placement="bottomLeft">
                        <template slot="title">
                          <div
                            class="tooltip-label"
                            v-text="getTooltipMoldStatus(result.moldStatus)"
                          ></div>
                        </template>
                        <span
                          v-if="result.moldStatus === 'WORKING'"
                          class="op-active label label-working"
                        ></span>
                        <span
                          v-if="result.moldStatus === 'IDLE'"
                          class="op-active label label-idle"
                        ></span>
                        <span
                          v-if="result.moldStatus === 'NOT_WORKING'"
                          class="op-active label label-not-working"
                        ></span>
                        <span
                          v-if="result.moldStatus === 'SENSOR_OFFLINE'"
                          class="op-active label label-sensor-offline"
                        ></span>
                        <span
                          v-if="result.moldStatus === 'SENSOR_DETACHED'"
                          class="op-active label label-sensor-detached"
                        ></span>
                        <span
                          v-if="result.moldStatus === 'NO_SENSOR'"
                          class="op-active label label-no-sensor"
                        ></span>
                        <span
                          v-if="result.moldStatus === 'DISABLED'"
                          class="op-active label label-disabled"
                        ></span>
                        <span
                          v-if="result.moldStatus === 'ON_STANDBY'"
                          class="op-active label label-on-standby"
                        ></span>
                        <span
                          class="mold-status-text"
                          v-text="getMoldStatusText(result.moldStatus)"
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
                            v-text="
                              getTooltipCounterStatus(result.counterStatus)
                            "
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
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                      v-else-if="column.field === 'equipmentStatus'"
                    >
                      <span v-if="result.equipmentStatus == 'INSTALLED'"
                        >MATCHED</span
                      >
                      <span v-if="result.equipmentStatus == 'AVAILABLE'"
                        >UNMATCHED</span
                      >
                      <span
                        v-else-if="
                          result.equipmentStatus != 'AVAILABLE' &&
                          result.equipmentStatus != 'INSTALLED'
                        "
                        >{{ result.equipmentStatus }}</span
                      >
                    </td>
                    <td
                      :class="{
                        'text-left': !['equipmentStatus'].includes(
                          column.field
                        ),
                      }"
                      class="titleCase"
                      v-else
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
    <counter-details :resources="resources"></counter-details>
    <sensor-detail-modal
      v-if="Object.entries(resources).length"
      :resources="resources"
    ></sensor-detail-modal>
    <revision-history :resources="resources"></revision-history>
    <system-note
      ref="system-note"
      system-note-function="COUNTER_SETTING"
      :resources="resources"
    ></system-note>
    <reset-drawer
      :resources="resources"
      :reload-page="reloadPage"
    ></reset-drawer>
    <cancel-popup
      :resources="resources"
      :reload-page="reloadPage"
      :resetdata="resetData"
    ></cancel-popup>
    <action-bar-feature feature="ACTION_BAR"></action-bar-feature>
    <creator-work-order-dialog
      :resources="resources"
      :enable-asset-selection="false"
      :is-show="workorderModalVisible"
      :asset-selected="sensorSelectedToCreateWorkorder"
      :modal-type="'CREATE'"
      :is-sensor-asset-selected="true"
      @close="closeCreateWorkOrder"
    ></creator-work-order-dialog>
    <manage-tab-view
      :resources="resources"
      :object-type="'COUNTER'"
      @open-edit="openEdit"
      @open-duplicate="openDuplicate"
      @move-success="getTabsByCurrentUser"
      :trigger-update="triggerUpdateManageTab"
    ></manage-tab-view>
    <tab-view-modal
      :resources="resources"
      :object-type="'COUNTER'"
      @show-counter-details="showCounterDetails"
      @show-company-details-by-id="showCompanyDetailsById"
      :all-tooling-list="allToolingList"
      :page-type="pageType"
      @edit-success="editSuccess"
      @create-success="createSuccess"
    ></tab-view-modal>
    <counter-details :resources="resources"></counter-details>
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
var Page = Common.getPage("counters");
const PAGE_SIZE = 20;
const NEW_TABLE_URL = Common.getNewTableUrl();
module.exports = {
  components: {
    "company-details": httpVueLoader("/components/company-details.vue"),
    "counter-details": httpVueLoader("/components/counter-details.vue"),
    "sensor-detail-modal": httpVueLoader(
      "/components/counter/sensor-detail-modal.vue"
    ),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "op-status": httpVueLoader("/components/configuration/OpStatus.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "reset-drawer": httpVueLoader("/components/counter/ResetDrawer.vue"),
    "cancel-popup": httpVueLoader("/components/counter/CancelPopup.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "creator-work-order-dialog": httpVueLoader(
      "/components/work-order/creator/creator-work-order-dialog.vue"
    ),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-counter-modal.vue"),
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
      results: [],
      ListFilterItems: [
        {
          id: "OP",
          title: "By Tooling Status",
          childrens: [
            { title: "On Standby", code: "ON_STANDBY" },
            { title: "In Production", code: "IN_PRODUCTION" },
            { title: "Idle", code: "IDLE" },
            { title: "Inactive", code: "NOT_WORKING" },
            { title: "Sensor Offline", code: "SENSOR_OFFLINE" },
            { title: "Sensor Detached", code: "SENSOR_DETACHED" },
            { title: "No Sensor", code: "NO_SENSOR" },
          ],
        },
        {
          id: "status",
          // title:'Status',
          title: "By Sensor Status",
          childrens: [
            { code: "INSTALLED", title: "Installed" },
            { code: "NOT_INSTALLED", title: "Not Installed" },
            { code: "DETACHED", title: "Detached" },
          ],
        },
      ],
      listOfItemsForStatus: [
        {
          code: "IN_PRODUCTION",
          title: "In Production",
          description: "",
          enabled: true,
        },
        {
          code: "IDLE",
          title: "Idle",
          description: "",
          enabled: true,
        },
        {
          code: "NOT_WORKING",
          title: "Inactive",
          description: "",
          enabled: true,
        },
        {
          code: "SENSOR_OFFLINE",
          title: "Sensor Offline",
          description: "",
          enabled: true,
        },
        {
          code: "SENSOR_DETACHED",
          title: "Sensor Detached",
          description: "",
          enabled: true,
        },
      ],
      listOfItemsForFilter: [{ title: "By Status" }],
      showFilter: false,
      dropDownType: "main",
      showDrowdown: null,
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        status: "all",
        sort: "id,desc",
        page: 1,
        id: "",
        equipmentStatuses: [],
        operatingStatus: "",
        equipmentStatus: "",
        moldStatusList: [],
        counterStatusList: [],
        pageType: "COUNTER_SETTING",
        tabId: "",
      },

      firstCompare: "eq",
      secondCompare: "and",
      thirdCompare: "eq",
      firstCaviti: "",
      secondCaviti: "",
      toolingId: "",
      partId: "",

      codes: {},
      sortType: {
        COUNTER_ID: "equipmentCode",
        TOOLING_ID: "mold.equipmentCode",
        COMPANY: "location.company.name",
        LOCATION: "location.name",
        LAST_SHOT: "shotCount",
        BATTERY: "batteryStatus",
        OP: "operatingStatus",
        STATUS: "equipmentStatus",
      },
      pageType: "COUNTER_SETTING",
      allColumnList: [],
      currentSortType: "id",
      isDesc: true,
      showDropdown: false,
      showDropdownTerm: false,
      showResetDropdown: false,
      listAllIds: [],
      listChecked: [],
      listCheckedFull: [],
      mapCheckedItem: {},
      ACTION: {
        RESET: "RESET",
        EDIT_RESET: "EDIT_RESET",
        CANCEL_RESET: "CANCEL_RESET",
      },
      renderKey: true,
      cancelToken: undefined,
      workorderModalVisible: false,
      assetSelected: null,
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
      listTerms: [
        { title: "1 Year", value: 1 },
        { title: "2 Years", value: 2 },
        { title: "3 Years", value: 3 },
        { title: "4 Years", value: 4 },
        { title: "6 Years", value: 6 },
        { title: "7 Years", value: 7 },
        { title: "8 Years", value: 8 },
        { title: "9 Years", value: 9 },
        { title: "10 Years", value: 10 },
      ],
      currentUser: null,
      visibleMove: false,
      isFilterChanging: false,
      defaultStatusListLength: 0,
      sensorSelectedToCreateWorkorder: [],
    };
  },
  computed: {
    isAll() {
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
      axios.post("/api/tab-table/save-tab-table-counter", params).then(() => {
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
        .get("/api/tab-table/by-current-user?objectType=COUNTER")
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
        case "WORKING":
          return this.ruleOpStatus.inProduct;
        case "IDLE":
          return this.ruleOpStatus.idle;
        case "NOT_WORKING":
          return this.ruleOpStatus.inactive;
        case "SENSOR_OFFLINE":
          return this.ruleOpStatus.sensorOffline;
        case "SENSOR_DETACHED":
          return this.ruleOpStatus.sensorDetached;
        case "NO_SENSOR":
          return this.ruleOpStatus.noSensor;
        case "DISABLED":
          return this.ruleOpStatus.disabled;
        case "ON_STANDBY":
          return this.ruleOpStatus.onStandby;
        default:
          return "";
      }
    },
    getOpConfig() {
      Common.getCurrentOpConfig().then((data) => {
        if (data) {
          const _inProductStatus = data.filter(
            (item) => item.operatingStatus === "WORKING"
          )[0];
          const _idleStatus = data.filter(
            (item) => item.operatingStatus === "IDLE"
          )[0];
          const _inActiveStatus = data.filter(
            (item) => item.operatingStatus === "NOT_WORKING"
          )[0];
          const _disconnectStatus = data.filter(
            (item) => item.operatingStatus === "DISCONNECTED"
          )[0];

          this.ruleOpStatus.inProduct =
            "There has been a shot made within the last " +
            _inProductStatus.time +
            this.getTimeUnit(_inProductStatus.timeUnit);
          this.ruleOpStatus.idle =
            "There has been a shot made after " +
            _inProductStatus.time +
            this.getTimeUnit(_inProductStatus.timeUnit) +
            " but within " +
            _idleStatus.time +
            this.getTimeUnit(_idleStatus.timeUnit);
          this.ruleOpStatus.inactive = _inActiveStatus
            ? "There is no shot made within " +
              _inActiveStatus.time +
              this.getTimeUnit(_inActiveStatus.timeUnit)
            : "";
          this.ruleOpStatus.sensorOffline =
            "There is no signal from the sensor within " +
            _disconnectStatus.time +
            this.getTimeUnit(_disconnectStatus.timeUnit);
        }
      });
    },
    getTimeUnit(timeUnit) {
      return timeUnit === "HOURS" ? " hour(s) " : " day(s) ";
    },
    getMoldStatusText(status) {
      switch (status) {
        case "WORKING":
          return "In Production";
        case "IDLE":
          return "Idle";
        case "NOT_WORKING":
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
      }
    },
    showCreateWorkorder() {
      console.log("this.listCheckedFull[0]", this.listCheckedFull[0]);
      this.assetSelected = this.listCheckedFull[0].id;
      this.sensorSelectedToCreateWorkorder = [
        {
          ...this.listCheckedFull[0],
          checked: true,
          assetView: "counter",
          title: this.listCheckedFull[0]?.equipmentCode,
        },
      ];
      this.workorderModalVisible = true;
    },
    closeCreateWorkOrder() {
      this.workorderModalVisible = false;
      this.assetSelected = null;
    },
    resetHandler() {
      this.ListFilterItems = [
        {
          id: "OP",
          title: "By Tooling Status",
          childrens: [
            { title: "On Standby", code: "ON_STANDBY", checked: true },
            { title: "In Production", code: "IN_PRODUCTION", checked: true },
            { title: "Idle", code: "IDLE", checked: true },
            { title: "Inactive", code: "NOT_WORKING", checked: true },
            { title: "Sensor Offline", code: "SENSOR_OFFLINE", checked: true },
            {
              title: "Sensor Detached",
              code: "SENSOR_DETACHED",
              checked: true,
            },
            { title: "No Sensor", code: "NO_SENSOR", checked: true },
          ],
        },
        {
          id: "status",
          // title:'Status',
          title: "By Sensor Status",
          childrens: [
            { code: "INSTALLED", title: "Installed", checked: true },
            { code: "NOT_INSTALLED", title: "Not Installed", checked: true },
            { code: "DETACHED", title: "Detached", checked: true },
          ],
        },
      ];
      this.isFilterChanging = false;
    },
    openExtendedDropdown(item) {
      this.dropDownType = item.title;
      // console.log(item)
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
      console.log("handlerForFilter ", items);
      const _moldStatusList = items[0].childrens.map((item) => item.code);
      const _counterStatusList = items[1].childrens.map((item) => item.code);
      this.requestParam.moldStatusList = _moldStatusList;
      this.requestParam.counterStatusList = _counterStatusList;
      const currentStatusListLength =
        this.requestParam.moldStatusList.length +
        this.requestParam.counterStatusList.length;
      if (this.defaultStatusListLength > currentStatusListLength) {
        this.isFilterChanging = true;
      } else {
        this.isFilterChanging = false;
      }
      console.log(
        "check this",
        this.defaultStatusListLength,
        currentStatusListLength,
        this.requestParam
      );
      this.search();
    },

    showCompanyDetailsById: function (company) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(company);
      }
    },

    resetData() {
      this.paging(this.requestParam.page);
      this.renderKey = false;
      let listCheck = this.listChecked;
      console.log(this.listChecked);
      setTimeout(() => {
        this.listChecked = [];
        setTimeout(() => {
          this.listChecked = listCheck;
          console.log(this.listChecked);
        }, 1);
      }, 1);
      setTimeout(() => {
        this.renderKey = true;
      }, 1);
    },
    handleClickReset(action, item) {
      if (item) {
        let tabType = null;
        let itemReset = null;
        switch (action) {
          case this.ACTION.RESET:
          case this.ACTION.EDIT_RESET:
            tabType =
              item.mold && item.equipmentStatus != "AVAILABLE"
                ? "mold"
                : "counter";
            itemReset = tabType == "counter" ? item : item.mold;
            this.onShowResetModal(itemReset, tabType);
            break;
          case this.ACTION.CANCEL_RESET:
            //todo: call show popup action
            tabType =
              item.mold && item.equipmentStatus != "AVAILABLE"
                ? "mold"
                : "counter";
            itemReset = tabType == "counter" ? item : item.mold;
            this.onShowCancelPopup(itemReset, tabType);
            break;
        }
      }
    },
    checkShowReset(action, item) {
      if (item) {
        switch (action) {
          case this.ACTION.RESET:
            if (item.presetStatus == "READY") return false;
            return true;
          case this.ACTION.EDIT_RESET:
            if (item.presetStatus == "READY") return true;
            return false;
          case this.ACTION.CANCEL_RESET:
            if (item.presetStatus == "READY") return true;
            return false;
        }
      }
      return false;
    },
    onShowResetModal(item, tabType) {
      var child = Common.vue.getChild(this.$children, "reset-drawer");
      if (child != null) {
        child.onShow(item, tabType);
      }
    },
    onShowCancelPopup(item, tabType) {
      var child = Common.vue.getChild(this.$children, "cancel-popup");
      if (child != null) {
        child.onShow(item, tabType);
      }
    },
    closeShowResetDropDown() {
      this.showResetDropdown = false;
    },
    reloadPage() {
      console.log("reloadPage");
      this.paging(this.requestParam.page);
    },
    closeShowDropDown() {
      this.showDropdown = false;
    },
    closeShowDropDownTerm() {
      this.showDropdownTerm = false;
    },
    checkSelect: function (id) {
      const findIndex = this.listChecked.findIndex((value) => {
        return value == id;
      });

      return findIndex !== -1;
    },
    log: function (s) {
      console.log("s", s);
    },
    convertDate: function (s) {
      return s?.slice(0, 10);
    },

    convertTimeString: function (s) {
      return s?.slice(10);
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
      window.location.href = `/admin/companies/${this.listChecked[0]}`;
    },
    setAllColumnList() {
      this.allColumnList = [
        {
          label: this.resources["counter_id"],
          field: "equipmentCode",
          default: true,
          mandatory: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["tooling_id"],
          field: "mold.equipmentCode",
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
        {
          label: this.resources["shot_number"],
          field: "shotCount",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["battery"],
          field: "batteryStatus",
          default: true,
          sortable: true,
        },
        // {label: this.resources['op'], field: 'operatingStatus', default: true, sortable: true, defaultSelected: true, defaultPosition: 5},
        // {label: this.resources['status'], field: 'equipmentStatus', default: true, sortable: true, defaultSelected: true, defaultPosition: 6},
        {
          label: this.resources["tooling_status"],
          field: "toolingStatus",
          sortable: true,
          sortField: "toolingStatus",
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["sensor_status"],
          field: "sensorStatus",
          sortable: true,
          sortField: "sensorStatus",
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: this.resources["reset_status"],
          field: "presetStatus",
          default: false,
          sortable: true,
        },
        {
          label: this.resources["activation_date"],
          field: "activationDate",
          sortable: true,
          sortField: "activatedAt",
        },
        {
          label: this.resources["installation_date"],
          field: "installedAt",
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
          label: this.resources["active_period"],
          field: "activePeriod",
          sortable: false,
          sortField: "activePeriod",
        },
        {
          label: this.resources["subscription_status"],
          field: "subscriptionStatus",
          sortable: false,
          sortField: "subscriptionStatus",
        },
        {
          label: this.resources["subscription_expiry"],
          field: "subscriptionExpiry",
          sortable: true,
          sortField: "subscriptionExpiry",
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
    handleResetColumnsListSelected: function () {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
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
    showRevisionHistory: function (id, equipmentStatus) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "COUNTER", equipmentStatus);
      }
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
    tab(tab) {
      this.currentActiveTab = tab;
      this.total = null;
      this.requestParam.tabId = "";
      this.tabCurrent = tab.name;
      this.listChecked = [];
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
        this.paging(1);
      }
      this.changeShow(null);
    },
    search(page) {
      this.paging(1);
    },

    enable() {
      // if (user.length <= 1) {
      //   user[0].enabled = true;
      //   this.save(user[0]);
      // } else {
      //   user.forEach(function (item) {
      //     item.enabled = true;
      //   });
      //   this.saveBatch(user, true);
      // }
      const listIds =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      this.saveBatch(listIds, true);
    },
    disable() {
      // if (user.length <= 1) {
      //   user[0].enabled = false;
      //   this.save(user[0]);
      // } else {
      //   user.forEach(function (item) {
      //     item.enabled = false;
      //   });
      //   this.saveBatch(user, false);
      // }
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
        .post("/api/counters/change-status-in-batch", param)
        .then(function (response) {
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
    save: function (location) {
      const self = this;
      var param = {
        id: location.id,
        enabled: location.enabled,
      };
      axios
        .put(Page.API_BASE + "/" + location.id + "/enable", param)
        .then(function (response) {
          self.showDrowdown = null;
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
    showCounterDetails: function (data) {
      var child = Common.vue.getChild(this.$children, "sensor-detail-modal");
      if (child != null) {
        child.showDetails(data);
      }
    },

    changeEquipmentStatus: function (data, status) {
      this.showDropdown = false;
      var param = {
        id: data.id,
        equipmentStatus: status,
      };
      const self = this;
      axios
        .put(Page.API_BASE + "/" + param.id + "/equipment-status", param)
        .then(function (response) {
          self.paging(1);
        })
        .catch(function (error) {
          console.log(error);
        });
    },

    paging: function (pageNumber) {
      console.log("paging", pageNumber);
      var self = this;
      const page = pageNumber == undefined ? 1 : pageNumber;
      const payload = {
        filterCode: self.filterCode,
        query: this.requestParam.query,
        tabName: this.currentActiveTab.name
          ? this.currentActiveTab.name
          : "All",
        moldStatusList: this.requestParam.moldStatusList,
        counterStatusList: this.requestParam.counterStatusList,
        page: page,
        sort: this.requestParam.sort,
        size: PAGE_SIZE,
      };
      self.requestParam.page = page;
      let objectId = Common.getParameter("objectId");
      self.requestParam.id = objectId;
      let requestParamConvert;
      if (this.requestParam.equipmentStatuses.length <= 0) {
        requestParamConvert = {
          id: this.requestParam.id,
          tabId: this.requestParam.tabId,
          pageType: this.requestParam.pageType,
          query: this.requestParam.query,
          status: this.requestParam.status,
          sort: this.requestParam.sort,
          page: this.requestParam.page,
          operatingStatus: this.requestParam.operatingStatus,
          equipmentStatus: this.requestParam.equipmentStatus,
          moldStatusList: this.requestParam.moldStatusList,
          counterStatusList: this.requestParam.counterStatusList,
        };
      } else {
        requestParamConvert = this.requestParam;
      }
      if (self.requestParam.operatingStatus === "ALL") {
        requestParamConvert.operatingStatus = "";
      }

      if (self.requestParam.equipmentStatus === "ALL") {
        requestParamConvert.equipmentStatus = "";
      }
      // checking is All
      this.listCheckedFull = [];
      this.listChecked = [];
      var param = Common.param(requestParamConvert);

      const {
        firstCompare,
        secondCompare,
        thirdCompare,
        firstCaviti,
        secondCaviti,
        toolingId,
        partId,
      } = self;
      let queryParams = "";
      if (toolingId || partId) {
        queryParams = `&where=tool${toolingId},part${partId}`;
      } else {
        queryParams = `&where=op${secondCompare},${firstCompare}${firstCaviti},${thirdCompare}${secondCaviti}`;
      }
      param = param + queryParams;

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request");
      }

      this.cancelToken = axios.CancelToken.source();

      const newParam = Common.param(payload);
      const newUrl = NEW_TABLE_URL.COUNTER;
      axios
        .get(`${newUrl}?${newParam}`, {
          cancelToken: this.cancelToken.token,
        })
        .then(function (response) {
          self.total = response.data.totalElements;
          const currentTabIndex = self.listSelectedTabs.findIndex(
            (item) => item.id === self.currentActiveTab.id
          );
          if (currentTabIndex >= 0) {
            self.listSelectedTabs[currentTabIndex].totalItem =
              response.data.totalElements;
          }
          self.results = response.data.content.map((value) => {
            if (value.activatedAt != null) {
              value.activationDate = moment
                .unix(value.activatedAt)
                .format("YYYY-MM-DD HH:mm:ss");
            }
            return value;
          });
          self.pagination = Common.getPagingData(response.data);

          self.setResultObject();
          Common.handleNoResults("#app", self.results.length);

          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    setResultObject: function () {
      for (var i = 0; i < this.results.length; i++) {
        if (typeof this.results[i] !== "object") {
          var counterId = this.results[i];
          for (var j = 0; j < this.results.length; j++) {
            if (typeof this.results[j] !== "object") {
              continue;
            }

            var counter = this.results[j];
            if (counterId == counter.id) {
              this.results[i] = counter;
              break;
            }

            if (
              counter.mold != null &&
              counter.mold.part != null &&
              counter.mold.part.molds != null
            ) {
              this.results[i] = this.findCounter(
                counter.mold.part.molds,
                counterId
              );

              if (typeof this.results[i] === "object") {
                break;
              }
            }
          }
        }
      }

      for (var i = 0; i < this.results.length; i++) {
        if (typeof this.results[i].mold !== "object") {
          var moldId = this.results[i].mold;
          for (var j = 0; j < this.results.length; j++) {
            if (
              typeof this.results[j].mold !== "object" ||
              this.results[j].mold == null
            ) {
              continue;
            }

            var mold = this.results[j].mold;
            if (moldId == mold.id) {
              this.results[i].mold = mold;
              break;
            }

            if (mold.part != null && mold.part.molds != null) {
              this.results[i].mold = this.findMold(mold.part.molds, moldId);

              if (typeof this.results[i].mold === "object") {
                break;
              }
            }
          }
        }
      }
    },

    findCounter: function (molds, counterId) {
      for (var k = 0; k < molds.length; k++) {
        var mold = molds[k];

        if (typeof mold !== "object" || typeof mold.counter !== "object") {
          continue;
        }

        //console.log(">> " + counterId);
        if (counterId == mold.counter.id) {
          return mold.counter;
          break;
        }

        // parts에서 검색
        if (mold.part != null && mold.part.molds != null) {
          var findCounter = this.findCounter(mold.part.molds, counterId);
          if (typeof findCounter === "object") {
            return findCounter;
            break;
          }
        }
      }
    },

    findMold: function (results, moldId) {
      for (var j = 0; j < results.length; j++) {
        if (typeof results[j] !== "object") {
          continue;
        }

        var mold = results[j];
        if (moldId == mold.id) {
          return mold;
          break;
        }

        // parts에서 검색
        if (mold.part != null && mold.part.molds != null) {
          var findMold = this.findMold(mold.part.molds, moldId);
          if (typeof findMold === "object") {
            return findMold;
            break;
          }
        }
      }
    },

    isUnchangeableEquipmentStatus: function (result) {
      var opStatus = result.operatingStatus;
      var equipmentStatus = result.equipmentStatus;

      if (
        equipmentStatus == "DISCARDED" ||
        (opStatus == "ACTIVE" && equipmentStatus == "INSTALLED")
      ) {
        return true;
      }

      return false;
    },
    isChangeableEquipmentStatus: function (result, status) {
      var opStatus = result.operatingStatus;
      var equipmentStatus = result.equipmentStatus;

      if ("INSTALLED" == status) {
        return false;
      }

      if ("ACTIVE" == opStatus) {
        return false;
      }

      // 상태코드
      if (equipmentStatus == status || "DISCARDED" == equipmentStatus) {
        return false;
      }
      if (
        status == "DETACHED"
        // && ("INSTALLED" != equipmentStatus)
      ) {
        return false;
      }

      return true;
    },
    sortBy: function (type) {
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
    sort: function () {
      this.paging(1);
    },
    installCounter: function (id) {
      location.href = "/admin/counters/new?" + id;
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
    handleClick: function (item) {
      // this.searchProductionData(this.partIds, item.key, this.filterType);
      // this.selectedItem = item.key
      // this.displayCaret = true;
    },
    subscriptionTermBatch(value) {
      const idArr =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      var param = {
        ids: idArr,
        subscriptionTerm: value,
      };
      let self = this;
      axios
        .post("/api/counters/subscription-term", param)
        .then(function (response) {
          this.reloadPage();
          let num = idArr.length;
          self.showToastAlert({
            title: "Success!",
            content: `${num} sensor${
              num > 1 ? "s'" : "'s"
            } subscription periods have been set to ${value} year${
              value > 1 ? "s" : ""
            }.`,
          });
          setTimeout(() => {
            self.closeToastAlert();
          }, 3000);
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          this.listChecked = [];
          this.showDropdownTerm = false;
        });
    },
    // async getCurrentUser() {
    //   const me = await Common.getSystem("me");
    //   this.currentUser = JSON.parse(me);
    // },
    handleSelectAll(actionType) {
      if (actionType === "page") {
        this.listAllIds = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} sensors on this current page are selected.`,
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
          title: `All ${this.total} sensors are selected.`,
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
          frameType: "COUNTER_SETTING",
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
      () => headerVm?.currentUser,
      (newVal) => {
        if (newVal) {
          this.currentUser = Object.assign({}, this.currentUser, newVal);
        }
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.systemCodes,
      (newVal) => {
        if (newVal) {
          this.codes = Object.assign({}, this.codes, newVal);
        }
      },
      { immediate: true }
    );
  },
  mounted() {
    this.defaultStatusListLength = 10;
    this.setAllColumnList();
    this.getOpConfig();
    this.getTabsByCurrentUser(true);
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
