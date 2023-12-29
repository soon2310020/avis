<template>
  <div class="new-feature-content" id="action-bar-feature">
    <div class="action-bar-feature" :style="{maxHeight: contentHeight + 'px'}">
      <div class="right-content" :style="{marginLeft: contentMarginLeft + 'px'}">
        <div class="content-header">
          Action bar
        </div>
        <div class="content-body">
          <div class="desc-item">
            You can now find the <strong>data table actions</strong>
            within the <strong>action bar</strong>.
          </div>
          <div class="desc-item">
            Access the <strong>action bar</strong> by selecting a checkbox.
          </div>
          <div class="desc-item">
            Single and batch selection is possible.
            (Available actions will vary)
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
    },
    data() {
      return {
        retry: 0,
        retryLimit: 100,
        firstElement: null,
        contentHeight: 287,
        contentMarginLeft: 24,
        largeWidth: 40,
        smallWidth: 27
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
        let firstCheckboxOffset = this.getFirstCheckboxOffset();
        if (firstCheckboxOffset && firstCheckboxOffset.x > 0) {
          let element = document.getElementById("action-bar-feature");
          element.style.margin = `${firstCheckboxOffset.y}px ${firstCheckboxOffset.x}px`;
          element.style.opacity = '1';
          this.contentMarginLeft = this.smallWidth + this.getLeftOffset() + 16;
          firstCheckboxOffset.x = firstCheckboxOffset.x + this.getLeftOffset();
          this.$emit('calculate-position-done', firstCheckboxOffset, this.smallWidth, this.calculateRightHeight());
        } else {
          if (this.retry > this.retryLimit) {
            return;
          }
          this.retry++;
          setTimeout(() => {
            this.calculatePosition();
          }, 300);
        }
      },
      getLeftOffset() {
        if (this.firstElement.clientWidth > this.smallWidth) {
          return (this.firstElement.clientWidth - this.smallWidth)/2;
        }
        return 0;
      },
      getAllCheckboxElementDocumentModel() {
        let tableCellElements = document.getElementsByTagName("td");
        let elements = [];
        for(let i = 0; i < tableCellElements.length; i++) {
          let tmpElement = tableCellElements[i];
          let checkboxElements = tmpElement.getElementsByClassName('checkbox');
          if (checkboxElements.length > 0) {
            for(let j = 0; j< checkboxElements.length; j++) {
              elements.push(tmpElement);
              break;
            }
          }
        }
        return elements;
      },
      getFirstCheckboxElementDocumentModel() {
        let elements = this.getAllCheckboxElementDocumentModel();
        if (elements && elements.length > 0) {
          return elements[0];
        }
      },
      getFirstCheckboxOffset() {
        this.firstElement = this.getFirstCheckboxElementDocumentModel();
        if (this.firstElement) {
          let position = this.getTopLeftPosition(this.firstElement);
          if (!position.x) {
            return;
          }
          return position;
        }
      },
      getTopLeftPosition(element) {
        let bounding = this.firstElement.getBoundingClientRect();
        return {
          x: bounding.x,
          y: bounding.y
        }
      },
      getBottomLeftPosition(element) {
        let bounding = element.getBoundingClientRect();
        return {
          x: bounding.x,
          y: bounding.y + element.clientHeight
        }
      },
      getDistance(firstElement, lastElement) {
        let top = this.getTopLeftPosition(firstElement);
        let bottom = this.getBottomLeftPosition(lastElement);
        return bottom.y - top.y;
      },
      calculateRightHeight() {
        let elements = this.getAllCheckboxElementDocumentModel();
        let first = elements[0];
        let last = elements[elements.length - 1];
        let distance = this.getDistance(first, last);
        if (distance < this.contentHeight) {
          return distance;
        }
        return this.contentHeight;

      }
    },
    computed: {
    },
    mounted() {
    }
  };
</script>

<style scoped>
  /* Modal Content/Box */
  .new-feature-content {
    opacity: 0;
    width: 407px;
  }

  .action-bar-feature {
    display: flex;
    max-height: 287px;
    overflow: hidden;
  }
  .action-bar-feature .left-content {
    width: 45px;
  }
  .action-bar-feature .right-content {
    background: #5a4ab9;
    color: #FFF;
    margin-left: 1rem;
    padding: 1rem 25px 1rem 25px;
    font-size: 16px;
  }
  .right-content .content-header {
    font-weight: bold;
    padding: 0.5rem 0 1rem;
  }
  .right-content .content-body .desc-item {
    padding: 0.25rem 0;
    line-height: 22px;
  }
  .right-content .content-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem 0 1rem;
  }
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
  .new-feature-content .right-content{
    border-radius: 4px;
  }
</style>
