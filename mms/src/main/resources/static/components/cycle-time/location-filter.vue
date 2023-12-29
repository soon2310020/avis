<template>
  <a-popover v-model="visible" placement="bottom" trigger="click">
    <a-button style="margin-right: 10px; background: white; width: 100%" class="dropdown_button">
      {{titleShow()}}
      <a-icon type="caret-down" />
    </a-button>
    <div
      slot="content"
      style="padding: 8px 6px; max-height: 300px; overflow-y: scroll;"
      class="dropdown-scroll custom-truncate-dropdown"
    >
      <a-input style="margin-bottom: 10px;" :placeholder=resources["input_search"] v-model.trim="input">
        <a-icon slot="prefix" type="search" />
      </a-input>
      <a-col style="padding: 7px;" v-if="showAllType">
        <p-check
          :checked="checkedAll()"
          @change="onChange($event, allItem)"
          color="#5756f7"
        >{{allType}}</p-check>
      </a-col>
      <template v-for="item in filtered">
        <a-col style="padding: 7px;" :key="item.code">
          <p-check :checked="checked(item)" @change="onChange($event, item)">{{item.title}}</p-check>
        </a-col>
      </template>
    </div>
  </a-popover>
</template>

<script>
module.exports = {
  props: {
    allType: String,
    optionArray: Array,
    type: String,
    query: Function,
    findMap: Function,
    title: String,
    selectedData: Array,
    titlePlaceholder: String,
    resources:Object


  },
  data() {
    return {
      options: this.optionArray,
      allItem: {
        code: null,
        title: this.allType
      },
      checkedList: this.optionArray,
      oldCheckedList: [],
      input: "",
      visible: false
    };
  },
  methods: {
    titleShow: function() {
      if (this.checkedList.length == this.optionArray.length) {
        return this.titlePlaceholder;
      } else {
        if (this.checkedList.length < 2) {
          return `${this.checkedList.length} ${String(this.title).toLowerCase()}`;
        } else {
          if (this.title === "Category") {
            return `${this.checkedList.length} Categories`;
          }
          if(this.type === "status"){
            return `${this.checkedList.length} statuses`;
          }
          if(this.title === "country"){
            return `${this.checkedList.length} countries`;
          }
          if(this.type === "supplier"){
            return `${this.checkedList.length} suppliers`;
          }
          return `${this.checkedList.length} ${this.title}`;
        }
      }
    },

    checked: function(item) {
      const findIndex = this.checkedList.findIndex(
        value => value.code == item.code
      );
      return findIndex !== -1;
    },

    checkedAll: function() {
      return this.checkedList.length == this.options.length;
    },

    onMouseleave: function() {
      if (
        this.checkedList.map(value => value.code).toString() ==
        this.oldCheckedList.map(value => value.code).toString()
      ) {
        return;
      }
      this.oldCheckedList = this.checkedList;
      let items = this.checkedList.map(value => value.code)
      this.$emit('operatingstatus', this.oldCheckedList)
    },

    onChange: function(isChecked, item) {
      if (isChecked) {
        if (item.code == null) {
          return (this.checkedList = this.options);
        }
        this.checkedList = this.checkedList.concat(item);
      } else {
        if (item.code == null && this.checkedList.length == this.options.length) {
          return (this.checkedList = []);
        }
        this.checkedList = this.checkedList.filter(
          value => value.code !== item.code
        );
      }
    }
  },

  computed: {
    filtered() {
      console.log("filtered",this.input);
      const str = this.input;
      if (!str || str=='') {
        return this.optionArray;
      }
      if(this.optionArray!=null){
        return this.optionArray.filter(function (e) {
        //   return e.title!=null && e.title.includes(str);
        // }).sort((firstItem, secondItem) => firstItem.title > secondItem.title ? 1 : -1)
        return e.title!=null && e.title.toUpperCase().includes(str.toUpperCase());
      }).sort((firstItem, secondItem) => firstItem.title.toUpperCase() > secondItem.title.toUpperCase() ? 1 : -1)
      }
      return this.optionArray;

/*
      var options = {
        keys: ["title"]
      };
      var searcher = new Fuse(this.optionArray, options);
      return searcher.search(str);
*/
    },
    showAllType(){
      const str = this.input;
      console.log('input',str);
      if (str==null || str.trim()=='' || this.allType!=null && this.allType.toUpperCase().includes(str.toUpperCase())) {
        return true;
      }
      return false;
    },
  },
  watch: {
    optionArray: function(oldValue, newValue) {
      console.log("vao dayoptionArray: ", this.optionArray);
      this.options = this.optionArray;
      this.checkedList = this.optionArray;
      this.oldCheckedList = this.optionArray;
    },
    visible: function(newValue, oldValue) {
      if (newValue == false && oldValue == true) {
        this.onMouseleave();
      }
    }
  },
  mounted() {}
};
</script>
<style>
.custom-truncate-dropdown {
  max-width: 220px;
  overflow: hidden auto;
}
.custom-truncate-dropdown .pretty .state label {
  width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
<style scoped>
.dropdown_button {
  display: flex;
  justify-content: space-between;
  padding: 0px 10px;
  align-items: center;
  width: 100px;
  background: #f2f5fa;
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus {
  color: black !important;
  fill: black !important;
  /* background-image: linear-gradient(to right, #414ff7, #6a4efb) !important; */
}
.ant-btn.active,
.ant-btn:active,
.ant-btn:focus > svg {
  fill: #fff !important;
}
.down-icon {
  fill: #564efb;
}
.ant-menu-submenu-title:hover {
  /* background-image: linear-gradient(#414ff7, #6a4efb) !important; */
  background-color: red;
}
.first-layer-wrapper {
  position: relative;
}
.first-layer {
  position: absolute;
  top: 0;
}
</style>
