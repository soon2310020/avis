<template>
  <div
      v-if="isVisible"
      v-click-outside="onClose"
      class="modal-custom-barchart"
      style="width: 397px"
  >
    <div
        class="icon-wrapper"
        @click="onClose"
        aria-hidden="true"
    >
      <span class="t-icon-close"></span>
    </div>
    <div>
      <div class="info-depreciation-wrapper">
        <div class="basic-info-depreciation-wrapper">
          <span class="tooling-id-name"><span class="bold-headline">Tooling ID:</span> {{mold.equipmentCode}}</span>
          <img :src="previewImgUrl || '/images/no-image-available.png'" style="width: 126px; height: 115px; margin-top: 4px" />
        </div>
        <div v-if="isStraightLine" class="depreciation-content">
          <div>
            <div><span class="bold-headline">S.L. Depreciation:</span> {{mold.slDepreciation}} of {{ mold.slDepreciationTerm }} Years</div>
            <progress-bar
                style="width: 100%; margin-top:4px"
                :progress-percentage="mold.slDepreciation /mold.slDepreciationTerm*100"
            ></progress-bar>
          </div>
          <div><span class="bold-headline">Depreciation Term:</span> {{ mold.slDepreciationTerm }} years</div>
          <div><span class="bold-headline">Yearly Depreciation:</span> {{ getNumberWithCurrency(mold.slYearlyDepreciation) }}</div>
          <div><span class="bold-headline">Latest Depreciation Point:</span> {{ formatToDate(mold.slLatestDepreciationPoint) }}</div>
          <div><span class="bold-headline">Current Book Value of Asset:</span> {{ getNumberWithCurrency(mold.slCurrentBookValue) }}</div>
        </div>
        <div v-else class="depreciation-content">
          <div>
            <div>
              <span class="bold-headline">U.P. Depreciation:</span> {{
              getUpDepreciationPercentage(mold.upDepreciation, mold.upDepreciationTerm)
            }}%
            </div>
            <progress-bar
                style="width: 100%; margin-top:4px"
                :progress-percentage="getUpDepreciationPercentage(mold.upDepreciation, mold.upDepreciationTerm)"
            ></progress-bar>
          </div>
          <div><span class="bold-headline">Forecasted Max Shot:</span> {{mold.designedShot}} {{mold.designedShot > 1 ? 'shots' : 'shot'}}</div>
          <div><span class="bold-headline">Dep. Per Shot:</span> {{ getNumberWithCurrency(mold.upDepreciationPerShot)}}</div>
          <div><span class="bold-headline">Accumulated Shots:</span> {{mold.upDepreciation || 0}}</div>
          <div><span class="bold-headline">Latest Depreciation Point:</span> {{formatToDate(mold.upLatestDepreciationPoint)}}</div>
          <div><span class="bold-headline">Current Book Value:</span> {{ getNumberWithCurrency(mold.upCurrentBookValue) }}</div>
        </div>
      </div>
      </div>
      <div class="divider-line"></div>
      <div class="PO-info-wrapper">
        <div>
          <div><span class="bold-headline">PO Number:</span> {{mold.poNumber}}</div>
          <div><span class="bold-headline">PO Date:</span> {{formatToDate(mold.poDate)}}</div>
        </div>
        <div>
          <div><span class="bold-headline">Original Cost:</span> {{getNumberWithCurrency(mold.cost)}}</div>
          <div><span class="bold-headline">Salvage Value:</span> {{getNumberWithCurrency(mold.salvageValue)}}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    isVisible: {
      type: Boolean,
      default: () => false
    },
    mold: {
      type: Object,
      default: () => ({id: ''})
    },
    isStraightLine: {
      type: Boolean,
      default: () => false
    },
  },
  components: {
    "progress-bar": httpVueLoader("/components/@base/progress-bar/progress-bar.vue"),
  },
  data() {
    return {
      previewImgUrl: ''
    };
  },
  methods: {
    getUpDepreciationPercentage(numerator, denominator){
        return Math.min(100, Math.round(numerator / denominator * 1000)/10)
    },
    getNumberWithCurrency(number){
      return new Intl.NumberFormat('en', {style: "currency", currency: this.mold.costCurrencyType || 'USD'}).format(number || 0);
    },
    onClose(){
      this.$emit('close')
    },
    fetchPreviewImage(){
      const param = {
        storageTypes: "MOLD_CONDITION",
        refId: this.mold.id
      };

      axios
          .get("/api/file-storage/mold?" + Common.param(param))
          .then((response) => {
            const files = response.data["MOLD_CONDITION"];
            if(files?.length){
              this.previewImgUrl = files[0].saveLocation;
            } else {
              this.previewImgUrl = ''
            }
          });
    }
  },
  computed: {},
  watch: {
    isVisible: function(newValue){
      if(newValue){
        this.fetchPreviewImage();
      }
    }
  },
  mounted() {},
};
</script>
<style scoped>
.icon-wrapper {
  height: 15px;
  display: block;
  float: right;
  cursor: pointer;
}
.t-icon-close {
  width: 8px;
  height: 8px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
  display: block;
}
.info-depreciation-wrapper {
  display: flex;
  justify-content: space-between;
  font-size: 11.25px;
  line-height: 13px;
  color: #4B4B4B;
}
.basic-info-depreciation-wrapper{
  display: flex;
  flex-direction: column;
}
.divider-line {
  border: 0.5px solid #B2B6BA;
  width: 100%;
  margin-top: 11px;
  margin-bottom: 11px;
}
.bold-headline {
  font-weight: 700;
}
.PO-info-wrapper{
  display: grid;
  grid-template-columns: 1fr 1fr;
  font-size: 11.25px;
  line-height: 13px;
  color: #4B4B4B;
}
.depreciation-content{
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.tooling-id-name{
  width: 140px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
