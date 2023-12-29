<template>
  <div
      id="op-large-code"
      class="modal fade"
      tabindex="-1"
      role="dialog"
      aria-labelledby="title-part-chart"
      aria-hidden="true"
  >
    <div class="modal-dialog modal-dialog-version " role="document">
      <div class="modal-content">
        <div class="modal-title">
          <div class="head-line"></div>
          <div>
            <span class="span-title versionTitle">Version History</span>
          </div>
          <span class="close-button" data-dismiss="modal" aria-label="Close">
            <span class="t-icon-close"></span>
          </span>
        </div>

        <div class="modal-body">
          <div v-for="(item, index) in list" :key="index">
            <div class="update">Updates on Ver. {{ item.version }}</div>
            <div class="releaseDate">{{ item.releaseDate }}</div>
            <ul class="more-details custom-style">
              <li v-for="(value, index) in item.items" :key="index" v-html='value.description' class="customListStyle">
                <!--                <div style="width: 100%;" ' ></div>-->
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
module.exports = {
  name: "versionhistorymodel",
  data() {
    return {
      param: {
        'enabled':true
      },
    };
  },
  computed: {
    list() {
      return headerVm?.versions
    }
  },
  methods: {
    showPopupLargeCode() {
      $("#op-large-code").modal("show");
    },
    urlify() {
      var urlRegex = /(https?:\/\/[^\s]+)/g;
      this.list.map((item)=>{
        item.items.map((value)=>{
          value.description=value.description.replace(urlRegex, function(url) {
            return '<a '+'target="_blank"'+' href="' + url + '">'+ url+'</a>';
          })
        })
      })
    },
  },
  watch: {
    list() {
      this.urlify()
    }
  }
};
</script>

<style scoped>

.more-details {
  padding: 0px;
  margin-left: 15px;
  margin-top: 14px;
}

.more-details.custom-style{
  width : 100%;
}
.customListStyle{
  font-family: 'Helvetica Neue';
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 23px;
  color: #4B4B4B;
  margin-bottom:4px;
}
/*.more-details > li {*/
/*  letter-spacing: 0px;*/
/*  list-style: none;*/
/*  margin-bottom: 4px;*/
/*  display: flex;*/
/*  align-items: center;*/
/*}*/
/*.more-details > li:before {*/
/*  content: "";*/
/*  width: 4px;*/
/*  height: 4px;*/
/*  border-radius: 50%;*/
/*  margin-right: 4.5px;*/
/*  background-color: #4B4B4B;*/
/*  display: block;*/
/*}*/
.releaseDate {
  text-align: left;
  letter-spacing: 0px;
  opacity: 1;
  font-family: 'Helvetica Neue';
  font-style: normal;
  font-weight: 400;
  font-size: 14.66px;
  line-height: 17px;
  color: #C4C4C4;
}
.update {
  text-align: left;
  letter-spacing: 0px;
  opacity: 1;
  margin-bottom: 5px;

  font-family: 'Helvetica Neue';
  font-style: normal;
  font-weight: 600;
  font-size: 14.66px;
  line-height: 17px;
  color: #4B4B4B;
}
.close-button{
  font-size: 25px ;
  display: flex ;
  align-items: center ;
  height: 17px ;
  cursor: pointer ;
}
.head-line{
  position: absolute;
  background: #52a1ff ;
  height: 8px;
  border-radius: unset ;
  top: 0;
  left: 0;
  width: 100%;
}

.modal {
  overflow-x: hidden;
  overflow-y: hidden !important;
}
.modal-dialog {
  max-width: unset;
  position: relative;
  display: flex;
  width: auto;
  height: 100%;
  margin: 0.5rem;
  pointer-events: none;
  justify-content: flex-end;
  align-items: flex-end;
  align-content: space-around;
}

.modal-content {
  position: static;
  margin-bottom: 60px;
  inset: 311px 10px 0px auto;
  width: 608px;
  height: 335px;
  display: flex;
  pointer-events: auto;
  background-color: rgb(255, 255, 255);
  background-clip: padding-box;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 6px;
  box-shadow: 0px 3px 6px #00000029;
  outline: 0px;
}

.modal-title {
  background: #f5f8ff;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  /*margin-bottom: 1px;*/
  padding: 19.5px 25.5px 11.5px 20px;
  border-radius: 6px 6px 0 0;
}

.modal-body {
  background: white;
  padding: 20px;
  overflow: scroll;
  border-radius: 6px;
}

.t-icon-close {
  width: 12px;
  height: 12px;
  /*line-height: 12px;*/
  background-image: url("/images/icon/black-close-12.svg");
  background-repeat: no-repeat;
  background-size: 100%;
}

.head-line {
  position: absolute;
  background: #52a1ff;
  height: 8px;
  border-radius: 6px 6px 0 0;
  top: -2px;
  width: 100%;
}

.versionTitle {
  color: #4B4B4B;
  font-weight: 700;
  font-size: 14.66px;
  line-height: 100%;
  margin-right: 64px;
}
.versionTitle:hover {
  color: #4B4B4B;
  cursor: none;
  text-decoration: none;
}

.switch-zone div {
  cursor: pointer;
  width: 130px;
  height: 26px;
  background: #fff;
  border-radius: 3px;
  border: 1px solid #d0d0d0;
  font-size: 16px;
  color: #888888;
  margin-right: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
@keyframes primary-active-switch {
  0% {
  }
  33% {
    box-shadow: 0 0 0 3px #89d1fd;
    text-decoration: none !important;
  }
  66% {
    box-shadow: 0 0 0 3px #89d1fd;
    text-decoration: none !important;
  }
  100% {
  }
}
.info-zone .item .head b {
  color: #4b4b4b;
}

.graph-dropdown-wrap label {
  margin-bottom: 0;
  display: flex;
  align-items: center;
}

.graph-dropdown-wrap li {
  list-style: none;
  color: #4b4b4b;
  font-size: 11px;
  display: flex;
  align-items: center;
}

.graph-dropdown-wrap li input {
  margin-right: 8px;
}
</style>