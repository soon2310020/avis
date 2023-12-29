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
                style="margin-bottom: -1px; width: calc(100% - 235px)"
              >
                <template v-for="(tabItem, index) in listSelectedTabs">
                  <li
                    v-if="
                      tabItem.shown ||
                      (tabItem.defaultTab && tabItem.name === 'All')
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
                      <span class="badge badge-light badge-pill">
                        {{ tabItem.total }}
                      </span>
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
                </template>
              </ul>
              <div class="data-table-top-action" style="width: 235px">
                <customization-modal
                  :all-columns-list="allColumnList"
                  @save="saveSelectedList"
                  :resources="resources"
                  :is-more-action="true"
                ></customization-modal>
                <div>
                  <a-popover
                    v-model="visiblePopover"
                    placement="bottomLeft"
                    trigger="click"
                  >
                    <a
                      @click="animationOutlineDropdown()"
                      :class="{ 'outline-animation': showAnimation }"
                      href="javascript:void(0)"
                      class="btn-custom btn-custom-primary animationPrimary all-time-filters"
                    >
                      <span>{{ resources["create_machine"] }}</span>
                      <img
                        class="img-transition"
                        src="/images/icon/icon-cta-white.svg"
                        :class="{ 'caret-show': displayCaret }"
                        alt=""
                      />
                    </a>
                    <a-menu
                      slot="content"
                      class="wrapper-action-dropdown"
                      style="
                        border-right: unset !important;
                        max-height: 250px;
                        overflow: auto;
                      "
                      @click="visiblePopover = false"
                    >
                      <a-menu-item @click="handleClickCreateMachine">
                        <span>{{ resources["create_machine"] }}</span>
                      </a-menu-item>
                      <a-menu-item @click="showDrawer">
                        <div>Create Machine Property</div>
                      </a-menu-item>
                    </a-menu>
                  </a-popover>
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
              <thead id="thead-actionbar" class="custom-header-table">
                <tr
                  :class="{ invisible: listChecked.length != 0 }"
                  style="height: 57px"
                  class="header-s tr-sort"
                >
                  <th class="custom-td">
                    <div class="custom-td_header">
                      <select-all
                        :resources="resources"
                        :show="allColumnList.length > 0"
                        :checked="isAll"
                        :total="total"
                        :count="results.length"
                        :unit="['machine', 'machines']"
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
                        item.field === 'enabled' ? '' : 'text-left',
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
                            :unit="['machine', 'machines']"
                            @select-all="handleSelectAll('all')"
                            @select-page="handleSelectAll('page')"
                            @deselect="handleSelectAll('deselect')"
                          ></select-all>
                        </div>
                      </div>
                      <div class="action-bar">
                        <!-- <div
                          v-if="
                            listChecked.length > 1 && tab.name !== 'Disabled'
                          "
                        >
                          No batch action is available
                        </div> -->
                        <div
                          v-if="
                            listChecked.length == 1 &&
                            requestParam.tabName !== 'Disabled'
                          "
                          @click="goToEdit"
                          class="action-item"
                        >
                          <span>Edit</span>
                          <i class="icon-action edit-icon"></i>
                        </div>
                        <div
                          v-if="
                            listChecked.length === 1 &&
                            machineBatchActionType === 'match'
                          "
                          class="action-item"
                          @click="match()"
                        >
                          <span>Match with Tooling</span>
                          <div class="icon-action match-icon"></div>
                        </div>
                        <div
                          v-if="machineBatchActionType === 'un-match'"
                          class="action-item"
                          @click.prevent="unMatch()"
                        >
                          <span>Un-match from Tooling</span>
                          <div class="unmatch-icon"></div>
                        </div>
                        <div
                          v-if="requestParam.tabName !== 'Disabled'"
                          class="action-item"
                          @click="showSystemNoteModal()"
                        >
                          <span>Memo</span>
                          <i class="icon-action memo-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length >= 1"
                          @click="exportMachine()"
                          class="action-item"
                        >
                          <span>Export</span>
                          <i class="icon-action export-icon"></i>
                        </div>
                        <div
                          v-if="
                            listChecked.length == 1 &&
                            requestParam.tabName !== 'Disabled'
                          "
                          @click="showRevisionHistory(listChecked[0])"
                          class="action-item"
                        >
                          <span>View Edit History</span>
                          <i class="icon-action view-edit-history-icon"></i>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            requestParam.tabName !== 'Disabled'
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
                            requestParam.tabName === 'Disabled'
                          "
                          class="action-item"
                          @click.prevent="enable()"
                        >
                          <span>Enable</span>
                          <i class="icon-action enable-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length == 1"
                          class="action-item"
                          @click="showCreateWorkorder(listChecked[0])"
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
                            !currentActiveTab.defaultTab
                          "
                          class="action-item"
                        >
                          <move-to-action
                            :type-view="'machine'"
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
                    v-for="(column, columnIndex) in allColumnList"
                    v-if="column.enabled"
                  >
                    <td
                      :key="columnIndex + 'machineCode'"
                      v-if="column.field === 'machineCode'"
                      class="text-left part"
                    >
                      <div class="part">
                        <a href="#" @click.prevent="showMachineDetails(result)">
                          <a-tooltip placement="bottomLeft">
                            <template slot="title">
                              <span
                                v-if="
                                  result.machineCode &&
                                  result.machineCode.length > 20
                                "
                                >{{
                                  result.machineCode.length > 20 &&
                                  result.machineCode
                                }}</span
                              >
                            </template>
                            <span>{{
                              replaceLongtext(result.machineCode, 20)
                            }}</span>
                          </a-tooltip>
                        </a>
                      </div>
                      <div class="small text-muted font-size-11-2">
                        <span>{{ resources["registered"] }}:</span>
                        {{ result.creationDate }}
                      </div>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.field === 'mold'"
                      :key="columnIndex + 'mold'"
                    >
                      <div v-if="result.moldId !== null">
                        <a
                          href="#"
                          @click.prevent="showOnlyChart(result.moldId)"
                          style="color: #1aaa55"
                          class="mr-1 font-size-14"
                          ><i class="fa fa-bar-chart"></i
                        ></a>
                        <a
                          href="#"
                          @click.prevent="showMoldDetails(result.moldId)"
                        >
                          {{ result.moldCode }}
                        </a>
                      </div>

                      <div
                        class="small text-muted font-size-11-2"
                        v-if="result.moldId != null"
                      >
                        <span>{{ resources["updated_on"] }}</span>
                        {{ result.moldUpdatedAtDate }}
                      </div>
                    </td>

                    <td
                      :key="columnIndex + 'company'"
                      class="text-left"
                      v-else-if="column.field === 'company'"
                    >
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
                    </td>

                    <td
                      :key="columnIndex + 'location'"
                      class="text-left"
                      v-else-if="column.field === 'location'"
                    >
                      <div v-if="result.locationCode">
                        <div class="font-size-14">
                          {{ result.locationName }}
                        </div>

                        <div class="small text-muted font-size-11-2">
                          {{ result.locationCode }}
                        </div>
                      </div>
                    </td>
                    <td
                      style="vertical-align: middle !important"
                      class="text-center op-status-td"
                      v-else-if="column.field === 'enabled'"
                      :key="columnIndex + 'enabled'"
                    >
                      <span
                        v-if="result.moldId == null"
                        :class="{
                          'label label-danger': result.moldId == null,
                          'label label-success': result.moldId != null,
                        }"
                      >
                        {{ resources["unmatched"] }}
                      </span>
                      <span
                        v-else
                        :class="{
                          'label label-danger': result.moldId == null,
                          'label label-success': result.moldId != null,
                        }"
                      >
                        {{ resources["matched"] }}
                      </span>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.isCustomField"
                      :key="columnIndex + 'customField'"
                    >
                      <a-tooltip placement="bottomLeft">
                        <template slot="title">
                          <span
                            v-if="
                              customFieldValueMachine(result, column.label) &&
                              customFieldValueMachine(result, column.label)
                                .length > 20
                            "
                            >{{
                              customFieldValueMachine(result, column.label)
                            }}</span
                          >
                        </template>
                        <span>{{
                          replaceLongtext(
                            customFieldValueMachine(result, column.label),
                            20
                          )
                        }}</span>
                      </a-tooltip>
                    </td>
                    <td
                      class="text-left part"
                      v-else
                      :key="columnIndex + 'not-custom-field'"
                    >
                      <a-tooltip placement="bottomLeft">
                        <template slot="title">
                          <span
                            v-if="
                              result[column.field] &&
                              result[column.field].length > 20
                            "
                            >{{
                              result[column.field].length > 20 &&
                              result[column.field]
                            }}</span
                          >
                        </template>
                        <span>{{
                          replaceLongtext(result[column.field], 20)
                        }}</span>
                      </a-tooltip>
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
              is-type="MACHINE"
            />
          </div>
        </div>
      </div>
    </div>
    <revision-history :resources="resources"></revision-history>
    <system-note
      ref="system-note"
      system-note-function="MACHINE_SETTING"
      :resources="resources"
      :handle-submit="handleSubmitNote"
    ></system-note>
    <!-- <delete-popup
      :confirm-delete="confirmDelete"
      :is-show-warning-again="isShowWarningAgain"
      :list="total === listAllIds.length ? listAllIds : listChecked"
      :resources="resources"
      :name="'Machine'"
    ></delete-popup> -->
    <unmatch-popup
      :confirm-unmatch="confirmUnmatch"
      :list="total === listAllIds.length ? listAllIds : listChecked"
      :resources="resources"
      :name="'Machine'"
    ></unmatch-popup>
    <action-bar-feature feature="ACTION_BAR"></action-bar-feature>

    <file-previewer :back="backToMoldDetail"></file-previewer>
    <creator-work-order-dialog
      :resources="resources"
      :is-show="workorderModalVisible"
      :asset-selected="listChecked[0]"
      :modal-type="'CREATE'"
      @close="closeCreateWorkOrderModal"
    ></creator-work-order-dialog>
    <manage-tab-view
      :resources="resources"
      :object-type="'MACHINE'"
      @open-edit="openEdit"
      @open-duplicate="openDuplicate"
      @move-success="getTabsByCurrentUser"
      :trigger-update="triggerUpdateManageTab"
    ></manage-tab-view>
    <tab-view-modal
      :resources="resources"
      :object-type="'MACHINE'"
      @show-machine-details="showMachineDetails"
      @show-company-details-by-id="showCompanyDetailsById"
      :all-tooling-list="allToolingList"
      :page-type="pageType"
      @edit-success="editSuccess"
      @create-success="createSuccess"
    ></tab-view-modal>
    <machine-details :resources="resources"></machine-details>
    <company-details :resources="resources"></company-details>

    <chart-mold
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
      :user-type="userType"
    ></chart-mold>
    <mold-details
      :show-file-previewer="showFilePreviewer"
      :resources="resources"
    ></mold-details>
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
const Page = Common.getPage("machines");
const API_BASE_MCH_STP = Common.getApiBase().MACHINE;
const API_BASE_TAB_STP = Common.getApiBase().TAB;
module.exports = {
  components: {
    "company-details": httpVueLoader("/components/company-details.vue"),
    "machine-details": httpVueLoader("/components/machine-details.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "show-columns": httpVueLoader("/components/part/show-columns.vue"),
    "customization-modal": httpVueLoader("/components/customization-modal.vue"),
    "create-property-drawer": httpVueLoader(
      "/components/import-tooling/CreatePropertyDrawer.vue"
    ),
    // "delete-popup": httpVueLoader("/components/delete-machine-popup.vue"),
    "unmatch-popup": httpVueLoader("/components/unmatch-popup.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "chart-mold": httpVueLoader("/components/chart-mold/chart-mold-modal.vue"),
    "file-previewer": httpVueLoader(
      "/components/mold-detail/file-previewer.vue"
    ),
    "mold-details": httpVueLoader("/components/mold-details.vue"),
    "creator-work-order-dialog": httpVueLoader(
      "/components/work-order/creator/creator-work-order-dialog.vue"
    ),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view-new.vue"),
    "tab-view-modal": httpVueLoader(
      "/components/tab-view-machine-modal-new.vue"
    ),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action-new.vue"),
  },
  props: {
    resources: Object,
    searchText: String,
    reloadKey: [String, Number],
  },
  data() {
    return {
      isShowWarningAgain: !localStorage.getItem("dontShowWarning"),
      deletePopup: false,
      status: "all",
      visible: false,
      dropdownOpening: false,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        filterCode: "COMMON",
        query: "",
        sort: "id,desc",
        page: 1,
        size: 20,
        tabName: "",
      },
      codes: {},
      tabOptions: [],
      sortType: {
        MACHINE_ID: "machineCode",
        MOLD: "mold",
        COMPANY: "company.name",
        LOCATION: "location.name",
        MACHINE_MAKER: "machineMaker",
        MACHINE_TYPE: "machineMaker",
        MACHINE_MODEL: "machineMaker",
        STATUS: "enabled",
      },
      pageType: "MACHINE_SETTING",
      objectType: "MACHINE",
      allColumnList: [],
      currentSortType: "id",
      isDesc: true,
      showDropdown: false,
      listAllIds: [],
      listChecked: [],
      listCheckedFull: [],
      cancelToken: undefined,
      displayCaret: false,
      showAnimation: false,
      visiblePopover: false,
      matchedAble: true,
      allmatched: false,
      searchList: [],
      userType: null,
      optimalCycleTime: {},
      octs: {
        ACT: "Approved CT",
        WACT: "Weighted Average CT",
      },
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
      selectType: "",
      isStopedExport: false,
      isCallingDone: false,
      currentIntervalIds: [],
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
    machineBatchActionType() {
      if (this.listCheckedFull.length === 0) return "no-action";
      if (
        this.listCheckedFull.every((item) => item.moldId !== null) ||
        this.listCheckedFull.every((item) => item.moldId === null)
      ) {
        if (this.listCheckedFull[0].moldId) {
          return "un-match";
        } else {
          return "match";
        }
      } else {
        return "no-action";
      }
    },
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
    resources: {
      handler(newVal) {
        if (!newVal || !Object.keys(newVal).length) return;
        this.setTabOptions();
        this.setAllColumnList();
      },
      immediate: true,
    },
  },
  methods: {
    handleClickCreateMachine() {
      Common.onChangeHref("/admin/machines/new");
    },
    async handleSubmitNote(data) {
      const self = this;
      const { filterCode, query, tabName } = self.requestParam;
      let listId = [];
      if (self.selectType === "all") {
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
        selectionMode: self.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [self.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
      });
      try {
        await axios.post(`/api/common/mch-stp/note-batch?${param}`, data);
      } catch (error) {
        console.log(error);
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
        await axios.put(`${API_BASE_MCH_STP}/move-tab-items-batch?${param}`);
        this.moveSuccess({
          title: "Success!",
          content: `Your data has been moved to ${tab.name} tab`,
        });
      } catch (error) {
        console.log(error);
      }
    },
    openCustomizationModal() {
      this.visibleMoreAction = false;
      var child = Common.vue.getChild(this.$children, "customization-modal");
      if (child != null) {
        child.openModal();
      }
    },
    async showTab(tab) {
      try {
        tab.shown = true;
        if (tab.name === this.currentActiveTab.name) {
          this.setNewActiveTab();
        }
        if (tab.defaultTab) {
          await axios.put(
            `${API_BASE_TAB_STP}/MACHINE/names/${tab.name}/shown`
          );
        } else {
          await axios.put(`${API_BASE_TAB_STP}/MACHINE/${tab.id}/shown`);
        }
      } catch (error) {
        console.log("devices-terminal-new:::showTab:::error", error);
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
            `${API_BASE_TAB_STP}/MACHINE/names/${tab.name}/hidden`
          );
        } else {
          await axios.put(`${API_BASE_TAB_STP}/MACHINE/${tab.id}/hidden`);
        }
      } catch (error) {
        console.log("devices-terminal-new:::closeTab:::error", error);
      }
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
        await axios.delete(`${API_BASE_MCH_STP}/tab-items-batch?${param}`);
        this.moveSuccess({
          title: "Success!",
          content: `Your data has been removed from ${this.currentActiveTab.name} tab`,
        });
      } catch (error) {
        console.log("data-family-machine:::removeFromTab:::error", error);
      }
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
    handleToggleMoreActions() {
      this.visibleMoreAction = true;
      this.isTabView = false;
    },
    // async updateTabStatus() {
    //   let tabsChange = this.listSelectedTabs.map((tab) => {
    //     return {
    //       id: tab.id,
    //       isShow: tab.shown,
    //       deleted: tab.deleted,
    //       name: tab.name,
    //       isDefaultTab: tab.defaultTab,
    //       objectType: tab.objectType,
    //     };
    //   });
    //   axios.put("/api/tab-table", tabsChange);
    // },
    convertCodeToName(tabItem) {
      if (tabItem.defaultTab) {
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
      try {
        const { data } = await axios.get(`${API_BASE_TAB_STP}/MACHINE`);
        this.listSelectedTabs = data.content;
        console.log("getTabsByCurrentUser", this.listSelectedTabs);
        if (isCallAtMounted) {
          this.setNewActiveTab();
        }
      } catch (error) {
        console.log(
          "devices-terminal-new:::getTabsByCurrentUser:::error",
          error
        );
      }
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
    showCreateWorkorder(item) {
      console.log("item", item);
      this.workorderModalVisible = true;
    },
    closeCreateWorkOrderModal() {
      this.workorderModalVisible = false;
    },
    async showMoldDetails(moldId) {
      var child = Common.vue.getChild(this.$children, "mold-details");
      const { data } = await axios.get(`/api/molds/${moldId}`);
      const moldData = data;
      if (child != null) {
        let reasonData = { reason: "", approvedAt: "" };
        const tab = "switch-detail";
        if (moldData.dataSubmission === "DISAPPROVED") {
          axios
            .get(`/api/molds/data-submission/${moldId}/disapproval-reason`)
            .then((response) => {
              reasonData = response.data;
              // reasonData.approvedAt = reasonData.approvedAt ? moment.unix(reasonData.approvedAt).format('MMMM DD YYYY HH:mm:ss') : '-';
              reasonData.approvedAt = reasonData.approvedAt
                ? this.convertTimestampToDateWithFormat(
                    reasonData.approvedAt,
                    "MMMM DD YYYY HH:mm:ss"
                  )
                : "-";
              moldData.notificationStatus = moldData.dataSubmission;
              moldData.reason = reasonData.reason;
              moldData.approvedAt = reasonData.approvedAt;
              moldData.approvedBy = reasonData.approvedBy;
              // child.showMoldDetails(mold);
              this.showChart(moldData, tab);
            });
        } else {
          // child.showMoldDetails(mold);
          this.showChart(moldData, tab);
        }
      }
    },
    backToMoldDetail(fromPopup) {
      var child = Common.vue.getChild(
        this.$children,
        fromPopup || "chart-mold"
      );
      if (child != null) {
        child.backFromPreview();
      }
    },
    showFilePreviewer(file, fromPopup) {
      var child = Common.vue.getChild(this.$children, "file-previewer");
      if (child != null) {
        child.showFilePreviewer(file, fromPopup);
      }
    },
    showOnlyChart: async function (moldId, tab = null) {
      var child = Common.vue.getChild(this.$children, "chart-mold");
      const { data: moldData } = await axios.get(`/api/molds/${moldId}`);
      if (child != null) {
        child.showChart(
          moldData,
          "QUANTITY",
          "DAY",
          tab ? tab : "switch-graph",
          this.octs[this.optimalCycleTime.strategy]
        );
      }
    },
    showChart: async function (
      mold,
      type = null,
      dateViewType = null,
      tab,
      oct
    ) {
      if (oct === undefined || oct === null) {
        oct = this.octs[this.optimalCycleTime.strategy];
      }

      if (!type) {
        type = "QUANTITY";
      }
      if (!dateViewType) {
        dateViewType = "DAY";
      }

      var child = Common.vue.getChild(this.$children, "chart-mold");
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
      }
    },
    match() {
      window.location.href = `/admin/machines/tooling/${this.listChecked[0]}`;
    },
    unMatch() {
      var child = Common.vue.getChild(this.$children, "unmatch-popup");
      if (child != null) {
        child.showUnmatchPopup();
      }
    },
    async confirmUnmatch() {
      const { filterCode, query, tabName } = this.requestParam;
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
        await axios.put(`${API_BASE_MCH_STP}/unmatch-batch?${param}`);
        this.paging(1);
        this.listChecked = [];
        this.listCheckedFull = [];
        this.getTabsByCurrentUser();
        this.allmatched = false;
      } catch (error) {
        Common.alert("Failed to un-matched");
        console.log("confirmUnmatch:::error", error);
      }
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
    closeShowDropDown() {
      this.showDropdown = false;
    },
    confirmDelete(checked) {
      if (checked) {
        localStorage.setItem("dontShowWarning", "true");
      }
      const x = this.listChecked.map((item) => {
        return parseInt(item);
      });
      const params = {
        ids: x,
      };
      const self = this;
      self.disable(x);
      // axios
      //   .post(`/api/machines/delete-in-batch`, params)
      //   .then((res) => {
      //     $("#op-delete-popup").modal("hide");
      //     self.listChecked = [];
      //     self.getTabsByCurrentUser();
      //     self.paging(1);
      //   })
      //   .catch((e) => {
      //     console.log(e);
      //   });
    },
    // showDeletePopup() {
    //   var child = Common.vue.getChild(this.$children, "delete-popup");
    //   if (child != null) {
    //     child.showDeletePopup();
    //   }
    // },
    restoreItem() {
      const self = this;
      axios
        .post(`/api/machines/restore/${this.listChecked[0]}`)
        .then((res) => {
          self.paging(self.requestParam.page);
        })
        .finally(() => {
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        });
    },
    setTabOptions() {
      this.tabOptions = [
        {
          label: "All",
          value: "all",
        },
        {
          label: this.resources["matched"],
          value: "matched",
        },
        {
          label: this.resources["unmatched"],
          value: "unmatched",
        },
      ];
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

      var temp = this.results.filter((item) => item.id == this.listChecked[0]);
      if (temp[0] != undefined) {
        if (temp[0].moldId != null) this.matchedAble = false;
        else this.matchedAble = true;
      }

      for (index in this.listChecked) {
        var result = this.results.filter(
          (item) => item.id == this.listChecked[index]
        );
        if (result[0].moldId != null && this.matchedAble == false) {
          this.allmatched = true;
        } else {
          this.allmatched = false;
          break;
        }
      }
    },
    goToEdit() {
      window.location.href = `/admin/machines/${this.listChecked[0]}`;
    },
    showDrawer() {
      this.visible = true;
    },
    closeDrawer() {
      this.visible = false;
    },
    animationPrimary() {
      $(".animationPrimary").click(function () {
        $(this).addClass("primary-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
            $(this).removeClass("primary-animation");
          }
        );
      });
    },
    animationOutline() {
      $(".animationOutline").click(function () {
        $(this).addClass("outline-animation");
        $(this).one(
          "webkitAnimationEnd oanimationend msAnimationEnd animationend",
          function (event) {
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
      this.allColumnList = [
        {
          label: this.resources["machine_id"],
          field: "machineCode",
          default: true,
          mandatory: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 0,
        },
        {
          label: this.resources["tooling_id"],
          field: "mold",
          default: true,
          mandatory: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 1,
        },
        {
          label: this.resources["company"],
          field: "company",
          default: true,
          sortable: true,
          sortField: "company.name",
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
          label: this.resources["line"],
          field: "line",
          default: false,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 4,
        },
        {
          label: this.resources["machine_maker"],
          field: "machineMaker",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 5,
        },
        {
          label: this.resources["machine_type"],
          field: "machineType",
          default: true,
          sortable: true,
          defaultSelected: true,
          defaultPosition: 6,
        },
        {
          label: this.resources["machine_model"],
          field: "machineModel",
          default: true,
          sortable: true,
        },
        {
          label: this.resources["status"],
          field: "enabled",
          default: true,
          sortable: true,
        },
      ];
      try {
        this.resetColumnsListSelected();
        this.getCustomColumnAndInit();
      } catch (e) {
        console.log(e);
        this.resetColumnsListSelected();
        this.getColumnListSelected();
      }
    },
    showCompanyDetailsById: function (companyId) {
      var child = Common.vue.getChild(this.$children, "company-details");
      if (child != null) {
        child.showDetailsById(companyId);
      }
    },

    showLocationHistory: function (mold) {
      var child = Common.vue.getChild(this.$children, "location-history");
      if (child != null) {
        child.showLocationHistory(mold);
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
    handleResetColumnsListSelected: function () {
      this.resetColumnsListSelected();
      this.saveSelectedList();
      this.$forceUpdate();
    },
    getCustomColumnAndInit() {
      Common.getCustomColumn(this.objectType)
        .then(async (data) => {
          this.allColumnList.push(...data);
        })
        .finally(() => {
          this.resetColumnsListSelected();
          this.getColumnListSelected();
        });
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

    saveSelectedList(dataCustomize, list) {
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
        this.showDropdown = resultId;
      } else {
        this.showDropdown = null;
      }
    },
    showRevisionHistory: function (id, equipmentStatus) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "MACHINE", equipmentStatus);
      }
    },
    tab(tab) {
      this.currentActiveTab = tab;
      this.matchedAble = true;
      this.allmatched = false;
      this.requestParam.query = "";
      this.total = null;
      this.listChecked = [];
      this.requestParam.tabName = tab.name;
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.paging(1);
      this.changeShow(null);
    },
    search(page) {
      var temp = this.results.filter((item) => {
        return (
          item.moldId != null &&
          ("" + item.moldId).includes(this.requestParam.query)
        );
      });
      this.paging(1);
      this.searchList = temp;
    },

    async paging(pageNumber) {
      const self = this;
      this.showDropdown = null;
      self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let objectId = Common.getParameter("objectId");
      // self.requestParam.id = objectId;
      if (this.status == "matched" || this.status == "unmatched") {
        self.requestParam.size = 100;
      } else {
        self.requestParam.size = 20;
      }
      // let requestParamConvert = self.requestParam;
      const param = Common.param(self.requestParam);

      await axios
        .get(API_BASE_MCH_STP + "?" + param)
        .then(function (response) {
          const machineData = response.data;
          self.results = machineData.content;
          self.total = response.data.totalElements;
          const currentTabIndex = self.listSelectedTabs.findIndex(
            (item) => item.id === self.currentActiveTab.id
          );
          if (currentTabIndex >= 0) {
            self.listSelectedTabs[currentTabIndex].totalItem =
              response.data.totalElements;
          }
          self.results = response.data.content;
          if (self.searchList.length > 0 && self.requestParam.query != "") {
            var check = false;
            self.searchList.map((item) => {
              self.results.map((item1) => {
                if (item1.moldId != null && item.moldId == item1.moldId) {
                  check = true;
                }
              });
              if (!check) {
                self.results.push(item);
              }
            });
          }
          console.log("self.listSelectedTabs before", self.listSelectedTabs);
          self.listSelectedTabs = self.listSelectedTabs.map((tab) => {
            const total =
              machineData.tabs.find(
                (machineTab) => machineTab.tabName === tab.name
              )?.totalElements || 0;
            return {
              ...tab,
              total,
            };
          });
          console.log("self.listSelectedTabs after", self.listSelectedTabs);

          self.pagination = Common.getPagingData(response.data);
          if (self.selectType === "all") {
            self.listAllIds = [];
            self.listChecked = [];
            self.listCheckedFull = [];
            self.selectType = "";
          }
          if (objectId !== "") {
            let searchParam = self.results.filter(
              (item) => parseInt(item.id) === parseInt(objectId)
            )[0];
            window.history.replaceState(null, null, "/admin/machines");
          }

          Common.handleNoResults("#app", self.results.length);
          var pageInfo =
            self.requestParam.page === 1
              ? ""
              : "?page=" + self.requestParam.page;
          history.pushState(null, null, Common.$uri.pathname + pageInfo);
          if (self.results.length > 0) {
            Common.triggerShowActionbarFeature(self.$children);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
      if (this.status == "matched") {
        var length = 0;
        this.results = this.results.filter((item) => {
          if (item.moldId != null) {
            length++;
          }
          return item.moldId != null;
        });
        this.total = length;
      }
      if (this.status == "unmatched") {
        var length = 0;
        this.results = this.results.filter((item) => {
          if (item.moldId == null) {
            length++;
          }
          return item.moldId == null;
        });
        this.total = length;
      }
      this.listChecked = [];
    },

    findCounter: function (molds, counterId) {
      for (var k = 0; k < molds.length; k++) {
        var mold = molds[k];

        if (typeof mold !== "object" || typeof mold.counter !== "object") {
          continue;
        }
        if (counterId === mold.counter.id) {
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
    sortBy(type) {
      if (this.currentSortType !== type) {
        this.currentSortType = type;
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc;
      }
      if (type) {
        /*
        if (type == "mold" || type == "enabled") {
          type = "mold.id";
        }
*/
        if (type == "enabled") {
          type = "mold";
        }
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
    deleteMachine: function (company) {
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
            self.getTabsByCurrentUser();
            self.paging(1);
          } else {
            alert(response.data.message);
          }
        })
        .catch(function (error) {
          console.log(error);
        });
    },
    showMachineDetails: function (company) {
      var child = Common.vue.getChild(this.$children, "machine-details");
      if (child != null) {
        child.showDetailsById(company.id);
      }
    },
    enable: function () {
      this.saveBatch("enable");
    },
    disable: function () {
      this.saveBatch("disable");
    },
    async saveBatch(action) {
      const self = this;
      const { filterCode, query, tabName } = self.requestParam;
      let listId = [];
      if (self.selectType === "all") {
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
        selectionMode: self.selectType === "all" ? "UNSELECTED" : "SELECTED",
        [self.selectType === "all" ? "unselectedIds" : "selectedIds"]: listId,
      });
      try {
        await axios.put(`${API_BASE_MCH_STP}/${action}-batch?${param}`);
        self.paging(1);
        this.listChecked = [];
        this.listCheckedFull = [];
        this.showDropdown = false;
      } catch (error) {
        console.log(error);
      }
    },
    handleSelectAll(actionType) {
      this.selectType = actionType;
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
          frameType: "MACHINE_SETTING",
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
    customFieldValueMachine(object, customFieldLabel) {
      try {
        if (object != null && customFieldLabel != null) {
          if (
            object.customFields &&
            object.customFields?.some(
              (field) => field.fieldName === customFieldLabel
            ) &&
            object.customFields?.find(
              (field) => field.fieldName === customFieldLabel
            )?.values.length > 0
          ) {
            return object.customFields?.find(
              (field) => field.fieldName === customFieldLabel
            ).values[0];
          }
        }
      } catch (e) {
        console.log(e);
      }
      return null;
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
    async exportMachine() {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_machine_data`;
      document.getElementById("toggleExportProgressBar").click();
      this.progressBarRun();
      const { filterCode, query, tabName } = this.requestParam;
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
          `${API_BASE_MCH_STP}/export?${param}`,
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
        console.log("data-family-machine:::exportMachine:::error:::", error);
        this.isStopedExport = true;
      }
    },
  },
  created() {
    this.$watch(
      () => headerVm?.systemCode,
      (newVal) => {
        if (!newVal) return;
        this.codes = Object.assign({}, this.codes, newVal);
      },
      { immediate: true }
    );

    this.$watch(
      () => headerVm?.options,
      (newVal) => {
        if (!newVal) return;
        this.options = Object.assign({}, this.options, newVal);
        this.optimalCycleTime = this.options.OPTIMAL_CYCLE_TIME;
      },
      { immediate: true }
    );
  },
  mounted() {
    this.animationOutline();
    this.animationPrimary();

    const url = new URL(location.href);
    const [_, stringParams] = url.hash.replace("#", "").split("?");
    const params = Common.parseParams(stringParams);
    if (params.name) {
      this.requestParam.query = params.name;
    }
    if (params.type) {
      this.requestParam.searchType = params.type;
    }
    if (params.ids) {
      this.requestParam.id = params.ids;
    }
    if (params.locationIds) {
      this.requestParam.locationIds = params.locationIds;
    }

    this.getTabsByCurrentUser(true);
  },
};
</script>

<style></style>
