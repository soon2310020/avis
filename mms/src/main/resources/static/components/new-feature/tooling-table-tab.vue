<template>
  <div class="new-feature-content" id="tooling-table-tab">
    <div class="tooling-table-tab">
      <div class="bottom-content">
        <div class="triangle-arrow"></div>
        <div class="close-hint" @click="skip">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M14 1.41L12.59 0L7 5.59L1.41 0L0 1.41L5.59 7L0 12.59L1.41 14L7 8.41L12.59 14L14 12.59L8.41 7L14 1.41Z" fill="#F0F1F3"/>
          </svg>
        </div>
        <div class="content-header">
          Interactive table tab
        </div>
        <div class="content-body">
          <div class="desc-item">
            Remove tabs from the table by simply clicking on the “x" icon on the right.
          </div>
        </div>
        <div class="content-footer">
          <div class="left-footer">
            <span>Step 2 of 3</span>
          </div>
          <div class="right-footer">
            <base-button
                size="medium"
                level="secondary"
                @click.stop="skip"
            >
              Next
            </base-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  data() {
    return {
      retry: 0,
      retryLimit: 100,
      firstElement: null
    };
  },
  methods: {
    skip() {
      this.$emit('skip')
    },
    calculatePosition() {
      console.log('calculate position');
      let firstCheckboxOffset = this.getFirstHintOffset();
      if (firstCheckboxOffset) {
        let element = document.getElementById("tooling-table-tab");
        element.style.margin = `${firstCheckboxOffset.y + 42}px ${firstCheckboxOffset.x + 88}px`;
        let position = {
          x: firstCheckboxOffset.x-12,
          y: firstCheckboxOffset.y - 12
        };
        this.$emit('calculate-position-done', position, firstCheckboxOffset.width + 24,  firstCheckboxOffset.height+ 24);
      } else {
        if (this.retry > this.retryLimit) {
          return;
        }
        this.retry++;
        setTimeout(() => {
          this.calculatePosition();
        }, 200);
      }
    },
    getFirstHintElementDocumentModel() {
      let hintElements = document.getElementsByClassName('tooling-tab-hint');
      if (hintElements.length > 0) {
        return hintElements[0];
      }
    },
    getFirstHintOffset() {
      this.firstElement = this.getFirstHintElementDocumentModel();
      if (this.firstElement) {
        let bounding = this.firstElement.getBoundingClientRect();
        return {
          x: bounding.x,
          y: bounding.y,
          width: bounding.width,
          height: bounding.height,
        }
      }
    }
  },
  watch: {
  },
  computed: {
  },
};
</script>

<style scoped>
.triangle-arrow {
  position: absolute;
  top: 0;
  right: 50%;
  background: rgb(90, 74, 185);
  width: 30px;
  height: 30px;
  transform: rotate(45deg) translate(2px, -50%);
}
/* Modal Content/Box */
.new-feature-content {
  margin: 15% 10%; /* 15% from the top and centered */
  width: 407px;
}

.tooling-table-tab {
  max-height: 287px;
  overflow: hidden;
}
.tooling-table-tab .top-content {
  background-color: #FFF;
  display: inline-block;
  font-size: 15px;
}
.tooling-table-tab .bottom-content {
  background: #5a4ab9;
  color: #FFF;
  margin-top: 2.5rem;
  padding: 1rem 32px 1rem 25px;
  font-size: 16px;
  position: relative;
}
.bottom-content .content-header {
  font-weight: bold;
  padding: 0.5rem 0 1rem;
}
.bottom-content .content-body .desc-item {
  padding: 0.25rem 0;
  line-height: 22px;
}
.bottom-content .content-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 0 1rem;
}
</style>
