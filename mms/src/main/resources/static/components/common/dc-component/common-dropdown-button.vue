<template>
  <a
      href="javascript:void(0)"
      ref="custom-dropdown-button"
      :disabled="isDisabled"
      @click="handleClick()"
      class="custom-dropdown-button selected-item-title"
      :class="{ 'hover-btn': !isShow, 'disabled-btn' : isDisabled }"
  >
    <span v-if="title!==''" class="button-title">{{ title }}</span>
    <span v-else class="button-title">{{ placeholder }}</span>
    <div class="dropdown-carret float-right" :class="{ rotate: isShow }">
      <svg xmlns="http://www.w3.org/2000/svg" width="13" height="6" viewBox="0 0 13 6">
        <path id="Polygon_1" data-name="Polygon 1"
              d="M5.822.626a1,1,0,0,1,1.357,0l3.942,3.639A1,1,0,0,1,10.442,6H2.558a1,1,0,0,1-.678-1.735Z"
              transform="translate(13 6) rotate(180)" :fill="isBlue ? '#40a9ff' : '#909090'"/>
      </svg>
    </div>
  </a>
</template>

<script>
module.exports = {
  props: {
    title: {
      type: String,
      default: () => "",
    },
    placeholder: {
      type: String,
      default: () => "",
    },
    // isSelected: {
    //   type: Boolean,
    //   default: () => false,
    // },
    isDisabled: {
      type: Boolean,
      default: () => false,
    },
    isShow: {
      type: Boolean,
      default: () => false,
    },
    isBlue: {
      type: Boolean,
      default: () => false,
    },
  },
  data() {
    return {
      buttonAnimation: null,
      isSelected:false,
      tempTitle:''
    };
  },
  watch: {
    isShow: function (newValue) {
      if (!newValue) {
        this.handleClose();
      }
    },
    title: function (newValue) {
      if (newValue!=this.tempTitle) {
        this.isSelected=true;

      }
      else{
        this.isSelected=false;
      }
    },
  },
  methods: {
    handleClick() {
      const el = this.$refs["custom-dropdown-button"];
      if (!this.isShow) {
        setTimeout(() => {
          el.classList.add("custom-dropdown-button-animation");
          this.buttonAnimation = setTimeout(() => {
            el.classList.remove("custom-dropdown-button-animation");
            this.$emit("click", true);
          }, 350);
        }, 0);
      } else {
        this.handleClose();
      }
    },
    handleClose() {
      const el = this.$refs["custom-dropdown-button"];
      if (this.buttonAnimation != null) {
        clearTimeout(this.buttonAnimation);
        this.buttonAnimation = null;
      }
      el.classList.remove("custom-dropdown-button-animation");
      this.$emit("click", false);
    },
  },
  mounted(){
    this.tempTitle=this.title;
  }
};
</script>

<style scoped>
.custom-dropdown-button {
  width: 100%;
  height: fit-content;
  padding: 6px 8px;
  border: 0.5px solid #909090;
  border-radius: 1.7px;
  background-color: #ffffff;
  color: #4b4b4b;
  display: flex;
  align-items: center;
  text-decoration: none !important;
  align-content: space-between;
  justify-content: space-between;
}

.custom-dropdown-button.hover-btn:hover {
  border: 0.5px solid #4B4B4B;
  border-radius: 1.7px;
}

/*.custom-dropdown-button.hover-btn:focus {*/
/*  border: 1.7px solid #DEEDFF;*/
/*  border-radius: 1.7px;*/
/*}*/

.selected-item-title {
  color: #4b4b4b;
}
/* .hover-btn:hover {
  border: 1px solid #4b4b4b;
} */

/*@keyframes custom-dropdown-button-primary-animation {*/
/*  0% {*/
/*  }*/
/*  33% {*/
/*    border: 3px solid #deedff;*/
/*  }*/
/*  66% {*/
/*    border: 3px solid #deedff;*/
/*  }*/
/*  0% {*/
/*  }*/
/*}*/
.custom-dropdown-button-animation {
  animation: custom-dropdown-button-primary-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  border: 1px solid transparents;
}
.rotate {
  transform: rotate(180deg);
}
.disabled-btn{
  background: #E8E8E8;;
  color: #909090;
  cursor: no-drop !important;
  border-radius: 1.7px;
  border:unset;
}
.dropdown-carret {
  display: flex;
  align-items: center;
  margin-left: 4px;
  transition-property: transform;
  transition-duration: 0.3s;
  transition-timing-function: linear;
}
.button-title {
  line-height: 17px;
}
</style>