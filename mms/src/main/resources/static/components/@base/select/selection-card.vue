<template>
  <div class="select-item-card" :class="getChipColorClass">
    <!-- <div v-show="false" :ref="`select-item-${selection.id}`" class="select-item-card__title">
      {{ selection.title }}
    </div> -->
    <div v-show="!allowTooltips || !getChipTitle.shouldTruncate" class="select-item-card__title">
      {{ getChipTitle.rawText }}
    </div>
    <a-tooltip v-show="allowTooltips && getChipTitle.shouldTruncate" placement="bottom">
      <template slot="title">
        <span style="padding: 6px 8px; line-height: 17px; display: block;">{{ getChipTitle.rawText }}</span>
      </template>
      <div class="select-item-card__title">
        {{ getChipTitle.text }}
      </div>
    </a-tooltip>
    <div v-show="closeAble" class="select-item-card__close" @click="handleClose">
      <img src="/images/icon/common/selection-card-close.svg" alt="" />
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    color: {
      type: String,
      default: () => "blue",
      validator: (value) => {
        return ["blue", "red", "green", "orange", "yellow", "lemon", "dawn", "mint", 
          "blond", 'wheat', 'lavender', 'pale-blue', 'melon', 'teal-deer', 'atomic-tangerine', 'cheese', 'hot-pink', 'aquamarine', 'blue-purple'
        ].includes(value);
      },
    },
    selection: {
      type: Object,
      default: () => ({
        id: 1,
        title: "title",
        name: '',
      }),
    },
    closeAble: {
      type: Boolean,
      default: () => (true),
    },
    titleField: {
      type: String,
      default: 'title'
    },
    allowTooltips: {
      type: Boolean,
      default: () => (true),
    }
  },
  computed: {
    getChipTitle() {
      const rawText = this.selection[this.titleField]
      if (rawText) {
        if (rawText.length > 10) {
          return {
            shouldTruncate: true,
            text: `${rawText.slice(0, 10)}...`,
            rawText: rawText
          };
        }
        return {
          shouldTruncate: false,
          rawText: rawText
        };
      } else {
        return {
          shouldTruncate: false,
          rawText: ''
        };
      }
      
    },
    getChipColorClass() {
      return `chip--${this.color}`
    }
  },
  methods: {
    handleClose() {
      this.$emit("close", this.selection);
    },
  },
};
</script>

<style scoped>

.select-item-card {
  height: 21px;
  border-radius: 3px;
  font-size: 11.25px;
  padding: 4px 8px;
  line-height: 12px;
  color: #4b4b4b;
  margin: 6px 8px 6px 0;
  display: inline-flex;
}

.chip--blue {
  background-color: #deedff;
}

.chip--red {
  background-color: #f9b5b5;
}
.chip--green {
  background-color: #90E5AA;
}
.chip--orange {
  background-color: #FFA167;
}
.chip--lemon {
  background-color: #F7F4B6;
}
.chip--dawn {
  background-color: #F7DEB7;
}
.chip--mint {
  background-color: #B8F7EF;
}

.chip--blond {
  background-color: var(--blond);
}
.chip--wheat {
  background-color: var(--wheat );
}
.chip--lavender {
  background-color: var(--lavender );
}
.chip--pale-blue {
  background-color: var(--pale-blue);
}
.chip--melon {
  background-color: var(--melon );
}
.chip--teal-deer {
  background-color: var(--teal-deer);
}
.chip--atomic-tangerine {
  background-color: var(--atomic-tangerine);
}
.chip--cheese {
  background-color: var(--cheese);
}
.chip--hot-pink {
  background-color: var(--hot-pink);
}
.chip--aquamarine {
  background-color: var(--aquamarine );
}
.chip--blue-purple {
  background-color: var(--blue-purple);
}


.select-item-card__close {
  display: inline-block;
  margin-left: 5px;
  cursor: pointer;
}

.select-item-card__title {
  /* max-width: 70px;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: nowrap; */
}

.select-item-card__close img {
  width: 8px;
  height: 8px;
}
</style>