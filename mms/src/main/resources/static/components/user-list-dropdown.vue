<template>
  <div v-if="userList">
    <!-- One Record -->
    <div v-if="userList.length <= 4" class="requested-bar">
      <div v-for="(item, index) in userList" :style="{ 'background-color': getRequestedColor(item.name,item.id)}" class="requested-item mr-2">
        <a-tooltip placement="bottom">
          <template slot="title">
            <div style="padding: 6px;font-size: 13px;">
              <div><b>{{item.name}}</b></div>
              <div>
                Company: <span v-if="item.company">{{ item.company.name }}</span>
              </div>
              <div>
                Department: {{ item.department }}
              </div>
              <div style="margin-bottom: 15px">
                Position: {{ item.position }}
              </div>
              <div>
                Email: {{ item.email }}
              </div>
              <div>
                Phone: {{ item.mobileNumber }}
              </div>
            </div>
          </template>
          <div>
            {{convertName(item.name)}}
          </div>
        </a-tooltip>
      </div>
    </div>
    <!-- List Records -->
    <div :id="'reason-' + index" v-else>
    <span class="d-inline-flex align-items-center position-relative">
        <div v-for="(item, index) in userList.slice(0, 4)" :style="{ 'background-color': getRequestedColor(item.name,item.id)}" class="requested-item mr-2">
            <a-tooltip placement="bottom">
              <template slot="title">
                <div style="padding: 6px;font-size: 13px;">
                  <div><b>{{item.name}}</b></div>
                  <div>
                    Company: <span v-if="item.company">{{ item.company.name }}</span>
                  </div>
                  <div>
                    Department: {{ item.department }}
                  </div>
                  <div style="margin-bottom: 15px">
                    Position: {{ item.position }}
                  </div>
                  <div>
                    Email: {{ item.email }}
                  </div>
                  <div>
                    Phone: {{ item.mobileNumber }}
                  </div>
                </div>
              </template>
              <div>
                {{convertName(item.name)}}
              </div>
            </a-tooltip>
          </div>
        <span class="mr-1">
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
            padding: 2px 5px 1px 3px;
            border-radius: 50%;
            background-color: #43a1fc;
          ">+{{ userList.length - 4 }}
        </span>

      <!-- Dropdown list -->
        <div id="dropdown-cate" v-if="selectedDropdown === 'arrow-cate-' + index" class="dropdown-cate requested-bar">
          <div v-for="(item, index) in userList" :style="{ 'background-color': getRequestedColor(item.name,item.id)}" class="requested-item mr-2">
            <a-tooltip placement="bottom">
              <template slot="title">
                <div style="padding: 6px;font-size: 13px;">
                  <div><b>{{item.name}}</b></div>
                  <div>
                    Company: <span v-if="item.company">{{ item.company.name }}</span>
                  </div>
                  <div>
                    Department: {{ item.department }}
                  </div>
                  <div style="margin-bottom: 15px">
                    Position: {{ item.position }}
                  </div>
                  <div>
                    Email: {{ item.email }}
                  </div>
                  <div>
                    Phone: {{ item.mobileNumber }}
                  </div>
                </div>
              </template>
              <div>
                {{convertName(item.name)}}
              </div>
            </a-tooltip>
          </div>
        </div>
      <!--  -->

      </span>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    userList:Array,
    index: Number,
    showCompanyDetailsById: Function
  },
  data() {
    return {
      dropdownOpening: false,
      showExportModal: false,
      selectedDropdown: null,
      nextSelectDropdown: null,
      isShowingDropDown: false,
      showingClass: false,
      colorList: [
        '#AFACFF',
        '#FF7171',
        '#DB91FC',
        '#FFACAC',
        '#FFB371',
        '#FFACF2',
        '#FFACE5',
        '#86C7FF',
        '#62B0D9',
        '#ACBFFF',
        '#FF8699',
        '#90E5AA',
        '#CCACFF',
        '#74AEFC',
        '#E0CD7E',
        '#FF8489',
        '#FFA167',
        '#64D0C3',
        '#86B0FF',
        '#D2F8E2',
        '#C69E9E',
        '#FFC68D',
        '#F8D2D2',
        '#E58383',
      ],
    };
  },
  computed: {

  },
  methods: {
    convertName(name){
      let nameParts = name.split(/\s+/)
      // console.log(nameParts)
      let convertName = ''
      if (nameParts) {
        if (nameParts.length == 1) {
          convertName = nameParts[0].charAt(0).toUpperCase();
        } else {
          convertName = `${nameParts[0].charAt(0).toUpperCase()}${nameParts[nameParts.length - 1].charAt(0).toUpperCase()}`;
        }
      }
      return convertName;
    },
    getRequestedColor(name, id){
      let f=this.convertName(name);
      let index=0;
      if (f.length > 0) {
        index = f.charCodeAt(0);
        if (index > 65) index -= 65;
        if (f.length > 1) {
          index += f.charCodeAt(1);
          if (index > 65) index -= 65;
        }
      }
      if(id){
        index +=id;
      }
      index = index % this.colorList.length;
      return this.colorList[index]
    },
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
        this.showCompanyDetailsById(id)
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
.requested-item{
  display: flex;
  justify-content: center;
  align-items: center;
  width: 25px;
  height: 25px;
  background-color: #AFACFF;
  border-radius: 25px;
  margin-bottom: 3px;
  /*margin-right: 3px;*/
}
.requested-bar{
  display: grid;
  grid-template-columns: repeat(4,1fr);
  width: 120px;
  /*display: flex;*/
  /*flex-flow: row wrap;*/
  /*position: relative;*/
}
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
  z-index: 99;
  left: 50%;
  transform: translate(-50%, 1rem);
  max-height: 120px;
  overflow: scroll;
  top: 100%;
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
  margin-bottom: 0px!important;
}
.category-disable:hover, .category-disable:active {
  text-decoration: auto!important;
}
</style>