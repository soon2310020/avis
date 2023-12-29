<template>
  <div>
    <p class="common-component-desc"></p>
    <div class="common-component-content">
        <time-picker
            @change-time="timeChangeHandler"
            :min-time="startTime"
            :max-time="endTime"
            :enable-minute="EnableMinute"
            :disabled="disabledProps"
            :is-range="EnableRange"
            :style-props="styleProps"
            :selected-time="dayEnd"
            :id="1"
            :active="activeProps"
        ></time-picker>
  <div class="sub-content" style="margin-top: 30px">
  <!-- Select Min time and max time  -->
    <div class="props-select-row">
      <p><span>Output</span></p>
        <b>{{ output }}</b>

    </div>
    <div class="props-select-row">
      <p><span>enable-minutes</span></p>

      <select  v-model="EnableMinute" :disabled="EnableRange">
        <option  :value="false" >false</option>
        <option :value="true">true</option>
      </select>
      <p>
        <b >{{ EnableMinute}}</b>
      </p>

    </div>

    <!-- Active  props -->
    <div class="props-select-row">
      <p><span>active</span></p>
      <select  v-model="activeProps" >
        <option :value="false">false</option>
        <option :value="true" >true</option>

      </select>
      <p>
        <b>{{ activeProps}}</b>
      </p>
    </div>
      <!-- styleProps props -->
      <div class="props-select-row">
        <p><span>style-props</span></p>
        <input v-model="styleProps" />
        <p>
          Specifies the time picker style. (color, height, etc.)
          <br />
          <b>{{ styleProps }}</b>
        </p>
      </div>


    <!-- Time-Picker type props -->
    <div class="props-select-row">
      <p><span>is-Range</span></p>
      <time-picker
          v-show="EnableRange"
          @change-time="handleChangeStartDate"
          :min-time="'0000'"
          :id="5"
          :max-time="'0000'"
          :selected-time="dayStart"
          :enable-minute="false"
          :disabled="disabledProps"
          :style-props="styleProps"
          :range-props="false"
      ></time-picker>
      <select  v-model="EnableRange" >
        <option :value="false">false</option>
        <option :value="true" >true</option>

      </select>
      <p>
        <b>{{ EnableRange}}</b>
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


  </div>
    </div>





  </div>

</template>

<script>
module.exports= {
  name: "time-picker-test",
  components: {
    "time-picker": httpVueLoader("/components/common/time-picker.vue"),
  },
  data(){
    return{
      output:'',
      startTime:'0000',
      endTime:'0000',
      styleProps: 'height: 40px; color: #349IFB',
      EnableMinute:true,
      EnableRange:false,
      disabledProps: false,
      activeProps:false,

      dayStart: {
        hour: 0,
        minute: 0,
        isAddDay: false,
      },
      dayEnd: {
        hour: 0,
        minute: 0,
        isAddDay: false,
      },
      minTime:'0000',
      maxTime:'2359',
      addDay:"false"


    }
  },
  watch:{
    addDay(){
      if(this.addDay || this.timeChangeHandler){
        this.dayEnd.isAddDay = true;
      }
      else{
        this.dayEnd.isAddDay = false;
      }

    },
    EnableRange(){
      if(this.EnableRange){
        this.EnableMinute=false;
      }
    },
    EnableMinute(){
      if(this.EnableMinute){
        this.EnableRange=false;

      }
    },
    activeProps(){
      if(this.activeProps){
        this.styleProps="height: 40px; color: #349IFB ;  color: #3585E5; background: #DAEEFF; border: 1px solid #3585E5; border-radius: 3px;"

      }
      else{
        this.styleProps="height: 40px;"

      }
    }

  },

  methods:{


    handleChangeStartDate(time, type) {
      if(time.hour<10){
        this.startTime = '0'+time.hour+'00';
      }
      else{
        this.startTime = time.hour+'00';
      }

    },
    timeChangeHandler(time, type) {

        this.output=time


    },
  },
}

</script>

<style scoped>
.dropdown-menu{
  min-width: unset !important;

}
.dropdown-item:hover, .dropdown-menu .selected-item {
  background-color: #e6f7ff !important;
}
.time-dropdown {
  padding: 12px 12px 12px 12px !important;
  width: unset !important;


          }
.time-dropdown:hover{
  background-color: rgb(218, 238, 255);
  border: 1px solid rgb(52, 145, 255);
}
.time-dropdown:focus{
  background-color: rgb(218, 238, 255);
  border: 2px solid rgb(52, 145, 255);
}
.bonus-time {
  right: 8% !important;
  top: 0px !important;
}

</style>