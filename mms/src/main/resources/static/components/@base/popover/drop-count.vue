<template>
  <div
    class="d-flex align-center"
    style="width: fit-content"
    v-click-outside="closeDropDown"
  >
    <div :class="{ 'position-relative': type === 'HYPER_LINK' }">
      <a
        href="javascript:void(0)"
        @click.prevent="handleTitleClick"
        :class="{
          'custom-hyper-link': isHyperLink,
          'text--grey-dark': !isHyperLink,
        }"
      >
        <a-tooltip v-if="getTitle.showToolTips" placement="bottom">
          <template slot="title">
            <span>{{ title }}</span>
          </template>
          <label style="white-space: nowrap; margin: 0"></label
          >{{ getTitle.text }}
        </a-tooltip>
        <label v-else style="white-space: nowrap; margin: 0">{{ title }}</label>
        <template v-if="type === 'HYPER_LINK'">
          <div class="hyper-link-icon" style="margin-left: 3px"></div>
          <div
            ref="dropCountPopup"
            class="drop-count-popup drop-count-hidden"
            :style="customStyle"
          >
            <slot></slot>
          </div>
        </template>
      </a>
    </div>
    <div
      v-if="type === 'COUNT' && getExpandNumber > 0"
      class="d-flex"
      style="position: relative"
    >
      <img
        src="/images/icon/@base/drop-count-arrow.svg"
        alt=""
        @click="showDropdown"
        class="drop-count-arrow"
        :class="{ 'drop-count-arrow__active': visible }"
      />
      <span class="drop-count-circle rounded-circle" @click="showDropdown">
        {{ `+${getExpandNumber}` }}
      </span>
      <div ref="dropCountPopup" class="drop-count-popup drop-count-hidden">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    title: {
      type: String,
      default: () => "",
    },
    dataList: {
      type: Array,
      default: () => [],
    },
    isHyperLink: {
      type: Boolean,
      default: () => false,
    },
    type: {
      type: String,
      default: () => "COUNT",
      validator: (value) => {
        return ["COUNT", "HYPER_LINK"].includes(value);
      },
    },
    customStyle: {
      type: Object,
      default: () => ({}),
    },
  },
  setup(props, { emit, refs }) {
    /* STATE */
    const visible = ref(false);

    /* COMPUTED */
    const getExpandNumber = computed(() => {
      if (props.type == "HYPER_LINK") {
        return props.dataList.length;
      }
      return props.dataList.length - 1;
    });

    const getTitle = computed(() => {
      let result = {
        text: props.title,
        showToolTips: false,
      };
      if (props.title.length > 20) {
        return {
          text: props.title.slice(0, 20),
          showToolTips: true,
        };
      }
      return result;
    });

    /* METHODS */
    const handleTitleClick = () => {
      console.log("handleTitleClick", props.type);
      if (props.type == "HYPER_LINK") {
        showDropdown();
      } else {
        emit("title-click");
      }
    };

    const closeDropDown = () => {
      visible.value = false;
      const dropCountPopup = refs.dropCountPopup;
      if (dropCountPopup) {
        dropCountPopup.classList.add("drop-count-hidden");
      }
    };

    const showDropdown = () => {
      const dropCountPopup = refs.dropCountPopup;
      if (dropCountPopup) {
        if (!visible.value) {
          visible.value = true;
          dropCountPopup.classList.remove("drop-count-hidden");
          dropCountPopup.classList.add("open-animation");
          setTimeout(() => {
            dropCountPopup.classList.remove("open-animation");
          }, 300);
        } else {
          visible.value = false;
          dropCountPopup.classList.add("drop-count-hidden");
        }
      }
    };

    return {
      visible,
      getExpandNumber,
      getTitle,
      handleTitleClick,
      showDropdown,
      closeDropDown,
    };
  },
};
</script>

<style scoped>
.drop-count-arrow {
  transition-property: transform;
  transition-duration: 0.3s;
  transition-timing-function: linear;
  width: 10px;
  margin: 0 4px;
  cursor: pointer;
}

.drop-count-arrow__active {
  transform: rotate(180deg);
}

.drop-count-circle {
  color: #ffffff;
  font-size: 11px;
  padding: 3px 4px 4px 2px;
  width: fit-content;
  height: fit-content;
  display: inline-flex;
  align-content: center;
  align-items: center;
  justify-content: center;
  background-color: #43a1fc;
  cursor: pointer;
}

.drop-count-popup {
  position: absolute;
  padding: 4px 4px 4px 5px;
  background-color: #ffffff;
  box-shadow: 0px 3px 6px #00000029;
  width: auto;
  z-index: 100;
  /* left: 50%; */
  /* transform: translate(-50%, 20px); */
  /* transform-origin: top center; */
  top: 100%;
  left: -300%;
  perspective: 1000px;
  border-radius: 4px;
  /* transform: translate(-50%, 0px); */
}

.open-animation {
  animation-name: open-drop-count;
  animation-duration: 0.3s;
  animation-iteration-count: 1;
  animation-timing-function: ease-in-out;
}

@keyframes open-drop-count {
  0% {
    transform: scale(0);
    opacity: 0;
  }

  80% {
    transform: scale(1.1);
  }

  100% {
    transform: scale(1);
    opacity: 1;
  }
}

.close-animation {
  animation-name: close-drop-count;
  animation-duration: 0.3s;
  animation-iteration-count: 1;
  animation-timing-function: ease-in-out;
}

.drop-count-hidden {
  display: none;
}
</style>
