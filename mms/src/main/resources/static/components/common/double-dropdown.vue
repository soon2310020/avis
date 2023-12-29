

<template>
  <div class="dropdown-wrap" :style="styleProps">

    <ul class="parent-item row" style="height: 305px;">

      <li v-for="(item, index) in filtered" :key="index" class="col-md custom-padding">
          <label>
           <h5 class="font-setting">{{ item.title }}</h5>
          </label>
            <ul  class="child-item">
              <li v-for="(subItem, index2) in item.childrens" :key="`${index2}-${index}`">
                <label :for="`dropdown-input-${index}-${index2}-${id}`" @mouseup="toggleCheckValue(item,subItem,index)">
                  <template>
                    <input
                        :id="`dropdown-input-${index}-${index2}-${id}`"
                        type="checkbox"
                        :value="JSON.stringify(subItem)"
                        :checked='subItem.checked'
                        v-model="subItem.checked"
                    />
                    <div class="checkbox-custom"></div>
                  </template>
                  <div v-if="subItem.image">
                    <img :src="subItem.image" alt=""/>
                  </div>
                  {{ subItem.title }}
                </label>
              </li>
            </ul>
        <span v-show="index!=(items.length-1)" class="vertical-line"></span>
      </li>
    </ul>
    <div class="buttons-custom d-flex justify-content-between">
      <div class=" all-select-list reset-styles">
        <div @click="resetDataHandler()" class="reset-btn-styles" >Reset to Default</div>
      </div>
      <div class="">
