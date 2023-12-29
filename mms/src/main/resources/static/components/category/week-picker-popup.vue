<template>
  <div>
    <a-popover placement="bottom" v-model="visible" trigger="click">
      <template slot="content">
        <div class="week-picker-popup-container">
          <weekly-block
            ref="weekly-block"
            :is-range="false"
            :current-date="tempDate"
            :date-range="dateRange"
            :show-footer="false"
            :enable-null="false"
            @change-date="handleChangeDate"
          ></weekly-block>
        </div>
      </template>
      <!-- <primary-button
        :is-active="visible"
        :title="getTitle"
        :default-class="getButtonClass.default"
        :active-class="getButtonClass.active"
        :prefix="getPrefixButton.default"
        :prefix-position="'right'"
        
      ></primary-button> -->
      <base-button
        size="medium"
        icon="arrow"
        level="secondary"
        :active="visible"
        @click="handleClick"
      >
        {{ getTitle }}
      </base-button>
    </a-popover>
  </div>
</template>

<script>
module.exports = {
  components: {
    "primary-button": httpVueLoader("/components/common/primary-button.vue"),
    "weekly-block": httpVueLoader(
      "/components/@base/date-picker/weekly-block.vue"
    ),
  },
  props: {
    currentDate: {
      type: Object,
      default: () => ({
        from: new Date(),
        to: new Date(),
        fromTitle: moment().format("YYYY-ww"),
        toTitle: moment().format("YYYY-ww"),
      }),
    },
    dateRange: {
      type: Object,
      default: () => ({
        minDate: null,
        maxDate: new Date(),
      }),
    },
    buttonType: String,
  },
  data() {
    return {
      tempDate: this.currentDate,
      title: {
        from: "",
        to: "",
      },
      visible: false,
    };
  },
  computed: {
    getTitle() {
      let start = moment(this.tempDate.from).format("YYYY-MM-DD");
      let end = moment(this.tempDate.to).format("YYYY-MM-DD");
      return `${start} to ${end}`;
    },
  },
  mounted() {
    this.initCurrentDate();
  },
  watch: {
    visible(newValue) {
      if (!newValue) {
        const passDate = { ...this.currentDate };
        this.$emit("on-close", passDate);
      } else {
        this.tempDate = this.currentDate;
        let child = this.$refs["weekly-block"];
        if (child != null) {
          let passDate = { ...this.tempDate };
          console.log("watch visible", passDate);
          child.setDefault(passDate);
        }
      }
    },
  },
  methods: {
    initCurrentDate() {
      console.log("currentDate", this.currentDate, this.tempDate);
      if (this.tempDate) {
        this.tempDate.fromTitle = `${this.tempDate.from.getFullYear()}-${this.getCurrentWeekNumber(
          this.tempDate.from
        )}`;
        this.tempDate.toTitle = `${this.tempDate.to.getFullYear()}-${this.getCurrentWeekNumber(
          this.tempDate.to
        )}`;
      }
    },
    handleInit() {
      this.tempDate = this.currentDate;
      this.initCurrentDate();
      let child = this.$refs["weekly-block"];
      if (child != null) {
        let passDate = { ...this.tempDate };
        console.log("watch visible", passDate);
        child.setDefault(passDate);
      }
    },
    handleClick() {
      this.visible = !this.visible;
      if (this.visible) {
        setTimeout(() => {
          let child = this.$refs["weekly-block"];
          console.log("mounted", child);
          if (child != null) {
            let passDate = { ...this.tempDate };
            child.setDefault(passDate);
          }
        }, 0);
      }
    },
    handleChangeDate(data, title) {
      console.log("handleChangeDate", data, title);
      this.tempDate.from = data.start;
      this.tempDate.to = data.end;
      this.changeTitleValue(title);
    },

    changeTitleValue(newValue) {
      console.log("change title", newValue);
      this.tempDate.fromTitle = newValue.start;
      this.tempDate.toTitle = newValue.end;
    },
    getCurrentWeekNumber(date) {
      let oneJan = new Date(date.getFullYear(), 0, 1);
      let numberOfDays = Math.floor((date - oneJan) / (24 * 60 * 60 * 1000));
      let result = Math.ceil((date.getDay() + 1 + numberOfDays) / 7);
      return result;
    },
  },
};
</script>

<style>
.week-picker-popup-container .block {
  margin-right: unset !important;
  padding: 10px;
}
</style>
<style
  scoped
  src="/components/common/date-picker/date-picker-modal-style.css"
></style>
<style
  scoped
  src="/components/common/date-picker/date-picker-style.css"
></style>
