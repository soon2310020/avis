<template>
  <div>
    <a-modal
      v-model="visible"
      :body-style="{ padding: '0' }"
      width="1032px"
      :closable="false"
      :footer="null"
      centered
    >
      <div class="custom-modal-title">
        <span class="modal-title">Machine Availability</span>
        <div
          class="t-close-button close-button"
          @click="closeModal"
          aria-hidden="true"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="11.699"
            height="11.699"
            viewBox="0 0 11.699 11.699"
          >
            <g
              id="Group_8882"
              data-name="Group 8882"
              transform="translate(0 0)"
            >
              <rect
                id="Rectangle_10"
                data-name="Rectangle 10"
                width="2.363"
                height="14.181"
                transform="translate(10.028 0) rotate(45)"
              />
              <rect
                id="Rectangle_11"
                data-name="Rectangle 11"
                width="2.364"
                height="14.181"
                transform="translate(11.699 10.028) rotate(135)"
              />
            </g>
          </svg>
        </div>
        <div class="head-line"></div>
      </div>
      <div class="custom-modal-body">
        <div class="custom-modal-body-content">
          <div
            style="
              display: flex;
              margin-bottom: 20px;
              justify-content: space-between;
            "
          >
            <ul class="type-bar">
              <li>
                <a
                  href="javascript:void(0)"
                  class="nav-button"
                  :class="{ 'nav-button-selected': type == 'PLANNED_DOWNTIME' }"
                  @click="changeType('PLANNED_DOWNTIME')"
                  >Planned Downtime</a
                >
              </li>
              <li>
                <a
                  href="javascript:void(0)"
                  class="nav-button"
                  :class="{
                    'nav-button-selected': type == 'UNPLANNED_DOWNTIME',
                  }"
                  @click="changeType('UNPLANNED_DOWNTIME')"
                  >Unplanned Downtime</a
                >
              </li>
              <li>
                <a
                  href="javascript:void(0)"
                  class="nav-button"
                  :class="{
                    'nav-button-selected': type == 'DAILY_WORKING_HOUR',
                  }"
                  @click="changeType('DAILY_WORKING_HOUR')"
                  >Daily Work Hours</a
                >
              </li>
            </ul>
            <div class="right-tool date-filter-modal">
              <date-filter
                @change-date="filterWithDate"
                :default-date="selectedDateRaw"
              ></date-filter>
            </div>
          </div>
          <div class="machine-title">
            <strong>Machine ID</strong><span>{{ machineCode }}</span>
          </div>
          <div v-if="!warning" class="custom-modal-body-content-detail">
            <div>
              <div class="status-bar">
                <span style="margin-right: 32px"
                  ><strong>Actual Daily Work Hours</strong>
                  {{ machineAvailability.actualWorkingHour }}
                  {{ machineAvailability.actualWorkingHour > 1 ? "Hrs" : "Hr" }}
                  {{
                    machineAvailability.actualWorkingHourMinute > 1
                      ? machineAvailability.actualWorkingHourMinute + " Mins"
                      : machineAvailability.actualWorkingHourMinute == 1
                      ? machineAvailability.actualWorkingHourMinute + " Min"
                      : ""
                  }}
                </span>
                <div class="hour-bar" style="width: 264px">
                  <div
                    class="actual-hour-bar"
                    :style="{
                      width:
                        machineAvailability.actualWorkingHourDecimal * 11 +
                        'px',
                    }"
                  ></div>
                </div>
              </div>
              <p class="actual-hour-popup-title current-content-title">
                Downtime Summary
              </p>
              <div class="current-content">
                <template>
                  <p class="actual-hour-popup-title" style="margin-bottom: 0">
                    Daily Work Hours
                  </p>
                  <div class="gap">
                    <div
                      class="actual-hour-popup-sub-title"
                      style="margin-bottom: 0"
                    >
                      {{
                        `${
                          machineAvailability.dailyWorkingHour > 0
                            ? machineAvailability.dailyWorkingHour
                            : ""
                        } ${
                          machineAvailability.dailyWorkingHour > 1
                            ? "hrs"
                            : machineAvailability.dailyWorkingHour == 1
                            ? "hr"
                            : " "
                        }`
                      }}
                      {{
                        machineAvailability.dailyWorkingHourMinute > 1
                          ? machineAvailability.dailyWorkingHourMinute + " mins"
                          : machineAvailability.dailyWorkingHourMinute == 1
                          ? machineAvailability.dailyWorkingHourMinute + " min"
                          : ""
                      }}
                    </div>
                    <div class="actual-hour-popup-sub-content">
                      {{ machineAvailability.dailyWorkingHourNote }}
                    </div>
                  </div>
                </template>
                <template
                  v-if="
                    machineAvailability.downtimeItems != null &&
                    machineAvailability.downtimeItems.length > 0
                  "
                >
                  <template
                    v-if="
                      getNoteItemByType('PLANNED_DOWNTIME') != null &&
                      getNoteItemByType('PLANNED_DOWNTIME').length > 0
                    "
                  >
                    <p class="actual-hour-popup-title">Planned Downtime</p>
                    <div class="gap">
                      <div
                        class="downtime-note"
                        v-for="(item, index) in getNoteItemByType(
                          'PLANNED_DOWNTIME'
                        )"
                        :key="index"
                      >
                        <div class="actual-hour-popup-sub-title">
                          {{
                            `${item.hour > 0 ? item.hour : ""} ${
                              item.hour > 1
                                ? "hrs"
                                : item.hour == 1
                                ? "hr"
                                : " "
                            }`
                          }}
                          {{
                            item.minute > 1
                              ? item.minute + " mins"
                              : item.minute == 1
                              ? item.minute + " min"
                              : ""
                          }}
                          {{
                            `[${
                              item.hourFrom > 9
                                ? item.hourFrom
                                : "0" + item.hourFrom
                            }:${
                              item.minuteFrom > 9
                                ? item.minuteFrom
                                : "0" + item.minuteFrom
                            } - ${
                              item.hourTo > 9 ? item.hourTo : "0" + item.hourTo
                            }:${
                              item.minuteTo > 9
                                ? item.minuteTo
                                : "0" + item.minuteTo
                            }]`
                          }}
                          <div class="actual-hour-popup-type">
                            {{ item.reason }}
                          </div>
                        </div>
                        <div class="actual-hour-popup-sub-content">
                          {{ item.note }}
                        </div>
                      </div>
                    </div>
                  </template>
                  <template
                    v-if="
                      getNoteItemByType('UNPLANNED_DOWNTIME') != null &&
                      getNoteItemByType('UNPLANNED_DOWNTIME').length > 0
                    "
                  >
                    <p class="actual-hour-popup-title">Unplanned Downtime</p>

                    <div
                      class="downtime-note"
                      v-for="(item, index) in getNoteItemByType(
                        'UNPLANNED_DOWNTIME'
                      )"
                      :key="index"
                    >
                      <div class="actual-hour-popup-sub-title">
                        {{
                          `${item.hour > 0 ? item.hour : ""} ${
                            item.hour > 1 ? "hrs" : item.hour == 1 ? "hr" : " "
                          }`
                        }}
                        {{
                          item.minute > 1
                            ? item.minute + " mins"
                            : item.minute == 1
                            ? item.minute + " min"
                            : ""
                        }}
                        {{
                          `[${
                            item.hourFrom > 9
                              ? item.hourFrom
                              : "0" + item.hourFrom
                          }:${
                            item.minuteFrom > 9
                              ? item.minuteFrom
                              : "0" + item.minuteFrom
                          } - ${
                            item.hourTo > 9 ? item.hourTo : "0" + item.hourTo
                          }:${
                            item.minuteTo > 9
                              ? item.minuteTo
                              : "0" + item.minuteTo
                          }]`
                        }}
                        <div class="actual-hour-popup-type">
                          {{ item.reason }}
                        </div>
                      </div>
                      <div class="actual-hour-popup-sub-content">
                        {{ item.note }}
                      </div>
                    </div>
                  </template>
                </template>
                <template
                  v-if="
                    machineAvailability.downtimeItems == null ||
                    machineAvailability.downtimeItems.length == 0
                  "
                >
                  <div>
                    <p
                      class="actual-hour-popup-title"
                      style="text-align: center"
                    >
                      Notice
                    </p>
                    <div
                      style="
                        display: flex;
                        flex-direction: column;
                        align-items: center;
                        justify-content: center;
                      "
                    >
                      <div class="actual-hour-popup-sub-content">
                        Planned or unplanned downtime has not yet been added
                      </div>
                    </div>
                  </div>
                </template>
              </div>
            </div>
            <div
              v-if="type == 'PLANNED_DOWNTIME' || type == 'UNPLANNED_DOWNTIME'"
              class="new-content"
            >
              <div class="new-content-block">
                <label for="downtime-start-planned">
                  <strong>Start Planned Downtime</strong>
                  <span class="badge-require"></span>
                </label>
                <div class="new-content-detail">
                  <a-date-picker
                    id="downtime-start-planned"
                    @change="handleChangeDate"
                    :allow-clear="false"
                    format="YYYY-MM-DD (dddd)"
                    v-model="downtime.start.date"
                    :class="{
                      'invalid-field': error.includes('DOWNTIME_DATE'),
                    }"
                  >
                    <svg
                      slot="suffixIcon"
                      xmlns="http://www.w3.org/2000/svg"
                      width="14.188"
                      height="14.426"
                      viewBox="0 0 14.188 14.426"
                    >
                      <g
                        id="Component_1_9"
                        data-name="Component 1 – 9"
                        transform="translate(0.25 0.25)"
                      >
                        <path
                          id="Subtraction_4"
                          data-name="Subtraction 4"
                          d="M6.678,11.635H0V10.471L.005,2.327V1.164h1.74V0H2.909V1.164H7.562V0H8.726V1.164h1.745v5.7a4.073,4.073,0,0,0-1.164.243V4.072H1.164v6.4H6.651a4.147,4.147,0,0,0,.026,1.162ZM4.415,9.308h0L2.592,7.562l.317-.339L4.415,8.789,7.6,5.6l.339.269L4.416,9.308Z"
                          transform="translate(0 0)"
                          fill="#9d9d9d"
                          stroke="#9d9d9d"
                          stroke-width="0.5"
                        />
                        <path
                          id="Subtraction_2"
                          data-name="Subtraction 2"
                          d="M3.217,6.433A3.217,3.217,0,1,1,6.433,3.217,3.22,3.22,0,0,1,3.217,6.433Zm0-4.871a.3.3,0,0,0-.3.3V3.9a.3.3,0,0,0,.163.265l1.359.679a.3.3,0,0,0,.4-.132.3.3,0,0,0-.133-.4l-1.195-.6V1.858A.3.3,0,0,0,3.217,1.562Z"
                          transform="translate(7.504 7.742)"
                          fill="#9d9d9d"
                        />
                      </g>
                    </svg>
                  </a-date-picker>
                  <div
                    class="dropdown-container"
                    v-click-outside="closeTimeDropdownStart"
                  >
                    <a
                      href="javascript:void(0)"
                      class="time-dropdown"
                      @click="chanegDropdown('start')"
                      :class="{
                        'invalid-field': error.includes('DOWNTIME_DATE'),
                      }"
                      >{{
                        downtime.start.hour >= 10
                          ? downtime.start.hour
                          : "0" + downtime.start.hour
                      }}:{{
                        downtime.start.minute >= 10
                          ? downtime.start.minute
                          : "0" + downtime.start.minute
                      }}</a
                    >
                    <div
                      style="
                        left: auto !important;
                        width: 105px;
                        transform: translate(0px, 10px);
                        overflow-y: unset;
                      "
                      class="dropdown-menu"
                      :class="[selectedDropdown == 'start' ? 'show' : '']"
                    >
                      <div style="display: flex">
                        <div class="hour-dropdown-col">
                          <ul class="hour-dropdown">
                            <li
                              v-for="index in 24"
                              :key="index"
                              :class="{
                                'selected-item':
                                  index == downtime.start.hour + 1,
                              }"
                            >
                              <span
                                class="dropdown-item"
                                onmouseover="this.style.color='black';"
                                onmouseout="this.style.color='black';"
                                @click="
                                  handleChangeTime(index - 1, 'start', 'hour')
                                "
                              >
                                {{
                                  `${
                                    index - 1 >= 10
                                      ? index - 1
                                      : "0" + (index - 1)
                                  }`
                                }}
                              </span>
                            </li>
                          </ul>
                        </div>
                        <div class="hour-dropdown-col">
                          <ul class="hour-dropdown">
                            <li
                              v-for="index in 60"
                              :key="index"
                              :class="{
                                'selected-item':
                                  index == downtime.start.minute + 1,
                              }"
                            >
                              <span
                                class="dropdown-item"
                                onmouseover="this.style.color='black';"
                                onmouseout="this.style.color='black';"
                                @click="
                                  handleChangeTime(index - 1, 'start', 'minute')
                                "
                              >
                                {{
                                  `${
                                    index - 1 >= 10
                                      ? index - 1
                                      : "0" + (index - 1)
                                  }`
                                }}
                              </span>
                            </li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="new-content-block">
                <label for="downtime-end-planned">
                  <strong>End Planned Downtime</strong>
                  <span class="badge-require"></span>
                </label>
                <div class="new-content-detail">
                  <a-date-picker
                    id="downtime-end-planned"
                    @change="handleChangeDate"
                    :allow-clear="false"
                    format="YYYY-MM-DD (dddd)"
                    v-model="downtime.end.date"
                    :class="{
                      'invalid-field': error.includes('DOWNTIME_DATE'),
                    }"
                  >
                    <svg
                      slot="suffixIcon"
                      xmlns="http://www.w3.org/2000/svg"
                      width="14.188"
                      height="14.426"
                      viewBox="0 0 14.188 14.426"
                    >
                      <g
                        id="Component_1_9"
                        data-name="Component 1 – 9"
                        transform="translate(0.25 0.25)"
                      >
                        <path
                          id="Subtraction_4"
                          data-name="Subtraction 4"
                          d="M6.678,11.635H0V10.471L.005,2.327V1.164h1.74V0H2.909V1.164H7.562V0H8.726V1.164h1.745v5.7a4.073,4.073,0,0,0-1.164.243V4.072H1.164v6.4H6.651a4.147,4.147,0,0,0,.026,1.162ZM4.415,9.308h0L2.592,7.562l.317-.339L4.415,8.789,7.6,5.6l.339.269L4.416,9.308Z"
                          transform="translate(0 0)"
                          fill="#9d9d9d"
                          stroke="#9d9d9d"
                          stroke-width="0.5"
                        />
                        <path
                          id="Subtraction_2"
                          data-name="Subtraction 2"
                          d="M3.217,6.433A3.217,3.217,0,1,1,6.433,3.217,3.22,3.22,0,0,1,3.217,6.433Zm0-4.871a.3.3,0,0,0-.3.3V3.9a.3.3,0,0,0,.163.265l1.359.679a.3.3,0,0,0,.4-.132.3.3,0,0,0-.133-.4l-1.195-.6V1.858A.3.3,0,0,0,3.217,1.562Z"
                          transform="translate(7.504 7.742)"
                          fill="#9d9d9d"
                        />
                      </g>
                    </svg>
                  </a-date-picker>
                  <div
                    class="dropdown-container"
                    v-click-outside="closeTimeDropdownEnd"
                  >
                    <a
                      href="javascript:void(0)"
                      class="time-dropdown"
                      @click="chanegDropdown('end')"
                      :class="{
                        'invalid-field': error.includes('DOWNTIME_DATE'),
                      }"
                      >{{
                        downtime.end.hour >= 10
                          ? downtime.end.hour
                          : "0" + downtime.end.hour
                      }}:{{
                        downtime.end.minute >= 10
                          ? downtime.end.minute
                          : "0" + downtime.end.minute
                      }}</a
                    >
                    <div
                      style="
                        left: auto !important;
                        width: 105px;
                        transform: translate(0px, 10px);
                        overflow-y: unset;
                      "
                      class="dropdown-menu"
                      :class="[selectedDropdown == 'end' ? 'show' : '']"
                    >
                      <div style="display: flex">
                        <div class="hour-dropdown-col">
                          <ul class="hour-dropdown">
                            <li
                              v-for="index in 24"
                              :key="index"
                              :class="{
                                'selected-item': index == downtime.end.hour + 1,
                              }"
                            >
                              <span
                                class="dropdown-item"
                                onmouseover="this.style.color='black';"
                                onmouseout="this.style.color='black';"
                                @click="
                                  handleChangeTime(index - 1, 'end', 'hour')
                                "
                              >
                                {{
                                  `${
                                    index - 1 >= 10
                                      ? index - 1
                                      : "0" + (index - 1)
                                  }`
                                }}
                              </span>
                            </li>
                          </ul>
                        </div>
                        <div class="hour-dropdown-col">
                          <ul class="hour-dropdown">
                            <li
                              v-for="index in 60"
                              :key="index"
                              :class="{
                                'selected-item':
                                  index == downtime.end.minute + 1,
                              }"
                            >
                              <span
                                class="dropdown-item"
                                onmouseover="this.style.color='black';"
                                onmouseout="this.style.color='black';"
                                @click="
                                  handleChangeTime(index - 1, 'end', 'minute')
                                "
                              >
                                {{
                                  `${
                                    index - 1 >= 10
                                      ? index - 1
                                      : "0" + (index - 1)
                                  }`
                                }}
                              </span>
                            </li>
                          </ul>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="new-content-block">
                <label for="downtime-reason">
                  <strong>Reason</strong>
                  <span class="badge-require"></span>
                </label>
                <div class="new-content-detail">
                  <div
                    class="dropdown-container"
                    v-click-outside="closeReasonDropdown"
                  >
                    <a
                      id="downtime-reason"
                      href="javascript:void(0)"
                      class="reason-dropdown"
                      @click="chanegDropdown('reason')"
                      :class="{
                        'invalid-field': error.includes('DOWNTIME_REASON'),
                      }"
                      >{{
                        downtime.reason == ""
                          ? "Click to Enter the Reason"
                          : downtime.reason
                      }}</a
                    >
                    <new-dropdown
                      v-if="selectedDropdown === 'reason'"
                      :items="reasonOptionList"
                      :click-handler="reasonSelect"
                      :searchbox="true"
                      :checkbox="true"
                      :wrap-style="{
                        top: 'calc(100% + 9px)',
                        left: '-140px',
                      }"
                    ></new-dropdown>
                    <!-- <ul
                      style="
                        left: auto !important;
                        transform: translate(50%, 10px);
                      "
                      class="dropdown-menu"
                      :class="[selectedDropdown == 'reason' ? 'show' : '']"
                    >
                      <template v-if="type == 'PLANNED_DOWNTIME'">
                        <li
                          v-for="(reason, index) in reasonsPlanned"
                          :key="index"
                          :class="{
                            'selected-item': reason == downtime.reason,
                          }"
                        >
                          <span
                            class="dropdown-item"
                            onmouseover="this.style.color='black';"
                            onmouseout="this.style.color='black';"
                            @click="handleChangeReason(reason)"
                          >
                            {{ reason }}
                          </span>
                        </li>
                      </template>
                      <template v-if="type == 'UNPLANNED_DOWNTIME'">
                        <li
                          v-for="(reason, index) in reasonsUnplanned"
                          :key="index"
                          :class="{
                            'selected-item': reason == downtime.reason,
                          }"
                        >
                          <span
                            class="dropdown-item"
                            onmouseover="this.style.color='black';"
                            onmouseout="this.style.color='black';"
                            @click="handleChangeReason(reason)"
                          >
                            {{ reason }}
                          </span>
                        </li>
                      </template>
                    </ul> -->
                  </div>
                </div>
              </div>
              <div class="new-content-block">
                <label for="downtime-note">
                  <strong>Note</strong><span>(optional)</span>
                </label>
                <div class="new-content-detail">
                  <a-textarea
                    id="downtime-note"
                    v-model="downtime.note"
                    placeholder="Type your notes here"
                    :rows="5"
                    :auto-size="{ minRows: 5, maxRows: 10 }"
                  />
                </div>
              </div>
            </div>
            <div v-else-if="type == 'DAILY_WORKING_HOUR'" class="new-content">
              <div class="new-content-block">
                <label for="downtime-reason">
                  <strong>Edit Daily Work Hours</strong>
                  <span class="badge-require"></span>
                </label>
                <div class="new-content-detail">
                  <div
                    class="dropdown-container"
                    v-click-outside="closeEditDropdown"
                  >
                    <svg
                      style="
                        position: absolute;
                        z-index: 3;
                        top: 50%;
                        transform: translate(0, -50%);
                        left: 10px;
                      "
                      xmlns="http://www.w3.org/2000/svg"
                      width="14.188"
                      height="14.426"
                      viewBox="0 0 14.188 14.426"
                    >
                      <g
                        id="Component_1_9"
                        data-name="Component 1 – 9"
                        transform="translate(0.25 0.25)"
                      >
                        <path
                          id="Subtraction_4"
                          data-name="Subtraction 4"
                          d="M6.678,11.635H0V10.471L.005,2.327V1.164h1.74V0H2.909V1.164H7.562V0H8.726V1.164h1.745v5.7a4.073,4.073,0,0,0-1.164.243V4.072H1.164v6.4H6.651a4.147,4.147,0,0,0,.026,1.162ZM4.415,9.308h0L2.592,7.562l.317-.339L4.415,8.789,7.6,5.6l.339.269L4.416,9.308Z"
                          transform="translate(0 0)"
                          fill="#9d9d9d"
                          stroke="#9d9d9d"
                          stroke-width="0.5"
                        />
                        <path
                          id="Subtraction_2"
                          data-name="Subtraction 2"
                          d="M3.217,6.433A3.217,3.217,0,1,1,6.433,3.217,3.22,3.22,0,0,1,3.217,6.433Zm0-4.871a.3.3,0,0,0-.3.3V3.9a.3.3,0,0,0,.163.265l1.359.679a.3.3,0,0,0,.4-.132.3.3,0,0,0-.133-.4l-1.195-.6V1.858A.3.3,0,0,0,3.217,1.562Z"
                          transform="translate(7.504 7.742)"
                          fill="#9d9d9d"
                        />
                      </g>
                    </svg>
                    <a
                      id="downtime-reason"
                      href="javascript:void(0)"
                      class="reason-dropdown"
                      @click="chanegDropdown('edit')"
                      :class="{
                        'invalid-field': error.includes('DAILY_HOURS'),
                      }"
                      >{{
                        dailyWorkHour.hours > 1
                          ? dailyWorkHour.hours + " hours per day"
                          : dailyWorkHour.hours + " hour per day"
                      }}</a
                    >
                    <ul
                      style="
                        left: auto !important;
                        transform: translate(50%, 10px);
                      "
                      class="dropdown-menu"
                      :class="[selectedDropdown == 'edit' ? 'show' : '']"
                    >
                      <li
                        v-for="hour in 24"
                        :key="hour"
                        :class="{
                          'selected-item': hour == 25 - dailyWorkHour.hours,
                        }"
                      >
                        <span
                          class="dropdown-item"
                          onmouseover="this.style.color='black';"
                          onmouseout="this.style.color='black';"
                          @click="handleChangeHour(25 - hour)"
                        >
                          {{ 25 - hour >= 10 ? 25 - hour : "0" + (25 - hour) }}
                        </span>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
              <div class="new-content-block">
                <label for="downtime-start-planned">
                  <strong>From</strong>
                  <span class="badge-require"></span>
                </label>
                <div class="new-content-detail">
                  <a-date-picker
                    id="downtime-start-planned"
                    @change="handleChangeDate"
                    :allow-clear="false"
                    format="YYYY-MM-DD (dddd)"
                    v-model="dailyWorkHour.start.date"
                    :class="{ 'invalid-field': error.includes('DAILY_DATE') }"
                  >
                    <svg
                      slot="suffixIcon"
                      xmlns="http://www.w3.org/2000/svg"
                      width="14.188"
                      height="14.426"
                      viewBox="0 0 14.188 14.426"
                    >
                      <g
                        id="Component_1_9"
                        data-name="Component 1 – 9"
                        transform="translate(0.25 0.25)"
                      >
                        <path
                          id="Subtraction_4"
                          data-name="Subtraction 4"
                          d="M6.678,11.635H0V10.471L.005,2.327V1.164h1.74V0H2.909V1.164H7.562V0H8.726V1.164h1.745v5.7a4.073,4.073,0,0,0-1.164.243V4.072H1.164v6.4H6.651a4.147,4.147,0,0,0,.026,1.162ZM4.415,9.308h0L2.592,7.562l.317-.339L4.415,8.789,7.6,5.6l.339.269L4.416,9.308Z"
                          transform="translate(0 0)"
                          fill="#9d9d9d"
                          stroke="#9d9d9d"
                          stroke-width="0.5"
                        />
                        <path
                          id="Subtraction_2"
                          data-name="Subtraction 2"
                          d="M3.217,6.433A3.217,3.217,0,1,1,6.433,3.217,3.22,3.22,0,0,1,3.217,6.433Zm0-4.871a.3.3,0,0,0-.3.3V3.9a.3.3,0,0,0,.163.265l1.359.679a.3.3,0,0,0,.4-.132.3.3,0,0,0-.133-.4l-1.195-.6V1.858A.3.3,0,0,0,3.217,1.562Z"
                          transform="translate(7.504 7.742)"
                          fill="#9d9d9d"
                        />
                      </g>
                    </svg>
                  </a-date-picker>
                </div>
              </div>
              <div class="new-content-block">
                <label for="downtime-end-planned">
                  <strong>To</strong>
                  <span class="badge-require"></span>
                </label>
                <div class="new-content-detail">
                  <a-date-picker
                    id="downtime-end-planned"
                    @change="handleChangeDate"
                    :allow-clear="false"
                    format="YYYY-MM-DD (dddd)"
                    v-model="dailyWorkHour.end.date"
                    :class="{ 'invalid-field': error.includes('DAILY_DATE') }"
                  >
                    <svg
                      slot="suffixIcon"
                      xmlns="http://www.w3.org/2000/svg"
                      width="14.188"
                      height="14.426"
                      viewBox="0 0 14.188 14.426"
                    >
                      <g
                        id="Component_1_9"
                        data-name="Component 1 – 9"
                        transform="translate(0.25 0.25)"
                      >
                        <path
                          id="Subtraction_4"
                          data-name="Subtraction 4"
                          d="M6.678,11.635H0V10.471L.005,2.327V1.164h1.74V0H2.909V1.164H7.562V0H8.726V1.164h1.745v5.7a4.073,4.073,0,0,0-1.164.243V4.072H1.164v6.4H6.651a4.147,4.147,0,0,0,.026,1.162ZM4.415,9.308h0L2.592,7.562l.317-.339L4.415,8.789,7.6,5.6l.339.269L4.416,9.308Z"
                          transform="translate(0 0)"
                          fill="#9d9d9d"
                          stroke="#9d9d9d"
                          stroke-width="0.5"
                        />
                        <path
                          id="Subtraction_2"
                          data-name="Subtraction 2"
                          d="M3.217,6.433A3.217,3.217,0,1,1,6.433,3.217,3.22,3.22,0,0,1,3.217,6.433Zm0-4.871a.3.3,0,0,0-.3.3V3.9a.3.3,0,0,0,.163.265l1.359.679a.3.3,0,0,0,.4-.132.3.3,0,0,0-.133-.4l-1.195-.6V1.858A.3.3,0,0,0,3.217,1.562Z"
                          transform="translate(7.504 7.742)"
                          fill="#9d9d9d"
                        />
                      </g>
                    </svg>
                  </a-date-picker>
                </div>
              </div>
              <div class="new-content-block">
                <label for="downtime-note">
                  <strong>Note</strong><span>(optional)</span>
                </label>
                <div class="new-content-detail">
                  <a-textarea
                    id="downtime-note"
                    v-model="dailyWorkHour.note"
                    placeholder="Type your notes here"
                    :rows="5"
                    :auto-size="{ minRows: 5, maxRows: 10 }"
                  />
                </div>
              </div>
            </div>
          </div>
          <div v-else class="warning-zone">
            <div>
              <svg
                class="warning-icon"
                xmlns="http://www.w3.org/2000/svg"
                width="24.419"
                height="21.545"
                viewBox="0 0 24.419 21.545"
              >
                <path
                  id="Icon_awesome-exclamation-triangle"
                  data-name="Icon awesome-exclamation-triangle"
                  d="M24.145,18.516a2.021,2.021,0,0,1-1.763,3.029H2.037A2.021,2.021,0,0,1,.275,18.516L10.447,1.009a2.044,2.044,0,0,1,3.525,0ZM12.21,14.9a1.936,1.936,0,1,0,1.95,1.936A1.943,1.943,0,0,0,12.21,14.9ZM10.358,7.938l.314,5.723a.507.507,0,0,0,.508.477h2.058a.507.507,0,0,0,.508-.477l.314-5.723a.507.507,0,0,0-.508-.532H10.866A.507.507,0,0,0,10.358,7.938Z"
                  fill="#db3b21"
                />
              </svg>
            </div>
            <template
              v-if="type == 'PLANNED_DOWNTIME' || type == 'UNPLANNED_DOWNTIME'"
            >
              <div class="warning-title">
                <strong>Previously entered downtime has been detected</strong>
              </div>
              <div class="warning-text">
                <strong
                  >Delete past downtime entries, and add new downtime for the
                  {{ warningDays.length > 1 ? "dates" : "date" }} below:
                </strong>
              </div>
            </template>
            <template v-else-if="type == 'DAILY_WORKING_HOUR'">
              <div class="warning-text">
                <strong
                  >Editing Daily Work Hours will result in deletion of
                  previously entered downtime for the
                  {{ warningDays.length > 1 ? "dates" : "date" }} below:
                </strong>
              </div>
            </template>

            <div class="warning-content">
              <div
                v-for="(day, index) in warningDays"
                :key="index"
                class="warning-content-day"
              >
                {{ convertDate(day) }}
              </div>
            </div>
            <div class="warning-ques">
              <strong> Do you want to proceed? </strong>
            </div>
            <div class="warning-action">
              <a
                id="warning-danger-btn"
                href="javascript:void(0)"
                class="warning-button warning-button-danger"
                @click="showWarningAnimation('warning-danger-btn', false)"
                >No. Don’t Delete</a
              >
              <a
                id="warning-confirm-btn"
                href="javascript:void(0)"
                class="warning-button warning-button-confirm"
                @click="showWarningAnimation('warning-confirm-btn', true)"
                >Yes, Proceed</a
              >
            </div>
          </div>
          <div v-if="!warning" class="footer">
            <a
              id="footer-button"
              href="javascript:void(0)"
              class="footer-button"
              :class="{ 'inactive-btn': !checkEnable }"
              @click="showAnimation"
              >{{ getButtonTitle }}</a
            >
          </div>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    updateTable: Function,
  },
  components: {
    "date-filter": httpVueLoader(
      "/components/machine-availability/date-filter.vue"
    ),
    "new-dropdown": httpVueLoader("/components/common/dropdown.vue"),
  },
  data() {
    return {
      reasonOptionList: [], // downtime reason dropdown options list
      visible: false,
      type: "PLANNED_DOWNTIME",
      machineAvailability: {
        actualWorkingHour: 0,
      },
      machineCode: "",
      machineId: "",
      selectedDate: "",
      selectedDateRaw: null,
      downtime: {
        start: {
          date: moment(),
          hour: 0,
          minute: 0,
        },
        end: {
          date: moment(),
          hour: 0,
          minute: 0,
        },
        reason: "",
        note: "",
      },
      reasonsPlanned: ["Maintenance", "Other"],
      reasonsUnplanned: ["Breakdown", "Other"],
      selectedDropdown: "",
      dailyWorkHour: {
        start: {
          date: moment(),
        },
        end: {
          date: moment(),
        },
        hours: "",
        note: "",
      },
      warning: false,
      tempData: null,
      error: [],
      warningDays: [],
    };
  },
  watch: {
    visible(newValue) {
      if (!newValue) {
        this.clearData();
      }
    },
  },
  computed: {
    getButtonTitle() {
      switch (this.type) {
        case "PLANNED_DOWNTIME":
          return "Add Planned Downtime";
        case "UNPLANNED_DOWNTIME":
          return "Add Unplanned Downtime";
        case "DAILY_WORKING_HOUR":
          return "Edit Daily Work Hours";
      }
    },
    checkEnable() {
      let start = "";
      let end = "";
      let isValid = true;
      switch (this.type) {
        case "PLANNED_DOWNTIME":
          start = `${moment(this.downtime.start.date).format("YYYYMMDD")}${
            this.downtime.start.hour >= 10
              ? this.downtime.start.hour
              : "0" + this.downtime.start.hour
          }0000`;
          end = `${moment(this.downtime.end.date).format("YYYYMMDD")}${
            this.downtime.end.hour >= 10
              ? this.downtime.end.hour
              : "0" + this.downtime.end.hour
          }0000`;

          if (start > end) {
            isValid = false;
          }
          if (this.downtime.reason == "") {
            isValid = false;
          }
          break;
        case "UNPLANNED_DOWNTIME":
          start = `${moment(this.downtime.start.date).format("YYYYMMDD")}${
            this.downtime.start.hour >= 10
              ? this.downtime.start.hour
              : "0" + this.downtime.start.hour
          }0000`;
          end = `${moment(this.downtime.end.date).format("YYYYMMDD")}${
            this.downtime.end.hour >= 10
              ? this.downtime.end.hour
              : "0" + this.downtime.end.hour
          }0000`;

          if (start > end) {
            isValid = false;
          }
          if (this.downtime.reason == "") {
            isValid = false;
          }
          break;
        case "DAILY_WORKING_HOUR":
          start = `${moment(this.dailyWorkHour.start.date).format(
            "YYYYMMDD"
          )}000000`;
          end = `${moment(this.dailyWorkHour.end.date).format(
            "YYYYMMDD"
          )}000000`;
          if (start > end) {
            isValid = false;
          }
          if (this.dailyWorkHour.hours == "") {
            isValid = false;
          }
          break;
      }
      return isValid;
    },
  },
  methods: {
    // get downtime reason option
    getReasonOption(type) {
      // 임의 통신.
      // 추후에 API merge 되면 수정

      if (this.type === "PLANNED_DOWNTIME") {
        // api: /api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500&group1Code=UNPLANNED
        // dummy:  ../../json/planned.json
        axios
          .get(
            "/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500&group1Code=PLANNED"
          )
          .then((res) => {
            this.reasonOptionList = res.data.content;
            console.log("reasonOptionList: ", this.reasonOptionList);
          });
      }

      if (this.type === "UNPLANNED_DOWNTIME") {
        // api: /api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500&group1Code=UNPLANNED
        // dummy:  ../../json/unplanned.json
        axios
          .get(
            "/api/common/cod-viw/code-types/MACHINE_DOWNTIME_REASON/data?size=500&group1Code=UNPLANNED"
          )
          .then((res) => {
            this.reasonOptionList = res.data.content;
            console.log("reasonOptionList: ", this.reasonOptionList);
          });
      }
    },
    // down time reason clickhandler
    reasonSelect(event) {
      let downtime = JSON.parse(event.target.value);
      this.downtime.reason = downtime.title;
      this.downtime.reasonCode = downtime.code;
      this.selectedDropdown = "";
    },
    showModal(machineAvailability, type, date) {
      console.log("date send", machineAvailability, type, date);
      this.type = type;
      this.machineCode = machineAvailability.machine.machineCode;
      this.machineId = machineAvailability.machine.id;
      this.selectedDate = date.date;
      this.selectedDateRaw = date.rawDate;
      this.machineAvailability = machineAvailability;
      console.log("modal data", {
        machineAvailability: machineAvailability,
        type: type,
        date: date,
      });
      this.getMachineAvailability()
        .then((response) => {
          this.machineAvailability = response.data;
          this.machineId = this.machineAvailability.machine.id;
          console.log("response", this.machineAvailability);
          this.clearData();
          this.visible = true;
        })
        .catch((error) => {
          console.log(error);
        });
      this.changeType(this.type);
    },
    getNoteItemByType(type) {
      if (this.machineAvailability.downtimeItems) {
        let element = this.machineAvailability.downtimeItems.map(
          (item) => item.type == type
        );
        return element;
      }
      return null;
    },
    closeModal() {
      const modalBody = document.querySelectorAll(".ant-modal-content");
      if (modalBody) {
        const el = modalBody[0];
        el.classList.add("fade-out-animate");
        setTimeout(() => {
          el.classList.add("hidden");
          setTimeout(() => {
            this.visible = false;
            this.clearData();
            setTimeout(() => {
              el.classList.remove("hidden");
            }, 200);
            setTimeout(() => {
              el.classList.remove("fade-out-animate");
            }, 50);
          }, 250);
        }, 250);
      }
    },
    changeType(type) {
      this.type = type;
      if (
        this.type === "PLANNED_DOWNTIME" ||
        this.type === "UNPLANNED_DOWNTIME"
      ) {
        console.log("type: ", this.type);
        this.getReasonOption(this.type);
      }
      this.clearData();
    },
    clearData() {
      this.downtime = {
        start: {
          date: moment(),
          hour: 0,
          minute: 0,
        },
        end: {
          date: moment(),
          hour: 0,
          minute: 0,
        },
        reason: "",
        note: "",
      };
      this.dailyWorkHour = {
        start: {
          date: this.selectedDateRaw,
        },
        end: {
          date: this.selectedDateRaw,
        },
        hours: "24",
        note: "",
      };
      this.error = [];
      this.selectedDropdown = "";
      this.warning = false;
    },
    handleChangeDate(date, dateString) {
      console.log(date, dateString);
    },
    disabledDate(current) {
      // Can not select days before today and today
      return current && current > moment().endOf("day");
    },
    chanegDropdown(type) {
      if (type && type != this.selectedDropdown) {
        this.selectedDropdown = type;
      } else {
        this.selectedDropdown = "";
      }
      console.log("dropdown type", this.selectedDropdown);
    },
    handleChangeReason(reason) {
      if (this.downtime.reason != reason) {
        this.downtime.reason = reason;
        this.selectedDropdown = "";
      }
    },
    handleChangeTime(time, type, hourType) {
      if (this.downtime[type][hourType] != time) {
        this.downtime[type][hourType] = time;
        this.selectedDropdown = "";
      }
      // switch (type) {
      //   case 'start':
      //     if (this.downtime.start.hour != time) {
      //       this.downtime.start.hour = time
      //     }
      //     break;
      //   case 'end':
      //     if (this.downtime.end.hour != time) {
      //       this.downtime.end.hour = time
      //     }
      //     break;
      // }
    },
    handleChangeHour(hour) {
      this.dailyWorkHour.hours = hour;
      this.selectedDropdown = "";
    },
    submit() {
      let payloadData = {};
      let start = "";
      let end = "";
      let isValid = true;
      switch (this.type) {
        case "PLANNED_DOWNTIME":
          start = `${moment(this.downtime.start.date).format("YYYYMMDD")}${
            this.downtime.start.hour >= 10
              ? this.downtime.start.hour
              : "0" + this.downtime.start.hour
          }${
            this.downtime.start.minute >= 10
              ? this.downtime.start.minute
              : "0" + this.downtime.start.minute
          }00`;
          end = `${moment(this.downtime.end.date).format("YYYYMMDD")}${
            this.downtime.end.hour >= 10
              ? this.downtime.end.hour
              : "0" + this.downtime.end.hour
          }${
            this.downtime.end.minute >= 10
              ? this.downtime.end.minute
              : "0" + this.downtime.end.minute
          }00`;

          if (start > end) {
            isValid = false;
            // this.error.push("DOWNTIME_DATE");
          }
          if (this.downtime.reasonCode == "") {
            isValid = false;
            // this.error.push("DOWNTIME_REASON");
          }
          if (isValid) {
            payloadData = {
              machineId: this.machineId,
              startDateStr: start,
              endDateStr: end,
              plannedDowntimeType: this.downtime.reasonCode.toUpperCase(),
              plannedDowntimeNote: this.downtime.note,
            };
          }

          break;
        case "UNPLANNED_DOWNTIME":
          start = `${moment(this.downtime.start.date).format("YYYYMMDD")}${
            this.downtime.start.hour >= 10
              ? this.downtime.start.hour
              : "0" + this.downtime.start.hour
          }${
            this.downtime.start.minute >= 10
              ? this.downtime.start.minute
              : "0" + this.downtime.start.minute
          }00`;
          end = `${moment(this.downtime.end.date).format("YYYYMMDD")}${
            this.downtime.end.hour >= 10
              ? this.downtime.end.hour
              : "0" + this.downtime.end.hour
          }${
            this.downtime.end.minute >= 10
              ? this.downtime.end.minute
              : "0" + this.downtime.end.minute
          }00`;

          if (start > end) {
            isValid = false;
            // this.error.push("DOWNTIME_DATE");
          }
          if (this.downtime.reasonCode == "") {
            isValid = false;
            // this.error.push("DOWNTIME_REASON");
          }
          if (isValid) {
            payloadData = {
              machineId: this.machineId,
              startDateStr: start,
              endDateStr: end,
              unplannedDowntimeType: this.downtime.reasonCode.toUpperCase(),
              unplannedDowntimeNote: this.downtime.note,
            };
          }
          break;
        case "DAILY_WORKING_HOUR":
          start = `${moment(this.dailyWorkHour.start.date).format(
            "YYYYMMDD"
          )}000000`;
          end = `${moment(this.dailyWorkHour.end.date).format(
            "YYYYMMDD"
          )}000000`;
          if (start > end) {
            isValid = false;
            // this.error.push("DAILY_DATE");
          }
          if (this.dailyWorkHour.hours == "") {
            isValid = false;
            // this.error.push("DAILY_HOURS");
          }
          if (isValid) {
            payloadData = {
              machineId: this.machineId,
              startDateStr: start,
              endDateStr: end,
              dailyWorkingHour: this.dailyWorkHour.hours,
              dailyWorkingHourNote: this.dailyWorkHour.note,
            };
          }

          break;
      }
      console.log(`submit data - type: ${this.type}`, payloadData);
      if (isValid) {
        this.checkConflic(payloadData)
          .then((response) => {
            console.log(response);
            if (!response.data.data.warning) {
              console.log(1, response.data.data.warning);
              this.confirmSubmit(payloadData);
            } else {
              console.log(2, response.data.data.warning);
              this.warning = true;
              this.tempData = payloadData;
              this.warningDays = response.data.data.days;
            }
          })
          .catch((error) => {
            console.log(error);
          });
      }
    },
    async getMachineAvailability() {
      let paramData = {
        machineId: this.machineId,
        day: this.selectedDate,
      };
      const param = Common.param(paramData);
      const data = await axios.get(`/api/machines/statistics/get-one?${param}`);
      return data;
    },
    async checkConflic(data) {
      const response = await axios.post(
        `/api/machines/statistics/check-conflict?type=${this.type}`,
        data
      );
      return response;
    },
    async updateDowntime(data) {
      const response = await axios.post(
        `/api/machines/statistics/update-config?type=${this.type}`,
        data
      );
      return response;
    },
    confirmSubmit(data) {
      this.updateDowntime(data)
        .then((res) => {
          console.log("post data", res);
          if (res.statusText == "OK") {
            this.getMachineAvailability()
              .then((response) => {
                this.machineAvailability = response.data;
                this.machineId = this.machineAvailability.machine.id;
                console.log("response", this.machineAvailability);
              })
              .catch((error) => {
                console.log(error);
              });
            this.updateTable();
            this.closeModal();
            this.warning = false;
          }
        })
        .catch((error) => {
          console.log(error);
        });
    },
    filterWithDate(date, rawDate) {
      console.log(date, rawDate);
      this.selectedDate = date;
      this.selectedDateRaw = rawDate;
      this.getMachineAvailability()
        .then((response) => {
          this.machineAvailability = response.data;
          this.machineId = this.machineAvailability.machine.id;
          this.clearData();
          console.log("response", this.machineAvailability);
        })
        .catch((error) => {
          console.log(error);
        });
      // this.paging(1);
    },
    convertDate(rawDate) {
      return `${rawDate.slice(0, 4)}-${rawDate.slice(4, 6)}-${rawDate.slice(
        6,
        8
      )}`;
    },
    showAnimation() {
      const el = document.getElementById("footer-button");
      if (el) {
        el.classList.add("footer-button-animation");
        setTimeout(() => {
          el.classList.remove("footer-button-animation");
          this.submit();
        }, 700);
      }
    },
    showWarningAnimation(id, type) {
      const el = document.getElementById(id);
      if (el) {
        if (type) {
          el.classList.add("warning-button-success-animation");
          setTimeout(() => {
            el.classList.remove("warning-button-success-animation");
            this.confirmSubmit(this.tempData);
          }, 700);
        } else {
          el.classList.add("warning-button-danger-animation");
          setTimeout(() => {
            el.classList.remove("warning-button-danger-animation");
            this.warning = false;
          }, 700);
        }
      }
    },
    closeTimeDropdownStart() {
      if (this.selectedDropdown == "start") {
        this.selectedDropdown = "";
      }
    },
    closeTimeDropdownEnd() {
      if (this.selectedDropdown == "end") {
        this.selectedDropdown = "";
      }
    },
    closeReasonDropdown() {
      console.log("outside", this.selectedDropdown);
      if (this.selectedDropdown == "reason") {
        this.selectedDropdown = "";
      }
    },
    closeEditDropdown() {
      console.log("outside", this.selectedDropdown);
      if (this.selectedDropdown == "edit") {
        this.selectedDropdown = "";
      }
    },
  },
};
</script>
<style>
.ant-modal-body {
  padding: 0;
}
</style>
<style scope>
.ant-select-dropdown {
  z-index: 99999999;
}
.ant-select-dropdown-menu-item-selected {
  font-weight: 400;
  background-color: #fff;
}
.custom-modal-body {
  padding: 35px 55px 65px 55px;
}
.head-line {
  left: 0;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
}
.modal-title {
  font-weight: bold;
  margin-top: 12px;
  margin-left: 30px;
  margin-bottom: 12px;
  font-size: 16px;
}
.custom-modal-title {
  border-radius: 6px 6px 0 0;
  background: #f5f8ff;
  color: #4b4b4b;
  display: flex;
  justify-content: space-between;
  position: relative;
  align-items: end;
  padding: 8px 15px 0 0;
  align-items: center;
}
.type-bar {
  list-style-type: none;
  display: flex;
  padding: 0;
}
.nav-button {
  font-size: 12px;
  color: #8b8b8b;
  border: 1px solid #d0d0d0;
  padding: 6px 20px;
  text-decoration: none !important;
}
.nav-button:hover {
  color: #3585e5;
  background-color: #deedff;
}
.type-bar li:first-child .nav-button {
  border-radius: 3px 0 0 3px;
}
.type-bar li:last-child .nav-button {
  border-radius: 0 3px 3px 0;
}
.nav-button-selected {
  color: #3491ff !important;
  background-color: #deedff;
  border: 1px solid #3491ff !important;
}
.machine-title {
  font-size: 16px;
  color: #595959;
  margin-bottom: 30px;
}
.machine-title strong {
  margin-right: 16px;
}
.custom-modal-body-content-detail {
  display: flex;
  justify-content: space-between;
}
.status-bar {
  color: #595959;
  display: flex;
  align-items: center;
  margin-bottom: 27px;
}
.current-content-title {
  font-size: 16px;
  color: #13599b;
}
.current-content {
  height: 304px;
  width: 492px;
  color: #13599b;
  background-color: #fcfcfc;
  overflow-y: auto;
  padding: 10px 30px;
}
.current-content-empty {
  height: 343px;
}
.new-content {
  width: 355px;
}
.new-content-block {
  color: #595959;
  margin-bottom: 20px;
}
.new-content-block strong {
  margin-bottom: 20px;
}
.new-content-detail {
  display: flex;
  justify-content: space-between;
}
.time-dropdown,
.reason-dropdown,
.ant-input:not(.tool-wrapper .ant-input) {
  font-size: 14px;
  color: #3491fb;
  text-decoration: none;
  background-color: #f5f5f5;
  border: none;
  line-height: 16px;
  border-radius: 3px;
  cursor: pointer;
  text-align: left !important;
}
.time-dropdown {
  padding: 9px 0 9px 15px;
  width: 105px;
}
.ant-input,
.reason-dropdown {
  padding: 9px 0 9px 35px;
  width: 230px;
}
.time-dropdown:hover,
.reason-dropdown:hover,
.ant-input:not(.tool-wrapper .ant-input, textarea.ant-input):hover {
  text-decoration: underline !important;
  color: #0e65c7;
}
.dropdown-container {
  position: relative;
  width: 105px;
  height: 34px;
}
.dropdown-container a {
  position: absolute;
}
.ant-input::placeholder {
  font-size: 14px;
  color: #3491fb;
}
.ant-calendar-picker-icon {
  left: 10px;
}
.ant-calendar-footer,
.ant-calendar-input-wrap,
.ant-calendar-next-year-btn,
.ant-calendar-prev-year-btn {
  display: none !important;
}
.ant-calendar-next-month-btn {
  right: 7px !important;
  transform: scale(1.8);
}
.ant-calendar-prev-month-btn {
  left: 7px !important;
  transform: scale(1.8);
}
.ant-calendar-date {
  border-radius: 50%;
}
.ant-calendar-selected-day
  .ant-calendar-date:not(.ant-calendar-disabled-cell .ant-calendar-date) {
  color: #fff !important;
  background-color: #519ffc !important;
  border: 1px solid #519ffc !important;
}
.ant-calendar-today .ant-calendar-date {
  border: none;
  background-color: unset;
  color: #5790ff;
  font-weight: normal;
}
.ant-calendar-column-header .ant-calendar-column-header-inner {
  font-weight: bold;
}
.ant-calendar-header {
  border: none;
}
.ant-calendar-disabled-cell .ant-calendar-date {
  color: #d5d5d5;
  border: none;
  background-color: unset;
}
.footer {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.footer-button {
  padding: 6px 8px;
  background-color: #3491ff;
  border-radius: 3px;
  color: #fff;
  text-decoration: none !important;
}
.footer-button-disable {
  padding: 6px 8px;
  background-color: #c4c4c4;
  color: #fff;
  cursor: pointer;
}
.footer-button:hover {
  background-color: #0e65c7;
  color: #fff;
}
.footer-button-animation {
  animation: footer-primary-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  outline: none;
  background-color: #3585e5 !important;
}
@keyframes footer-primary-animation {
  0% {
  }
  33% {
    box-shadow: 0 0 0 0.2rem #89d1fd;
  }
  66% {
    box-shadow: 0 0 0 0.2rem #89d1fd;
  }
  100% {
  }
}

textarea.ant-input {
  width: 100%;
  resize: none;
  border: 1px solid #909090 !important;
  font-size: 15px;
  background-color: #fff !important;
  text-decoration: none !important;
  padding: 13px;
  color: #595959 !important;
}
textarea.ant-input::placeholder {
  color: #aaaaaa !important;
  font-size: 15px;
  text-decoration: none !important;
}
.dropdown-menu:not(.header-dropdown) {
  transform: translate(1px, 10px);
  max-height: 145px;
  overflow-y: auto;
}
.warning-zone {
  padding: 35px 250px 65px 250px;
  background-color: #fcfcfc;
  text-align: center;
}

.warning-text,
.warning-title,
.warning-ques {
  font-size: 16px;
  margin-bottom: 20px;
  text-align: center;
}
.warning-text {
  color: #4b4b4b;
}
.warning-title {
  color: #ff0000;
}
.warning-ques {
  color: #000000;
}
.warning-icon {
  margin-bottom: 27px;
}
.warning-content {
  background-color: #f2f2f2;
  text-decoration: none;
  margin-bottom: 20px;
  height: 152px;
  padding: 20px 0;
  text-align: center;
  overflow-y: auto;
}
.warning-content-day {
  color: #0e65c7;
  font-weight: 500;
  margin-bottom: 10px;
}
.warning-action {
  display: flex;
  justify-content: space-between;
  padding: 0 30px;
  text-decoration: none !important;
}
.warning-button {
  font-size: 14px;
  padding: 6px 8px;
  border-radius: 3px;
  text-decoration: none !important;
}
.warning-button-danger {
  color: #db3b21;
  background-color: #fff;
}
.warning-button-danger:hover {
  color: #db3b21;
  background: #f8f8f8 !important;
}
.warning-button-danger-animation {
  animation: danger-button-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  outline: none;
}
@keyframes danger-button-animation {
  0% {
  }
  33% {
    box-shadow: 0 0 0 0.2rem #db3b21;
  }
  66% {
    box-shadow: 0 0 0 0.2rem #db3b21;
  }
  100% {
  }
}
.warning-button-confirm {
  color: #fff;
  background-color: #47b576;
}
.warning-button-confirm:hover {
  color: #fff;
  background-color: #3ea662;
  border-color: #3a9d5d;
}
.warning-button-success-animation {
  animation: success-button-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  outline: none;
}
@keyframes success-button-animation {
  0% {
  }
  33% {
    box-shadow: 0 0 0 0.2rem rgb(77 189 116 / 50%);
  }
  66% {
    box-shadow: 0 0 0 0.2rem rgb(77 189 116 / 50%);
  }
  100% {
  }
}
.custom-modal-body-content .right-tool {
  display: unset;
}
.date-filter-modal .tool-wrapper .date-filter svg:not(.arrow-icon) {
  display: none;
}
.date-filter-modal .tool-wrapper {
  border: 1px solid #3491fb;
}
.date-filter-modal .tool-wrapper .ant-input {
  color: #3491ff;
}
.date-filter-modal svg.arrow-icon {
  top: 50%;
  right: 10px;
  position: absolute;
  transform: translate(0, -50%);
}
.invalid-field {
  border: 1px solid red;
  border-radius: 3px;
}
.inactive-btn {
  background-color: #c4c4c4;
  pointer-events: none;
  color: #fff;
}
.hidden {
  opacity: 0 !important;
}
.ant-modal-content {
  /* opacity: 0; */
  z-index: 3;
}
.fade-out-animate {
  -webkit-animation: fadeout 0.5s; /* Safari, Chrome and Opera > 12.1 */
  -moz-animation: fadeout 0.5s; /* Firefox < 16 */
  -ms-animation: fadeout 0.5s; /* Internet Explorer */
  -o-animation: fadeout 0.5s; /* Opera < 12.1 */
  animation: fadeout 0.5s;
  animation-iteration-count: 1;
}
@keyframes fadeout {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* Firefox < 16 */
@-moz-keyframes fadeout {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* Safari, Chrome and Opera > 12.1 */
@-webkit-keyframes fadeout {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* Internet Explorer */
@-ms-keyframes fadeout {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}

/* Opera < 12.1 */
@-o-keyframes fadeout {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
.dropdown-menu .hour-dropdown .selected-item,
.dropdown-menu:not(.header-dropdown) .hour-dropdown .dropdown-item {
  width: 46px !important;
  min-width: 46px !important;
}
.hour-dropdown {
  list-style: none;
  padding: 0;
  width: 50px;
}
.hour-dropdown-col {
  overflow-y: auto;
  max-height: 145px;
}
.hour-dropdown-col:last-child {
  margin-left: 1px;
}
.hour-dropdown-col::-webkit-scrollbar {
  width: 4px;
  background-color: #fff;
}
.hour-dropdown-col::-webkit-scrollbar-thumb {
  background: #f5f5f5;
}
.current-content .actual-hour-popup-sub-title {
  color: #555555;
}
.current-content .actual-hour-popup-type {
  border: 1px solid #555555;
}
</style>


