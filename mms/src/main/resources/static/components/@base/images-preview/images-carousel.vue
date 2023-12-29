<template>
  <div class="d-flex justify-space-between align-center">
    <div style="margin: 0 4px; width: fit-content; height: fit-content">
      <a
        v-show="listItem.length > 4"
        href="javascript:void(0)"
        class="carousel-button"
        :class="{ 'inactive-button': currentIndex === 0 }"
        @click="handleChangePage('-')"
      >
        <img src="/images/icon/common/paging-arrow.svg" alt="" />
      </a>
    </div>
    <div class="d-flex justify-center">
      <div
        v-for="item in listItem"
        v-show="checkVisibleItem(item)"
        :key="item.id"
        :ref="`image-card-${item.id}`"
        class="image-item-card"
        :class="{ active: item.id === selectedItem.id }"
        @click="handleClickItem(item)"
      >
        <div class="image-item-card-content">
          <div v-if="item.saveLocation">
            <img
              :src="item.saveLocation"
              alt=""
              style="object-fit: contain; width: 66px; height: 49px"
            />
          </div>
          <div v-else class="default-image--small"></div>
        </div>
      </div>
    </div>
    <div style="margin: 0 4px; width: fit-content; height: fit-content">
      <a
        v-show="listItem.length > 4"
        href="javascript:void(0)"
        class="carousel-button"
        :class="{ 'inactive-button': isLast }"
        @click="handleChangePage('+')"
      >
        <img
          src="/images/icon/common/paging-arrow.svg"
          style="transform: rotate(180deg)"
          alt=""
        />
      </a>
    </div>
  </div>
</template>

<script>
const STEP = 4;
const SIZE = 4;

module.exports = {
  props: {
    listItem: {
      type: Array,
      default: () => [],
    },
    selectedItem: {
      type: Object,
      default: () => ({}),
    },
    changeKey: [Number, String],
  },
  setup(props, ctx) {
    const currentIndex = ref(0);
    const getDisplayItem = computed(() => {
      const firstIndex =
        (currentIndex.value + 1) * STEP > props.listItem.length - 1 &&
        props.listItem.length > 4
          ? props.listItem.length - 4
          : currentIndex.value * STEP;
      const result = [...props.listItem].splice(firstIndex, SIZE);
      console.log("getDisplayItem", firstIndex, result, props.changeKey);
      return result;
    });

    const isLast = computed(() => {
      console.log("getDisplayItem", props.changeKey);
      const index = currentIndex.value * STEP + SIZE;
      return index >= props.listItem.length;
    });

    const checkVisibleItem = (item) => {
      const find = getDisplayItem.value.find((i) => i.id === item.id);
      if (find) {
        return true;
      }
      return false;
    };

    const handleChangePage = (sign) => {
      currentIndex.value = eval(`${currentIndex.value} ${sign} 1`);
    };

    const handleClickItem = (item) => {
      const el = ctx.refs[`image-card-${item.id}`][0];
      if (el) {
        console.log(`image-card-${item.id}`, el);
        el.classList.add("image-card-animation");
        setTimeout(() => {
          el.classList.remove("image-card-animation");
          ctx.emit("change-image", item);
        }, 700);
      }
    };

    return {
      currentIndex,
      isLast,
      getDisplayItem,
      handleChangePage,
      handleClickItem,
      checkVisibleItem,
    };
  },
};
</script>

<style scoped>
.image-item-card {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px 8px;
  width: 66px;
  height: 49px;
  margin: 0 4px;
  cursor: pointer;
  outline: 1px solid #f2f2f2;
}
.image-item-card:hover,
.image-item-card.active {
  outline: 1px solid #3585e5;
}
.image-card-animation {
  animation: image-card-outline-animation 0.7s;
  animation-iteration-count: 1;
  animation-direction: alternate;
  outline: none;
}
@keyframes image-card-outline-animation {
  33% {
    box-shadow: 0 0 0 2px #daeeff;
  }

  66% {
    box-shadow: 0 0 0 2px #daeeff;
  }
}
.image-item-card-content {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.image-item-card-icon {
  margin-left: 4px;
}

.image-item-card-content-subtitle {
  width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: center;
}
</style>
