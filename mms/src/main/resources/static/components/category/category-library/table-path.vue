<template>
  <div class="table-path d-flex">
    <span> {{ `Category / ` }} </span>
    <div
      v-if="category.id"
      class="content__hyper-link text--truncate"
      @click="changeBreadcrumb({ route: 'index', data: {} }, 0, null)"
      style="max-width: 400px"
    >
      {{ ` ${category.name}` }}
    </div>
    <span v-else>{{ "No Category" }}</span>
    <span> / </span>
    <span
      v-if="brand.id"
      :class="{
        'content__hyper-link':
          stepList[stepList.length - 1].route != 'brand-list',
        inactive: stepList[stepList.length - 1].route == 'brand-list',
      }"
      @click="
        changeBreadcrumb(
          {
            route: 'brand-list',
            data: { brand: brand },
          },
          1,
          null
        )
      "
    >
      {{ ` ${brand.name}` }}
    </span>
    <span v-else>{{ "No Brand" }}</span>
    <span v-if="checkIsSelectedProject"> / </span>
    <span
      v-if="checkIsSelectedProject"
      :class="{
        'content__hyper-link':
          stepList[stepList.length - 1].route != 'project-profile',
        inactive: stepList[stepList.length - 1].route == 'project-profile',
      }"
      @click="
        changeBreadcrumb(
          {
            route: 'project-profile',
            data: { project: project },
          },
          2,
          null
        )
      "
    >
      {{ ` ${project.name}` }}
    </span>
    <span v-for="(step, index) in stepList" :key="index">
      <!-- <span v-html="getItem(step, index)"></span> -->
      <span
        v-for="(item, index1) in getItem(step, index)"
        :key="index1"
        :class="{
          'content__hyper-link': item.isHyperLink,
          inactive: !item.isHyperLink,
        }"
        @click="changeBreadcrumb(step, index, item.directType)"
        >{{ item.title }}</span
      >
    </span>
  </div>
</template>

<script>
module.exports = {
  props: {
    stepList: {
      type: Array,
      default: () => [],
    },
    category: {
      type: Object,
      default: () => ({
        id: null,
        name: "",
        projectProfiles: [],
      }),
    },
    brand: {
      type: Object,
      default: () => ({
        id: null,
        molds: [],
        name: "",
        parts: [],
        products: [],
        brandImage: null,
        suppliers: [],
        totalProduced: 0,
      }),
    },
    project: {
      type: Object,
      default: () => ({
        id: null,
        productionDemand: null,
        molds: [],
        name: "",
        parts: [],
        projectImage: null,
        suppliers: [],
        totalProduced: 0,
      }),
    },
  },
  computed: {
    checkIsSelectedProject() {
      if (this.project.id != null) {
        return true;
      }
      return false;
    },
  },
  methods: {
    // getItem(step, index) {
    //   let result = "";
    //   if (["list-part", "list-mold", "list-supplier"].includes(step.route)) {
    //     let prevStep = this.stepList[index - 1];
    //     result += this.getStepTitle(prevStep, step, index);
    //   }
    //   return result;
    // },
    // getStepTitle(prevStep, step, index) {
    //   let result = "";
    //   result += "<span> / </span>";
    //   if (prevStep.data.name) {
    //     result += `<span class="content__hyper-link" @click="changeBreadcrumb(step, index)">${prevStep.data.name}</span>`;
    //     result += "<span> / </span>";
    //   }
    //   let newText = "";
    //   switch (step.route) {
    //     case "list-part":
    //       newText =
    //         index == this.stepList.length - 1
    //           ? `<span class="">Parts</span>`
    //           : `<span class="content__hyper-link" @click="changeBreadcrumb(step, index)">Parts</span>`;
    //       break;

    //     case "list-mold":
    //       newText =
    //         index == this.stepList.length - 1
    //           ? `<span class="">Toolings</span>`
    //           : `<span class="content__hyper-link" @click="changeBreadcrumb(step, index)">Toolings</span>`;
    //       break;
    //     case "list-supplier":
    //       newText =
    //         index == this.stepList.length - 1
    //           ? `<span class="">Company</span>`
    //           : `<span class="content__hyper-link" @click="changeBreadcrumb(step, index)">Company</span>`;
    //       break;
    //   }
    //   result += newText;
    //   console.log("new text", newText, step);
    //   return result;
    // },
    getItem(step, index) {
      if (["list-part", "list-mold", "list-supplier"].includes(step.route)) {
        let result = [];
        let prevStep = this.stepList[index - 1];
        let titleList = this.getStepTitle(prevStep, step, index);
        result = [...result, ...titleList];
        return result;
      }
    },
    getStepTitle(prevStep, step, index) {
      let result = [];
      result.push({ title: " / ", isHyperLink: false });
      if (prevStep.data.name) {
        result.push({
          title: prevStep.data.name,
          isHyperLink: true,
          directType: "detail",
        });
        result.push({ title: " / ", isHyperLink: false });
      }
      let newText = "";
      switch (step.route) {
        case "list-part":
          newText = "Parts";
          break;
        case "list-mold":
          newText = "Toolings";
          break;
        case "list-supplier":
          newText = "Company";
          break;
      }
      result.push({
        title: newText,
        isHyperLink: index < this.stepList.length - 1 ? true : false,
        directType: "list",
      });
      return result;
    },
    changeBreadcrumb(step, index, directType) {
      console.log(step, index, directType, this.stepList);
      if (directType) {
        if (directType == "detail") {
          let prevStep = this.stepList[index - 1];
          let newSt = { ...step };
          switch (prevStep.route) {
            case "list-mold":
              newSt.route = "mold-detail";
              break;
            case "list-part":
              newSt.route = "part-detail";
              break;
            case "list-supplier":
              newSt.route = "company-detail";
              break;
          }
          this.$emit("change-path", newSt, index, directType);
        } else if (directType == "list") {
          this.$emit("change-path", step, index, directType);
        }
      } else {
        if (step.route == "project-profile") {
          index = this.stepList.indexOf(step);
        }
        this.$emit("change-path", step, index, directType);
      }
    },
  },
};
</script>

<style scoped>
.table-path {
  margin-top: 20px;
}
</style>
