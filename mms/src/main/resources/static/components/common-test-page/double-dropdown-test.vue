<template>
  <div>
    <p class="common-component-desc"></p>
    <div class="common-component-content">
      <double-dropdown
          :style-props="styleProps"
          :items="listItems"
          :apply-handler="clickHandler"
          :reset-handler="clickHandler"
      >
      </double-dropdown>
      <div class="sub-content" style="margin-top: 30px">
        <!-- input data props -->
        <div class="props-select-row">
          <p>Enter Items:</p>

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
    'double-dropdown': httpVueLoader('/components/common/double-dropdown.vue'),

  },
  data() {
    return {

      stringOfItems:'',
      inputText: '',
      result: null,
      styleProps: "position: static; width: 342px;",
      listItems: [
      {
        id:"OP",
        title:'By OP',
        childrens:[
          {title:'All',code:"ALL"},
          {title:'Active',code:"WORKING"},
          {title:'Idle',code:"IDLE"},
          {title:'Inactive',code:"NOT_WORKING"},
          {title:'Disconnected',code:"DISCONNECTED"}
        ]
      },
      {
        id:"status",
        // title:'Status',
        title:'By Status',
        childrens:[
          {title:'All',code:"ALL"},
          {code: "DISCARDED", title: "Discarded",},
          {code: "INSTALLED",title: "Installed"},
          {code: "FAILURE", title: "Failure" },
          {code: "AVAILABLE",title: "Available" },
          {code: "DETACHED", title: "Detached" }
        ]
      }
    ],
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