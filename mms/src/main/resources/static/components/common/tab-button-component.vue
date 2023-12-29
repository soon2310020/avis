

<template>
  <div>
  <div v-if="tabType==='primary-tab'||tabType==='secondary-tab'" :class="computedTabStyle">
    <div v-for="(button, index) in tabButtons"
         @click="executor(button)"
         :class="[buttonClassComputed, index===0 && tabType==='secondary-tab' && tabStyle==='horizontal'? 'horizontal-secondary-first-tab' :
         (index===tabButtons.length-1 && tabType==='secondary-tab' && tabStyle==='horizontal'? 'horizontal-secondary-last-tab' : '') ]"
         :style="styleProps"
         :active="button.active"
    >
<!--      /images/icon/part-icon-hover.svg-->
      {{button.title}}
    </div>
  </div>
  <div v-else-if="tabType==='icon-tab'" :class="computedTabStyle" style="flex-wrap: wrap;width: 100%;" >
    <div v-for="button in tabButtons"  class="chart-wrapper" :class="{'small' : size==='small'}" :active="button.active" :id="button.title" @mouseover="hover = true"
         @mouseleave="hover = false">
      <div @click="executor(button)">
        <div class="img-zone" :active='button.active' :class="button.active||hover === true ? 'img-zone-hover-background' : ''">
          <img :src="button.icon" alt="" :class="{'hover-icon' : button.active}">
        </div>
        <div class="chart-title">
          <b>{{ button.itemRate }}%</b>
          <span>{{button.title}}</span>
        </div>
      </div>
    </div>
  </div>
  </div>
</template>

<script>
module.exports = {
    props: {
        tabButtons: Array,
        styleProps: {
            default: '',
            Type: String,
        },
        clickHandler: Function,
        tabType: String, // '', 'blue'(default), 'blue-fill', 'red', 'red-fill', 'green', 'green-fill', 'white'
        // '', 'dropdown', 'date-picker', 'upload', 'export', 'text', 'text dropdown'
        tabStyle: {
            type: String,
            default: '',
        },
        // '', 'small'
        size: {
            type: String,
            default: 'large',
        },
    },
    computed: {
        buttonClassComputed() {
          let  item = `default ${this.tabStyle} ${this.tabType} center`

              return item;

        },

      computedTabStyle(){
        let item = `${this.tabStyle}-style-${this.tabType}`;

        return item;
      },
    },
  data() {
    return {
      hover: false
    };
  },
    methods: {
        executor(button) {
             this.tabButtons.forEach((btn)=>{
               btn.active=false;
             })
             button.active=true;
            if (this.clickHandler) {
                this.clickHandler(button);
            }
          this.$forceUpdate();
        },
    },
};
</script>

<style scoped>


.default {
    padding: 0px 8px;
    font-size: 14.66px;
    min-width: 79px;
    width: fit-content;
    display: flex;
    height: 26px;
    align-items: center;
    justify-content: space-between;
    margin-right: 5px;
    margin: 2px;
    position: relative;
    cursor: pointer;
    transition: 0.3s;
    font-family: 'Helvetica Neue';
    font-style: normal;
    font-weight: 400;
    line-height: 17px;
}
.default.center {
    justify-content: center;
}
.default[active] {
    border-radius: 0px;
}
.default[active]:before {
    content: '';
    top: -2px;
    left: -2px;
    width: calc(100% + 4px);
    height: calc(100% + 4px);
    position: absolute;
    animation-duration: 1s;
}

/* default button style end ======================================== */

/* disabled style start =================================== */
[disabled] {
    background-color: rgb(196, 196, 196);
    color: white !important;
    border: inherit;
    pointer-events: none;
}

/* disabled style end =================================== */

.primary-tab{
  background: #FFFFFF;
  border: 0.5px solid #D0D0D0;
  border-radius: 3px;
}
.primary-tab[active]{
  background: #3491FF;
  border: 0.5px solid #D0D0D0;
  border-radius: 3px;
  font-family: 'Helvetica Neue';
  color: #FFFFFF;
}

