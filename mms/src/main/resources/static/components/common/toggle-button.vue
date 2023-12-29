<template>
<div class="d-flex justify-content-start align-items-center">  
<p :class="[checked ? 'btn-off' : 'btn-on', disabled && 'default-off' ]">Off</p>
<input type="checkbox" v-model="checked" :disabled="disabled" id="switch" @change="changeListner"/>
<label :class="[disabled ? 'disable-label' : '', size === 'default' ? 'default-size-label' : 'large-size-label']" for="switch">Toggle</label>
<p :class="[checked ? 'btn-on' : 'btn-off', disabled && 'default-off' ]">On</p>
</div>

</template>

<script>
module.exports = {
 props: {
        checked: Boolean,
		disabled : Boolean,
		size: {
            type: String,
            default: 'default',
        },
      toogleHandler: Function,
    },

 data() {
        return {
        };
    },
  methods:{
    changeListner(){
      this.toogleHandler(this.checked)
    }
  },

watch: {
    }

    
}
</script>

<style scoped>

.default-size-label{
	width: 43px;
	height: 14px;
}

.default-size-label:after{
	width: 11px;
	height: 11px;
}

.default-size-label:active:after {
	width: 25px;
	border-radius: 50%;
}


.large-size-label{
	width: 53px;
	height: 20px;
}

.large-size-label:after{
	width: 17px;
	height: 17px;
}

.large-size-label:active:after {
	width:25px;
	border-radius: 50%;
}

.disable-label{
	pointer-events: none;
	background: #D9D9D9 !important;
	outline: #D9D9D9 solid 1px !important;
}

.btn-off{
font-weight: 400;
font-size: 12px;
line-height: 15px;
color: #C4C4C4;
}

.default-off {
	color: #C4C4C4 !important;
}

.btn-on{
font-weight: 400;
font-size: 12px;
line-height: 15px;
color: #4B4B4B;
}


input[type=checkbox]{
	height: 0;
	width: 0;
	visibility: hidden;
}

label {
	cursor: pointer;
	text-indent: -9999px;
	
    margin: auto 10px;
	outline:1px solid #595959;
	display: block;
	border-radius: 100px;
	position: relative;
}

label:after {
	content: '';
	position: absolute;
	top: 1.5px;
	left: 1.5px;
	
	background: #595959 ;
	border-radius: 90px;
	transition: 0.2s;
}

.disable-label:after{
	background: #fff !important;
}

input:checked + label {
	background: #3585E5;
	outline:1px solid #3585E5;
}

input:checked + label:after {
	left: calc(100% - 1.5px);
	transform: translateX(-100%);
	background : #fff;
}




</style>