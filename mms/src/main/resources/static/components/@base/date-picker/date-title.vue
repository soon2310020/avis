<template>
  <div>
    <div v-if="enableEdit" class="d-flex" style="justify-content: space-between;">
      <div>
        <span class="right-title">From</span><br />
        <input
          type="text"
          class="date-bar id-input"
          :class="{
            'warning-input':
              range.startMessage != 'valid' && range.startMessage != '',
          }"
          placeholder="yyyy-mm-dd"
          @input="handleDateFromInput($event)"
          :value="dateTitle.from"
        /><br />
        <p
          v-if="range.startMessage != 'valid' && range.startMessage != ''"
          class="warning-text"
        >
          {{ range.startMessage }}
        </p>
      </div>
      <div>
        <span class="right-title">To</span><br />
        <input
          type="text"
          class="date-bar id-input"
          :class="{
            'warning-input':
              range.endMessage != 'valid' && range.endMessage != '',
          }"
          placeholder="yyyy-mm-dd"
          @input="handleDateToInput($event)"
          :value="dateTitle.to"
        /><br />
        <p
          v-if="range.endMessage != 'valid' && range.endMessage != ''"
          class="warning-text"
        >
          {{ range.endMessage }}
        </p>
      </div>
    </div>
    <div v-else class="d-flex" style="justify-content: space-between;">
      <div>
        <span class="right-title">From</span><br />
        <a-tooltip placement="bottomLeft">
          <template slot="title">
            <div style="font-size: 11px; color: #ebebeb">
              {{ tooltipMessage }}
            </div>
          </template>
          <input
            type="text"
            class="date-bar id-input date-bar-disable warning-cursor"
            placeholder="yyyy-mm-dd"
            readonly
            :value="getFromTitle"
          />
        </a-tooltip>
      </div>
      <div>
        <span class="right-title">To</span><br />
        <a-tooltip placement="bottomRight">
          <template slot="title">
            <div style="font-size: 11px; color: #ebebeb">
              {{ tooltipMessage }}
            </div>
          </template>
          <input
            type="text"
            class="date-bar id-input date-bar-disable warning-cursor"
            placeholder="yyyy-mm-dd"
            readonly
            :value="getToTitle"
          />
        </a-tooltip>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    enableEdit: Boolean,
    dateTitle: {
      type: Object,
      default: () => ({
        from: "",
        to: "",
      }),
    },
    dateValue: {
      type: Object,
      default: () => ({
        from: null,
        to: null,
      }),
    },
    tooltipMessage: {
      type: String,
      default: () => "",
    },
    frequency: {
      type: String,
      default: () => "",
    },
    changeTitle: Function,
    clearData: Function,
  },
  data() {
    return {
      inputType: "",
      title: this.dateTitle,
      range: {
        start: this.dateValue.from,
        end: this.dateValue.to,
        startMessage: "",
        endMessage: "",
      },
    };
  },
  computed: {
    getFromTitle(){
      return this.dateValue?.from ? moment(this.dateValue.from).format('YYYY-MM-DD') : ''
    },
    getToTitle(){
      return this.dateValue?.to ? moment(this.dateValue.to).format('YYYY-MM-DD') : ''
    }
  },
  watch: {
    frequency(newValue) {
      this.clearData();
      this.clearWarningTitle();
    },
    dateValue(newValue){
      // console.log('watch date value', newValue)
      if (newValue.from && newValue.to) {
        this.range.startMessage = 'valid'
        this.range.endMessage = 'valid'
      }
    }
  },
  methods: {
    handleDateFromInput: _.debounce(function (event) {
      this.inputType = "from";
      this.range.start = null;
      let input = event.target.value;
      let checkedValue = this.convertDate(input);
      this.range.startMessage = checkedValue.message;
      this.range.start = checkedValue.date;
      this.changeTitle({ start: checkedValue.title, end: this.dateTitle.to });
      if (this.range.startMessage == 'valid' && this.range.endMessage != "Please enter the date in the format ‘yyyy-mm-dd’" && this.range.endMessage != "") {
        this.checkDateOrder("from", checkedValue.title, this.dateTitle.to);
      }
      if (this.range.startMessage == 'valid' && this.range.endMessage == 'valid') {
        this.$emit('title-change', true, { start: checkedValue.title, end: this.dateTitle.to })
      } else {
        this.$emit('title-change', false)
      }
    }, 500),
    handleDateToInput: _.debounce(function (event) {
      this.inputType = "to";
      this.range.end = null;
      let input = event.target.value;
      let checkedValue = this.convertDate(input);
      this.changeTitle({ start: this.dateTitle.from, end: checkedValue.title });
      this.range.end = checkedValue.date;
      this.range.endMessage = checkedValue.message;
      if (this.range.endMessage == 'valid' && this.range.startMessage != "Please enter the date in the format ‘yyyy-mm-dd’" && this.range.startMessage != "") {
        this.checkDateOrder("to", this.dateTitle.from, checkedValue.title);
      }
      if (this.range.startMessage == 'valid' && this.range.endMessage == 'valid') {
        this.$emit('title-change', true, { start: this.dateTitle.from, end: checkedValue.title })
      } else {
        this.$emit('title-change', false)
      }
    }, 500),
    checkDateOrder(type, from, to) {
      if (type == "from") {
        if (moment(from).isBefore(to) || moment(from).isSame(to)) {
          this.range.startMessage = "valid";
          this.range.endMessage = "valid";
        } else {
          this.range.startMessage =
            "‘From’ date must be earlier than ‘To’ date.";
            this.range.start = null
        }
      } else if (type == "to") {
        if (moment(from).isBefore(to) || moment(from).isSame(to)) {
          this.range.endMessage = "valid";
          this.range.startMessage = "valid";
        } else {
          this.range.endMessage = "‘To’ date must be later than ‘From’ date.";
          this.range.end = null
        }
      }
    },
    convertDate(input) {
      let dateConverted = this.parseInputDate(input);
      let result = null;
      if (input == "") {
        result = {
          title: input,
          date: null,
          message: "",
        };
      } else {
        if (dateConverted) {
          let isValid = moment(dateConverted, "YYYY-MM-DD", true).isValid();
          if (isValid) {
            result = {
              title: dateConverted,
              date: moment(dateConverted, "YYYY-MM-DD").toDate(),
              message: "valid",
            };
          } else {
            result = {
              title: input,
              date: null,
              message: "Please enter the date in the format ‘yyyy-mm-dd’",
            };
          }
        } else {
          result = {
            title: input,
            date: null,
            message: "Please enter the date in the format ‘yyyy-mm-dd’",
          };
        }
      }
      return result;
    },
    parseInputDate(input) {
      const parten = /^([0-9]{4})-([0-9]{1,2})-([0-9]{1,2})$/;
      const match = input.match(parten);
      if (match) {
        let month = match[2];
        let date = match[3];

        if (!match[2].includes("0") && match[2] < 10) {
          month = "0" + match[2];
        }
        if (!match[3].includes("0") && match[3] < 10) {
          date = "0" + match[3];
        }
        return `${match[1]}-${month}-${date}`;
      }
      return null;
    },
    clearWarningTitle() {
      this.range.startMessage = "";
      this.range.endMessage = "";
    },
  },
};
</script>

<style>
</style>