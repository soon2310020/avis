<template>
  <div class="card-wrapper" :class="{ 'hover-box' : enableHover}" v-on="$listeners">
    <div class="title-style">{{title}}</div>
    <div :class="[getBottomColorClassName, {'clickable': clickable}]" @click="handleClick">
      <slot name="custom-bottom">
        <div>
          <span class="amount-text">{{amount}}</span>
          {{unit}}
        </div>
      </slot>
    </div>
  </div>

</template>

<script>
module.exports = {
  props: {
    bottomColorText: {
      type: String,
      default: () => "primary-black",
      validator: (value) => {
        return ["primary-black", "red", "blue"].includes(value);
      }
    },
    title: {
      type: String,
      default: () => ''
    },
    amount: {
      type: Number,
      default: () => 0
    },
    unit: {
      type: String,
      default: () => ''
    },
    enabledClick: {
      type: Boolean,
      default: () => false
    },
    enableHover: {
      type: Boolean,
      default: () => false
    },
    clickable: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    getBottomColorClassName() {
      return `bottom-text--${this.bottomColorText} ${this.enabledClick ? 'custom-hyper-link' : ''}`;
    },
  },
  methods: {
    handleClick(){
      this.$emit('on-click');
    }
  }
};
</script>

<style scoped>
.bottom-text--primary-black {
  color: #4B4B4B;
}
.bottom-text--red {
  color: #E34537;
}
.bottom-text--blue {
  color: #3491FF;
}
.bottom-text--blue, .bottom-text--primary-black, .bottom-text--red {
  font-family: 'Helvetica Neue';
  font-size: 15px;
  font-weight: 400;
  line-height: 17px;
  letter-spacing: 0em;
  text-align: left;
}
.card-wrapper {
  background: #FFFFFF;
  border: 1px solid #C4C4C4;
  border-radius: 3px;
  padding: 7.25px 21px;
  transition: all 300ms;
}
.hover-box:hover {  
  box-shadow: rgb(0 0 0 / 25%) 1px 2px 4px -1px;
}
.title-style {
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  color: #4B4B4B;
  margin-bottom : 15px;
}
.amount-text {
  font-family: Helvetica Neue;
  font-size: 30px;
  font-weight: 700;
  line-height: 37px;
  letter-spacing: 0em;
  text-align: left;

}
.clickable {
  cursor: pointer;
}
.clickable:hover {
  text-decoration: underline;
}
</style>