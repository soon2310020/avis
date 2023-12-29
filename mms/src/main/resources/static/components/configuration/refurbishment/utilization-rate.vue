<template>
  <div class="skeleton utilization-rate">
    <div class="skeleton-main" :style="{width: totalWidth + 'px'}">
      <div class="skeleton-item low" :style="{ width: configData.low.width + 'px' }">
        <div class="main-line"></div>
        <div class="bracket" :style="{ height: configData.low.width + 'px' }">
          <img src="/images/configuration/bracket.svg#svgView(preserveAspectRatio(none))" alt="bracket" />
        </div>
        <div class="top-content">
          <div class="top-text low-text">Low</div>
        </div>
        <div class="bottom-content">
          <div class="bottom-title">0%</div>
        </div>
      </div>
      <div class="skeleton-item medium" :style="{ width: configData.medium.width + 'px' }">
        <div class="main-line"></div>
        <div class="bracket" :style="{ height: configData.medium.width + 'px' }">
          <img src="/images/configuration/bracket.svg#svgView(preserveAspectRatio(none))" alt="bracket" />
        </div>
        <div class="top-content">
          <div class="top-text medium-text">Medium</div>
        </div>
        <div class="bottom-content">
          <div class="bottom-input-wrapper">
            <input class="bottom-input form-control" v-model="tempInput.lowValue"
                   @blur="changeValue(inputType.LOW)"
                   @keyup.enter="changeValue(inputType.LOW)" style="background-color: #FFFFFF" :readonly="!categoryStatus.enabled"/> %
          </div>
        </div>
      </div>
      <div class="skeleton-item high" :style="{ width: configData.high.width + 'px' }">
        <div class="main-line"></div>
        <div class="bracket" :style="{ height: configData.high.width + 'px' }">
          <img src="/images/configuration/bracket.svg#svgView(preserveAspectRatio(none))" alt="bracket" />
        </div>
        <div class="top-content">
          <div class="top-text high-text">High</div>
        </div>
        <div class="bottom-content">
          <div class="bottom-input-wrapper">
            <input class="bottom-input form-control" v-model="tempInput.mediumValue"
                   @blur="changeValue(inputType.MEDIUM)"
                   @keyup.enter="changeValue(inputType.MEDIUM)"
                   style="background-color: #FFFFFF" :readonly="!categoryStatus.enabled"
            /> %
          </div>
        </div>
      </div>
    </div>
    <div class="skeleton-extra">
      <div class="extra-content">
        <div class="main-line">
          <div class="extra-arrow">
            <i class="fa fa-angle-right"></i>
          </div>
        </div>
        <div class="bottom-content">
          <div class="bottom-title">100%</div>
        </div>
        <div class="extra-title">
          Utilization Rate
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "RefurbishmentConfiguration",
  props: {
    resources:Object,
    updateCategoryConfigStatus: Function,
    categoryStatus: Object,
    updateConfig: Function
  },
  data() {
    return {
      configCategory: 'REFURBISHMENT',
      initConfigValue: [],
      configData: {
        low: {
          width: 0,
          value: 50
        },
        medium: {
          width: 0,
          value: 80
        },
        high: {
          width: 0,
          value: 100
        }
      },
      tempInput: {
        lowValue: 50,
        mediumValue: 80
      },
      inputType: {
        LOW: 'LOW',
        MEDIUM: 'MEDIUM'
      },
      totalWidth: 540
    }
  },
  methods: {
    updateConfigStatus() {
      this.categoryStatus.enabled = !this.categoryStatus.enabled;
      this.updateCategoryConfigStatus(this.configCategory, this.categoryStatus.enabled);
    },
    getCurrentConfig() {
      Common.getCurrentRefurbishmentConfig().then(data => {
        if (data) {
          this.configData.low.value = data.lm;
          this.configData.medium.value = data.mh;
          this.tempInput.lowValue = this.configData.low.value;
          this.tempInput.mediumValue = this.configData.medium.value;
          this.$emit('set-total-config', data);
          this.updateElementWidth();
        }
      })
    },
    changeValue(type) {
      // validate
      let error = null;
      if(isNaN(this.tempInput.lowValue) || isNaN(this.tempInput.mediumValue)) {
        error = 'The input should be numeric value';
      }
      if (type === this.inputType.LOW) {
        if (Number(this.tempInput.lowValue) >= Number(this.tempInput.mediumValue)) {
          error = 'The input should be smaller than ' + this.tempInput.mediumValue + '%';
        }
      } else if (type === this.inputType.MEDIUM) {
        if (Number(this.tempInput.mediumValue) <= Number(this.tempInput.lowValue)) {
          error = 'The input should be greater than ' + this.tempInput.lowValue + '%';
        }
      }
      if (Number(this.tempInput.lowValue) < 0) {
        error = 'The input should be greater than 0';
      } else if (Number(this.tempInput.mediumValue) > 100) {
        error = 'The input should be smaller than 100';
      }
      if (error) {
        Common.alert(error);
        $('button').on('click', () => {
          if (type === this.inputType.LOW) {
            this.tempInput.lowValue = this.configData.low.value;
          } else if (type === this.inputType.MEDIUM) {
            this.tempInput.mediumValue = this.configData.medium.value;
          }
        });
        return;
      }

      if (type === this.inputType.LOW) {
        this.configData.low.value = Number(this.tempInput.lowValue);
      } else if (type === this.inputType.MEDIUM) {
        this.configData.medium.value = Number(this.tempInput.mediumValue);
      }
      this.updateElementWidth();
      this.updateToServer();
    },
    getTotalWidth() {
      return Number(this.totalWidth);
    },
    updateElementWidth() {
      let totalWidth = this.getTotalWidth();
      let lowValue = Number(this.configData.low.value);
      let mediumValue = Number(this.configData.medium.value) - lowValue;
      let lowWidth = Math.floor(lowValue*totalWidth/100);
      let mediumWidth = Math.floor(mediumValue*totalWidth/100);
      let highWidth = totalWidth - mediumWidth - lowWidth;
      this.configData.low.width = lowWidth;
      this.configData.medium.width = mediumWidth;
      this.configData.high.width = highWidth;
    },
    updateToServer() {
      let data = {
        lm: this.configData.low.value,
        mh: this.configData.medium.value,
      };
      this.updateConfig(data);
    },
    getAllData(){
      this.updateElementWidth();
      this.getCurrentConfig();
    }
  },
  mounted() {
    this.updateElementWidth();
    this.getCurrentConfig();
  }
}
</script>

<style scoped>
.dialogTooling:hover {
  color: blue;
}
.alert-input {
  width: 50px;
  margin-left: 10px;
  margin-right: 10px;
  margin-bottom: 8px;
  text-align: center;
}
</style>