<!--        class="btn-custom btn-custom-primary animationPrimary"-->
        <cta-button
            type="link"
            color-type="blue-fill"
            :click-handler="applyHandlerMethod">
          <span>Apply</span>
        </cta-button>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  components:{
    'cta-button': httpVueLoader('/components/common/cta-button.vue'),
  },
  name: 'Tree Select Dropdown',
  props: {
    items: Array,
    styleProps: String, // Set whether dropdown-list wrap-style or not (ex: {top: '10px', left: '10px', width: '10px'})
    id: String,
    applyHandler: Function,
    resetHandler: Function
  },
  data() {
    return {
      inputContent: '', // The input value. Shows the input list searched through filtered.
      checkedArray: [], // The checked list is saved. Passed to the parent component through the setResult function
    };
  },

  mounted() {
    setTimeout(()=>{
      this.filtered.forEach((item)=>{
        this.selectAll(item,item,item.title);
      })
    },1000);


  },
  computed: {
    // Filter your search terms
    filtered: {
      get: function () {
        if (this.items) {
          const str = this.inputContent.trim();
          if (!str || str == '') {
            return this.items;
          }
          if (this.items != null) {
            let filter = this.items.filter((item) => {
              return item.title != null && item.title.toUpperCase().includes(str.toUpperCase());
            });
            let result = filter.sort((firstItem, secondItem) => {
              return firstItem.title.toUpperCase() > secondItem.title.toUpperCase() ? 1 : -1;
            });
            return result;
          }
          return this.items;
        }
      },
    },
  },
  methods: {

    toggleCheckValue(parentItem, subItem, index){
         subItem.checked=!subItem.checked;
        if(subItem.title=='All' && subItem.checked==true){

            this.selectAllCheckBox(parentItem,index,true)
        }
        else if(subItem.title=='All' && subItem.checked==false){

          this.selectAllCheckBox(parentItem,index,false)
        }
      this.SyncCheckedArray();
      let checked = this.checkedArray.find((item)=>item.title==parentItem.title);
      if(checked!== undefined){
        if(checked?.childrens.length==this.filtered[index].childrens.length-1){
          this.selectOrDeselectAllbox(parentItem,index)
        }
      }

      this.SyncCheckedArray();
    },

    selectOrDeselectAllbox(parentItem,parentIndex) {
      let parentTitle = parentItem.title;
      let temp = _.cloneDeep(this.filtered)
      this.filtered.splice(0);
      this.$nextTick(() => {
        temp.map((item) => {
          let obj = {id: item.id, title: item.title, childrens: []}
          item.childrens.map((subItem) => {
            obj.childrens.push({title: subItem.title, code: subItem.code})
          })
          this.filtered.push(obj)
        })
        this.filtered.map((item, index) => {
          this.selectAllBox(item, temp[index], parentTitle)
        })
      })
    },

    selectAllBox(parentItem,value,parentTitle){

      let all = value.childrens.find((item)=>item.title=='All');
        parentItem.childrens.forEach((subItem,index) => {
          if(parentItem.title==parentTitle && subItem.title=='All'){
            subItem.checked = !value.childrens[index].checked;
          }
          else{
            subItem.checked = value.childrens[index].checked;
          }
        });
      this.$forceUpdate();
    },

    applyHandlerMethod(){
      this.$nextTick(() => {
        const self = this;
        if (self.applyHandler) {
          this.SyncCheckedArray();
         self.applyHandler(self.checkedArray);
        }
      });
      this.$forceUpdate();

    },

    selectAllCheckBox(parentItem,parentIndex,isSelect) {
      let parentTitle= parentItem.title;
      let temp = _.cloneDeep(this.filtered)
      this.filtered.splice(0);
      this.$nextTick(()=>{
        temp.map((item)=>{
          let obj={id:item.id,title:item.title}
          let arr=[]
            item?.childrens.map((subItem)=>{
              arr.push({title:subItem.title,code:subItem.code})
            })

          obj.childrens=arr;
          this.filtered.push(obj)
        })

        this.filtered.map((item,index)=>{
          if(isSelect){
            this.selectAll(item,temp[index],parentTitle)
          }
          else{
            this.unselectAll(item,temp[index],parentTitle)
          }
        })
      })
       this.$forceUpdate();
    },

    // select all
    selectAll(parentItem,value,parentTitle){
        if(parentTitle==parentItem.title){

          parentItem?.childrens.forEach((subItem) => {
            subItem.checked = true;

          });
        }
        else{
          parentItem?.childrens.forEach((subItem,index) => {
            subItem.checked = value.childrens[index].checked;

          });
        }


        this.$forceUpdate();
    },
    // Deselect all.
    unselectAll(parentItem,value,parentTitle) {
      if(parentTitle==parentItem.title){
        parentItem?.childrens.forEach((subItem) => {
          subItem.checked = false;

        });
      }
      else{
        parentItem?.childrens.forEach((subItem,index) => {
          subItem.checked = value.childrens[index].checked;

        });
      }

      this.$forceUpdate();
    },

    resetAllCheckBox() {

      let temp = _.cloneDeep(this.filtered)
      this.filtered.splice(0);
      this.$nextTick(()=>{
        temp.map((item)=>{
          let obj={id:item.id,title:item.title}
          let arr=[]
          item?.childrens.map((subItem)=>{
            arr.push({title:subItem.title,code:subItem.code})
          })
          obj.childrens=arr;
          this.filtered.push(obj)
        })
        this.filtered.map((item,index)=>{
          this.selectAll(item,temp[index],item.title)
        })
      })
      this.$forceUpdate();
    },

    resetDataHandler(){
      this.resetAllCheckBox();
      this.$nextTick(() => {
        const self = this;
        if (self.resetHandler) {
          this.SyncCheckedArray();
          console.log("reset handler applied")
          self.resetHandler(self.checkedArray);
        }
      });
      this.$forceUpdate();
    },
    // Save checked array.
    SyncCheckedArray() {

      const filteredClone =_.cloneDeep(this.filtered);
      const temp=[];

      filteredClone.forEach((item) => {
         const tempchild = item.childrens.filter((child)=>{
          return child.checked===true;
        });
         item.childrens=tempchild;
         temp.push(item);
      });
      this.checkedArray=temp;
    },
  },
};
</script>

