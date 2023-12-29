<template>
  <div class="system-note-user-filter" :id="id">
    <a-popover v-if="userFiltered" placement="bottomLeft" trigger="click" v-model="isShowFirstLayer">
      <div slot="content" class="search-user-menu">
        <div style="width: 320px; max-height: 296px; overflow: auto" class="search-user-container" :class="{'pa-2' : userFiltered.length > 0, 'pa-0' : userFiltered.length == 0}">
          <template>
            <ul class="list-user">
              <li v-for="(user, index) in userFiltered" :key="index" @click="selectUser(user)">
                <div class="dropdown-item mx-0 px-0 d-flex user-item-wrapper" :class="{closest: index === activeIndex}" @mouseover="activeIndex = index" style="padding: 4px 8px">
                  <div class="ml-1">
                    <p class="py-0 my-0" style="font-size: 14.66px; font-weight: 500; text-overflow: ellipsis; width: 200px;white-space: nowrap;overflow: hidden;">{{ user.name }}</p>
                    <span style="font-size: 11.25px; display: inline-block; text-overflow: ellipsis; width: 200px;white-space: nowrap;overflow: hidden !important;">{{user.email}}</span>
                  </div>
                </div>
              </li>
            </ul>
          </template>
        </div>
      </div>
    </a-popover>
  </div>
</template>

<script>
module.exports = {
  props: {
    id: String,
    resources: Object,
    users: {
      type: Array,
      default: () => ([])
    }
  },
  data() {
    return {
      activeIndex: 0,
      isShow: false,
      companyCategories: [],
      searchText: '',
      viewModes: {
        USER: 'USER',
        COMPANY: 'COMPANY',
        dialogBack: false
      },
      viewMode: 'USER',
      companyCategoryIndex: 0,
      companyIndex: 0,
      textFromTextarea: null,
      isFromTextarea: false,
      isShowFirstLayer: false,
    }
  },
  methods: {
    showSearchUserPopup(text, e){
      this.viewMode = this.viewModes.USER;
      this.isFromTextarea = true;
      this.textFromTextarea = text;
      this.isShow = true;
      let childrenLengthOfContent = $('#' + this.id).parent().find('.system-note-shadow').find('div').length;
      let offsetTop = childrenLengthOfContent*20 + 20;
      if (offsetTop > e.target.offsetHeight) {
        offsetTop = e.target.offsetHeight;
      }
      $('#' + this.id).css('top', offsetTop + 'px');
      this.isShowFirstLayer = true;
      setTimeout(() => {
        $('.ant-popover').each((i, e) => {
          if (e.querySelector('.search-user-menu')) {
            e.style.zIndex = '10000';
          }
        });
      }, 100);
    },
    hideSearchUserPopup(){
      this.companyCategoryIndex = 0;
      this.companyIndex = 0;
      this.searchText = null;
      this.isShow = false;
      this.isShowFirstLayer = false;
    },
    selectUser(user) {
      this.isShow = false;
      this.$emit('select-user',user);
      this.isShowFirstLayer = false;
      this.searchText = null
    },
    findClosestUser() {
        this.isShowFirstLayer = false;
        this.searchText = null;
        console.log("this.userFiltered", this.userFiltered)
        if (this.userFiltered.length > 0) {
          this.$emit('select-user',this.userFiltered[0], true);
        }
    },
    selectClosestUser(e) {
      console.log("showSearchUserPopup", e)
      if (e.code === 'Enter' && this.viewMode === this.viewModes.USER && this.userFiltered.length > 0) {
        this.isShowFirstLayer = false;
        this.$emit('select-user',this.userFiltered[0]);
        setTimeout(() => {
          this.searchText = null;
        }, 10);
      }
    }
  },
  mounted() {
  },
  watch: {
    searchText(value) {
      this.isFromTextarea = false;
    },
  },
  computed: {
    userFiltered() {
      if (this.searchText) {
        return this.users.filter(user => user.name.toUpperCase().includes(this.searchText.toUpperCase()));
      }
      if (this.textFromTextarea) {
        return this.users.filter(user => user.name.toUpperCase().includes(this.textFromTextarea.toUpperCase()) || user.email.toUpperCase().includes(this.textFromTextarea.toUpperCase()));
      }
      return this.users;
    }
  }
}
</script>
<style scoped>
.ant-popover-inner-content {
  /* padding: 0 !important; */
}
.user-item-wrapper {
  padding: 4px 8px
}
.system-note-user-filter {
  position: absolute;
}
.user-item-wrapper:hover {
  background-color: #E6F7FF;
}
.closest {
  background-color: #E6F7FF;
}
</style>