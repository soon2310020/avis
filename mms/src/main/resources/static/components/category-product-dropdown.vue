<template>

  <!-- One Record -->
  <div v-if="parts.length === 1">
    <a v-if="userType === 'OEM'" href="javascript:void(0)" @click="fetchProjectDetail(parts[0])">{{ parts[0].projectName }}</a>
    <p v-else class="category-disable" href="javascript:void(0)" >{{ parts[0].projectName }}</p>
    <div v-if="parts[0].categoryName > 20">
      <p class="font-size-11-2">
        {{ parts[0].categoryName }}
      </p>
    </div>
    <div v-else class="font-size-11-2"> {{ parts[0].categoryName }} </div>
  </div>
  <!--  -->

  <!-- List Records -->
  <div :id="'reason-' + index" v-else-if="parts.length > 1" style="position: relative">
    <span>
      <span class="one-line">
        <a v-if="userType === 'OEM'" href="javascript:;" @click="fetchProjectDetail(parts[0])">{{ parts[0].projectName }}</a>
        <p v-else class="category-disable" href="javascript:;" >{{ parts[0].projectName }}</p>
        <span>
          <svg @click="showReason(index)" style="cursor: pointer" :id="'arrow-cate-' + index" class="arrow"
            xmlns="http://www.w3.org/2000/svg" width="9.973" height="5.737" viewBox="0 0 9.973 5.737"
            v-click-outside="closeDropDown">
            <path id="Icon_feather-chevron-down" data-name="Icon feather-chevron-down"
              d="M9,13.5l3.926,3.926L16.852,13.5" transform="translate(-7.939 -12.439)" fill="none" stroke="#595959"
              stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" />
          </svg>
        </span>
        <span style="
            color: #ffffff;
            font-size: 11px;
            padding: 3px 4px 4px 2px;
            border-radius: 50%;
            background-color: #43a1fc;
          ">+{{ parts.length - 1 }}
        </span>

        <!-- Dropdown list -->
        <div id="dropdown-cate" v-if="selectedDropdown === 'arrow-cate-' + index" class="dropdown-cate">
          <div v-for="(part) in parts" :key="part.partId" :id="part.partId" class="single-part">
            <div style="line-height: 17px">
              <a v-if="userType === 'OEM'" href="javascript:;" class="font-size-14" style="color: #43a1fc" @click="fetchProjectDetail(part)">
                {{ part.projectName }}
              </a>
              <p v-else href="javascript:;"  class="font-size-14 category-disable" >
                {{ part.projectName }}
              </p>
            </div>
            <p class="part-name-drop-down one-line font-size-11-2" v-bind:title="part.categoryName">
              {{ part.categoryName }}
            </p>
          </div>
        </div>
        <!--  -->

      </span>
    </span>
    <div class="font-size-11-2"> {{ parts[0].categoryName }} </div>
    <!--  -->
  </div>
</template>

<script>
module.exports = {
  props: {
    mold: Object,
    listParts: Array,
    index: Number,
    showProjectDetail: Function,
    userType: String
  },
  data() {
    return {
      dropdownOpening: false,
      showExportModal: false,
      selectedDropdown: null,
      nextSelectDropdown: null,
      isShowingDropDown: false,
      showingClass: false
    };
  },
  computed: {
    parts() {
      const _parts = this.listParts.reduce((prev, curr) => {
        if (!curr.projectId || !curr.projectName) return [...prev]
        if (prev.some(item => item.projectId === curr.projectId)) return [...prev]
        return [...prev, curr]
      }, [])

      console.log(_parts)
      return _parts
    }
  },
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
    async fetchProjectDetail(part) {
      console.log('@fetchProjectDetail', part)
      try {
        this.showProjectDetail({ id: part.projectId })
      } catch (error) {
        console.log(error)
      }
    }
  },
  mounted() {
    console.log('mounted',this.userType)
  }
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
  cursor: auto;
  margin-bottom: 0px!important;
}
.category-disable:hover, .category-disable:active {
  text-decoration: auto!important;
}
</style>
