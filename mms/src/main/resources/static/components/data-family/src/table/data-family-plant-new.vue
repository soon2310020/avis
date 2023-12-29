<template>
  <div>
    <div class="row">
      <div class="col-lg-12">
        <div>
          <div class="card-header" style="display: none">
            <i class="fa fa-align-justify"></i>
            <span>{{ resources["striped_tbl"] }}</span>
          </div>
          <div class="card-overflow-reset data-table-tabs">
            <div class="card-tool d-flex justify-space-between">
              <ul
                class="nav nav-tabs tooling-tab-hint"
                style="width: calc(100% - 140px)"
              >
                <template v-for="(tabItem, index) in listSelectedTabs">
                  <li
                    v-if="
                      tabItem.shown ||
                      (tabItem.defaultTab && tabItem.name === 'Enabled')
                    "
                    :key="index"
                    class="nav-item"
                  >
                    <a
                      class="nav-link"
                      :class="{ active: currentActiveTab.name == tabItem.name }"
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
                            {{ convertCodeToName(tabItem) }}
                          </span>
                        </a-tooltip>
                      </div>
                      <span v-else class="part-name-drop-down one-line">
                        {{ convertCodeToName(tabItem) }}
                      </span>
                      <span class="badge badge-light badge-pill">
                        {{ tabItem.total }}
                      </span>
                      <span
                        v-if="tabItem.name !== 'Enabled' || !tabItem.defaultTab"
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
              <div
                class="btn-group btn-group-select-column btn-group-select-column-last-child data-table-top-action"
                style="position: initial; width: 140px"
              >
                <div class="d-flex">
                  <div class="action-gap"></div>
                  <a
                    @click="onClickNewLocation"
                    href="javascript:void(0)"
                    class="btn-custom btn-custom-primary animationPrimary all-time-filters btn-custom-focus"
                  >
                    <span>{{ resources["new_location"] }}</span>
                  </a>
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
                        style="padding: 6px; margin-left: 8px"
                        @click.stop="handleToggleMoreActions"
                        href="javascript:void(0)"
                        class="customize-hint btn-custom customize-btn btn-outline-custom-primary all-time-filters"
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
                    </a-menu>
                    <div v-else slot="content">
                      <tab-view-list
                        :list-selected-tabs="listSelectedTabs"
                        :create-tab-view="createTabView"
                        :open-manage-tab-view="openManageTabView"
                        :select-tab="selectTab"
                      ></tab-view-list>
                    </div>
                  </a-popover>
                </div>
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
                <col style="width: 130px" />
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
                        :show="true"
                        :checked="isAll"
                        :total="total"
                        :count="results.length"
                        :unit="['plant', 'plants']"
                        @select-all="handleSelectAll('all')"
                        @select-page="handleSelectAll('page')"
                        @deselect="handleSelectAll('deselect')"
                      ></select-all>
                    </div>
                  </th>
                  <th
                    class="text-left __sort"
                    v-bind:class="[
                      { active: currentSortType === sortType.NAME },
                      isDesc ? 'desc' : 'asc',
                    ]"
                    @click="sortBy(sortType.NAME)"
                  >
                    <span>{{ resources["location_name"] }}</span
                    ><span class="__icon-sort"></span>
                  </th>
                  <th
                    class="text-left __sort"
                    v-bind:class="[
                      { active: currentSortType === sortType.LOCATION_CODE },
                      isDesc ? 'desc' : 'asc',
                    ]"
                    @click="sortBy(sortType.LOCATION_CODE)"
                  >
                    <span>{{ resources["location_id"] }}</span
                    ><span class="__icon-sort"></span>
                  </th>
                  <th
                    class="text-left __sort"
                    v-bind:class="[
                      { active: currentSortType === sortType.PLANT_AREAS },
                      isDesc ? 'desc' : 'asc',
                    ]"
                    @click="sortBy(sortType.PLANT_AREAS)"
                  >
                    <span>{{ resources["plant_area_s"] }}</span
                    ><span class="__icon-sort"></span>
                  </th>
                  <th
                    class="text-left __sort"
                    v-bind:class="[
                      { active: currentSortType === sortType.NO_TERMINAL },
                      isDesc ? 'desc' : 'asc',
                    ]"
                    @click="sortBy(sortType.NO_TERMINAL)"
                  >
                    <span>{{ resources["number_of_terminals"] }}</span
                    ><span class="__icon-sort"></span>
                  </th>
                  <th
                    class="text-left __sort"
                    v-bind:class="[
                      { active: currentSortType === sortType.COMPANY },
                      isDesc ? 'desc' : 'asc',
                    ]"
                    @click="sortBy(sortType.COMPANY)"
                  >
                    <span>{{ resources["company"] }}</span
                    ><span class="__icon-sort"></span>
                  </th>
                  <th
                    class="__sort"
                    v-bind:class="[
                      { active: currentSortType === sortType.STATUS },
                      isDesc ? 'desc' : 'asc',
                    ]"
                    @click="sortBy(sortType.STATUS)"
                  >
                    <span>{{ resources["status"] }}</span
                    ><span class="__icon-sort"></span>
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
                            :show="true"
                            :checked="isAll"
                            :total="total"
                            :count="results.length"
                            :unit="['plant', 'plants']"
                            @select-all="handleSelectAll('all')"
                            @select-page="handleSelectAll('page')"
                            @deselect="handleSelectAll('deselect')"
                          ></select-all>
                        </div>
                      </div>
                      <div class="action-bar">
                        <div
                          v-if="
                            listChecked.length === 1 &&
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
                            requestParam.tabName !== 'Disabled'
                          "
                          @click="showRevisionHistory(listChecked[0])"
                          class="action-item"
                        >
                          <span>View Edit History</span>
                          <i class="icon-action view-edit-history-icon"></i>
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
                          @click="exportPlant()"
                          class="action-item"
                        >
                          <span>Export</span>
                          <i class="icon-action export-icon"></i>
                        </div>
                        <div
                          v-if="requestParam.tabName !== 'Disabled'"
                          class="action-item"
                          @click.prevent="disable()"
                        >
                          <span>Disable</span>
                          <div class="icon-action disable-icon"></div>
                        </div>
                        <div
                          v-if="requestParam.tabName === 'Disabled'"
                          class="action-item"
                          @click.prevent="enable()"
                        >
                          <span>Enable</span>
                          <div class="icon-action enable-icon"></div>
                        </div>
                        <div
                          v-if="enableBatchShiftConfig"
                          class="action-item"
                          @click.prevent="showShiftConfigModal()"
                        >
                          <span>Shift Config</span>
                          <svg
                            width="16"
                            height="16"
                            viewBox="0 0 13 14"
                            fill="#595959"
                            xmlns="http://www.w3.org/2000/svg"
                          >
                            <path
                              d="M5.58815 4.50244C5.3263 4.50244 5.10916 4.71734 5.10916 4.97649V7.94085C5.10916 8.16207 5.22412 8.36433 5.40933 8.4781L7.734 9.89392C7.94475 10.0203 8.21937 9.96345 8.35348 9.76119C8.50037 9.54629 8.43012 9.25554 8.2066 9.12281L6.06713 7.85236V4.97649C6.06713 4.71734 5.84999 4.50244 5.58815 4.50244Z"
                              fill="#595959"
                            />
                            <path
                              d="M10.1672 7.03061C10.1992 7.23919 10.2183 7.44777 10.2183 7.66267C10.2183 10.1277 8.23854 12.0871 5.74782 12.0871C3.2571 12.0871 1.27729 10.1277 1.27729 7.66267C1.27729 5.19763 3.2571 3.23824 5.74782 3.23824C6.19487 3.23824 6.62276 3.30145 7.02511 3.42154V2.1195C6.61637 2.02469 6.18848 1.97412 5.74782 1.97412C2.55458 1.97412 0 4.50236 0 7.66267C0 10.823 2.55458 13.3512 5.74782 13.3512C8.94105 13.3512 11.4956 10.823 11.4956 7.66267C11.4956 7.44777 11.4829 7.23919 11.4573 7.03061H10.1672Z"
                              fill="#595959"
                            />
                            <path
                              d="M11.8156 1.06763C11.6587 0.719448 11.3894 0.432665 11.0501 0.252244C10.7471 0.086783 10.4068 0 10.0608 0C9.71479 0 9.37444 0.086783 9.07151 0.252244C8.73215 0.432665 8.46289 0.719448 8.30594 1.06763C7.82736 2.13341 8.30594 2.84388 8.54523 3.23858C8.76571 3.60228 9.4603 4.50699 9.94247 4.95348C9.97451 4.98327 10.0168 4.99985 10.0608 4.99985C10.1048 4.99985 10.1471 4.98327 10.1791 4.95348C10.6613 4.50699 11.3559 3.60228 11.5763 3.23858C11.8156 2.84393 12.2945 2.13341 11.8156 1.06763ZM10.0608 2.58885C9.88751 2.58885 9.71813 2.538 9.57405 2.44272C9.42998 2.34745 9.31769 2.21203 9.25138 2.05359C9.18507 1.89516 9.16772 1.72082 9.20152 1.55263C9.23533 1.38443 9.31877 1.22994 9.44129 1.10867C9.56382 0.987413 9.71992 0.904832 9.88987 0.871377C10.0598 0.837921 10.236 0.855091 10.3961 0.920718C10.5561 0.986344 10.693 1.09748 10.7892 1.24007C10.8855 1.38265 10.9369 1.55029 10.9369 1.72178C10.9369 1.83565 10.9142 1.9484 10.8702 2.0536C10.8262 2.1588 10.7617 2.25439 10.6803 2.33491C10.5989 2.41542 10.5024 2.47929 10.3961 2.52286C10.2898 2.56643 10.1758 2.58885 10.0608 2.58885Z"
                              fill="#595959"
                            />
                            <path
                              d="M10.0608 6.11996C10.7926 6.11996 11.3859 5.93576 11.3859 5.70854C11.3859 5.48132 10.7926 5.29712 10.0608 5.29712C9.329 5.29712 8.73575 5.48132 8.73575 5.70854C8.73575 5.93576 9.329 6.11996 10.0608 6.11996Z"
                              fill="#595959"
                            />
                          </svg>
                        </div>
                        <div
                          v-if="
                            listChecked.length >= 1 &&
                            !currentActiveTab.defaultTab
                          "
                          class="action-item"
                        >
                          <move-to-action
                            :type-view="'location'"
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
                      <!--                                            <label class="custom-control-labelx" :for="result.id + 'checkbox'"></label>-->
                    </template>
                  </td>
                  <td
                    class="text-left custom-hyper-link"
                    @click="handleShowDetail(result)"
                  >
                    {{ result.name }}
                  </td>
                  <td class="text-left">
                    <div>
                      {{ result.locationCode }}
                    </div>

                    <div class="small text-muted font-size-11-2">
                      <span>{{ resources["registered"] }}:</span>
                      {{ result.createdAtDate }}
                    </div>
                  </td>
                  <td class="text-left">
                    <div v-if="result.areas">
                      <base-drop-count
                        :is-hyper-link="true"
                        :data-list="result.areas"
                        :title="result.areas[0] ? result.areas[0].name : ''"
                      >
                        <div
                          style="
                            max-height: 165px;
                            overflow: auto;
                            min-width: 200px;
                            width: fit-content;
                          "
                        >
                          <div
                            v-for="item in result.areas"
                            :key="item.id"
                            class="dropdown-chip request-item align-center justify-space-between"
                            style="
                              display: inline-flex;
                              width: 100%;
                              white-space: nowrap;
                              padding: 6px 8px;
                            "
                          >
                            <span>{{ item.name }}</span>
                            <base-chip
                              :selection="areaTypes[item.areaType]"
                              :allow-tooltips="false"
                              :color="'wheat'"
                              :close-able="false"
                            >
                            </base-chip>
                          </div>
                          <div style="height: 21px; padding: 6px 8px">
                            <span
                              class="custom-hyper-link"
                              @click="handleViewPlantTerminals(result.areas)"
                            >
                              View Plant Terminals
                              <div
                                class="hyper-link-icon"
                                style="margin-left: 3px"
                              ></div>
                            </span>
                          </div>
                        </div>
                      </base-drop-count>
                    </div>
                  </td>
                  <td class="text-left">
                    <div
                      v-on:click="showNumberOfTerminal(result.id)"
                      class="font-size-14 anchor-cpy"
                    >
                      {{ result.terminalCount }}
                    </div>
                  </td>
                  <td class="text-left">
                    <div v-if="result.companyName">
                      <div
                        class="font-size-14"
                        v-html="
                          companyName(
                            result.companyName,
                            result?.companyEnabled
                          )
                        "
                      ></div>
                      <div class="small text-muted font-size-11-2">
                        {{ result.companyCode }}
                      </div>
                    </div>
                  </td>
                  <td
                    style="vertical-align: middle !important"
                    class="text-center op-status-td"
                  >
                    <span v-if="result.enabled" class="label label-success">{{
                      resources["enabled"]
                    }}</span>
                    <span v-if="!result.enabled" class="label label-danger">{{
                      resources["disabled"]
                    }}</span>
                  </td>
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
          </div>
        </div>
      </div>
    </div>
    <revision-history :resources="resources"></revision-history>
    <system-note
      ref="system-note"
      system-note-function="LOCATION"
      :resources="resources"
      :handle-submit="handleSubmitNote"
    ></system-note>
    <number-of-terminal :resources="resources"></number-of-terminal>
    <action-bar-feature feature="ACTION_BAR"></action-bar-feature>
    <!-- <shift-config-modal
      ref="shiftConfigModal"
      :resources="resources"
    ></shift-config-modal> -->
    <shift-config-modal-new
      :resources="resources"
      :visible="visibleShiftConfig"
      :shift-config-locations="shiftConfigLocations"
      @close="handleCloseShiftConfigModal"
    ></shift-config-modal-new>
    <manage-tab-view
      :resources="resources"
      :object-type="'LOCATION'"
      @open-edit="openEdit"
      @open-duplicate="openDuplicate"
      @move-success="getTabsByCurrentUser"
      :trigger-update="triggerUpdateManageTab"
    ></manage-tab-view>
    <tab-view-modal
      :resources="resources"
      :object-type="'LOCATION'"
      :page-type="'LOCATION'"
      @edit-success="editSuccess"
      @create-success="createSuccess"
    >
    </tab-view-modal>
    <toast-alert
      @close-toast="closeToastAlert"
      :show="toastAlert.value"
      :title="toastAlert.title"
      :content="toastAlert.content"
    ></toast-alert>
    <plant-details-modal
      :visible="visibleDetailModal"
      :selected-plant="selectedPlant"
      @close="handleCloseDetailModal"
    ></plant-details-modal>
  </div>
