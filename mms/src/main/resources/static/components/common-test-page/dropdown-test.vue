<template>
  <div>
    <p class="common-component-desc"></p>
    <div class="common-component-content">
      <custom-dropdown
          :style-props="styleProps"
          :items="listItems"
          :searchbox="searchboxProps"
          :checkbox="checkboxProps"
          :all-checkbox="allCheckboxProps"
          :click-handler="clickHandler"
          :set-result="setResult"
          :placeholder="placeholder"
      >
      </custom-dropdown>
      <div class="sub-content" style="margin-top: 30px">
        <!-- input data props -->
        <div class="props-select-row">
          <p>Enter Items:</p>
          <!--                    <input v-model="inputText" />-->
          <!--                    <p>Disable:</p>-->
          <!--                    <select v-model="disableItem">-->
          <!--                      <option :value="true">true</option>-->
          <!--                      <option :value="false">false</option>-->
          <!--                    </select>-->
          <!--                  <a-button class="primary" @click="addItem" @keyup.prevent="addItem" :disabled="inputText.length <= 3">-->
          <!--                    Add Item-->
          <!--                  </a-button>-->
          <textarea
              rows="3"
              style="width: 100%;
              border-color: #909090;
              padding: 7px"
              v-model="stringOfItems">
          </textarea>
          <p><b> {{ stringOfItems }}</b></p>
        </div>

        <!-- output props -->
        <div class="props-select-row">
          <p><span>Selected Option</span></p>

          <p>
            <b>{{ result }}</b>
          </p>
        </div>


        <!-- searchbox props -->
        <div class="props-select-row">
          <p><span>Searchbox</span></p>
          <select v-model="searchboxProps">
            <option :value="true">true</option>
            <option :value="false">false</option>
          </select>
          <p>
            <b>{{ searchboxProps }}</b>
          </p>
        </div>

        <!-- searchbox placeholder -->
        <div class="props-select-row">
          <p><span>Search-box Placeholder</span></p>
          <input v-model="placeholder" :disabled="!searchboxProps"/>
          <p>
            <span>Specifies SearchBox placeholder</span>
            <b>{{ placeholder }}</b>
          </p>
        </div>

        <!-- checkbox props -->
        <div class="props-select-row">
          <p><span>checkbox</span></p>
          <select v-model="checkboxProps">
            <option :value="true">true</option>
            <option :value="false">false</option>
          </select>
          <p>
            <b>{{ checkboxProps }}</b>
          </p>
        </div>

        <!-- allCheckBox props -->
        <div class="props-select-row">
          <p><span>allCheckbox</span></p>
          <select v-model="allCheckboxProps" :disabled="listItems.length < 5">
            <option :value="true">true</option>
            <option :value="false">false</option>
          </select>
          <span>Defines whether the checklist Select All / Deselect All buttons are enabled. Item must be grater than 4.</span>
          <p>
            <b>{{ allCheckboxProps }}</b>
          </p>
        </div>


        <!-- style props -->
        <div class="props-select-row">
          <p><span>style-props</span></p>
          <input v-model="styleProps"/>
          <p>
            <span>Specifies dropdown style. (width, height, etc.)</span>
            <b>{{ styleProps }}</b>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  components: {
    'custom-dropdown': httpVueLoader('/components/common/custom-dropdown.vue'),

  },
  data() {
    return {
      stringOfItems:'',
      inputText: '',
      result: null,
      searchboxProps: false,
      checkboxProps: false,
      allCheckboxProps: true,
      styleProps: "position: static; width: fit-content; min-width:200px",
      listItems: [{title: "option 1"}, {title: "option 2 disabled", disabled: true}, {
        title: "option 3 with img",
        image: 'https://dev.emoldino.com/images/icon/e-icon.ico'
      }],
      disableItem: false,
      placeholder: 'Enter Placeholder',

    };
  },
  watch:{
    stringOfItems(){
      try{
        this.listItems=JSON.parse(this.stringOfItems)
      }
      catch(e){
          console.log("error in parsing please Enter valid input")
      }
    }
  },
  mounted(){
    this.stringOfItems=JSON.stringify(this.listItems)
  },
  methods: {

    addItem() {
      if (this.inputText != '') {
        this.listItems.push({title: this.inputText, disabled: this.disableItem});
        this.inputText = '';
      }
    },
    clickHandler(item) {
      this.result = item
    },
    setResult(items) {
      this.result = items
    }
  },

};
</script>
<!--<style>-->
<!--.common-component-content{-->
<!--  border: unset !important;-->
<!--}-->
<!--</style>-->
