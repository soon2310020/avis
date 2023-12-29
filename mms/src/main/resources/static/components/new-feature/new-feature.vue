<template>
  <div :id="modalId" class="new-feature-backdrop" @click="skipNotify">
    <div class="new-feature-backdrop-content">
      <div class="backdrop-row first-row">
      </div>
      <div class="backdrop-row main-row">
        <div class="backdrop-cell left-cell"></div>
        <div class="backdrop-cell main-cell"></div>
        <div class="backdrop-cell right-cell"></div>
      </div>
      <div class="backdrop-row bottom-row">
      </div>
    </div>
    <!-- Modal content -->
    <div class="new-feature-wrapper">
<!--      <action-bar @calculate-position-done="doneCalculatePosition" v-if="isFeature(features.ACTION_BAR) && isTriggeredFromOutside" @skip="skipNotify" @done="saveNotifyDone">-->
<!--      </action-bar>-->
<!--      <number-of-part @calculate-position-done="doneCalculatePosition" v-if="isFeature(features.NUMBER_OF_PART) && isTriggeredFromOutside" @skip="skipNotify" :data="data" @done="saveNotifyDone">-->
<!--      </number-of-part>-->
<!--      <tooling-tab @calculate-position-done="doneCalculatePosition" v-if="isFeature(features.TOOLING_TAB) && isTriggeredFromOutside" @skip="skipNotify" @done="saveNotifyDone">-->
<!--      </tooling-tab>-->
      <tooling-table-layout @calculate-position-done="doneCalculatePosition" v-if="isFeature(features.DATA_TABLE_TAB) && isTriggeredFromOutside && currentStep === 1" @skip="skipNotify">
      </tooling-table-layout>
      <tooling-table-tab @calculate-position-done="doneCalculatePosition" v-if="isFeature(features.DATA_TABLE_TAB) && isTriggeredFromOutside && currentStep === 2" @skip="skipNotify">
      </tooling-table-tab>
      <tooling-tab-view @calculate-position-done="doneCalculatePosition" v-if="isFeature(features.DATA_TABLE_TAB) && isTriggeredFromOutside && currentStep === 3" @done="saveNotifyDone">
      </tooling-tab-view>
    </div>
  </div>
</template>

<script>
  module.exports = {
    props: {
      feature: {
        type: String
      }
    },
    components: {
      'action-bar': httpVueLoader('/components/new-feature/action-bar.vue'),
      'number-of-part': httpVueLoader('/components/new-feature/number-of-part.vue'),
      'tooling-tab': httpVueLoader('/components/new-feature/tooling-tab.vue'),
      'tooling-table-layout': httpVueLoader('/components/new-feature/tooling-table-layout.vue'),
      'tooling-table-tab': httpVueLoader('/components/new-feature/tooling-table-tab.vue'),
      'tooling-tab-view': httpVueLoader('/components/new-feature/tooling-tab-view.vue')
    },
    data() {
      return {
        animationTimeMs: 2000,
        features: {
          ACTION_BAR: 'ACTION_BAR',
          NUMBER_OF_PART: 'NUMBER_OF_PART',
          TOOLING_TAB: 'TOOLING_TAB',
          DATA_TABLE_TAB: 'DATA_TABLE_TAB'
        },
        data: null,
        isShow: false,
        isTriggeredFromOutside: false,
        currentStep: 1
      };
    },
    computed: {
      modalId() {
        return 'new-feature-modal-' + this.feature
      }
    },
    methods: {
      triggerFromOutSide(data) {
        console.log('triggerFromOutSide');
        if (this.isTriggeredFromOutside) {
          return;
        }
        this.data = data;
        this.determineShowFeatureNotify();
        this.isTriggeredFromOutside = true;
      },
      isFeature(feature) {
        return this.feature === feature;
      },
      determineShowFeatureNotify() {
        axios.get('/api/on-boarding/get?' + Common.param(this.getFeatureParams())).then((response) => {
          if (response.data && response.data.data && !response.data.data.seen) {
            if (this.feature === this.features.DATA_TABLE_TAB) {
              this.calculatePosition('tooling-table-layout');
            }
            // if (this.feature === this.features.ACTION_BAR) {
            //   this.calculatePosition('action-bar');
            // } else if (this.feature === this.features.NUMBER_OF_PART) {
            //   this.calculatePosition('number-of-part');
            // } else if (this.feature === this.features.TOOLING_TAB) {
            //   this.calculatePosition('tooling-tab');
            // }
          }else {
            this.$emit("feature-notify-done",this.feature);
          }
        })
      },
      calculatePosition(component) {
        let child = Common.vue.getChild(this.$children, component);
        if (child) {
          child.calculatePosition();
        } else {
          setTimeout(() => {
            this.calculatePosition(component);
          }, 50)
        }
      },
      doneCalculatePosition(start, width, height) {
        let holeRectangle = new HoleRectangle(start, width, height);
        let holePopup = new HolePopup(holeRectangle);
        holePopup.build();
        console.log('doneCalculatePosition',holeRectangle)
        this.showFeatureNotify();
      },
      showFeatureNotify() {
        $('#' + this.modalId).fadeIn(this.animationTimeMs);
      },
      getFeatureParams() {
        return {
          feature: this.feature
        }
      },
      hideFeatureNotify() {
        $('#' + this.modalId).fadeOut(this.animationTimeMs);
      },
      skipNotify() {
        this.hideFeatureNotify();
        setTimeout(() => {
          this.$emit("feature-notify-next");
          this.currentStep++
          if(this.currentStep === 2) {
            this.calculatePosition('tooling-table-tab');
          } else if (this.currentStep === 3) {
            this.calculatePosition('tooling-tab-view');
          } else {
            this.saveNotifyDone()
          }
        }, 1500);
      },
      saveNotifyDone() {
        axios.post('/api/on-boarding/update', this.getFeatureParams()).then(() => {
          this.hideFeatureNotify();
          setTimeout(() => {
            this.$emit("feature-notify-done",this.feature);
          }, 2000);
        })
      }

    }
  };
</script>
<style src="/components/new-feature/new-feature-popup.css?v2022-04-28" cus-no-cache scoped></style>
<style scoped>
  .close-hint {
    position: absolute;
    right: 14px;
    top: 14px;
    cursor: pointer;
  }
  .new-feature-backdrop {
    display: none; /* Hidden by default */
    position: fixed; /* Stay in place */
    z-index: 9999999999; /* Sit on top */
    left: 0;
    top: 0;
    width: 100%; /* Full width */
    height: 100%; /* Full height */
    overflow: hidden; /* Disabled scroll if Ò */

  }

  .new-feature-backdrop-content {
    width: 100%;
    height: 100%;
  }
  .backdrop-row {
    display: flex;
    width: 100%;
    height: 100%;
  }
  .first-row, .bottom-row {
    background-color: rgb(0,0,0); /* Fallback color */
    background-color: rgba(0,0,0,0.85); /* Black w/ opacity */
  }
  .backdrop-cell {
    background-color: rgb(0,0,0); /* Fallback color */
    background-color: rgba(0,0,0,0.85); /* Black w/ opacity */
    height: 100%;
    width: 100%;
  }
  .right-cell {
    flex: 1;
  }
  .backdrop-cell.main-cell {
    background-color: transparent;
    overflow: hidden;
  }

  .backdrop-cell.main-cell:before {
    box-shadow: 0px 0px 0 100px rgb(0 0 0 / 85%);
    border-radius: 4px;
    content: '';
    width: inherit;
    height: inherit;
    display: inline-block;
  }

  .new-feature-wrapper {
    position: fixed;
    top: 0;
    left: 0;
  }

</style>
