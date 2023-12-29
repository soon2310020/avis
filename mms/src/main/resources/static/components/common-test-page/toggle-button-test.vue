<template>
    <div>
        <p class="common-component-desc"></p>
        <div class="common-component-content">

            <toggle-button :checked="checkedProps" :disabled="disableProps"  :size="sizeProps" :toogle-handler="toogleHandler"></toggle-button>

            <div class="sub-content" style="margin-top: 30px">

                <!-- button state props -->
                <div class="props-select-row">
                    <p><span>Toggle Button</span></p>
                     <select v-model="checkedProps">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <b>{{ checkedProps }}</b>
                    </p>
                </div>

               <!-- size type props -->
                <div class="props-select-row">
                    <p><span>size</span></p>
                    <select v-model="sizeProps">
                        <option value="default">default</option>
                        <option value="large">large</option>
                    </select>
                    <p>
                        <b>{{ sizeProps }}</b>
                    </p>
                </div>

                <!-- button disable props -->
                <div class="props-select-row">
                    <p><span>Disable Button</span></p>
                     <select v-model="disableProps">
                        <option :value="true">true</option>
                        <option :value="false">false</option>
                    </select>
                    <p>
                        <b>{{ disableProps }}</b>
                    </p>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
module.exports = {
    name: 'ToggleButtonTest',
    components: {
        'toggle-button': httpVueLoader('/components/common/toggle-button.vue'),
    },
    data() {
        return {
            checkedProps: true,
            disableProps: false,
            sizeProps: 'default',
        };
    },
  methods:{
    toogleHandler(checked){
      console.log(checked)
      this.checkedProps=checked;
    },
  },
};
</script>
