<template>
  <a-modal
    v-model="visible"
    width="264px"
    :footer="null"
    :closable="false"
    class="text-center col-3"
    @close="handleOkay"
    centered
  >
    <div class="flex-column-custom">
      <img
        style="width: 34px"
        src="/images/icon/check-invite.png"
        alt="check-invite"
      />
      <div style="font-size: 16px; margin: 9px 0px 3px; color: #3491ff">
        <span>Requested</span>
        <span>{{ getDataNumber }}</span>
        <span>data</span>
      </div>
      <div style="font-size: 13px; margin-bottom: 10px; color: #4b4b4b">
        Your request has been sent successfully.
      </div>
      <a
        @click="handleOkay()"
        href="javascript:void(0)"
        id="handleOkay"
        class="btn-custom btn-custom-primary"
      >
        <span style="font-size: 11px; line-height: 12px">Okay</span>
      </a>
    </div>
  </a-modal>
</template>

<script>
module.exports = {
  name: "requested-popup",
  props: {
    visibleRequested: {
      type: Boolean,
      default: false,
    },
    total: Number,
  },
  data() {
    return {
      visible: false,
    };
  },
  watch: {
    visibleRequested() {
      console.log("Watched");
      console.log(this.total);
      setTimeout(() => {
        this.visible = true;
      }, 50);
    },
  },
  methods: {
    handleOkay(e) {
      console.log(this.visible)
      let that = this
      const el = document.getElementById("handleOkay");
      el.classList.add("primary-animation");
      setTimeout(function () {
        el.classList.remove("primary-animation");
        that.visible = false;
        console.log(this.visible)
      }, 700);
      console.log(this.visible)
    },
  },
  computed: {
    getDataNumber() {
      return this.total;
    },
  },
};
</script>

<style>
.ant-modal-body{
  padding: 0 !important;
}
.flex-column-custom{
  padding: 24px 51px;
}
</style>