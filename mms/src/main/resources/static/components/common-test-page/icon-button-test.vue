<template>
<div>
        <p class="common-component-desc"></p>
        <div class="common-component-content">
            <icon-button
                :style-props="styleProps"
                :active="activeProps"
                :disabled="disabledProps"
                :button-type="buttonTypeProps"
                :click-handler="clickHandler"
                :icon-url="iconUrl"
                >
            </icon-button>

            <div class="sub-content" style="margin-top: 30px">

                <!-- button type props -->
                <div class="props-select-row">
                    <p><span>button-type</span></p>
                    <select v-model="buttonTypeProps">
                        <option v-for="(item,index) in buttonTypes " :key="index" :value="item">{{ item }}</option>
                    </select>
                    <p>
                        <b>{{ buttonTypeProps }}</b>
                    </p>
                </div>

              <!-- button iconUrl props -->
              <div v-show='custom' class="props-select-row">
                <p><span>button-icon-url</span></p>
                <input v-model="iconUrl"/>
                <p>
                  <span>Specifies button icon url</span>
                  <b>{{ iconUrl }}</b>
              </div>



                <!-- active props -->
                <div class="props-select-row">
                    <p><span>active</span></p>
                    <select v-model="activeProps">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <b>{{ activeProps }}</b>
                    </p>
                </div>

                <!-- disabled props -->
                <div class="props-select-row">
                    <p><span>disabled</span></p>
                    <select v-model="disabledProps">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <b>{{ disabledProps }}</b>
                    </p>
                </div>
              <!-- style props -->
              <div class="props-select-row">
                <p><span>style-props</span></p>
                <input v-model="styleProps"/>
                <p>
                  <span>Specifies icon Button style. (width, height, etc.)</span>
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
        'icon-button': httpVueLoader('/components/common/icon-button.vue'),
    },
    data() {
        return {
          iconUrl:'',
           custom:false,
            styleProps:'',
            activeProps: false,
            disabledProps: false,
            buttonTypeProps: 'filter',
            buttonTypes:['filter','custom-colum','kebab-menu','edit', 'trash', 'check', 'clone', 'add' ,'remove','previous', 'left-arrow','custom-button' ]
        };
    },
  watch:{
    buttonTypeProps(){
        if(this.buttonTypeProps=='custom-button'){
          this.custom=true;
        }
        else{
          this.custom=false;
        }
        console.log(this.custom)
      }

  },
   methods:{
     clickHandler(){
       console.log('clicked---------------')
     }
   }
};
</script>
