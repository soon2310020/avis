<template>
  <a
    href="javascript:void(0)"
    ref="custom-dropdown-button"
    @click="handleClick()"
    class="custom-dropdown-button"
    :class="{ 'selected-item-title': isSelected, 'hover-btn': !isShow }"
  >
    <span class="button-title">{{ title }}</span>
    <div class="dropdown-carret" :class="{ rotate: isShow }">
      <img src="/images/icon/Polygon 1.svg"/>
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
    isSelected: {
      type: Boolean,
      default: () => false,
    },
    isShow: {
      type: Boolean,
      default: () => false,
    },
  },
  data() {
    return {
      buttonAnimation: null,
    };
  },
  watch: {
    isShow: function (newValue) {
      console.log("watch", newValue);
      if (!newValue) {
        this.handleClose();
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
          }, 700);
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
};
</script>

<style scoped>
.custom-dropdown-button {
  width: fit-content;
  height: fit-content;
  padding: 6px 8px;
  border-radius: 3px;
  background-color: #ffffff;
  border: unset !important;
  outline: 1px solid #909090;
  color: #4b4b4b;
  display: flex;
  align-items: center;
  text-decoration: none !important;
}
.selected-item-title {
  color: #4b4b4b;
}
.hover-btn:hover {
  outline: 1px solid #4b4b4b;
}

@keyframes custom-dropdown-button-primary-animation {
  0% {
  }
  33% {
    outline: 3px solid #deedff;
  }
  66% {
    outline: 3px solid #deedff;
  }
  0% {
  }
}
.custom-dropdown-button-animation {
  animation: custom-dropdown-button-primary-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  animation-timing-function: ease-in-out;
  outline: 1px solid transparents;
}
.rotate {
  transform: rotate(180deg);
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