<style scoped>


.col-md.custom-padding{
  padding-left :12px;
  margin-bottom: 12px;;
}
ul,
li {
  list-style: none;
  margin: 0;
  /*padding: 0;*/
}

.dropdown-wrap {
  width: 100%;
  min-width: 180px;
  max-height: 534px;
  background: white;
  /*position: absolute;*/
  z-index: 999;
  top: 44px;
  left: 16px;
  position: static;
   height: fit-content;
   width: 342px;
  border-radius: 3px;
  box-shadow: 0px 4px 4px rgba(0, 0, 0, 0.25);
}


.dropdown-searchbox {
  height: 32px;
  border: 1px solid #d6dade;
  border-radius: 3px;
  display: flex;
  position: relative;
}





.dropdown-searchbox input::placeholder {
  color: #9d9d9d;
}

.dropdown-searchbox input:hover {
  border-color: #4b83de;
}

.dropdown-searchbox input:focus {
  outline: solid 1px #3491ff;
}

.dropdown-searchbox input:focus + .focus-border {
  width: 100%;
  height: 100%;
  position: absolute;
  z-index: -1;
  box-sizing: content-box;
  opacity: 0.2;
  border: 4px solid #579ffb;
  border-radius: 5px;
  top: -4px;
  left: -4px;
}

.parent-item {
  overflow-y: auto;
  max-height: 280px;
  padding: 0px;
}

ul.child-item {
  padding-left: 0px !important;
}

/*.dropdown-list {*/
.child-item li label {
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  display: flex;
  align-items: center;
  letter-spacing: 0px;
  color: #595959;
  height: 32px;
  padding: 0px 8px;
  cursor: pointer;
}

/*.child-item li label:hover {*/
/*  background: #e6f7ff;*/
/*}*/

.child-item li {
  margin: -4px 0px;
}

.child-item li label input {
  display: none;
}

.child-item li label input:checked + .checkbox-custom {
  border-color: #0075ff;
}

.child-item li label input:checked + .checkbox-custom::before {
  content: '';
  width: 9px;
  height: 9px;
  background-color: #0075ff;
}

.child-item li label .checkbox-custom {
  width: 15px;
  height: 15px;
  border: 1px solid #979797;
  border-radius: 1px;
  margin-right: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.float-right{
  /*margin:5px;*/
  margin-right: 16px;
  margin-bottom: 8px;
  width: 54px;

}


.all-select-list {
  display: flex;
  align-items: center;
}

.all-select-list > div:first-of-type {
  margin-right: 15px;
}

.all-select-list > div {
  font-size: 14px;
  padding: 5px;
  cursor: pointer;
  min-width: 90px;
}
span.vertical-line {
  position: absolute;
  border-left: 1px solid #D3D3D3;
  height: 93%;
  top: 28px;
  margin-left: 155px;
}
.all-select-list > [disabled] {
  color: #c4c4c4;
  pointer-events: none;
}
.btn-custom{
  width: 54px;
  height: 29px;
  /* UI Properties */
  background: #3491FF 0% 0% no-repeat padding-box;
  border-radius: 3px;
  text-align: center;
  padding: 0;

}
.font-setting{
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
  font-style: normal;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 18px;
  letter-spacing: 0px;
  margin-top: 14px;
  width: 87px;
  height: 18px;
  color: #595959;
}
.reset-styles{
  height: 17px;
  /* UI Properties */
  text-align: left;
  font: normal normal normal 14px/16px Helvetica Neue;
  letter-spacing: 0px;
  color: #3491FF;

}
.buttons-custom{
  padding: 9px 16px 17px 10px;
  align-items: center;
}
.reset-btn-styles{
  color: #3491FF !important;
  font-family: Helvetica Neue, Helvetica, Microsoft Sans Serif, Arial, Arimo;
}

</style>
