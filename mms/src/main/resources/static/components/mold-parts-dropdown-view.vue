<template>
  <div class="text-left" style="max-width: 600px">
    <div v-if="parts.length == 1">
      <div>
        <a
          href="#"
          @click.prevent="showPartChart(parts[0], mold)"
          class="font-size-14"
          :class="{ 'underline--none text--dark-grey': !hyperlink }"
          v-bind:data-tooltip-text="parts[0][titleField].trim()"
        >
          {{ replaceLongtext(parts[0][titleField], 20) }}
        </a>
      </div>

      <span
        class="font-size-11-2"
        v-bind:data-tooltip-text="parts[0].partCode.trim()"
      >
        {{ replaceLongtext(parts[0].partCode, 20) }}
      </span>
    </div>
    <div
      :id="'part-' + index"
      v-else-if="parts.length > 1"
      style="position: relative"
    >
      <span v-if="getActivePart(parts) == null">
        <!-- <label class="part-name">
                                                            {{parts[0].partName}}
                                                        </label> -->
        <span class="one-line" v-bind:title="parts[0][titleField].trim()">
          <a
            href="#"
            @click.prevent="showPartChart(parts[0], mold)"
            :class="{ 'underline--none text--dark-grey': !hyperlink }"
            v-bind:data-tooltip-text="parts[0][titleField]"
          >
            {{ replaceLongtext(parts[0][titleField], 20) }}
          </a>
          <span>
            <svg
              @click.stop="showParts(index)"
              style="cursor: pointer"
              :id="'arrow-' + index"
              class="arrow"
              xmlns="http://www.w3.org/2000/svg"
              width="9.973"
              height="5.737"
              viewBox="0 0 9.973 5.737"
            >
              <path
                id="Icon_feather-chevron-down"
                data-name="Icon feather-chevron-down"
                d="M9,13.5l3.926,3.926L16.852,13.5"
                transform="translate(-7.939 -12.439)"
                fill="none"
                stroke="#595959"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.5"
              />
            </svg>
          </span>
          <span class="plus-circle">+{{ parts.length - 1 }} </span>

          <div
            id="dropdown-parts"
            v-show="selectedDropdown == 'arrow-' + index"
            v-click-outside="closeDropDown"
            class="dropdown-parts"
            :style="customStyle"
          >
            <div
              v-for="part in parts"
              :key="part.partId"
              :id="part.partId"
              class="single-part"
              :class="{ 'active-part': part.cavity > 0 }"
            >
              <div v-if="part.cavity > 0" class="tag-active"></div>
              <span v-if="part.cavity > 0" class="text-active"> ACTIVE </span>
              <div style="line-height: 17px">
                <a
                  href="#"
                  @click.prevent="showPartChart(part, mold)"
                  class="font-size-14"
                  style="color: #43a1fc"
                  v-bind:data-tooltip-text="part[titleField]"
                  :class="{ 'underline--none text--dark-grey': !hyperlink }"
                >
                  {{ replaceLongtext(part[titleField], 40) }}
                </a>
              </div>
              <p
                class="part-name-drop-down one-line font-size-11-2"
                v-bind:title="part[titleField]"
              >
                {{ replaceLongtext(part.partCode, 20) }}
              </p>
            </div>
          </div>
        </span>

        <div class="font-size-11-2">
          {{ replaceLongtext(parts[0].partCode, 20) }}
        </div>
      </span>
      <span v-else>
        <span
          class="one-line"
          v-bind:title="getActivePart(parts)[0][titleField]"
        >
          <a
            href="#"
            @click.prevent="showPartChart(getActivePart(parts)[0], mold)"
            :class="{ 'underline--none text--dark-grey': !hyperlink }"
            v-bind:data-tooltip-text="getActivePart(parts)[0][titleField]"
          >
            {{ replaceLongtext(getActivePart(parts)[0][titleField], 20) }}
          </a>
          <span>
            <svg
              @click.stop="showParts(index)"
              style="cursor: pointer"
              :id="'arrow-' + index"
              class="arrow"
              xmlns="http://www.w3.org/2000/svg"
              width="9.973"
              height="5.737"
              viewBox="0 0 9.973 5.737"
            >
              <path
                id="Icon_feather-chevron-down"
                data-name="Icon feather-chevron-down"
                d="M9,13.5l3.926,3.926L16.852,13.5"
                transform="translate(-7.939 -12.439)"
                fill="none"
                stroke="#595959"
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="1.5"
              />
            </svg>
          </span>
          <span class="plus-circle">+{{ parts.length - 1 }} </span>

          <div
            id="dropdown-parts"
            v-if="selectedDropdown == 'arrow-' + index"
            class="dropdown-parts"
            :style="customStyle"
            v-click-outside="closeDropDown"
          >
            <div
              v-for="part in parts"
              :key="part.partId"
              :id="part.partId"
              class="single-part"
              :class="{ 'active-part': part.cavity > 0 }"
            >
              <div v-if="part.cavity > 0" class="tag-active"></div>
              <span v-if="part.cavity > 0" class="text-active"> ACTIVE </span>
              <div style="line-height: 17px">
                <a
                  href="#"
                  @click.prevent="showPartChart(part, mold)"
                  class="font-size-14"
                  style="color: #43a1fc"
                  v-bind:data-tooltip-text="part[titleField]"
                  :class="{ 'underline--none text--dark-grey': !hyperlink }"
                >
                  {{ replaceLongtext(part[titleField], 40) }}
                </a>
              </div>
              <p
                class="part-name-drop-down one-line font-size-11-2"
                v-bind:data-tooltip-text="part.partCode"
              >
                {{ replaceLongtext(part.partCode, 20) }}
              </p>
            </div>
          </div>
        </span>

        <div>
          <span class="font-size-11-2">
            {{ replaceLongtext(getActivePart(parts)[0].partCode, 20) }}
          </span>
        </div>
      </span>
    </div>
  </div>
