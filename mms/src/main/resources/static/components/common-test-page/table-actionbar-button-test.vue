<template>
  <div>
    <p class="common-component-desc"></p>
    <div class="common-component-content">
      <table-actionbar-button
          :button-type="actionBarButton"
          :disabled="disabledProps"
          :active="activeProps"
          :size="sizeProps"
          :icon-url="iconUrl"
          :click-handler="clickHandler"


      >{{ inputText }}</table-actionbar-button>

      <div class="sub-content" style="margin-top: 30px">
        <!-- button type props -->
        <div class="props-select-row">
          <p><span>button-type</span></p>
          <select v-model="actionBarButton">
            <option value="change-state">change state</option>
            <option value="disable-btn">disable</option>
            <option value="note">note</option>
            <option value="edit">edit</option>
            <option value="enable">enable</option>
            <option value="export">export</option>
            <option value="view">view</option>
            <option value="reset">reset</option>
            <option value="register">register</option>
            <option value="view-edit-history">view edit history</option>
            <option value="shift-config">shift config</option>
            <option value="role-user-config">role user config</option>
            <option value="role-permission-config">role permission config</option>
            <option value="remove-from-product">remove from product</option>
            <option value="custom">custom icon</option>

          </select>
          <p>
            <b>{{ actionBarButton }}</b>
          </p>
        </div>

        <!-- button iconUrl props -->
        <div class="props-select-row">
          <p><span>button-text</span></p>
          <input v-model="btntext" :disabled="!custom"/>
          <p>
            <span>Specifies button Text </span>
            <b>{{ btntext }}</b>
        </div>

        <!-- button iconUrl props -->
        <div class="props-select-row">
          <p><span>button-icon-url</span></p>
          <input v-model="iconUrl" :disabled="!custom"/>
          <p>
            <span>Specifies button icon url</span>
            <b class="icon-url-width">{{ iconUrl }}</b>
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
        <!-- size type props -->
        <div class="props-select-row">
          <p><span>size</span></p>
          <select v-model="sizeProps">
            <option value="">== size props ==</option>
            <option value="">default</option>
            <option value="small">small</option>
          </select>
          <p>
            <b>{{ sizeProps }}</b>
          </p>
        </div>


      </div>
    </div>
  </div>

</template>


<script>
module.exports = {
  name: "table-actionbar-button-test.vue",
  components: {
    'table-actionbar-button': httpVueLoader('/components/common/table-actionbar-button.vue'),
  },
  data() {
    return {
      inputText: 'change-state',
      activeProps: false,
      disabledProps: false,
      sizeProps: '',
      actionBarButton:'change-state',
      iconUrl:'',
      custom:false,
      btntext:'',
    };
  },
  watch:{
    disabledProps(){
      if(this.disabledProps){
        this.activeProps=!this.activeProps
      }
    },
    activeProps(){
      if(this.activeProps){
        this.disabledProps=!this.disabledProps
      }
    },

    actionBarButton(){
      if(this.actionBarButton=="change-state"){
        this.inputText="Change State"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="disable-btn"){
        this.inputText="Disable"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="note"){
        this.inputText="Note"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="edit"){
        this.inputText="Edit"
        this.btntext=""
        this.iconUrl=''
        this.custom =false
      }else if(this.actionBarButton=="enable"){
        this.inputText="Enable"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="export"){
        this.inputText="Export"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="view"){
        this.inputText="View"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="reset"){
        this.inputText="Reset"
        this.btntext=""
        this.iconUrl=''
        this.custom =false
      }
      else if(this.actionBarButton=="register"){
        this.inputText="Register"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="view-edit-history"){
        this.inputText="View Edit History"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="shift-config"){
        this.inputText="Shift Config"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="role-user-config"){
        this.inputText="Role-User Config"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }else if(this.actionBarButton=="remove-from-product"){
        this.inputText="Remove from Product"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }
      else if(this.actionBarButton=="role-permission-config"){
        this.inputText="Set Permission"
        this.btntext=""
        this.iconUrl=''
        this.custom =false

      }
      else{
        this.inputText=this.btntext
        this.custom=true;

      }

      },
    btntext(){
      if(this.btntext){
        this.inputText=this.btntext
      }


    }

      },
  methods:{
    clickHandler(){
      alert('click perform ----------  ')
    },
  }

};

</script>

<style scoped>
.icon-url-width{
  width: 70%;
  overflow-x: hidden;
}
.common-component-content > .sub-content {
  overflow-x: hidden;
}

</style>