.horizontal-style-primary-tab {
  flex-wrap: nowrap;
  gap: 10px;
  margin-left: 0px;
  margin-right: -15px;
  display: flex;
}
.horizontal-style-icon-tab{
  display: inline-flex;
}
.vertical-style-icon-tab{
  display: inline-grid;
}
.horizontal-style-secondary-tab {
  flex-wrap: nowrap;
  gap: 0px;
  margin-left: 0px;
  display: flex;
}
.vertical-style-primary-tab {
  flex-wrap: nowrap;
  gap: 10px;
  margin-left: 0px;
  margin-right: -15px;
  display: inline-grid;
}
.vertical-style-secondary-tab {
  flex-wrap: nowrap;
  gap: 10px;
  margin-left: 0px;
  margin-right: -15px;
  display: inline-grid;

}
.vertical.primary-tab{
  width: 171px;
  height: 25px;

}
.vertical.secondary-tab{
  width: fit-content;
  height: 29px;

}
.secondary-tab{
  width: fit-content;
  border: 0.5px solid #D0D0D0;
  background: transparent;
  font-family: 'Helvetica Neue';
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  transition: 0.2s;
  margin:0px;

  color: #8B8B8B;
}
.secondary-tab:hover{
  background: #DAEEFF;
  border: 0.5px solid #D0D0D0;
  color: #3585E5;
}
.horizontal-secondary-first-tab{
  border-radius: 3px 0px 0px 3px !important;
}
.horizontal-secondary-last-tab{
  border-radius: 0px 3px 3px 0px !important;
}
.secondary-tab[active]{
  background: #DEEDFF;
  border: 0.5px solid #3491FF;
  box-sizing: border-box;
  color: #3491FF;
}
.secondary-tab[active]:before{
  animation-name: blue-border-show;
}

/* small size style start ==================================== */
.small {
  width: 156.44px !important;
  height: 72px !important;
}
.small .img-zone {
  width: 36px !important;
  height: 36px !important;
  padding: 7px !important;
  margin-right: 4px !important;
  margin-left: 6px !important;
}
.chart-wrapper{
  margin-top: 30px;
  width: 247px;
  height: 88px;
  border: 1px solid #fff;
  border-radius: 3px;
  margin-right: 30px;
}
/*icon tab styles*/
.chart-wrapper[active]  {
  background: #FFFFFF;
  border: 4px solid rgba(218, 238, 255, 1) !important;
}
.chart-wrapper img {
  max-width: 100%;
}

.chart-wrapper .img-zone {
  width: 50px;
  height: 50px;
  padding: 11px;
  background: #E6E6E6;
  border-radius: 50%;
  margin-right: 22px;
  margin-left: 37px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chart-wrapper.active .img-zone img {
  width: 110%
}

.img-zone-hover-background{
  background: #DAEEFF !important;
}
.hover-icon{
  width: 110% !important;
  max-width: 110% !important;
}
.chart-wrapper .chart-title {
  font-size: 24px;
  color: #595959;
  width: fit-content;
  line-height: 20px;
  display: flex;
  flex-direction: column;
}
.chart-wrapper .chart-title span {
  color: #AAAAAA;
  font-size: 16px;
  font-weight: 500;
}

.chart-wrapper:hover {
  border: 1px solid rgba(53, 133, 229, 1);
  box-shadow: 0px 3px 4px 0px rgba(0, 0, 0, 0.15);

}


.chart-wrapper > div {
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  position: relative;
  box-shadow: 0px 3px 6px rgb(0 0 0 / 16%);
  border-radius: 3px;
  filter: grayscale(1);
  height:100%;
  width:100%;
  /*padding: 11px 8px 14px 8px;*/
}


.chart-wrapper:hover > div {
  filter: unset;
}
.chart-wrapper[active] > div {
  filter: unset;
}

</style>
