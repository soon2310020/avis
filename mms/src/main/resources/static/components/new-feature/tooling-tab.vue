<template>
  <div class="new-feature-content" id="tooling-tab-feature">
    <div class="tooling-tab-feature">
      <div class="bottom-content">
        <div class="content-header">
          New Tab for Tooling Data Table
        </div>
        <div class="content-body">
          <div class="desc-item">
            Tooling data is reorganized into 4 tabs: All, Digital, Non-digital and Disabled.
            Digital toolings are toolings installed with a counter.
            Non-digital toolings are toolings without a counter.
            Disabled toolings are toolings currently not in use.
          </div>
        </div>
        <div class="content-footer">
          <div class="left-footer">
            <span class="btn-skip" id="btn-skip" @click="skip">Skip</span>
          </div>
          <div class="right-footer">
            <a class="btn-done" href="javascript:void(0)" @click.prevent="done">Done</a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  module.exports = {
    props: {
      // data: [String, Number],
    },
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
      done() {
        this.$emit('done')
      },
      calculatePosition() {
        console.log('calculate position');
        let firstCheckboxOffset = this.getFirstHintOffset();
        if (firstCheckboxOffset) {
          let element = document.getElementById("tooling-tab-feature");
          element.style.margin = `${firstCheckboxOffset.y+26}px ${firstCheckboxOffset.x-12}px`;
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
  /* Modal Content/Box */
  .new-feature-content {
    margin: 15% 10%; /* 15% from the top and centered */
    width: 407px;
  }

  .tooling-tab-feature {
    max-height: 287px;
    overflow: hidden;
  }
  .tooling-tab-feature .top-content {
    background-color: #FFF;
    display: inline-block;
    font-size: 15px;
  }
  .tooling-tab-feature .bottom-content {
    background: #5a4ab9;
    color: #FFF;
    margin-top: 2rem;
    padding: 1rem 32px 1rem 25px;
    font-size: 16px;
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
  /*
  .left-footer .btn-skip {
    color: #CDCDCD;
    cursor: pointer;
  }
  .right-footer a.btn-done {
    border: 2px solid #EFEFEF;
    padding: 7.5px 25.5px;
    border-radius: 6px;
    color: #FFF;
    font-weight: 400;
    cursor: pointer;
    background-color: #3C2F93;
  }
  .right-footer a.btn-done:hover,
  .right-footer a.btn-done:active {
    text-decoration: none;
    background-color: #221B52;
  }
  */

</style>
