<template>
  <div v-if="reasons.length === 1">
    <a-tooltip v-if="reasons[0].reason.length > 20" placement="bottom">
      <template slot="title">
        <div style="padding: 6px 8px;">{{ reasons[0].reason }}</div>
      </template>
      <span style="color: #3491FF">{{ replaceLongtext(reasons[0].reason, 20) }}</span>
    </a-tooltip>
    <div v-else style="color: #3491FF"> {{ reasons[0].reason }} </div>
  </div>
  <div :id="'reason-' + index" v-else-if="reasons.length > 1" style="position: relative">
    <span>
      <span class="one-line">
        <span style="color: #3491FF">{{ reasons[0].reason }}</span>
        <span>
          <svg @click="showReason(index)" style="cursor: pointer" :id="'arrow-' + index" class="arrow" xmlns="http://www.w3.org/2000/svg" width="9.973" height="5.737" viewBox="0 0 9.973 5.737" v-click-outside="closeDropDown">
            <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down" d="M9,13.5l3.926,3.926L16.852,13.5" transform="translate(-7.939 -12.439)" fill="none" stroke="#595959" stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" />
          </svg>
        </span>
        <span style="
            color: #ffffff;
            font-size: 11px;
            padding: 3px 4px 4px 2px;
            border-radius: 50%;
            background-color: #43a1fc;
          ">+{{ reasons.length - 1 }} </span>
        <div id="dropdown-parts" v-if="selectedDropdown === 'arrow-' + index" class="dropdown-parts">
          <div style="padding: 5px 8px">
            <div style="border-bottom: 1px solid #cbcbcb" class="d-flex align-items-center justify-content-between">
              <b>{{ hourConverted }}</b>
              <b>{{ totalRejectAmount }} {{ totalRejectAmount > 1 ? 'parts' : 'part' }}</b>
            </div>
          </div>
          <div class="reason-wrapper">
            <div v-for="(reason, index) in reasons" :id="reason.id" class="single-part" :key="index">
              <div v-if="reason.reason.length > 20" class="d-flex align-items-center justify-content-between part-name-drop-down one-line">
                <a-tooltip placement="bottom">
                  <template slot="title">
                    <div style="padding: 6px 8px;">{{ reason.reason }}</div>
                  </template>
                  <span>{{ replaceLongtext(reason.reason, 20) }}</span>
                </a-tooltip>
                <span>{{ reason.rejectedAmount }} {{ reason.rejectedAmount > 1 ? 'Parts' : 'Part' }}</span>
              </div>
              <div v-else class="d-flex align-items-center justify-content-between part-name-drop-down one-line">
                <span>{{ reason.reason }}</span>
                <span>{{ reason.rejectedAmount }} {{ reason.rejectedAmount > 1 ? 'Parts' : 'Part' }}</span>
              </div>
            </div>
          </div>
        </div>
      </span>
    </span>
  </div>
</template>

<script>
module.exports = {
  props: {
    reasons: Array,
    index: Number,
    totalRejectAmount: Number,
    hourConverted: String
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
    showReason(index) {
      console.log("Click");
      const nextElement = document.getElementById(`arrow-${index}`);
      if (this.isShowingDropDown) {
        const currentElement = document.getElementById(this.selectedDropdown);
        currentElement.classList.remove("arrow-active");
        if (this.selectedDropdown !== `arrow-${index}`) {
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
      if (this.selectedDropdown != null && this.isShowingDropDown !== false) {
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
.reason-wrapper {
  height: 70px;
  overflow-y: scroll;
}

.single-part {
  padding: 4px 8px 4px 8px;
  text-align: left;
  min-width: 100%;
  width: fit-content;
}

.single-part:hover {
  background-color: #e6f7ff;
}

.active-part {
  position: relative;
  background-color: #e5f7ed;
}

.dropdown-parts {
  position: absolute;
  padding: 8px 0 15px 0;
  background-color: #ffffff;
  box-shadow: 0 3px 6px #00000029;
  min-width: 200px;
  z-index: 3;
  left: 50%;
  transform: translate(-50%, 0);
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
</style>