</template>
<script>
// import ChartPart from "chart-part-tool.vue";
module.exports = {
  props: {
    parts: Array,
    mold: Object,
    index: Number,
    showPartChart: Function,
    titleField: {
      type: String,
      default: "partName",
    },
    hyperlink: {
      type: Boolean,
      default: true,
    },
    customStyle: Object,
  },
  components: {
    // ChartPart,
  },
  data() {
    return {
      dropdownOpening: false,
      showExportModal: false,
      selectedDropdown: null,
      nextSelectDropdown: null,
      isShowingDropDown: false,
      showingClass: false,
    };
  },
  methods: {
    getActivePart(parts) {
      let result = parts.filter((item) => item.cavity > 0);
      // console.log(result)
      if (result.length > 0) {
        return result;
      } else {
        return null;
      }
    },
    showParts(index) {
      console.log("Click");
      const nextElement = document.getElementById(`arrow-${index}`);
      if (this.isShowingDropDown) {
        const currentElement = document.getElementById(this.selectedDropdown);
        currentElement.classList.remove("arrow-active");
        if (this.selectedDropdown != `arrow-${index}`) {
          nextElement.classList.add("arrow-active");
          this.nextSelectDropdown = `arrow-${index}`;
        } else {
          this.isShowingDropDown = false;
          this.selectedDropdown = null;
          this.nextSelectDropdown = null;
          this.showingClass = false;
        }
      } else {
        nextElement.classList.add("arrow-active");
        this.selectedDropdown = `arrow-${index}`;
        this.isShowingDropDown = true;
      }
      if (this.selectedDropdown != null && this.isShowingDropDown != false) {
      } else {
      }
      setTimeout(() => {
        let dropDown = document.getElementById("dropdown-parts");
        if (dropDown) {
          dropDown.scrollIntoView({
            behavior: "smooth",
            block: "center",
            inline: "nearest",
          });
        }
      }, 1);
    },
    closeDropDown(event) {
      const el = document.getElementById(this.selectedDropdown);
      if (this.selectedDropdown != null) {
        el.classList.remove("arrow-active");
        if (this.nextSelectDropdown != null) {
          this.selectedDropdown = this.nextSelectDropdown;
          this.nextSelectDropdown = null;
        } else {
          this.selectedDropdown = null;
          this.isShowingDropDown = false;
          this.showingClass = false;
        }
      }
    },
  },
};
</script>

<style>
.single-part {
  padding: 10px 31px 10px 30px;
  text-align: left;
  min-width: 100%;
  width: 100%;
  min-width: 210px;
}

.active-part {
  position: relative;
  background-color: #e5f7ed;
}

.dropdown-parts {
  position: absolute;
  padding: 4px 4px 4px 5px;
  background-color: #ffffff;
  box-shadow: 0px 3px 6px #00000029;
  width: auto;
  z-index: 3;
  /* overflow-x: auto;
  max-width: 350px;
  transition-delay: 200ms display; */
  left: 50%;
  transform: translate(-50%, 0);
  min-width: 150px;
}

.arrow {
  transition-property: transform;
  transition-duration: 0.2s;
  transition-timing-function: linear;
}

.arrow-active {
  transform: rotate(180deg);
}

.tag-active {
  position: absolute;
  top: 0;
  right: 0;
  height: 0;
  width: 55px;
  background-color: #e5f7ed;
  border-top: 16px solid #5d5d5d;
  border-left: 12px solid transparent;
  border-right: 0px solid transparent;
  z-index: 1;
}

.text-active {
  position: absolute;
  text-align: right;
  width: 55px;
  line-height: 16px;
  font-size: 11px;
  z-index: 2;
  color: #ffffff;
  top: 0;
  right: 2px;
}

.part-name-drop-down {
  line-height: 17px;
  margin: 0px;
  font-size: 14px;
}

.one-line {
  white-space: nowrap;
}

.plus-circle {
  color: #ffffff;
  font-size: 11px;
  /* padding: 3px 4px 4px 2px; */
  border-radius: 19px;
  background-color: #43a1fc;
  width: 19px;
  height: 19px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
</style>
