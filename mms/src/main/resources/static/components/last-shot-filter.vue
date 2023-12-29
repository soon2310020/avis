<template>
  <div class="last-shot-filter">
    <div class="dash-bottom">
      Sort
    </div>
    <div class="last-shot-filter-item last-shot-filter-item-sort">
      <div>
        <img @click="currentSortType = 'asc'" :class="{'active': currentSortType === 'asc'}" src="/images/icon/sort-ascending.svg" alt="">
      </div>
      <div>
        <img @click="currentSortType = 'desc'" :class="{'active': currentSortType === 'desc'}" src="/images/icon/sort-ascending2.svg" alt="">
      </div>
    </div>
    <div style="padding: 9px 14px 2px 14px" class="dash-bottom">
      Filter
    </div>
    <div style="padding: 6px 0" class="last-shot-filter-item" ref="optionsRef">
      <div @click="currentFilterYear = null" :class="{'active': currentFilterYear === null }" class="last-shot-filter-item-filter">
        <span>All time</span>
      </div>
      <div v-for="(item, index) in getListYEar(firstShotYear)" :key="index" @click="currentFilterYear = item" :class="{'active': currentFilterYear === item }" class="last-shot-filter-item-filter">
        <span>{{ item }}</span>
      </div>
    </div>
  </div>

</template>
<script>
module.exports = {
  props: {
    lastShotData: {
      type: Object,
      default: () => ({
        sort: '',
        filter: null
      })
    }
  },
  components: {},
  data() {
    return {
      firstShotYear: null,
      currentFilterYear: null,
      currentSortType: '', // no default option
      isHovering: false
    };
  },
  methods: {
    getListYEar (startYear) {
      let currentYear = new Date().getFullYear(), years = [];
      while ( startYear <= currentYear ) {
        years.unshift(startYear++);
      }
      return years;
    },
    getFirstShotYear () {
      axios.get('/api/molds/first-shot-year').then((res) => {
        this.firstShotYear = res.data.data
      })
    },
    updateVariable () {
      const data = {
        sort: this.currentSortType,
        filter: this.currentFilterYear,
      }
      this.$emit('update-variable', data)
    },
    hoverCapture(event) {
      this.isHovering = event.type === 'mouseover'
    }
  },
  watch: {
    currentFilterYear () {
      this.updateVariable()
    },
    currentSortType () {
      this.updateVariable()
    },
    'lastShotData.sort' (newVal, oldVal) {
      // if (newVal.sort !== this.currentSortType || newVal.filter !== this.currentFilterYear) {
      //   this.currentFilterYear = newVal.filter
        this.currentSortType = newVal
      // }
    }
  },
  mounted () {
    this.getFirstShotYear()
    this.$refs.optionsRef.addEventListener('mouseover', this.hoverCapture)
    this.$refs.optionsRef.addEventListener('mouseout', this.hoverCapture)
  },
  unmounted() {
    this.$refs.optionsRef.removeEventListener('mouseover', this.hoverCapture)
    this.$refs.optionsRef.removeEventListener('mouseout', this.hoverCapture)
  }
};
</script>

<style scoped>
.last-shot-filter {
  width: 100%;
  z-index: 9999;
  position: absolute;
  background: #fff;
  top: calc(100% - 0.5em);
  right: 0;
  border-radius: 3px;
  box-shadow: 0 0 5px 0 #00000029;
  font-size: 14px;
  color: #4B4B4B;
  font-weight: 400;
  line-height: 100%;
}
.dash-bottom {
  border-bottom: 1px solid #E8E8E8;
  padding: 17px 14px 2px 14px;
}
.last-shot-filter-item {
  padding: 17px 14px 0 14px;
  max-height: 150px;
  overflow-y: auto;
}
.last-shot-filter-item-filter.active,.last-shot-filter-item-filter:hover {
  background: #E6F7FF;
}
.last-shot-filter-item-filter {
  padding: 7px 14px;
  color: #595959;
}
.last-shot-filter-item-sort div {
  border-radius: 999px;
  padding: 6px;
  margin-right: 15px;
}
.last-shot-filter-item-sort div img:not(.active) {
  opacity: 0.3;
}
.last-shot-filter-item-sort div img {
  max-width: 100%;
}
.last-shot-filter-item-sort > div:hover {
  background: #E6F7FF;
}
.last-shot-filter-item-sort {
  display: flex;
  padding: 17px 10px 0 10px;
}
</style>