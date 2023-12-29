<template>
  <div :style="styleCustom">
    <div class="card">
      <div class="card-header">
        <i class="fa fa-align-justify"></i>
        <span v-text="resources['company']"></span>
      </div>

      <table class="table table-mold-details">
        <tbody>
          <tr>
            <th v-text="resources['company_type']"></th>
            <td>{{ company.companyTypeText }}</td>
          </tr>
          <tr>
            <th v-text="resources['company_name']"></th>
            <td>{{ company.name }}</td>
          </tr>
          <tr>
            <th v-text="resources['company_code']"></th>
            <td>{{ company.companyCode }}</td>
          </tr>
          <tr>
            <th v-text="resources['address']"></th>
            <td>{{ company.address }}</td>
          </tr>
          <tr>
            <th v-text="resources['manager']"></th>
            <td>{{ company.manager }}</td>
          </tr>
          <tr>
            <th v-text="resources['phone_number']"></th>
            <td>{{ company.phone }}</td>
          </tr>
          <tr>
            <th v-text="resources['email']"></th>
            <td>{{ company.email }}</td>
          </tr>
          <tr>
            <th v-text="resources['memo']"></th>
            <td>{{ company.memo }}</td>
          </tr>
          <tr>
            <th v-text="resources['status']"></th>
            <td>{{ company.enabled ? "Enabled" : "Disabled" }}</td>
          </tr>
          <tr>
            <th v-text="resources['total_toolings_company_single']"></th>
            <td v-if="company.moldCount <= 1">
              <a
                href="javascript:;"
                @click="directLink('TOOLING', { companyId: company.id })"
                >{{ company.moldCount }}
                <span v-text="resources['tooling']"></span
              ></a>
            </td>
            <td v-if="company.moldCount > 1">
              <a
                href="javascript:;"
                @click="directLink('TOOLING', { companyId: company.id })"
                >{{ company.moldCount }}
                <span v-text="resources['toolings']"></span
              ></a>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
module.exports = {
  props: {
    resources: Object,
    company: Object,
    isDoashBoard: {
      type: Boolean,
      default: false,
    },
    styleCustom: Object,
  },
  data() {
    return {};
  },
  methods: {
    directLink(page, dataObj) {
      const QUERY = Common.param(dataObj);
      location.href = `${Common.PAGE_URL[page]}?${QUERY}`;
    },
  },
  mounted() {},
};
</script>

<style scoped>
a {
  color: #3491ff;
}

a:hover {
  color: #3585e5;
  text-decoration: underline;
}
</style>
