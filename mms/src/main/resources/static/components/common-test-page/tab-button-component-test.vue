<template>
    <div>
        <p class="common-component-desc"></p>
        <div class="common-component-content">
            <tab-button-component
                :style-props="styleProps"
                :tab-type="tabType"
                :tab-style="tabStyle"
                :size="sizeProps"
                :tab-buttons="tabButtons"
                :click-handler="clickHandler"
                >
            </tab-button-component>

            <div class="sub-content" style="margin-top: 30px">

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

              <div class="props-select-row">
                <p><span>Output of Handler</span></p>

                <p>
                  <b>{{ result }}</b>
                </p>
              </div>
                <!-- Tab style props -->
                <div class="props-select-row">
                    <p><span>tab-style</span></p>
                    <select v-model="tabStyle">
                        <option value="horizontal">horizontal</option>
                        <option value="vertical">vertical</option>

                    </select>
                    <p>
                        <b>{{ tabStyle }}</b>
                    </p>
                </div>

                <!-- color type props -->
                <div class="props-select-row">
                    <p><span>tab-type</span></p>
                    <select v-model="tabType">
                        <option value="primary-tab">primary-tab</option>
                        <option value="secondary-tab">secondary-tab</option>
                      <option value="icon-tab">icon-tab</option>
                    </select>
                    <p>
                        <b>{{ tabType }}</b> <span style="color: #e34537">(Select the tab-type as <b>default</b>)</span>
                    </p>
                </div>
              <!-- size type props -->
              <div class="props-select-row">
                <p><span>size</span></p>
                <select v-model="sizeProps" :disabled="tabType!=='icon-tab'">
                  <option value="">== size props ==</option>
                  <option value="">default</option>
                  <option value="small">small</option>
                </select>
                <p>
                  <span>Only enabled when tab Type is icon-tab</span>
                  <b>{{ sizeProps }}</b>
                </p>
              </div>

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
        'tab-button-component': httpVueLoader('/components/common/tab-button-component.vue'),
    },
    data() {
        return {
            styleProps:'',
            result:{},
            stringOfItems:'',
            tabType: 'primary-tab',
            tabStyle: 'horizontal',
            sizeProps: 'large',
            tabButtons:[
              {title:'Date',active:true,icon:'/images/icon/part-icon-hover.svg',itemRate:30},
              {title:'Week',icon:'/images/icon/part-icon-hover.svg',itemRate:40},
              {title:'Month',icon:'/images/icon/part-icon-hover.svg',itemRate:60},
              {title:'Year',icon:'/images/icon/part-icon-hover.svg',itemRate:30}
            ]
        };
    },
  methods:{
    clickHandler(item) {
      this.result = item
    },
  },
  watch:{
    stringOfItems(){
      try{
        this.tabButtons=JSON.parse(this.stringOfItems)
      }
      catch(e){
        console.log("error in parsing please Enter valid input")
      }
    }
  },
  mounted(){
    this.stringOfItems=JSON.stringify(this.tabButtons)
  },
};
</script>
