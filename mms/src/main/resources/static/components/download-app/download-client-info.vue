<template>
  <div class="client-info-wrapper">

    <span v-if="mobile" class="text-company-name">{{ client.clientName }} SERVER CODE:</span>
    <span v-if="!mobile" class="text-company-name">{{ client.clientName }} Company ID</span>
    <span class="text-company-code">{{ client.id }}</span>
  </div>
</template>

<script>
module.exports = {
  props: {
    mobile: Boolean
  },
  data() {
    return {
      client: { id: null, clientName: null }
    }
  },
  mounted() {
    this.getClientInfo()
  },
  methods: {
    getClientInfo() {
      this.client = Common.getClientNameAndCompanyId();
    }
  },
}

</script>
<style scoped>
.text-company-name {
  font-weight: 700;
  font-size: 16px;
  line-height: 20px;
  text-align: center;
  color: #000000;
  white-space: nowrap
}
.text-company-code {
  font-weight: 700;
  font-size: 43px;
  line-height: 53px;
  text-align: center;
  color: #000000;
  white-space: nowrap;
  text-transform: uppercase;
}
.client-info-wrapper {
  text-align: center;
  display: flex;
  flex-direction: column;
}

</style>