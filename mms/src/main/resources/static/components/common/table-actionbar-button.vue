<template>
  <div @click="executor" :class="buttonClassComputed" :style="styleProps" :active="active" :disabled="disabled">
<!--    <img v-if="imagePosition === 'left'" class="left-side-image" src="" alt="button-left-side-image" />-->
    <slot></slot>

    <img v-if="buttonType==='custom' && imagePosition === 'right'" class="right-side-image" :src="iconUrl"/>
    <img v-else="imagePosition === 'right'" class="right-side-image " src="" alt="button-right-side-image" />
  </div>
</template>

<script>
module.exports = {
  props: {
    styleProps: {
      default: '',
      Type: String,
    },
    clickHandler: Function,
    active: Boolean,
    disabled: Boolean,
    buttonType: {
      type: String,
      default: '',
    },
    // '', 'small'
    size: {
      type: String,
      default: '',
    },
    actionBarButton:{
      type:String,


    },
    iconUrl: {
      type:String,
      default:'',
    }

  },
  data(){
    return{
      imagePosition:'right'
    }
  },
  computed: {
    buttonClassComputed() {
      let item = `default default-color ${this.buttonType}  ${this.size}  ${this.imagePosition}`;

      return item;
    },

  },
  methods: {

    executor() {
      if (this.clickHandler) {
        this.clickHandler();

      }
    },
  },


};
</script>

<style scoped>
/* small size style start ==================================== */
.small {
  height: 25px !important;
  font-size: 11.25px !important;
}
/* small size style end ====================================== */

/* blue color style start ==================================== */
.default-color{
  color: #3491ff;
  transition: 0.2s;
}
.default-color:hover{
  color: #3585e5;
  background-color: #daeeff;
  box-shadow:0px 1px 1px rgba(0, 0, 0, 0.25) ;
  border-color: transparent;
}
.default-color[active] {
  color: #3585e5;
  background: #DAEEFF !important;
  border-radius: 3px  !important;
  box-shadow: unset !important;

}
.default-color[active]:before {
  /*border: 2px solid #3491ff;*/
  animation-name: blue-border-show;
}


/* default button style start ======================================== */
.default {
  padding: 6px 8px;
  border-radius: 3px;
  font-size: 14.66px;
  width: fit-content;
  height: 29px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-right: 5px;
  margin: 2px;
  position: relative;
  cursor: pointer;
  transition: 0.3s;
  background: #F0F1F3;
  border-radius: 3px;
}
.default.center {
  justify-content: center;
}
/*.default[active] {*/
/*  background: #DAEEFF;*/
/*  border-radius: 3px;*/
/*}*/
.default[active]:before {
  content: '';
  top: -2px;
  left: -2px;
  width: calc(100% + 4px);
  height: calc(100% + 4px);
  position: absolute;
  border-radius: 3px;
  animation-duration: 1s;
}
.default:active {
  background: #DAEEFF !important;
  border-radius: 3px  !important;
  box-shadow: unset !important;
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



.change-state > .right-side-image{
  content: url(/images/icon/change-state-actionbar.svg);
  margin-left: 4px;

}
.disable-btn > .right-side-image{
  content: url(/images/icon/disable-actionbar.svg);
  margin-left: 4px;

}
.note > .right-side-image{
  content: url(/images/icon/memo-actionbar.svg);
  margin-left: 4px;

}
.edit > .right-side-image{
  content: url(/images/icon/edit-actionbtn.svg);
  margin-left: 4px;

}
.enable > .right-side-image{
  content: url(/images/icon/enable-actionbar.svg);
  margin-left: 4px;

}
.export > .right-side-image{
  content: url(/images/icon/export-actionbar.svg);
  margin-left: 4px;

}

.view > .right-side-image{
  content: url(/images/icon/view-actionbar.svg);
  margin-left: 4px;

}
.reset > .right-side-image{
  content: url(/images/icon/reset-actionbar.svg);
  margin-left: 4px;

}
.register > .right-side-image{
  content: url(/images/icon/register-actionbar.svg);
  margin-left: 4px;

}
.view-edit-history > .right-side-image{
  content: url(/images/icon/view-edit-history-actionbar.svg);
  margin-left: 4px;

}
.shift-config > .right-side-image{
  content: url(/images/icon/shift-config-actionbar.svg);
  margin-left: 4px;

}
.role-user-config > .right-side-image{
  content: url(/images/icon/role-config-actionbar.svg);
  margin-left: 4px;

}
.remove-from-product > .right-side-image{
  content: url(/images/icon/remove-actionbar.svg);
  margin-left: 4px;

}
.role-permission-config >.right-side-image{
  content: url(/images/icon/role-permission-config-actionbar.svg);
  margin-left: 4px;
}

.custom > .right-side-image{
  margin-left: 4px;
}
</style>
