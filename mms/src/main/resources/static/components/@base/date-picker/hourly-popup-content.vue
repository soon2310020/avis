<template>
  <div class="hourly-popup-container-style">
    <a-popover placement="bottom" v-model="visible" trigger="click">
      <template slot="content">
        <daily-block
          ref="hourly-daily-block"
          id="hourly-daily-block"
          :is-range="false"
          :current-date="currentDate"
          :date-range="dateRange"
          :show-footer="false"
          :enable-null="false"
          @change-date="handleChangeDate"
        ></daily-block>
      </template>
      <a href="javascript:void(0)">{{ getTitle }}</a>
    </a-popover>
    <div class="hourly-selection-container">
      <div class="paging__arrow">
        <a
          href="javascript:void(0)"
          class="paging-button"
          @click="changeSingleDate('-')"
        >
          <img src="/images/icon/category/paging-arrow.svg" alt="" />
        </a>
      </div>
      <hourly-block @change="handleChangeHour"></hourly-block>
      <div>
        <a
          href="javascript:void(0)"
          class="paging-button next-button-wrapper"
          :class="{ 'inactive-button': enableNextDate }"
          @click="changeSingleDate('+')"
        >
          <img
            src="/images/icon/category/paging-arrow.svg"
            style="transform: rotate(180deg)"
          />
        </a>
      </div>
    </div>
  </div>
</template>

<script>
const TODAY = new Date()
module.exports = {
  components: {
    "hourly-block": httpVueLoader(
      "/components/@base/date-picker/hourly-block.vue"
    ),
    "daily-block": httpVueLoader(
      "/components/@base/date-picker/daily-block.vue"
    ),
  },
  props: {
    currentDate: {
      type: Object,
      default: () => ({
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-MM-DD"),
        toTitle: moment().format("YYYY-MM-DD"),
      }),
    },
    dateRange: {
      type: Object,
      default: {},
    },
    isVisible: {
      type: Boolean,
      default: () => (false)
    }
  },
  data() {
    return {
      visible: false,
    };
  },
  computed: {
    getTitle() {
      return this.currentDate.from ? moment(this.currentDate.from).format('MMMM DD, YYYY') : 'Not Selected';
    },
    enableNextDate() {
      const todayTitle = moment(TODAY).format("YYYY-MM-DD")
      const result = moment(this.currentDate.fromTitle).isSame(todayTitle);
      console.log('enableNextDate', result, this.currentDate.fromTitle, todayTitle)
      return result
    }
  },
  watch: {
    isVisible(newVal) {
      setTimeout(() => {
        console.log('is visible', newVal)
        let defaultDate = { ...this.currentDate }
        if (!this.currentDate.from) {
          defaultDate = {
            from: new Date(),
            to: new Date(),
            fromTitle: moment().format("YYYY-MM-DD"),
            toTitle: moment().format("YYYY-MM-DD"),
          }
          this.currentDate = defaultDate
        }
        console.log('defaultDate', defaultDate)
        const dailyChild = this.$refs['hourly-daily-block']
        console.log(dailyChild)
        if (dailyChild) {
          dailyChild.setDefault(defaultDate)
        }
      }, 100);
    }
  },
  methods: {
    handleChangeDate(date, title) {
      console.log(date, title);
      this.$emit("change-date", date, title);
    },
    handleChangeHour(hour) {
      this.$emit('change-hour', hour)
    },
    changeSingleDate(sign) {
      const currentDate = { ...this.currentDate }
      let newDate = ''
      if (sign == '+') {
        newDate = moment(currentDate.from).add(1, 'd').toDate()
      } else if (sign == '-') {
        newDate = moment(currentDate.from).subtract(1, 'd').toDate()
      }
      const result = {
        start: newDate,
        end: newDate,
      }
      const title = {
        start: moment(newDate).format('YYYY-MM-DD'),
        end: moment(newDate).format('YYYY-MM-DD')
      }
      console.log('change change', currentDate, result, title)
      this.handleChangeDate(result, title)
    }
  },
  mounted() {

  },
};
</script>

<style>
.hourly-popup-container-style {
  text-align: center;
  padding: 20.46px 0 32.07px;
}
.hourly-selection-container {
  display: flex;
  align-items: center;
}
.next-button-wrapper {
  margin-left: 0;
  margin-right: 15px;
}
</style>
