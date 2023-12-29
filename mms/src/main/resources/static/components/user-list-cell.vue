<template>
  <div v-if="userList && Object.entries(resources).length" class="requested-bar">
    <div v-for="(item, index) in userList" :style="{ 'background-color': getRequestedColor(item.name,item.id)}" class="requested-item">
      <a-tooltip placement="bottom">
        <template slot="title">
          <div style="padding: 6px;font-size: 13px;">
            <div><b>{{item.name}}</b></div>
            <div>
              Company: <span v-if="item.company">{{ item.company.name }}</span>
            </div>
            <div>
              Department: {{ item.department }}
            </div>
            <div style="margin-bottom: 15px">
              Position: {{ item.position }}
            </div>
            <div>
              Email: {{ item.email }}
            </div>
            <div>
              Phone: {{ item.mobileNumber }}
            </div>
          </div>
        </template>
        <div>
          {{convertName(item.name)}}
        </div>
      </a-tooltip>
    </div>
  </div>
</template>
<script>
module.exports = {
  props: {
    resources: Object,
    userList:Array,
  },
  data() {
    return {
      colorList: [
        '#AFACFF',
        '#FF7171',
        '#DB91FC',
        '#FFACAC',
        '#FFB371',
        '#FFACF2',
        '#FFACE5',
        '#86C7FF',
        '#62B0D9',
        '#ACBFFF',
        '#FF8699',
        '#90E5AA',
        '#CCACFF',
        '#74AEFC',
        '#E0CD7E',
        '#FF8489',
        '#FFA167',
        '#64D0C3',
        '#86B0FF',
        '#D2F8E2',
        '#C69E9E',
        '#FFC68D',
        '#F8D2D2',
        '#E58383',
      ],
    };
  },
  methods:{
    convertName(name){
      let nameParts = name.split(/\s+/)
      // console.log(nameParts)
      let convertName = ''
      if (nameParts) {
        if (nameParts.length == 1) {
          convertName = nameParts[0].charAt(0).toUpperCase();
        } else {
          convertName = `${nameParts[0].charAt(0).toUpperCase()}${nameParts[nameParts.length - 1].charAt(0).toUpperCase()}`;
        }
      }
      return convertName;
    },

    getRequestedColor(name, id){
      let f=this.convertName(name);
      let index=0;
      if (f.length > 0) {
        index = f.charCodeAt(0);
        if (index > 65) index -= 65;
        if (f.length > 1) {
          index += f.charCodeAt(1);
          if (index > 65) index -= 65;
        }
      }
      if(id){
        index +=id;
      }
      index = index % this.colorList.length;
      return this.colorList[index]
    },
  },
  mounted() {

  }
};
</script>
<style scoped>

.requested-item{
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto;
  width: 25px;
  height: 25px;
  background-color: #AFACFF;
  border-radius: 25px;
  margin-bottom: 3px;
  /*margin-right: 3px;*/
}
.requested-bar{
  display: grid;
  grid-template-columns: repeat(4,1fr);
  width: 120px;
  /*display: flex;*/
  /*flex-flow: row wrap;*/
  /*position: relative;*/
}
</style>