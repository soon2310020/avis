<template>
  <!-- One Record -->
  <div
    v-if="dataList.length === 1"
    :class="{ 'cursor-none text--grey': disabled }"
  >
    <a
      class="category-disable"
      :class="{ 'text--grey': disabled }"
      @click="showCompanyDetail(dataList[0].id)"
      >{{ dataList[0].name }}</a
    >
    <div class="font-size-11-2">{{ dataList[0].companyCode }}</div>
  </div>
  <!--  -->

  <!-- List Records -->
  <div
    :id="'reason-' + index"
    v-else-if="dataList.length > 1"
    style="position: relative"
  >
    <span>
      <span class="one-line" :class="{ 'cursor-none text--grey': disabled }">
        <a
          class="category-disable"
          @click="showCompanyDetail(dataList[0].id)"
          >{{ dataList[0].name }}</a
        >
        <span>
          <svg
            @click="showReason(index)"
            style="cursor: pointer"
            :id="'arrow-cate-' + index"
            class="arrow"
            xmlns="http://www.w3.org/2000/svg"
            width="9.973"
            height="5.737"
            viewBox="0 0 9.973 5.737"
            v-click-outside="closeDropDown"
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
        <span
          style="
            color: #ffffff;
            font-size: 11px;
            padding: 3px 4px 4px 2px;
            border-radius: 50%;
            background-color: #43a1fc;
          "
          >+{{ dataList.length - 1 }}
        </span>

        <!-- Dropdown list -->
        <div
          id="dropdown-cate"
          v-if="selectedDropdown === 'arrow-cate-' + index"
          class="dropdown-cate"
        >
          <div
            v-for="part in dataList"
            :key="part.id"
            :id="part.id"
            class="single-part"
          >
            <div style="line-height: 17px">
              <a
                @click="showCompanyDetail(part.id)"
                class="font-size-14 category-disable"
              >
                {{ part.name }}
              </a>
            </div>
            <p
              class="part-name-drop-down one-line font-size-11-2"
              v-bind:title="part.companyCode"
            >
              {{ part.companyCode }}
            </p>
          </div>
        </div>
        <!--  -->
      </span>
    </span>
    <div class="font-size-11-2">{{ dataList[0].companyCode }}</div>
    <!--  -->
  </div>
</template>

<script>
module.exports = {
  props: {
    dataList: Array,
    index: Number,
    showCompanyDetailsById: Function,
    disabled: {
      type: Boolean,
      default: true,
    },
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
  computed: {},
  methods: {
    showReason(index) {
      console.log("Click");
      const nextElement = document.getElementById(`arrow-cate-${index}`);
      if (this.isShowingDropDown) {
        const currentElement = document.getElementById(this.selectedDropdown);
        currentElement.classList.remove("arrow-active");
        if (this.selectedDropdown !== `arrow-cate-${index}`) {
          nextElement.classList.add("arrow-active");
          this.nextSelectDropdown = `arrow-cate-${index}`;
        } else {
          this.isShowingDropDown = false;
          this.selectedDropdown = null;
          this.nextSelectDropdown = null;
          this.showingClass = false;
        }
      } else {
        nextElement.classList.add("arrow-active");
        this.selectedDropdown = `arrow-cate-${index}`;
        this.isShowingDropDown = true;
      }
      if (this.selectedDropdown != null && this.isShowingDropDown !== false) {
      } else {
      }
      setTimeout(() => {
        let dropDown = document.getElementById("dropdown-cate");
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
    async showCompanyDetail(id) {
      try {
        this.showCompanyDetailsById(id);
      } catch (error) {
        console.log(error);
      }
    },
  },
  mounted() {
    console.log("mounted", this.userType);
  },
};
</script>

<style>
.single-part {
  padding: 10px 31px 10px 30px;
  text-align: left;
  min-width: 100%;
  width: fit-content;
}

/*.single-part:hover {*/
/*  background-color: #e6f7ff;*/
/*}*/

.active-part {
  position: relative;
  background-color: #e5f7ed;
}

.dropdown-cate {
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
  transform: translate(-50%, 1rem);
  max-height: 120px;
  overflow: scroll;
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
.category-disable {
  margin-bottom: 0px !important;
}
.category-disable:hover,
.category-disable:active {
  text-decoration: auto !important;
}
</style>
