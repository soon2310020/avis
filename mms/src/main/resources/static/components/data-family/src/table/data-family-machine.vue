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
                      <a-menu-item
                        @click="Common.onChangeHref('/admin/machines/new')"
                      >
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
                          v-if="listChecked.length > 1 && status == 'Disable'"
                        >
                          No batch action is available
                        </div> -->
                        <div
                          v-if="listChecked.length == 1 && status != 'Disable'"
                          @click="goToEdit"
                          class="action-item"
                        >
                          <span>Edit</span>
                          <i class="icon-action edit-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length == 1 && matched == true"
                          class="action-item"
                          @click="match()"
                        >
                          <span>Match with Tooling</span>
                          <div class="icon-action match-icon"></div>
                        </div>
                        <div class="action-item" @click.prevent="unMatch()">
                          <span>Un-match from Tooling</span>
                          <div class="unmatch-icon"></div>
                        </div>
                        <div
                          v-if="status != 'Disable'"
                          class="action-item"
                          @click="showSystemNoteModal()"
                        >
                          <span>Memo</span>
                          <i class="icon-action memo-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length == 1 && status != 'Disable'"
                          @click="showRevisionHistory(listChecked[0])"
                          class="action-item"
                        >
                          <span>View Edit History</span>
                          <i class="icon-action view-edit-history-icon"></i>
                        </div>
                        <div
                          v-if="listChecked.length >= 1 && status != 'Disable'"
                          class="action-item"
                          @click.prevent="showDisabledPopup()"
                        >
                          <span>Disable</span>
                          <div class="icon-action disable-icon"></div>
                        </div>
                        <div
                          v-if="listChecked.length >= 1 && status === 'Disable'"
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
                            !currentActiveTab.isDefaultTab
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
                        {{ formatToDate(result.createdAt) }}
                      </div>
                    </td>
                    <td
                      :key="columnIndex + 'mold'"
                      class="text-left"
                      v-else-if="column.field === 'mold'"
                    >
                      <div v-if="result.mold != null">
                        <a
                          href="#"
                          @click.prevent="showChart(result.mold)"
                          style="color: #1aaa55"
                          class="mr-1 font-size-14"
                          ><i class="fa fa-bar-chart"></i
                        ></a>
                        <a
                          href="#"
                          @click.prevent="showMoldDetails(result.mold)"
                        >
                          {{ result.mold.equipmentCode }}
                        </a>
                      </div>

                      <div
                        class="small text-muted font-size-11-2"
                        v-if="result.mold != null"
                      >
                        <span>{{ resources["updated_on"] }}</span>
                        {{ result.mold.updatedDate }}
                      </div>
                    </td>

                    <td
                      class="text-left"
                      :key="columnIndex + 'company'"
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
                      :key="columnIndex + 'enabled'"
                      class="text-center op-status-td"
                      v-else-if="column.field === 'enabled'"
                    >
                      <span
                        v-if="result.mold == null"
                        :class="{
                          'label label-danger': result.mold == null,
                          'label label-success': result.mold != null,
                        }"
                      >
                        {{ resources["unmatched"] }}
                      </span>
                      <span
                        v-else
                        :class="{
                          'label label-danger': result.mold == null,
                          'label label-success': result.mold != null,
                        }"
                      >
                        {{ resources["matched"] }}
                      </span>
                    </td>
                    <td
                      class="text-left"
                      v-else-if="column.isCustomField"
                      :key="columnIndex + 'title'"
                    >
                      <a-tooltip placement="bottomLeft">
                        <template slot="title">
                          <span
                            v-if="
                              customFieldValue(result, column.field) &&
                              customFieldValue(result, column.field).length > 20
                            "
                            >{{ customFieldValue(result, column.field) }}</span
                          >
                        </template>
                        <span>{{
                          replaceLongtext(
                            customFieldValue(result, column.field),
                            20
                          )
                        }}</span>
                      </a-tooltip>
                    </td>
                    <td
                      :key="columnIndex + 'title-bottom-left'"
                      class="text-left part"
                      v-else
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
    ></system-note>
    <disable-popup
      :confirm-delete="disable"
      :is-show-warning-again="isShowWarningAgain"
      :list="total === listAllIds.length ? listAllIds : listChecked"
      :resources="resources"
      :name="'Machine'"
    ></disable-popup>
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
      :enable-asset-selection="false"
      :is-show="workorderModalVisible"
      :asset-selected="machineSelectedToCreateWorkorder"
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
    "disable-popup": httpVueLoader("/components/disable-machine-popup.vue"),
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
    "manage-tab-view": httpVueLoader("/components/manage-tab-view.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-machine-modal.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action.vue"),
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
        query: "",
        sort: "id,desc",
        page: 1,
        id: "",
        searchType: "",
        size: 20,
        status: "enabled",
        pageType: "MACHINE_SETTING",
        tabId: "",
      },
      codes: {},
      tabOptions: [],
      sortType: {
        MACHINE_ID: "machineCode",
        MOLD: "mold",
        COMPANY: "location.company.name",
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
      matched: true,
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
      machineSelectedToCreateWorkorder: [],
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
    searchText() {
      this.requestParam.query = newVal;
      this.paging(1);
    },
    visibleMoreAction(newVal) {
      if (!newVal) {
        this.updateTabStatus();
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
      axios.post("/api/tab-table/save-tab-table-machine", params).then(() => {
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
      // this.tab(tabCreated);
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
      // this.tab(tabCreated);
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
        if (tabItem.name === "Disable") {
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
        .get("/api/tab-table/by-current-user?objectType=MACHINE")
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
    showCreateWorkorder(item) {
      console.log("item", item);
      this.machineSelectedToCreateWorkorder = [
        {
          ...this.listCheckedFull[0],
          checked: true,
          assetView: "machine",
          title: this.listCheckedFull[0]?.machineCode,
        },
      ];
      this.workorderModalVisible = true;
    },
    closeCreateWorkOrderModal() {
      this.workorderModalVisible = false;
    },
    showMoldDetails(mold) {
      var child = Common.vue.getChild(this.$children, "mold-details");
      if (child != null) {
        let reasonData = { reason: "", approvedAt: "" };
        const tab = "switch-detail";
        if (mold.dataSubmission === "DISAPPROVED") {
          axios
            .get(`/api/molds/data-submission/${mold.id}/disapproval-reason`)
            .then((response) => {
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
    showChart: function (mold, tab = null) {
      var child = Common.vue.getChild(this.$children, "chart-mold");
      if (child != null) {
        child.showChart(
          mold,
          "QUANTITY",
          "DAY",
          tab ? tab : "switch-graph",
          this.octs[this.optimalCycleTime.strategy]
        );
      }
    },
    showChart: function (mold, type = null, dateViewType = null, tab, oct) {
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
      const ids =
        this.total === this.listAllIds.length
          ? this.listAllIds
          : this.listChecked;
      try {
        const response = await axios.post(
          "/api/machines/un-match-with-tooling-in-batch",
          { ids }
        );
        console.log(response);
        this.paging(1);
        this.getTabsByCurrentUser();
        this.allmatched = false;
      } catch (error) {
        Common.alert("Failed to un-matched");
        console.log(error);
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
      axios
        .post(`/api/machines/delete-in-batch`, params)
        .then((res) => {
          $("#op-delete-popup").modal("hide");
          self.listChecked = [];
          self.getTabsByCurrentUser();
          self.paging(1);
        })
        .catch((e) => {
          console.log(e);
        });
    },
    showDeletePopup() {
      var child = Common.vue.getChild(this.$children, "delete-popup");
      if (child != null) {
        child.showDeletePopup();
      }
    },
    showDisabledPopup() {
      var child = Common.vue.getChild(this.$children, "disable-popup");
      if (child != null) {
        child.showDeletePopup();
      }
    },
    showDisabledPopup() {
      var child = Common.vue.getChild(this.$children, "disable-popup");
      if (child != null) {
        child.showDeletePopup();
      }
    },
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

      var temp = this.results.filter((item) => item.id == this.listChecked[0]);
      if (temp[0] != undefined) {
        if (temp[0].mold != null) this.matched = false;
        else this.matched = true;
      }

      for (index in this.listChecked) {
        var result = this.results.filter(
          (item) => item.id == this.listChecked[index]
        );
        if (result[0].mold != null && this.matched == false) {
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
    showRevisionHistory(id, equipmentStatus) {
      var child = Common.vue.getChild(this.$children, "revision-history");
      if (child != null) {
        child.showRevisionHistories(id, "MACHINE", equipmentStatus);
      }
    },
    tab(tab) {
      console.log("tab:::", tab);
      this.currentActiveTab = tab;
      this.matched = true;
      this.allmatched = false;
      this.total = null;
      this.listChecked = [];
      this.requestParam.tabId = "";
      if (tab.name === "Un-matched") {
        this.requestParam.matchedWithTooling = false;
      } else if (tab.name === "Matched") {
        this.requestParam.matchedWithTooling = true;
      } else {
        delete this.requestParam.matchedWithTooling;
      }
      if (!tab.isDefaultTab) {
        this.requestParam.tabId = tab.id;
      }
      this.status = tab.name;
      this.currentSortType = "id";
      this.requestParam.sort = "id,desc";
      this.paging(1);
      this.changeShow(null);
    },
    search(page) {
      var temp = this.results.filter((item) => {
        return (
          item.mold != null &&
          ("" + item.mold.id).includes(this.requestParam.query)
        );
      });
      this.paging(1);
      this.searchList = temp;
    },

    async paging(pageNumber) {
      var self = this;
      this.showDropdown = null;
      self.requestParam.page = pageNumber === undefined ? 1 : pageNumber;
      let objectId = Common.getParameter("objectId");
      self.requestParam.id = objectId;
      if (this.status == "matched" || this.status == "unmatched") {
        self.requestParam.size = 100;
      } else {
        self.requestParam.size = 20;
      }

      if (this.status === "Disable" || this.status === "Disabled") {
        self.requestParam.status = "disabled";
      } else {
        self.requestParam.status = "enabled";
      }

      let requestParamConvert = self.requestParam;
      var param = Common.param(requestParamConvert);

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      this.cancelToken = axios.CancelToken.source();

      await axios
        .get(Page.API_BASE + "?" + param, {
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
          self.results = response.data.content;
          if (self.searchList.length > 0 && self.requestParam.query != "") {
            var check = false;
            self.searchList.map((item) => {
              self.results.map((item1) => {
                if (item1.mold != null && item.mold.id == item1.mold.id) {
                  check = true;
                }
              });
              if (!check) {
                self.results.push(item);
              }
            });
          }

          self.pagination = Common.getPagingData(response.data);

          Common.handleNoResults("#app", self.results.length);

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
          if (item.mold != null) {
            length++;
          }
          return item.mold != null;
        });
        this.total = length;
      }
      if (this.status == "unmatched") {
        var length = 0;
        this.results = this.results.filter((item) => {
          if (item.mold == null) {
            length++;
          }
          return item.mold == null;
        });
        this.total = length;
      }
      this.listChecked = [];
    },

    findCounter(molds, counterId) {
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
    sortBy(type) {
      if (this.currentSortType !== type) {
        this.currentSortType = type;
        this.isDesc = true;
      } else {
        this.isDesc = !this.isDesc;
      }
      if (type) {
        if (type == "mold" || type == "enabled") {
          type = "mold.id";
        }
        this.requestParam.sort = `${type},${this.isDesc ? "desc" : "asc"}`;
        this.sort();
      }
    },
    sort() {
      this.paging(1);
    },
    installCounter(id) {
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
    enable() {
      const listIdSelected = this.listChecked;
      const listId = listIdSelected.map((item) => {
        return parseInt(item);
      });
      this.saveBatch(listId, true);
    },
    disable() {
      const listIdSelected = this.listChecked;
      const listId = listIdSelected.map((item) => {
        return parseInt(item);
      });
      this.saveBatch(listId, false);
    },
    saveBatch(idArr, boolean) {
      const param = {
        ids: idArr,
        enabled: boolean,
      };
      const self = this;
      axios
        .post("/api/machines/change-status-in-batch", param)
        .then(function (response) {
          self.paging(1);
          self.getTabsByCurrentUser();
        })
        .catch(function (error) {
          console.log(error);
        })
        .finally(() => {
          $("#op-delete-popup").modal("hide");
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        });
    },
    save(location) {
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
          this.listChecked = [];
          this.listCheckedFull = [];
          this.showDropdown = false;
        });
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

    // const params = Common.parseParams(URI.parse(location.href).query);
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
      this.requestParam.ids = params.ids;
    }
    if (params.locationIds) {
      this.requestParam.locationIds = params.locationIds;
    }

    // history.replaceState(null, null, Common.PAGE_URL.MACHINE);
    this.getTabsByCurrentUser(true);
  },
};
</script>

<style></style>