</template>

<script>
var Page = Common.getPage("locations");
const API_BASE_PLT_STP = Common.getApiBase().LOCATION;
const API_BASE_TAB_STP = Common.getApiBase().TAB;
const PAGE_SIZE = 20;
module.exports = {
  components: {
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "system-note": httpVueLoader("/components/system-note.vue"),
    "revision-history": httpVueLoader(
      "/components/version/view-revision-history.vue"
    ),
    "number-of-terminal": httpVueLoader("/components/number-of-terminal.vue"),
    "action-bar-feature": httpVueLoader(
      "/components/new-feature/new-feature.vue"
    ),
    "primary-button": httpVueLoader("/components/common/primary-button.vue"),
    "shift-config-modal": httpVueLoader(
      "/components/shift-config/shift-config-modal.vue"
    ),
    "shift-config-modal-new": httpVueLoader(
      "/components/shift-config/shift-config-modal-new.vue"
    ),
    "tab-view-list": httpVueLoader("/components/@base/tab-view-list.vue"),
    "manage-tab-view": httpVueLoader("/components/manage-tab-view-new.vue"),
    "tab-view-modal": httpVueLoader("/components/tab-view-modal-new.vue"),
    "toast-alert": httpVueLoader("/components/@base/toast/toast-alert.vue"),
    "move-to-action": httpVueLoader("/components/move-to-action-new.vue"),
    "plant-details-modal": httpVueLoader(
      "/components/details-modal/plant-details-modal.vue"
    ),
  },
  props: {
    resources: Object,
    searchText: String,
    reloadKey: [String, Number],
    filterCode: String,
  },
  data() {
    return {
      userType: "",
      showDrowdown: null,
      results: [],
      total: 0,
      pagination: [],
      requestParam: {
        query: "",
        sort: "id,desc",
        page: 1,
        size: PAGE_SIZE,
        filterCode: "COMMON",
        tabName: "Enabled",
      },
      codes: {},
      sortType: {
        LOCATION_CODE: "locationCode",
        NAME: "name",
        COMPANY: "companyName",
        STATUS: "enabled",
        NO_TERMINAL: "terminalCount",
        PLANT_AREAS: "areas",
      },
      currentSortType: "id",
      isDesc: true,
      showDropdown: false,
      listAllIds: [],
      listAllLitePlants: [],
      listChecked: [],
      listCheckedFull: [],
      cancelToken: undefined,
      currentUser: {
        admin: false,
      },
      visibleMoreAction: false,
      isTabView: false,
      listSelectedTabs: [],
      currentActiveTab: {
        id: null,
        defaultTab: true,
        name: "Enabled",
        show: true,
      },
      tabStatus: "Enabled",
      triggerUpdateManageTab: 0,
      toastAlert: {
        value: false,
        title: "",
        content: "",
      },
      visibleMove: false,
      selectedPlant: {},
      visibleDetailModal: false,
      areaTypes: {
        MAINTENANCE_AREA: {
          id: 1,
          title: "Maintenance",
        },
        PRODUCTION_AREA: {
          id: 2,
          title: "Production",
        },
        WAREHOUSE: {
          id: 3,
          title: "Warehouse",
        },
      },
      selectType: "",
      shiftConfigLocations: [],
      visibleShiftConfig: false,
      isStopedExport: false,
      isCallingDone: false,
      currentIntervalIds: [],
    };
  },
  computed: {
    isAdmin() {
      return this.currentUser.admin || false;
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
    enableBatchShiftConfig() {
      return (
        this.listChecked.length > 0 &&
        this.requestParam.tabName !== "Disabled" &&
        this.isAdmin
      );
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
  },
  methods: {
    async handleSubmitNote(data) {
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
        await axios.post(`${API_BASE_PLT_STP}/note-batch?${param}`, data);
      } catch (error) {
        console.log(error);
      }
    },
    handleViewPlantTerminals(areasList) {
      const areaIds = areasList.map((item) => item.id);
      console.log("handleViewPlantTerminals", areaIds);
      window.location.href = `${
        Common.PAGE_URL.TERMINAL
      }?areaIds=${areaIds.join("-")}`;
    },
    getAreaTypeByKey(key) {
      return TYPES[key];
    },
    async handleShowDetail(plant) {
      const { data: plantData } = await axios.get(`/api/locations/${plant.id}`);
      this.selectedPlant = { ...plantData, ...plant };
      this.visibleDetailModal = true;
    },
    handleCloseDetailModal() {
      this.visibleDetailModal = false;
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
    async showTab(tab) {
      try {
        tab.shown = true;
        if (tab.defaultTab) {
          await axios.put(
            `${API_BASE_TAB_STP}/LOCATION/names/${tab.name}/shown`
          );
        } else {
          await axios.put(`${API_BASE_TAB_STP}/LOCATION/${tab.id}/shown`);
        }
      } catch (error) {
        console.log("data-family-plant:::showTab:::error", error);
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
            `${API_BASE_TAB_STP}/LOCATION/names/${tab.name}/hidden`
          );
        } else {
          await axios.put(`${API_BASE_TAB_STP}/LOCATION/${tab.id}/hidden`);
        }
      } catch (error) {
        console.log("data-family-plant:::closeTab:::error", error);
      }
    },
    truncateText(text, size) {
      if (text) {
        const trimText = text.trim();
        if (trimText) {
          if (trimText.length > size) {
            return text.slice(0, size) + "...";
          }
          return trimText;
        }
      }
      return "";
    },
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
        await axios.delete(`${API_BASE_PLT_STP}/tab-items-batch?${param}`);
        this.moveSuccess({
          title: "Success!",
          content: `Your data has been removed from ${this.currentActiveTab.name} tab`,
        });
      } catch (error) {
        console.log("data-family-plant:::removeFromTab:::error", error);
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
    showToastAlert(toast) {
      this.toastAlert.value = true;
      this.toastAlert.title = toast.title;
      this.toastAlert.content = toast.content;
    },
    closeToastAlert() {
      this.toastAlert.value = false;
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
    handleToggleMoreActions() {
      this.visibleMoreAction = true;
      this.isTabView = false;
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
        await axios.put(`${API_BASE_PLT_STP}/move-tab-items-batch?${param}`);
        this.moveSuccess({
          title: "Success!",
          content: `Your data has been moved to ${tab.name} tab`,
        });
      } catch (error) {
        console.log(error);
      }
    },
    async getTabsByCurrentUser(isCallAtMounted) {
      try {
        const { data } = await axios.get(`${API_BASE_TAB_STP}/LOCATION`);
        this.listSelectedTabs = data.content;
        if (isCallAtMounted) {
          this.setNewActiveTab();
        }
      } catch (error) {
        console.log("data-family-plant:::getTabsByCurrentUser:::error", error);
      }
    },
    setNewActiveTab() {
      const listShowed = this.listSelectedTabs.filter((t) => t.shown);
      if (listShowed.length) {
        this.tab(listShowed[0]);
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
      const { checked, value } = e.target;
      const itemId = Number(value);
      if (checked) {
        this.listChecked.push(itemId);
        const foundIndex = this.results.findIndex((item) => item.id === itemId);
        this.listCheckedFull.push(this.results[foundIndex]);
        Common.setHeightActionBar();
      } else {
        this.listAllIds = [];
        this.listAllLitePlants = [];
        this.listChecked = this.listChecked.filter((id) => id !== itemId);
        this.listCheckedFull = this.listCheckedFull.filter(
          (item) => item.id !== itemId
        );
      }
    },
    goToEdit() {
      console.log(this.listChecked[0], "this.listChecked[0]");
      window.location.href = `/admin/locations/${this.listChecked[0]}`;
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
        child.showRevisionHistories(id, "LOCATION");
      }
    },
    tab(tab) {
      this.total = null;
      this.listChecked = [];
      this.listCheckedFull = [];
      this.currentActiveTab = tab;
      this.requestParam.tabName = tab.name;
      if (tab.name === "Disabled") {
        this.paging(1);
      } else {
        this.currentSortType = "id";
        this.requestParam.sort = "id,desc";
        this.paging(1);
      }
      this.changeShow(null);
    },
    search: function (page) {
      this.paging(1);
    },
    enable() {
      this.saveBatch("enable");
    },
    disable() {
      this.saveBatch("disable");
    },
    async saveBatch(action) {
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
        await axios.put(`${API_BASE_PLT_STP}/${action}-batch?${param}`);
        this.paging(1);
      } catch (error) {
        console.log("data-family-plant:::saveBatch:::error", error);
      } finally {
        this.listChecked = [];
        this.listCheckedFull = [];
        this.showDropdown = false;
      }
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
      axios
        .post("/api/locations/delete-in-batch", param)
        .then(function (response) {
          if (response.data.success) {
            self.paging(1);
          } else {
            alert(response.data.message);
          }
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
    deleteLocation: function (location) {
      if (!confirm("Are you sure you want to delete?")) {
        return;
      }
      var param = {
        id: location.id,
      };
      const self = this;
      axios
        .delete(Page.API_BASE + "/" + location.id, param)
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
    async paging(pageNumber) {
      this.requestParam.page = pageNumber !== undefined ? pageNumber : 1;

      const param = Common.param(this.requestParam);

      if (typeof this.cancelToken != typeof undefined) {
        this.cancelToken.cancel("new request"); // cancel on new request
      }

      this.cancelToken = axios.CancelToken.source();
      try {
        const { data: plantData } = await axios.get(
          `${API_BASE_PLT_STP}?${param}`,
          {
            cancelToken: this.cancelToken.token,
          }
        );
        this.total = plantData?.totalElements;
        this.results = plantData?.content;
        console.log("this.results:::", this.results);
        this.pagination = Common.getPagingData(plantData);
        // Reset select all when change page
        if (this.selectType === "all") {
          this.listAllIds = [];
          this.listChecked = [];
          this.listCheckedFull = [];
          this.listAllLitePlants = [];
          this.selectType = "";
        }

        // Get tab count
        // await this.getTabsByCurrentUser();
        this.listSelectedTabs = this.listSelectedTabs.map((tab) => {
          const total =
            plantData.tabs.find((plantTab) => plantTab.tabName === tab.name)
              ?.totalElements || 0;
          return {
            ...tab,
            total,
          };
        });

        document.querySelector("#app .op-list").style.display =
          "table-row-group";
        document.querySelector(".pagination").style.display = "flex";
        const noResult = document.querySelector(".no-results");
        if (noResult !== null) {
          if (this.results.length === 0) {
            noResult.className = noResult.className.replace("d-none", "");
          } else {
            noResult.className =
              noResult.className.replace("d-none", "") + " d-none";
          }
        }
        const pageInfo =
          this.requestParam.page === 1 ? "" : "?page=" + this.requestParam.page;
        history.pushState(null, null, Common.$uri.pathname + pageInfo);
        if (this.results.length > 0) {
          Common.triggerShowActionbarFeature(this.$children);
        }
      } catch (error) {
        console.log("data-family-plant:::paging:::error", error);
      }
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
    showNumberOfTerminal: function (id) {
      var child = Common.vue.getChild(this.$children, "number-of-terminal");
      if (child != null) {
        child.showDetails(id);
      }
    },

    async showShiftConfigModal() {
      // const shiftConfigModal = this.$refs.shiftConfigModal;
      // if (shiftConfigModal) {
      let locations = [];
      if (this.selectType === "all") {
        // Get a list of all items when selecting all.
        const param = Common.param({ ...this.requestParam, size: 99999 });
        const { data: plantData } = await axios.get(
          `${API_BASE_PLT_STP}?${param}`
        );
        locations = plantData?.content;
      } else {
        locations = this.listCheckedFull;
      }
      locations = locations.map((location) => ({
        ...location,
        locationTitle: location.name,
      }));
      this.shiftConfigLocations = locations;
      this.visibleShiftConfig = true;
      // shiftConfigModal.showModal(locations);
      // }
    },
    handleCloseShiftConfigModal() {
      this.visibleShiftConfig = false;
    },

    handleSelectAll(actionType) {
      this.selectType = actionType;
      if (actionType === "page") {
        this.listAllIds = [];
        this.listAllLitePlants = [];
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.listCheckedFull.length} plants on this current page are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
      }
      if (actionType === "all") {
        // this.fetchAllListIds();
        this.listChecked = this.results.map((i) => i.id);
        this.listCheckedFull = [...this.results];
        this.setToastAlertGlobal({
          title: `All ${this.total} plants are selected.`,
          content: `Unselect the checkbox to clear selection.`,
          show: true,
        });
        Common.setHeightActionBar();
      }
      if (actionType === "deselect") {
        this.listAllIds = [];
        this.listAllLitePlants = [];
        this.listChecked = [];
        this.listCheckedFull = [];
      }

      console.log("@handleSelectAll", {
        actionType,
        listChecked: this.listChecked,
        listCheckedFull: this.listCheckedFull,
      });
    },
    onClickNewLocation() {
      Common?.onChangeHref("/admin/locations/new");
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
    async exportPlant() {
      this.isStopedExport = false;
      this.isCallingDone = false;
      let today = new Date();
      let dd = String(today.getDate()).padStart(2, "0");
      let mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
      let yyyy = today.getFullYear();
      let fileName = `${yyyy}${mm}${dd}_eMoldino_plant_data`;

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
          `${API_BASE_PLT_STP}/export?${param}`,
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
        console.log("data-family-part:::exportPlant:::error:::", error);
        this.isStopedExport = true;
      }
    },
  },
  async created() {
    this.$watch(
      () => headerVm?.currentUser,
      (newVal) => {
        if (!newVal) return;
        this.currentUser = Object.assign({}, this.currentUser, newVal);
      },
      { immediate: true }
    );
    this.$watch(
      () => headerVm?.systemCode,
      (newVal) => {
        if (!newVal) return;
        this.codes = Object.assign({}, this.codes, newVal);
      },
      { immediate: true }
    );
    this.$watch(
      () => headerVm?.userType,
      (newVal) => {
        if (!newVal) return;
        this.userType = newVal;
      },
      { immediate: true }
    );

    // capture query
    const url = new URL(location.href);
    const urlParam = url.hash.replace("#", "").split("?")[1];
    const params = Common.parseParams(urlParam);
    if (params.ids) {
      this.requestParam.id = params.ids;
    }
  },
  mounted() {
    this.getTabsByCurrentUser(true);
  },
};
</script>

<style></style>
