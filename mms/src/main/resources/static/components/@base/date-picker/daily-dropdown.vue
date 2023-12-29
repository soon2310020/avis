<template>
  <div>
    <a-popover placement="bottom" v-model="visible" trigger="click">
      <template slot="content">
        <div class="daily-picker-popup-container">
          <daily-block
            ref="daily-block"
            :is-range="false"
            :current-date="currentDate"
            :date-range="dateRange"
            :show-footer="false"
            @change-date="handleChangeDate"
          ></daily-block>
        </div>
      </template>
      <base-button
        size="medium"
        icon="calendar"
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
      default: () => ({
        minDate: null,
        maxDate: new Date(),
      }),
    },
    buttonType: String,
  },
  data() {
    return {
      tempDate: { ...this.currentDate },
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
      //   let end = moment(this.tempDate.to).format("YYYY-MM-DD");
      return `${start}`;
    },
  },
  watch: {
    visible(newValue) {
      if (!newValue) {
        const passDate = { ...this.currentDate };
        this.$emit("on-close", passDate);
      } else {
        this.tempDate = this.currentDate;
        let child = this.$refs["daily-block"];
        if (child != null) {
          let passDate = { ...this.tempDate };
          console.log("watch visible", passDate);
          child.setDefault(passDate);
        }
      }
    },
  },
  methods: {
    handleClick() {
      this.visible = !this.visible;
      if (this.visible) {
        setTimeout(() => {
          let child = this.$refs["daily-block"];
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
      console.log("handleChangeDate after", this.currentDate);
    },

    changeTitleValue(newValue) {
      console.log("change title", newValue);
      this.tempDate.fromTitle = newValue.start;
      this.tempDate.toTitle = newValue.end;
    },
  },
};
</script>

<style>
.daily-picker-popup-container{
    padding: 10px;
}
.daily-picker-popup-container .block {
  margin-right: unset !important;
}
.daily-picker-popup-container .daily-container {
  display: flex;
  justify-content: center;
  align-items: center;
}
</style>
<style scoped src="/components/common/date-picker/date-picker-modal-style.css"></style>
<style scoped src="/components/common/date-picker/date-picker-style.css"></style>