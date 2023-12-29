<template>
  <div class="unit-select">
<!--    <a-popover v-model="showDropdown" placement="bottomLeft" trigger="click">-->
<!--      <a :class="{'outline-animation': true}" href="javascript:void(0)" class="btn-custom animationPrimary all-time-filters" style="color: #5c6873">-->
<!--        <div style="width: 70px">{{select}}</div>-->
<!--        <img class="img-transition" style="width: 10px; height: 10px; margin-left: 5px;" src="/images/icon/down-arrow-grey.svg" alt="">-->
<!--      </a>-->
<!--      <a-menu v-for="(unit, index) in selectionList" slot="content" class="wrapper-action-dropdown" style="border-right: unset!important;max-height: 250px;overflow: auto" >-->
<!--        <a-menu-item-->
<!--            class="unit-select-option"-->
<!--            v-if="type === 'volume' && (unit.code === 'MM' || unit.code === 'CM' || unit.code === 'M')"-->
<!--            @click="changeSelect(unit)">-->
<!--          <span>{{ unit.title }}³</span>-->
<!--        </a-menu-item>-->
<!--        <a-menu-item-->
<!--            class="unit-select-option"-->
<!--            v-else-if="type === 'CurrencyType'"-->
<!--            @click="changeSelect(unit)">-->
<!--          <span>{{ unit.code }}</span>-->
<!--        </a-menu-item>-->
<!--        <a-menu-item-->
<!--            class="unit-select-option"-->
<!--            v-else-if="type === 'OutsideUnit'"-->
<!--            @click="changeSelect(unit)">-->
<!--          <span>{{ unit.title === "s" ? `second` : unit.title }}</span>-->
<!--        </a-menu-item>-->
<!--        <a-menu-item-->
<!--            class="unit-select-option"-->
<!--            v-else-->
<!--            @click="changeSelect(unit)">-->
<!--          <span>{{ unit.title }}</span>-->
<!--        </a-menu-item>-->
<!--      </a-menu>-->
<!--    </a-popover>-->

    <!--  Start   -->
    <common-button
        :title="select"
        :class="{'outline-animation': true}"
        @click="handleToggle"
        :is-show="showUnitType"
    >
    </common-button>
    <common-select-popover
        :is-visible="showUnitType"
        @close="handlecloseUnitType"
        class="w-95"
        >
      <common-select-dropdown
          :searchbox="false"
          :class="{show:showUnitType}"
          :style="{position:'static'}"
          :items="selectionList"
          :checkbox="false"
          class="dropdown"
          :set-result="()=>{}"
          :click-handler="changeSelect"
      ></common-select-dropdown>
    </common-select-popover>
    <!--   End   -->
  </div>
</template>

<script>
module.exports = {
  props: {
    title: {
      type: String,
      default: () => ''
    },
    selectionList: {
      type: Array,
      default: () => []
    },
    selectedUnit: {
      type: String,
      default: () => ''
    },
    type: {
      type: String,
      default: ''
    },
  },

  data() {
    return {
      select: '',
      showDropdown: false,
      showUnitType:false,

    }
  },
  computed: {
    getSelectedTitle() {
      let selectedUnit = this.getSelectedUnit;
      return this.type === 'volume' && (selectedUnit.code === 'MM' || selectedUnit.code === 'CM' || selectedUnit.code === 'M')
          ? selectedUnit.title+'³' : this.type === 'OutsideUnit' && selectedUnit.code === 's' ? 'second`' :  selectedUnit.title;


    },
    getSelectedUnit() {
      let keyWord = this.selectedUnit
      console.log('getSelectedUnit 1', this.type, this.select, keyWord)
      if (keyWord) {
        if (this.type == 'volume') {
          keyWord = keyWord.replaceAll('3', '')
          keyWord = keyWord.replaceAll('³', '')
          const find = this.selectionList.filter(item => item.title === keyWord)
          console.log('getSelectedUnit', this.type, keyWord, find)
          if (find.length > 0) {
            return find[0]
          }
        } else if (this.type == 'CurrencyType') {
          const find = this.selectionList.filter(item => item.code == keyWord)
          if (find.length > 0) {
            return find[0]
          }
        } else if (this.type == 'OutsideUnit') {
          keyWord = keyWord.replaceAll('econd', '').toLowerCase()
          const find = this.selectionList.filter(item => item.title == keyWord)
          console.log('getSelectedUnit', this.type, keyWord, find)

          if (find.length > 0) {
            return find[0]
          }
        } else {
          const find = this.selectionList.filter(item => item.title == keyWord)
          console.log('getSelectedUnit', this.type, keyWord, find)

          if (find.length > 0) {
            return find[0]
          }
        }
      } else {
        console.log(this.type, this.select)
        return this.selectionList[0]
      }


    }
  },
  watch: {
    select(newVal) {
      console.log('change select', newVal, this.type)


    }
  },
  methods: {
    <!-- Start  -->

    handlecloseUnitType(){
      this.showUnitType = false
    },
    handleToggle: function (isShow) {

      this.showUnitType=isShow;

    },
    <!--  End   -->
    changeSelect(item) {
      this.handlecloseUnitType()
      console.log('changeSelect', item)
      if(this.type === 'volume' && (item.code === 'MM' || item.code === 'CM' || item.code === 'M')) {
        this.select = item.title + '³';
      } else if (this.type === 'CurrencyType') {
        this.select = item.code;
      } else if (this.type === 'OutsideUnit') {
        this.select = item.title === 's' ? `second` : item.title;
      } else {
        this.select = item.title;
      }
      this.$emit('change-unit', this.select)
      this.toggleDropdown();
    },
    toggleDropdown() {
      this.showDropdown = !this.showDropdown;
    }
  },
  mounted() {
    const currentUnit = this.getSelectedUnit
    console.log('mounted unit selection', this.getSelectedUnit, 'type', this.type, 'selectedUnit', this.selectedUnit)
    if (currentUnit) {
      switch (this.type) {
        case 'volume':
          if (currentUnit.code == 'MM' || currentUnit.code == 'CM' || currentUnit.code == 'M') {
            this.select = currentUnit.title + '³'
            console.log("volume", this.select)
          } else {
            this.select = currentUnit.title
          }
          break;
        case 'CurrencyType':
          this.selectionList.forEach(item=>{
            item.title=item.code
          })
          this.select = currentUnit.code
          break;
        case 'OutsideUnit':
          if (currentUnit.title == 's') {
            this.select = 'second'
          } else {
            this.select = currentUnit.title
          }
          break;
        default:
          this.select = currentUnit.title
          break;
      }
    }

  },
}
</script>

<style>
.unit-select {

  color: #5c6873;
  width: 177px;
}
.unit-select:hover {
  color: #5c6873;
}
.ant-popover-inner-content {
  /* width: 250px; */
  padding: 0px !important;
  margin-right: 0;
  max-height: 176px;
  overflow: scroll;
}
.unit-select-option {
  height: 20px!important;
  width: 90px
}
.dropdown-wrap {
  width: 100% !important;
}
.common-popover{
  min-width: unset !important;
  width: 174px !important;
}

</style>